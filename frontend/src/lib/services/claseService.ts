import type {
	DTOClase,
	DTOCurso,
	DTOTaller,
	DTOPeticionCrearCurso,
	DTOPeticionCrearTaller,
	DTORespuestaPaginada,
	DTORespuestaPaginadaDTOClaseConEstadoInscripcion,
	DTOProfesor,
	ObtenerClasesPresencialidadEnum,
	DTORespuestaEnrollment,
	DTOPeticionCrearClase,
	Material
} from '$lib/generated/api';
import { claseApi, profesorApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class ClaseService {
	/**
	 * Get all classes (filtered by user role)
	 */
	static async getClases(params: Record<string, unknown> = {}): Promise<DTORespuestaPaginada> {
		try {
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getClases');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a class by ID (basic version)
	 */
	static async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'getClaseById');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a class with all relationships loaded using Entity Graph (optimized)
	 */
	static async getClaseConDetalles(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'getClaseConDetalles');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get professors for a specific class
	 */
	static async getProfesoresPorClase(claseId: number): Promise<DTOProfesor[]> {
		try {
			// First, get the class details which include the profesoresId array
			const clase = await claseApi.obtenerClasePorId({ id: claseId });

			// If no professors are assigned, return empty array
			if (!clase.profesoresId || clase.profesoresId.length === 0) {
				return [];
			}

			// Fetch each professor by their ID
			const profesores: DTOProfesor[] = [];
			for (const profesorId of clase.profesoresId) {
				try {
					const profesor = await profesorApi.obtenerProfesorPorId({ id: parseInt(profesorId) });
					profesores.push(profesor);
				} catch (error) {
					console.warn(`Failed to fetch professor with ID ${profesorId}:`, error);
					// Continue with other professors even if one fails
				}
			}

			return profesores;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPorClase');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get classes that the authenticated teacher teaches
	 */
	static async getMyClasses(): Promise<DTOClase[]> {
		try {
			const { userOperationsApi } = await import('$lib/api');
			return await userOperationsApi.obtenerMisClases();
		} catch (error) {
			ErrorHandler.logError(error, 'getMyClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Update class parameters (for teachers)
	 */
	static async updateClassParameters(
		id: number,
		updateData: DTOPeticionCrearClase
	): Promise<DTOClase> {
		try {
			// Try different endpoint patterns since backend doesn't support PUT for class updates
			// First try PATCH method
			let response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/clases/${id}`, {
				method: 'PATCH',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify(updateData)
			});

			// If PATCH doesn't work, try POST
			if (!response.ok && response.status === 405) {
				response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/clases/${id}`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						Authorization: `Bearer ${localStorage.getItem('token')}`
					},
					body: JSON.stringify(updateData)
				});
			}

			// If that doesn't work, try a different endpoint pattern
			if (!response.ok && response.status === 405) {
				response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/clases/${id}/update`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						Authorization: `Bearer ${localStorage.getItem('token')}`
					},
					body: JSON.stringify(updateData)
				});
			}

			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}

			return await response.json();
		} catch (error) {
			ErrorHandler.logError(error, 'updateClassParameters');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get materials for a specific class
	 */
	static async getClassMaterials(claseId: number): Promise<Material[]> {
		try {
			const { classManagementApi } = await import('$lib/api');
			return await classManagementApi.obtenerMaterialesClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'getClassMaterials');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Add material to a class
	 */
	static async addMaterialToClass(claseId: number, material: Material): Promise<string> {
		try {
			const { classManagementApi } = await import('$lib/api');
			return await classManagementApi.agregarMaterialAClase({ claseId, material });
		} catch (error) {
			ErrorHandler.logError(error, 'addMaterialToClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Remove material from a class
	 */
	static async removeMaterialFromClass(claseId: number, materialId: number): Promise<string> {
		try {
			const { classManagementApi } = await import('$lib/api');
			return await classManagementApi.quitarMaterialDeClase({ claseId, materialId });
		} catch (error) {
			ErrorHandler.logError(error, 'removeMaterialFromClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new course
	 */
	static async crearCurso(curso: DTOPeticionCrearCurso): Promise<DTOCurso> {
		try {
			return await claseApi.crearCurso({ dTOPeticionCrearCurso: curso });
		} catch (error) {
			ErrorHandler.logError(error, 'crearCurso');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new workshop
	 */
	static async crearTaller(taller: DTOPeticionCrearTaller): Promise<DTOTaller> {
		try {
			return await claseApi.crearTaller({ dTOPeticionCrearTaller: taller });
		} catch (error) {
			ErrorHandler.logError(error, 'crearTaller');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a class by ID
	 */
	static async borrarClasePorId(id: number): Promise<void> {
		try {
			await claseApi.eliminarClase({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'borrarClasePorId');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Get classes with optimized loading based on context
	 */
	static async getClasesOptimizadas(params: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
		q?: string;
		titulo?: string;
		descripcion?: string;
		nivel?: string;
		presencialidad?: ObtenerClasesPresencialidadEnum;
		precioMinimo?: number;
		precioMaximo?: number;
		profesorId?: string;
		cursoId?: string;
		tallerId?: string;
	}): Promise<DTORespuestaPaginada> {
		try {
			// Use the optimized endpoint with all available filters
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getClasesOptimizadas');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get available classes for enrollment (excluding those where the student is already enrolled)
	 * For students: shows only classes they can enroll in
	 * For admins/professors: shows all classes (same as regular endpoint)
	 */
	static async getClasesDisponibles(params: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
		q?: string;
		titulo?: string;
		descripcion?: string;
		nivel?: string;
		presencialidad?: ObtenerClasesPresencialidadEnum;
		precioMinimo?: number;
		precioMaximo?: number;
	}): Promise<DTORespuestaPaginada> {
		try {
			// Use the new endpoint that excludes enrolled classes for students
			return await claseApi.obtenerClasesDisponibles(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getClasesDisponibles');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get classes catalog with enrollment status information
	 * This endpoint returns all classes with enrollment status for students
	 * For students: shows enrollment status for each class
	 * For admins/professors: shows all classes (isEnrolled is always false)
	 */
	static async getClasesCatalog(params: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
		q?: string;
		titulo?: string;
		descripcion?: string;
		dificultad?: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO';
		presencialidad?: 'ONLINE' | 'PRESENCIAL' | 'HIBRIDO';
		profesorId?: string;
		cursoId?: string;
		tallerId?: string;
	}): Promise<DTORespuestaPaginadaDTOClaseConEstadoInscripcion> {
		try {
			// Use the new catalog endpoint that includes enrollment status
			const response = await claseApi.obtenerCatalogoClases(params);
			// Cast the response to the correct type since the API might not be properly typed
			return response as unknown as DTORespuestaPaginadaDTOClaseConEstadoInscripcion;
		} catch (error) {
			ErrorHandler.logError(error, 'getClasesCatalog');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get class details with all relationships for detailed view
	 */
	static async getClaseDetallada(id: number): Promise<DTOClase> {
		try {
			// Use the optimized endpoint that loads all relationships
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'getClaseDetallada');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Validate class creation data
	 */
	static validateClassData(data: DTOPeticionCrearCurso | DTOPeticionCrearTaller): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Common validation for both course and workshop
		if (!data.titulo || data.titulo.trim().length === 0) {
			errors.push('El título es obligatorio');
		}

		if (!data.descripcion || data.descripcion.trim().length === 0) {
			errors.push('La descripción es obligatoria');
		}

		if (data.precio !== undefined && data.precio < 0) {
			errors.push('El precio no puede ser negativo');
		}

		// Course-specific validation
		if ('duracionHoras' in data) {
			if (!data.duracionHoras || data.duracionHoras <= 0) {
				errors.push('La duración en horas debe ser mayor que 0');
			}
		}

		// Workshop-specific validation
		if ('fechaInicio' in data && 'fechaFin' in data) {
			if (
				data.fechaInicio &&
				data.fechaFin &&
				new Date(data.fechaInicio) >= new Date(data.fechaFin)
			) {
				errors.push('La fecha de inicio debe ser anterior a la fecha de fin');
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Format class data for display
	 */
	static formatClassData(clase: DTOClase): {
		formattedTitle: string;
		formattedDescription: string;
		formattedPrice: string;
		formattedLevel: string;
		formattedPresenciality: string;
		statusText: string;
		statusColor: string;
	} {
		return {
			formattedTitle: clase.titulo || 'Sin título',
			formattedDescription: clase.descripcion || 'Sin descripción',
			formattedPrice: clase.precio ? `${clase.precio}€` : 'Gratis',
			formattedLevel: clase.nivel || 'N/A',
			formattedPresenciality: clase.presencialidad || 'N/A',
			statusText: 'Activa', // Assuming active by default
			statusColor: 'bg-green-100 text-green-800'
		};
	}

	/**
	 * Get available actions for a class based on user permissions
	 */
	static getAvailableActions(
		user: { role?: string }, // User with role property
		clase?: DTOClase
	): Array<{
		id: string;
		label: string;
		icon: string;
		color: string;
		action: () => void;
	}> {
		const actions: Array<{
			id: string;
			label: string;
			icon: string;
			color: string;
			action: () => void;
		}> = [];

		// View action - always available
		if (clase) {
			actions.push({
				id: 'view',
				label: 'Ver detalles',
				icon: '👁️',
				color: 'blue',
				action: () => {
					// Navigate to class details
					console.log('View class details');
				}
			});
		}

		// Edit action - for professors and admins
		if (user && (user.role === 'PROFESOR' || user.role === 'ADMIN')) {
			actions.push({
				id: 'edit',
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				action: () => {
					// Navigate to edit class
					console.log('Edit class');
				}
			});
		}

		// Delete action - for admins only
		if (user && user.role === 'ADMIN') {
			actions.push({
				id: 'delete',
				label: 'Eliminar',
				icon: '🗑️',
				color: 'red',
				action: () => {
					// Trigger delete confirmation
					console.log('Delete class');
				}
			});
		}

		return actions;
	}

	/**
	 * Check if user can enroll in a class
	 */
	static canEnrollInClass(user: { role?: string }, clase: DTOClase): boolean {
		// Basic checks - can be expanded based on business rules
		return user && user.role === 'ALUMNO' && clase && clase.id !== undefined;
	}

	/**
	 * Check if user can manage a class
	 */
	static canManageClass(user: { role?: string }): boolean {
		return user && (user.role === 'PROFESOR' || user.role === 'ADMIN');
	}

	/**
	 * Get class statistics
	 */
	static getClassStatistics(clase: DTOClase): {
		totalStudents: number;
		totalProfessors: number;
		totalExercises: number;
		totalMaterials: number;
	} {
		return {
			totalStudents: clase.alumnosId?.length || 0,
			totalProfessors: clase.profesoresId?.length || 0,
			totalExercises: clase.ejerciciosId?.length || 0,
			totalMaterials: clase.material?.length || 0
		};
	}

	// ==================== ENROLLMENT METHODS ====================

	/**
	 * Enroll a student in a class (for admins/professors)
	 */
	static async enrollStudentInClass(
		alumnoId: number,
		claseId: number
	): Promise<DTORespuestaEnrollment> {
		try {
			const { classManagementApi } = await import('$lib/api');
			return await classManagementApi.inscribirAlumnoEnClase({ claseId, studentId: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, 'enrollStudentInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Unenroll a student from a class (for admins/professors)
	 */
	static async unenrollStudentFromClass(
		alumnoId: number,
		claseId: number
	): Promise<DTORespuestaEnrollment> {
		try {
			const { classManagementApi } = await import('$lib/api');
			return await classManagementApi.darDeBajaAlumnoDeClase({ claseId, studentId: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, 'unenrollStudentFromClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Check if a student is enrolled in a class
	 */
	static async checkStudentEnrollment(alumnoId: number, claseId: number): Promise<boolean> {
		try {
			const clase = await this.getClaseById(claseId);
			return clase.alumnosId?.includes(alumnoId.toString()) || false;
		} catch (error) {
			ErrorHandler.logError(error, 'checkStudentEnrollment');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get count of students enrolled in a class
	 */
	static async contarAlumnosEnClase(claseId: number): Promise<number> {
		try {
			const clase = await this.getClaseById(claseId);
			return clase.alumnosId?.length || 0;
		} catch (error) {
			ErrorHandler.logError(error, 'contarAlumnosEnClase');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Handle enrollment with proper error handling for already enrolled students
	 */
	static async handleStudentEnrollment(
		alumnoId: number,
		claseId: number
	): Promise<{
		success: boolean;
		message: string;
		alreadyEnrolled?: boolean;
	}> {
		try {
			// First check if already enrolled
			const isEnrolled = await this.checkStudentEnrollment(alumnoId, claseId);
			if (isEnrolled) {
				return {
					success: false,
					message: 'El estudiante ya está inscrito en esta clase',
					alreadyEnrolled: true
				};
			}

			// Try to enroll
			const result = await this.enrollStudentInClass(alumnoId, claseId);
			return {
				success: result.success || false,
				message: result.message || 'Estudiante inscrito exitosamente'
			};
		} catch (error: unknown) {
			// Handle specific error for already enrolled students
			const errorMessage = error instanceof Error ? error.message : String(error);
			if (errorMessage.includes('already enrolled') || errorMessage.includes('ya está inscrito')) {
				return {
					success: false,
					message: 'El estudiante ya está inscrito en esta clase',
					alreadyEnrolled: true
				};
			}

			ErrorHandler.logError(error, 'handleStudentEnrollment');
			throw await ErrorHandler.parseError(error);
		}
	}
}
