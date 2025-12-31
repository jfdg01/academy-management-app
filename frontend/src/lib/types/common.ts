// Common TypeScript interfaces and types for the application

// ==================== PAGINATION TYPES ====================

export interface PaginationParams {
	page?: number;
	size?: number;
	sortBy?: string;
	sortDirection?: 'ASC' | 'DESC';
}

export interface PaginatedResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	currentPage?: number;
	pageSize?: number;
}

// ==================== FILTER TYPES ====================

export interface BaseFilters extends PaginationParams {
	q?: string; // General search parameter
}

export interface AlumnoFilters extends BaseFilters {
	firstName?: string;
	lastName?: string;
	dni?: string;
	email?: string;
	enrolled?: boolean;
	enabled?: boolean;
	available?: boolean;
}

export interface ProfesorFilters extends BaseFilters {
	firstName?: string;
	lastName?: string;
	email?: string;
	username?: string;
	dni?: string;
	enabled?: boolean;
	claseId?: string;
	sinClases?: boolean;
}

export interface ClaseFilters extends BaseFilters {
	nombre?: string;
	descripcion?: string;
	precio?: number;
	enabled?: boolean;
	profesorId?: string;
}

export interface EjercicioFilters extends BaseFilters {
	classId?: string;
	name?: string;
	statement?: string;
	status?: string;
}

export interface EntregaFilters extends BaseFilters {
	alumnoId?: string;
	ejercicioId?: string;
	estado?: string;
	notaMin?: number;
	notaMax?: number;
}

export interface MaterialFilters extends BaseFilters {
	name?: string;
	url?: string;
	type?: string;
}

export interface PagoFilters extends BaseFilters {
	alumnoId?: string;
	estado?: string;
	fechaDesde?: string;
	fechaHasta?: string;
}

// ==================== SERVICE RESULT TYPES ====================

export interface ServiceResult<T = void> {
	success: boolean;
	message: string;
	data?: T;
	error?: Error;
}

export interface ValidationResult {
	isValid: boolean;
	message: string;
}

// ==================== CONSTANTS ====================

export const VALIDATION_CONSTANTS = {
	MIN_NAME_LENGTH: 3,
	MAX_NAME_LENGTH: 100,
	MIN_USERNAME_LENGTH: 3,
	MAX_USERNAME_LENGTH: 50,
	MIN_PASSWORD_LENGTH: 6,
	MAX_EMAIL_LENGTH: 254,
	MAX_LOCAL_EMAIL_LENGTH: 64,
	MIN_PHONE_DIGITS: 6,
	MAX_PHONE_DIGITS: 14,
	MIN_GRADE: 0,
	MAX_GRADE: 10,
	MIN_PRICE: 0
} as const;

export const STATUS_TYPES = {
	DELIVERY: ['PENDIENTE', 'ENTREGADO', 'CALIFICADO'] as const,
	EXERCISE: ['ACTIVE', 'EXPIRED', 'FUTURE', 'WITH_DELIVERIES', 'WITHOUT_DELIVERIES'] as const,
	PAYMENT: ['EXITO', 'PENDIENTE', 'PROCESANDO', 'ERROR', 'REEMBOLSADO'] as const
} as const;

export const CURRENCIES = ['EUR', 'USD'] as const;

// ==================== UTILITY TYPES ====================

export type StatusType = keyof typeof STATUS_TYPES;
export type DeliveryStatus = (typeof STATUS_TYPES.DELIVERY)[number];
export type ExerciseStatus = (typeof STATUS_TYPES.EXERCISE)[number];
export type PaymentStatus = (typeof STATUS_TYPES.PAYMENT)[number];
export type Currency = (typeof CURRENCIES)[number];

export type SortDirection = 'ASC' | 'DESC';

// ==================== ENTITY TYPES ====================

export interface EntityWithId {
	id: number;
}

export interface EntityWithStatus extends EntityWithId {
	estado?: string;
}

export interface EntityWithTimestamps extends EntityWithId {
	createdAt?: string;
	updatedAt?: string;
}

// ==================== API RESPONSE TYPES ====================

export interface ApiErrorResponse {
	message: string;
	errorCode?: string;
	fieldErrors?: Record<string, string[]>;
	timestamp?: string;
	path?: string;
	status?: number;
}

export interface ApiSuccessResponse<T> {
	data: T;
	message?: string;
	timestamp?: string;
}
