import { entregaApi } from '$lib/api';
import type {
	DTOEntregaEjercicio,
	DTORespuestaPaginada,
	DTORespuestaModificacionEntrega,
	DTOOperacionArchivoTipoEnum
} from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { FormatterUtils } from '$lib/utils/formatters';
import { PermissionUtils } from '$lib/utils/permissions';
import { ValidationUtils } from '$lib/utils/validators';
import type { DTOAlumno, DTOProfesor } from '$lib/generated/api';
import { authStore } from '$lib/stores/authStore.svelte';

// Type definitions for better type safety
export interface EntregaFilters {
	readonly alumnoId?: string;
	readonly ejercicioId?: string;
	readonly estado?: string;
	readonly notaMin?: number;
	readonly notaMax?: number;
	readonly page?: number;
	readonly size?: number;
	readonly sortBy?: string;
	readonly sortDirection?: string;
}

export interface CreateEntregaData {
	readonly ejercicioId: number;
	readonly alumnoId: number;
	readonly archivosEntregados?: string[];
}

export interface UpdateEntregaData {
	readonly alumnoId?: number;
	readonly ejercicioId?: number;
	readonly nota?: number;
	readonly archivosEntregados?: string[];
	readonly comentarios?: string;
}

export interface GradeData {
	readonly nota: number;
	readonly comentarios?: string;
}

// New types for delivery modification
export interface FileOperation {
	readonly tipo: DTOOperacionArchivoTipoEnum;
	readonly rutaArchivo: string;
	readonly nuevoNombre?: string;
}

export interface ModifyDeliveryData {
	readonly comentarios?: string;
	readonly operacionesArchivos?: FileOperation[];
}

export interface ServiceResult<T = void> {
	readonly success: boolean;
	readonly message: string;
	readonly data?: T;
	readonly error?: Error;
}

export interface PaginatedResult<T> {
	readonly content: readonly T[];
	readonly totalElements: number;
	readonly totalPages: number;
	readonly currentPage: number;
	readonly pageSize: number;
}

export interface DeliveryStatistics {
	readonly totalEntregas: number;
	readonly pendingEntregas: number;
	readonly gradedEntregas: number;
	readonly averageGrade: number;
	readonly formattedStats: {
		readonly totalFormatted: string;
		readonly pendingFormatted: string;
		readonly gradedFormatted: string;
		readonly averageFormatted: string;
	};
}

export class EntregaService {
	private static readonly api = entregaApi;

	// ==================== VALIDATION METHODS ====================

	/**
	 * Validate delivery creation data
	 */
	static validateCreateData(data: Readonly<CreateEntregaData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate ejercicioId
		if (!data.ejercicioId || data.ejercicioId <= 0) {
			errors.push('El ID del ejercicio es obligatorio y debe ser mayor que 0');
		}

		// Validate alumnoId
		if (!data.alumnoId || data.alumnoId <= 0) {
			errors.push('El ID del alumno es obligatorio y debe ser mayor que 0');
		}

		// Validate archivosEntregados if provided
		if (data.archivosEntregados && data.archivosEntregados.length > 0) {
			for (const archivo of data.archivosEntregados) {
				if (!archivo || archivo.trim().length === 0) {
					errors.push('Los archivos no pueden estar vacíos');
					break;
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Validate grade data using ValidationUtils
	 */
	static validateGradeData(data: Readonly<GradeData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate nota using ValidationUtils
		const gradeValidation = ValidationUtils.validateGrade(data.nota);
		if (!gradeValidation.isValid) {
			errors.push(gradeValidation.message);
		}

		// Validate comentarios length if provided
		if (data.comentarios && data.comentarios.length > 500) {
			errors.push('Los comentarios no pueden superar 500 caracteres');
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Validate delivery update data
	 */
	static validateUpdateData(data: Readonly<UpdateEntregaData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate nota if provided
		if (data.nota !== undefined && data.nota !== null) {
			const gradeValidation = ValidationUtils.validateGrade(data.nota);
			if (!gradeValidation.isValid) {
				errors.push(gradeValidation.message);
			}
		}

		// Validate comentarios length if provided
		if (data.comentarios && data.comentarios.length > 500) {
			errors.push('Los comentarios no pueden superar 500 caracteres');
		}

		// Validate archivosEntregados if provided
		if (data.archivosEntregados && data.archivosEntregados.length > 0) {
			for (const archivo of data.archivosEntregados) {
				if (!archivo || archivo.trim().length === 0) {
					errors.push('Los archivos no pueden estar vacíos');
					break;
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Validate delivery modification data
	 */
	static validateModifyData(data: Readonly<ModifyDeliveryData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate comentarios length if provided
		if (data.comentarios && data.comentarios.length > 1000) {
			errors.push('Los comentarios no pueden superar 1000 caracteres');
		}

		// Validate operacionesArchivos if provided
		if (data.operacionesArchivos && data.operacionesArchivos.length > 0) {
			for (const operacion of data.operacionesArchivos) {
				if (!operacion.rutaArchivo || operacion.rutaArchivo.trim().length === 0) {
					errors.push('La ruta del archivo es obligatoria');
					break;
				}

				if (
					operacion.tipo === DTOOperacionArchivoTipoEnum.Renombrar &&
					(!operacion.nuevoNombre || operacion.nuevoNombre.trim().length === 0)
				) {
					errors.push('El nuevo nombre es obligatorio para operaciones de renombrado');
					break;
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	// ==================== PERMISSION METHODS ====================

	/**
	 * Check if user can grade deliveries
	 */
	static canGradeDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canGradeDelivery(user);
	}

	/**
	 * Check if user can view all deliveries
	 */
	static canViewAllDeliveries(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canViewAllDeliveries(user);
	}

	/**
	 * Check if user can edit delivery
	 */
	static canEditDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canEditEntity('entregas', user);
	}

	/**
	 * Check if user can delete delivery
	 */
	static canDeleteDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canDeleteEntity('entregas', user);
	}

	/**
	 * Check if user can modify delivery
	 */
	static canModifyDelivery(
		user: DTOAlumno | DTOProfesor | { id?: number; roles?: string[] },
		deliveryAlumnoId?: number
	): boolean {
		// Admins and professors can modify any delivery
		if (PermissionUtils.isAdmin(user) || PermissionUtils.isProfessor(user)) {
			return true;
		}

		// Students can only modify their own deliveries
		if (PermissionUtils.isStudent(user) && deliveryAlumnoId) {
			return user.id === deliveryAlumnoId;
		}

		return false;
	}

	// ==================== FORMATTING METHODS ====================

	/**
	 * Format delivery status for display
	 */
	static formatDeliveryStatus(status: string | undefined | null): string {
		return FormatterUtils.formatStatus(status, 'delivery');
	}

	/**
	 * Get delivery status color for styling
	 */
	static getDeliveryStatusColor(status: string | undefined | null): string {
		return FormatterUtils.getStatusColor(status, 'delivery');
	}

	/**
	 * Format grade for display
	 */
	static formatGrade(nota: number | undefined | null): string {
		return FormatterUtils.formatGrade(nota);
	}

	/**
	 * Get grade color for styling
	 */
	static getGradeColor(nota: number | undefined | null): string {
		return FormatterUtils.getGradeColor(nota);
	}

	/**
	 * Format delivery date
	 */
	static formatDeliveryDate(date: Date | string | undefined | null): string {
		return FormatterUtils.formatDate(date, { includeTime: true });
	}

	// ==================== BASIC CRUD OPERATIONS ====================

	static async getEntregas(params?: Readonly<EntregaFilters>): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas(params);
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregas');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregaById(id: number): Promise<DTOEntregaEjercicio> {
		try {
			const response = await EntregaService.api.obtenerEntregaPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregaById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregaWithAlumno(id: number): Promise<DTOEntregaEjercicio> {
		try {
			const response = await EntregaService.api.obtenerEntregaConAlumno({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregaWithAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregaWithEjercicio(id: number): Promise<DTOEntregaEjercicio> {
		try {
			const response = await EntregaService.api.obtenerEntregaConEjercicio({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregaWithEjercicio(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async createEntrega(entrega: Readonly<CreateEntregaData>): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before creating
			const validation = this.validateCreateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.crearEntrega({
				dTOPeticionCrearEntregaEjercicio: {
					alumnoId: entrega.alumnoId,
					ejercicioId: entrega.ejercicioId,
					archivosEntregados: entrega.archivosEntregados
				}
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createEntrega');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async updateEntrega(
		id: number,
		entrega: Readonly<UpdateEntregaData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before updating
			const validation = this.validateUpdateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.actualizarEntregaParcial({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async replaceEntrega(
		id: number,
		entrega: Readonly<UpdateEntregaData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before replacing
			const validation = this.validateUpdateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.reemplazarEntrega({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `replaceEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async deleteEntrega(id: number): Promise<void> {
		try {
			await EntregaService.api.eliminarEntrega({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== DELIVERY MODIFICATION OPERATIONS ====================

	/**
	 * Modify delivery with comments and file operations
	 */
	static async modifyDelivery(
		id: number,
		data: Readonly<ModifyDeliveryData>
	): Promise<DTORespuestaModificacionEntrega> {
		try {
			// Validate data before modifying
			const validation = this.validateModifyData(data);
			if (!validation.isValid) {
				throw new Error(`Datos de modificación inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.modificarEntrega({
				id,
				dTOPeticionModificarEntrega: {
					comentarios: data.comentarios,
					operacionesArchivos: data.operacionesArchivos
				}
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `modifyDelivery(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Add files to delivery
	 */
	static async addFilesToDelivery(id: number, files: File[]): Promise<string> {
		try {
			if (!files || files.length === 0) {
				throw new Error('No se han proporcionado archivos para agregar');
			}

			// Create FormData for proper multipart upload
			const formData = new FormData();
			files.forEach((file) => {
				formData.append('files', file);
			});

			// Make the request manually since the generated API has issues with file uploads
			const response = await fetch(`/api/entregas/${id}/add-files`, {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${authStore.token}`
					// Don't set Content-Type for FormData - browser will set it automatically with boundary
				},
				body: formData
			});

			if (!response.ok) {
				const errorData = await response.json().catch(() => ({}));
				throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
			}

			return await response.text();
		} catch (error) {
			ErrorHandler.logError(error, `addFilesToDelivery(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete all files from delivery
	 */
	static async deleteAllFiles(id: number): Promise<object> {
		try {
			const response = await EntregaService.api.eliminarTodosLosArchivos({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `deleteAllFiles(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a specific file from delivery
	 */
	static async deleteFile(id: number, filePath: string): Promise<void> {
		try {
			await EntregaService.api.eliminarArchivo({ id, rutaArchivo: filePath });
		} catch (error) {
			ErrorHandler.logError(error, `deleteFile(${id}, ${filePath})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== GRADING OPERATIONS ====================

	static async gradeEntrega(
		id: number,
		gradeData: Readonly<GradeData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate grade data before grading
			const validation = this.validateGradeData(gradeData);
			if (!validation.isValid) {
				throw new Error(`Datos de calificación inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.actualizarEntregaParcial({
				id,
				dTOPeticionActualizarEntregaEjercicio: gradeData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `gradeEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== FILTERED QUERY OPERATIONS ====================

	static async getEntregasByEjercicio(ejercicioId: number): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({
				ejercicioId: ejercicioId.toString()
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregasByEjercicio(${ejercicioId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasByAlumno(alumnoId: number): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ alumnoId: alumnoId.toString() });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregasByAlumno(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasPendientes(): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ estado: 'PENDIENTE' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregasPendientes');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasCalificadas(): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ estado: 'CALIFICADO' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregasCalificadas');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get deliveries for teacher's classes
	 */
	static async getTeacherDeliveries(params: EntregaFilters = {}): Promise<DTORespuestaPaginada> {
		try {
			// Note: This endpoint might not exist yet in the generated API
			// We'll use the existing endpoint with teacher filtering for now
			return await this.api.obtenerEntregas(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getTeacherDeliveries');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get recent deliveries for a specific class
	 */
	static async getClassRecentDeliveries(
		claseId: number,
		params: EntregaFilters = {}
	): Promise<DTORespuestaPaginada> {
		try {
			return await this.api.obtenerEntregasRecientesClase({ claseId, ...params });
		} catch (error) {
			ErrorHandler.logError(error, 'getClassRecentDeliveries');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Grade a delivery (for teachers)
	 */
	static async gradeDelivery(id: number, gradeData: GradeData): Promise<DTOEntregaEjercicio> {
		try {
			return await this.api.reemplazarEntrega({
				id,
				dTOPeticionActualizarEntregaEjercicio: {
					nota: gradeData.nota,
					comentarios: gradeData.comentarios
				}
			});
		} catch (error) {
			ErrorHandler.logError(error, 'gradeDelivery');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle delivery grading with validation and business logic
	 */
	static async handleDeliveryGrading(
		deliveryId: number,
		gradeData: Readonly<GradeData>,
		user: DTOAlumno | DTOProfesor
	): Promise<{ success: boolean; message: string; updatedDelivery?: DTOEntregaEjercicio }> {
		try {
			// Check permissions
			if (!this.canGradeDelivery(user)) {
				return {
					success: false,
					message: 'No tienes permisos para calificar entregas'
				};
			}

			// Validate input
			if (!deliveryId || deliveryId <= 0) {
				return {
					success: false,
					message: 'ID de entrega inválido'
				};
			}

			// Validate grade data
			const validation = this.validateGradeData(gradeData);
			if (!validation.isValid) {
				return {
					success: false,
					message: `Datos de calificación inválidos: ${validation.errors.join(', ')}`
				};
			}

			// Check if delivery exists
			const existingDelivery = await this.getEntregaById(deliveryId);
			if (!existingDelivery) {
				return {
					success: false,
					message: 'Entrega no encontrada'
				};
			}

			// Check if delivery is already graded
			if (existingDelivery.estado === 'CALIFICADO') {
				return {
					success: false,
					message: 'La entrega ya está calificada'
				};
			}

			// Perform the grading
			const updatedDelivery = await this.gradeEntrega(deliveryId, gradeData);

			return {
				success: true,
				message: 'Entrega calificada correctamente',
				updatedDelivery
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al calificar la entrega'
			};
		}
	}

	/**
	 * Handle delivery modification with validation and business logic
	 */
	static async handleDeliveryModification(
		deliveryId: number,
		modifyData: Readonly<ModifyDeliveryData>,
		user: DTOAlumno | DTOProfesor | { id?: number; roles?: string[] },
		deliveryAlumnoId?: number
	): Promise<{ success: boolean; message: string; result?: DTORespuestaModificacionEntrega }> {
		try {
			// Check permissions
			if (!this.canModifyDelivery(user, deliveryAlumnoId)) {
				return {
					success: false,
					message: 'No tienes permisos para modificar esta entrega'
				};
			}

			// Validate input
			if (!deliveryId || deliveryId <= 0) {
				return {
					success: false,
					message: 'ID de entrega inválido'
				};
			}

			// Validate modification data
			const validation = this.validateModifyData(modifyData);
			if (!validation.isValid) {
				return {
					success: false,
					message: `Datos de modificación inválidos: ${validation.errors.join(', ')}`
				};
			}

			// Check if delivery exists and can be modified
			const existingDelivery = await this.getEntregaById(deliveryId);
			if (!existingDelivery) {
				return {
					success: false,
					message: 'Entrega no encontrada'
				};
			}

			// Check if delivery can be modified (not graded)
			if (existingDelivery.estado === 'CALIFICADO') {
				return {
					success: false,
					message: 'No se puede modificar una entrega ya calificada'
				};
			}

			// Perform the modification
			const result = await this.modifyDelivery(deliveryId, modifyData);

			return {
				success: true,
				message: 'Entrega modificada correctamente',
				result
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al modificar la entrega'
			};
		}
	}

	/**
	 * Get delivery statistics with formatted data
	 */
	static async getEstadisticas(): Promise<DeliveryStatistics> {
		try {
			const response = await EntregaService.api.obtenerEstadisticas1();

			// Parse the response and provide default values if needed
			const stats = (response as Record<string, unknown>) || {};
			const totalEntregas = (stats.totalEntregas as number) || 0;
			const pendingEntregas = (stats.pendingEntregas as number) || 0;
			const gradedEntregas = (stats.gradedEntregas as number) || 0;
			const averageGrade = (stats.averageGrade as number) || 0;

			return {
				totalEntregas,
				pendingEntregas,
				gradedEntregas,
				averageGrade,
				formattedStats: {
					totalFormatted: totalEntregas.toString(),
					pendingFormatted: pendingEntregas.toString(),
					gradedFormatted: gradedEntregas.toString(),
					averageFormatted: this.formatGrade(averageGrade)
				}
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getEstadisticas');
			throw await ErrorHandler.parseError(error);
		}
	}
}
