import { alumnoApi } from '$lib/api';
import type {
	DTOAlumno,
	DTOActualizacionAlumno,
	DTOClaseInscrita,
	DTOPeticionRegistroAlumno,
	DTOProfesor
} from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';
import { FormatterUtils } from '$lib/utils/formatters';
import { PermissionUtils } from '$lib/utils/permissions';
import { NavigationUtils } from '$lib/utils/navigation';

export class AlumnoService {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Get all students with pagination and filters
	 */
	static async getAlumnos(params?: {
		q?: string;
		firstName?: string;
		lastName?: string;
		dni?: string;
		email?: string;
		enrolled?: boolean;
		enabled?: boolean;
		available?: boolean;
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}): Promise<{ content: DTOAlumno[]; totalElements: number; totalPages: number }> {
		try {
			const response = await alumnoApi.obtenerAlumnos(params || {});
			return {
				content: response.content || [],
				totalElements: response.totalElements || 0,
				totalPages: response.totalPages || 0
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getAlumnos');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a student by ID (basic version)
	 */
	static async getAlumno(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a student with all relationships loaded using Entity Graph (optimized)
	 */
	static async getAlumnoCompleto(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoCompleto({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getAlumnoCompleto(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a student with classes loaded using Entity Graph (optimized for class-related operations)
	 */
	static async getAlumnoConClases(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoConClases({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getAlumnoConClases(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new student
	 */
	static async createAlumno(alumnoData: DTOPeticionRegistroAlumno): Promise<DTOAlumno> {
		try {
			return await alumnoApi.crearAlumno({
				dTOPeticionRegistroAlumno: alumnoData
			});
		} catch (error) {
			ErrorHandler.logError(error, 'createAlumno');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Update a student
	 */
	static async updateAlumno(id: number, alumnoData: DTOActualizacionAlumno): Promise<DTOAlumno> {
		try {
			return await alumnoApi.actualizarAlumnoParcial({
				id,
				dTOActualizacionAlumno: alumnoData
			});
		} catch (error) {
			ErrorHandler.logError(error, `updateAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a student
	 */
	static async deleteAlumno(id: number): Promise<void> {
		try {
			await alumnoApi.eliminarAlumno({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== STUDENT PROFILE OPERATIONS ====================

	/**
	 * Get enrolled classes for a student
	 */
	static async getClasesInscritas(alumnoId: number): Promise<DTOClaseInscrita> {
		try {
			return await alumnoApi.obtenerClasesAlumno({ id: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, `getClasesInscritas(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== STATUS UPDATE OPERATIONS ====================

	/**
	 * Change enrollment status for a student
	 * Uses the proper generated API with the updated DTO
	 */
	static async changeEnrollmentStatus(id: number, enrolled: boolean): Promise<DTOAlumno> {
		try {
			const updateData: DTOActualizacionAlumno = {
				enrolled: enrolled
			};

			console.log('Sending enrollment status change:', {
				id,
				updateData
			});

			const result = await alumnoApi.actualizarAlumnoParcial({
				id,
				dTOActualizacionAlumno: updateData
			});

			console.log('Response from enrollment status change:', result);
			return result;
		} catch (error) {
			ErrorHandler.logError(error, `changeEnrollmentStatus(${id}, ${enrolled})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Toggle enabled status for a student
	 * Uses the proper generated API with the updated DTO
	 */
	static async toggleEnabled(id: number, enabled: boolean): Promise<DTOAlumno> {
		try {
			const updateData: DTOActualizacionAlumno = {
				enabled: enabled
			};

			console.log('Sending enabled status change:', {
				id,
				updateData
			});

			const result = await alumnoApi.actualizarAlumnoParcial({
				id,
				dTOActualizacionAlumno: updateData
			});

			console.log('Response from enabled status change:', result);
			return result;
		} catch (error) {
			ErrorHandler.logError(error, `toggleEnabled(${id}, ${enabled})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle account status change with validation and business logic
	 */
	static async handleAccountStatusChange(
		alumnoId: number,
		newStatus: boolean
	): Promise<{ success: boolean; message: string; updatedAlumno?: DTOAlumno }> {
		try {
			// Validate input
			if (!alumnoId || alumnoId <= 0) {
				return {
					success: false,
					message: 'ID de alumno inválido'
				};
			}

			// Check if student exists
			const existingAlumno = await this.getAlumno(alumnoId);
			if (!existingAlumno) {
				return {
					success: false,
					message: 'Alumno no encontrado'
				};
			}

			// Check if status is actually changing
			if (existingAlumno.enabled === newStatus) {
				return {
					success: false,
					message: `La cuenta ya está ${newStatus ? 'habilitada' : 'deshabilitada'}`
				};
			}

			// Perform the status change
			const updatedAlumno = await this.toggleEnabled(alumnoId, newStatus);

			return {
				success: true,
				message: `Cuenta ${newStatus ? 'habilitada' : 'deshabilitada'} correctamente`,
				updatedAlumno
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al cambiar estado de cuenta'
			};
		}
	}

	/**
	 * Handle enrollment status change with validation and business logic
	 */
	static async handleEnrollmentStatusChange(
		alumnoId: number,
		newEnrollmentStatus: boolean
	): Promise<{ success: boolean; message: string; updatedAlumno?: DTOAlumno }> {
		try {
			// Validate input
			if (!alumnoId || alumnoId <= 0) {
				return {
					success: false,
					message: 'ID de alumno inválido'
				};
			}

			// Check if student exists
			const existingAlumno = await this.getAlumno(alumnoId);
			if (!existingAlumno) {
				return {
					success: false,
					message: 'Alumno no encontrado'
				};
			}

			// Check if enrollment status is actually changing
			if (existingAlumno.enrolled === newEnrollmentStatus) {
				return {
					success: false,
					message: `El alumno ya está ${newEnrollmentStatus ? 'inscrito' : 'no inscrito'}`
				};
			}

			// Perform the enrollment status change
			const updatedAlumno = await this.changeEnrollmentStatus(alumnoId, newEnrollmentStatus);

			return {
				success: true,
				message: `Estado de inscripción ${newEnrollmentStatus ? 'activado' : 'desactivado'} correctamente`,
				updatedAlumno
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al cambiar estado de inscripción'
			};
		}
	}

	/**
	 * Validate student registration data with business rules
	 */
	static validateRegistrationData(data: DTOPeticionRegistroAlumno): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Basic validation rules using utility functions
		const usernameValidation = ValidationUtils.validateUsername(data.username);
		if (!usernameValidation.isValid) {
			errors.push(usernameValidation.message);
		}

		const passwordValidation = ValidationUtils.validatePassword(data.password);
		if (!passwordValidation.isValid) {
			errors.push(passwordValidation.message);
		}

		const firstNameValidation = ValidationUtils.validateName(data.firstName);
		if (!firstNameValidation.isValid) {
			errors.push(firstNameValidation.message);
		}

		const lastNameValidation = ValidationUtils.validateName(data.lastName);
		if (!lastNameValidation.isValid) {
			errors.push(lastNameValidation.message);
		}

		const dniValidation = ValidationUtils.validateDNI(data.dni);
		if (!dniValidation.isValid) {
			errors.push(dniValidation.message);
		}

		const emailValidation = ValidationUtils.validateEmail(data.email);
		if (!emailValidation.isValid) {
			errors.push(emailValidation.message);
		}

		// Phone validation using utility function
		if (data.phoneNumber) {
			const phoneValidation = ValidationUtils.validatePhoneNumber(data.phoneNumber);
			if (!phoneValidation.isValid) {
				errors.push(phoneValidation.message);
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	// ==================== UTILITY INTEGRATION METHODS ====================

	/**
	 * Check if current user can perform actions on students
	 */
	static canManageStudents(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canViewAllStudents(user);
	}

	/**
	 * Check if current user can edit a specific student
	 */
	static canEditStudent(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canEditEntity('alumnos', user);
	}

	/**
	 * Check if current user can delete a specific student
	 */
	static canDeleteStudent(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canDeleteEntity('alumnos', user);
	}

	/**
	 * Navigate to student details page
	 */
	static navigateToStudent(studentId: number): void {
		NavigationUtils.goToEntity('alumnos', studentId);
	}

	/**
	 * Navigate to student edit page
	 */
	static navigateToEditStudent(studentId: number): void {
		NavigationUtils.goToEntityEdit('alumnos', studentId);
	}

	/**
	 * Navigate to students list
	 */
	static navigateToStudentsList(): void {
		NavigationUtils.goToEntityList('alumnos');
	}

	/**
	 * Navigate to create new student page
	 */
	static navigateToCreateStudent(): void {
		NavigationUtils.goToEntityCreate('alumnos');
	}

	/**
	 * Format student data for display
	 */
	static formatStudentData(alumno: DTOAlumno): {
		fullName: string;
		formattedDNI: string;
		formattedEmail: string;
		formattedPhone: string;
		statusText: string;
		statusColor: string;
		enrollmentText: string;
		enrollmentColor: string;
		createdDate: string;
		lastModifiedDate: string;
	} {
		return {
			fullName: `${alumno.firstName} ${alumno.lastName}`,
			formattedDNI: alumno.dni || 'N/A',
			formattedEmail: alumno.email || 'N/A',
			formattedPhone: alumno.phoneNumber || 'N/A',
			statusText: alumno.enabled ? 'Activo' : 'Inactivo',
			statusColor: alumno.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800',
			enrollmentText: alumno.enrolled ? 'Inscrito' : 'No inscrito',
			enrollmentColor: alumno.enrolled ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800',
			createdDate: FormatterUtils.formatDate(alumno.enrollmentDate),
			lastModifiedDate: 'N/A' // DTOAlumno doesn't have updatedAt property
		};
	}

	/**
	 * Get available actions for a student based on user permissions
	 */
	static getAvailableActions(
		user: DTOAlumno | DTOProfesor,
		student?: DTOAlumno
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
		if (student) {
			actions.push({
				id: 'view',
				label: 'Ver detalles',
				icon: '👁️',
				color: 'blue',
				action: () => this.navigateToStudent(student.id!)
			});
		}

		// Edit action
		if (this.canEditStudent(user)) {
			actions.push({
				id: 'edit',
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				action: () => student && this.navigateToEditStudent(student.id!)
			});
		}

		// Delete action
		if (this.canDeleteStudent(user)) {
			actions.push({
				id: 'delete',
				label: 'Eliminar',
				icon: '🗑️',
				color: 'red',
				action: () => {
					// This would typically trigger a confirmation dialog
					console.log('Delete action triggered');
				}
			});
		}

		return actions;
	}

	/**
	 * Validate and format student update data
	 */
	static validateAndFormatUpdateData(data: Partial<DTOActualizacionAlumno>): {
		isValid: boolean;
		errors: string[];
		formattedData: DTOActualizacionAlumno;
	} {
		const errors: string[] = [];
		const formattedData: DTOActualizacionAlumno = {};

		// Validate and format first name
		if (data.firstName !== undefined) {
			const firstNameValidation = ValidationUtils.validateName(data.firstName);
			if (!firstNameValidation.isValid) {
				errors.push(firstNameValidation.message);
			} else {
				formattedData.firstName = data.firstName.trim();
			}
		}

		// Validate and format last name
		if (data.lastName !== undefined) {
			const lastNameValidation = ValidationUtils.validateName(data.lastName);
			if (!lastNameValidation.isValid) {
				errors.push(lastNameValidation.message);
			} else {
				formattedData.lastName = data.lastName.trim();
			}
		}

		// Validate and format email
		if (data.email !== undefined) {
			const emailValidation = ValidationUtils.validateEmail(data.email);
			if (!emailValidation.isValid) {
				errors.push(emailValidation.message);
			} else {
				formattedData.email = data.email.trim().toLowerCase();
			}
		}

		// Validate and format phone number
		if (data.phoneNumber !== undefined) {
			if (data.phoneNumber) {
				const phoneValidation = ValidationUtils.validatePhoneNumber(data.phoneNumber);
				if (!phoneValidation.isValid) {
					errors.push(phoneValidation.message);
				} else {
					formattedData.phoneNumber = data.phoneNumber.trim();
				}
			} else {
				formattedData.phoneNumber = data.phoneNumber;
			}
		}

		// Boolean fields
		if (data.enabled !== undefined) {
			formattedData.enabled = data.enabled;
		}

		if (data.enrolled !== undefined) {
			formattedData.enrolled = data.enrolled;
		}

		return {
			isValid: errors.length === 0,
			errors,
			formattedData
		};
	}
}
