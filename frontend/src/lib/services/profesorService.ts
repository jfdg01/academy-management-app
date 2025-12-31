import {
	type DTOProfesor,
	type DTOPeticionRegistroProfesor,
	type DTOActualizacionProfesor,
	type DTORespuestaPaginada,
	type DTOClase,
	type DTOProfesorPublico
} from '$lib/generated/api';
import { profesorApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';

// Define the ProfesorStatistics type used in the estadisticas page
export interface ProfesorStatistics {
	totalCount: number;
	activeCount: number;
	inactiveCount: number;
	newThisMonth: number;
	newThisYear: number;
	bySubject?: Record<string, number>;
	byExperience?: Record<string, number>;
}

// Import pagination types
import type { SortDirection } from '$lib/types/pagination';

export class ProfesorService {
	static async getAllProfesores(filters: {
		firstName?: string;
		lastName?: string;
		email?: string;
		enabled?: boolean;
	}): Promise<DTOProfesor[]> {
		try {
			const response = await profesorApi.obtenerProfesores(filters);
			return response.content as DTOProfesor[];
		} catch (error) {
			ErrorHandler.logError(error, 'getAllProfesores');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getProfesoresPaginados(
		filters: {
			q?: string; // General search parameter
			firstName?: string;
			lastName?: string;
			email?: string;
			username?: string;
			dni?: string;
			enabled?: boolean;
			claseId?: string;
			sinClases?: boolean;
		},
		pagination: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: SortDirection;
		}
	): Promise<DTORespuestaPaginada> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const response = await profesorApi.obtenerProfesores({
				...filters,
				page,
				size,
				sortBy,
				sortDirection
			});

			return response as DTORespuestaPaginada;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPaginados');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getProfesorById(id: number): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getProfesorById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get professor's public profile information
	 */
	static async getPerfilProfesor(id: number): Promise<DTOProfesorPublico> {
		try {
			const response = await profesorApi.obtenerPerfilProfesor({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getPerfilProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all classes that a professor teaches
	 */
	static async getClasesProfesor(id: number): Promise<DTOClase[]> {
		try {
			const response = await profesorApi.obtenerClasesProfesor({ id });
			return response as DTOClase[];
		} catch (error) {
			ErrorHandler.logError(error, `getClasesProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get professor with classes using Entity Graph for optimal performance
	 */
	static async getProfesorConClases(id: number): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorConClases({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getProfesorConClases(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get professor statistics
	 */
	static async getEstadisticas(): Promise<ProfesorStatistics> {
		try {
			const response = await profesorApi.obtenerEstadisticas();
			return response as ProfesorStatistics;
		} catch (error) {
			ErrorHandler.logError(error, 'getEstadisticas');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async createProfesor(profesorData: DTOPeticionRegistroProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.crearProfesor({
				dTOPeticionRegistroProfesor: profesorData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createProfesor');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async updateProfesor(
		id: number,
		profesorData: DTOActualizacionProfesor
	): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesorParcial({
				id,
				dTOActualizacionProfesor: profesorData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Replace entire professor record
	 */
	static async replaceProfesor(
		id: number,
		profesorData: DTOActualizacionProfesor
	): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.reemplazarProfesor({
				id,
				dTOActualizacionProfesor: profesorData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `replaceProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async deleteProfesor(id: number): Promise<void> {
		try {
			await profesorApi.eliminarProfesor({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle professor status change with validation and business logic
	 */
	static async handleStatusChange(
		profesorId: number,
		newStatus: boolean
	): Promise<{ success: boolean; message: string; updatedProfesor?: DTOProfesor }> {
		try {
			// Validate input
			if (!profesorId || profesorId <= 0) {
				return {
					success: false,
					message: 'ID de profesor inválido'
				};
			}

			// Check if professor exists
			const existingProfesor = await this.getProfesorById(profesorId);
			if (!existingProfesor) {
				return {
					success: false,
					message: 'Profesor no encontrado'
				};
			}

			// Check if status is actually changing
			if (existingProfesor.enabled === newStatus) {
				return {
					success: false,
					message: `La cuenta ya está ${newStatus ? 'habilitada' : 'deshabilitada'}`
				};
			}

			// Perform the status change
			const updatedProfesor = await this.updateProfesor(profesorId, { enabled: newStatus });

			return {
				success: true,
				message: `Cuenta ${newStatus ? 'habilitada' : 'deshabilitada'} correctamente`,
				updatedProfesor
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al cambiar estado de cuenta'
			};
		}
	}

	/**
	 * Validate professor registration data with business rules
	 */
	static validateRegistrationData(data: DTOPeticionRegistroProfesor): {
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

	/**
	 * Validate and format professor update data
	 */
	static validateAndFormatUpdateData(data: Partial<DTOActualizacionProfesor>): {
		isValid: boolean;
		errors: string[];
		formattedData: DTOActualizacionProfesor;
	} {
		const errors: string[] = [];
		const formattedData: DTOActualizacionProfesor = {};

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

		return {
			isValid: errors.length === 0,
			errors,
			formattedData
		};
	}

	/**
	 * Format professor data for display
	 */
	static formatProfessorData(profesor: DTOProfesor): {
		fullName: string;
		formattedDNI: string;
		formattedEmail: string;
		formattedPhone: string;
		statusText: string;
		statusColor: string;
		createdDate: string;
		lastModifiedDate: string;
	} {
		return {
			fullName: `${profesor.firstName} ${profesor.lastName}`,
			formattedDNI: profesor.dni || 'N/A',
			formattedEmail: profesor.email || 'N/A',
			formattedPhone: profesor.phoneNumber || 'N/A',
			statusText: profesor.enabled ? 'Activo' : 'Inactivo',
			statusColor: profesor.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800',
			createdDate: 'N/A', // DTOProfesor doesn't have createdAt property
			lastModifiedDate: 'N/A' // DTOProfesor doesn't have updatedAt property
		};
	}

	/**
	 * Get available actions for a professor based on user permissions
	 */
	static getAvailableActions(
		user: { role?: string }, // User with role property
		profesor?: DTOProfesor
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
		if (profesor) {
			actions.push({
				id: 'view',
				label: 'Ver detalles',
				icon: '👁️',
				color: 'blue',
				action: () => {
					// Navigate to professor details
					console.log('View professor details');
				}
			});
		}

		// Edit action - for admins only
		if (user && user.role === 'ADMIN') {
			actions.push({
				id: 'edit',
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				action: () => {
					// Navigate to edit professor
					console.log('Edit professor');
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
					console.log('Delete professor');
				}
			});
		}

		return actions;
	}
}
