import { ResponseError } from '$lib/generated/api/runtime';

export interface BackendErrorResponse {
	message: string;
	errorCode?: string;
	fieldErrors?: Record<string, string[]>;
	timestamp?: string;
	path?: string;
	status?: number;
}

export interface ErrorInfo {
	message: string;
	title?: string;
	type: 'error' | 'warning' | 'info';
	fieldErrors?: Record<string, string[]>;
	canRetry?: boolean;
}

/**
 * Centralized error handling utility for the frontend
 */
export class ErrorHandler {
	/**
	 * Parse error from API response and return user-friendly error info
	 */
	static async parseError(error: unknown): Promise<ErrorInfo> {
		// Handle ResponseError from the generated API client
		if (error instanceof ResponseError) {
			return await this.parseResponseError(error);
		}

		// Handle fetch errors
		if (error && typeof error === 'object' && 'response' in error) {
			return this.parseFetchError(error as { response: Response });
		}

		// Handle generic errors
		if (error instanceof Error) {
			return {
				message: error.message || 'Ha ocurrido un error inesperado',
				type: 'error',
				canRetry: true
			};
		}

		// Fallback for unknown errors
		return {
			message: 'Ha ocurrido un error inesperado',
			type: 'error',
			canRetry: true
		};
	}

	/**
	 * Parse ResponseError from the generated API client
	 */
	private static async parseResponseError(responseError: ResponseError): Promise<ErrorInfo> {
		try {
			const response = responseError.response;
			const status = response.status;

			// Try to parse the error response body
			let errorData: BackendErrorResponse | null = null;
			try {
				const responseText = await response.text();
				if (responseText) {
					errorData = JSON.parse(responseText);
				}
			} catch {
				// If we can't parse JSON, use default error handling
			}

			// If we have structured error data from backend, use it
			if (errorData?.message) {
				return {
					message: errorData.message,
					title: this.getErrorTitle(status),
					type: 'error',
					fieldErrors: errorData.fieldErrors,
					canRetry: this.canRetry(status)
				};
			}

			// Fallback to status-based error messages
			return this.getStatusBasedError(status);
		} catch {
			// If anything goes wrong in parsing, return generic error
			return {
				message: 'Error de comunicación con el servidor',
				type: 'error',
				canRetry: true
			};
		}
	}

	/**
	 * Parse fetch errors (for non-ResponseError cases)
	 */
	private static parseFetchError(error: { response: Response }): ErrorInfo {
		const status = error.response.status;
		return this.getStatusBasedError(status);
	}

	/**
	 * Get error title based on HTTP status code
	 */
	private static getErrorTitle(status: number): string {
		switch (status) {
			case 400:
				return 'Solicitud Incorrecta';
			case 401:
				return 'No Autorizado';
			case 403:
				return 'Acceso Denegado';
			case 404:
				return 'No Encontrado';
			case 409:
				return 'Conflicto';
			case 422:
				return 'Datos Inválidos';
			case 500:
				return 'Error del Servidor';
			default:
				return 'Error';
		}
	}

	/**
	 * Get user-friendly error message based on HTTP status code
	 */
	private static getStatusBasedError(status: number): ErrorInfo {
		switch (status) {
			case 400:
				return {
					message: 'La solicitud contiene datos incorrectos o incompletos',
					title: 'Solicitud Incorrecta',
					type: 'error',
					canRetry: false
				};
			case 401:
				return {
					message: 'Debes iniciar sesión para acceder a este recurso',
					title: 'No Autorizado',
					type: 'error',
					canRetry: true
				};
			case 403:
				return {
					message: 'No tienes permisos para realizar esta acción',
					title: 'Acceso Denegado',
					type: 'error',
					canRetry: false
				};
			case 404:
				return {
					message: 'El recurso solicitado no fue encontrado',
					title: 'No Encontrado',
					type: 'error',
					canRetry: false
				};
			case 409:
				return {
					message: 'El recurso ya existe o hay un conflicto con los datos',
					title: 'Conflicto',
					type: 'error',
					canRetry: false
				};
			case 422:
				return {
					message: 'Los datos proporcionados no son válidos',
					title: 'Datos Inválidos',
					type: 'error',
					canRetry: false
				};
			case 500:
				return {
					message: 'Error interno del servidor. Por favor, inténtalo más tarde',
					title: 'Error del Servidor',
					type: 'error',
					canRetry: true
				};
			default:
				return {
					message: 'Ha ocurrido un error inesperado',
					title: 'Error',
					type: 'error',
					canRetry: true
				};
		}
	}

	/**
	 * Determine if an operation can be retried based on status code
	 */
	private static canRetry(status: number): boolean {
		// Retry on server errors (5xx) and some client errors (401, 429)
		return status >= 500 || status === 401 || status === 429;
	}

	/**
	 * Format field errors for display
	 */
	static formatFieldErrors(fieldErrors: Record<string, string[]>): string {
		return Object.entries(fieldErrors)
			.map(([field, errors]) => `${field}: ${errors.join(', ')}`)
			.join('; ');
	}

	/**
	 * Log error for debugging (in development)
	 */
	static logError(error: unknown, context?: string): void {
		if (import.meta.env.DEV) {
			console.group(`Error${context ? ` in ${context}` : ''}`);
			console.error(error);
			console.groupEnd();
		}
	}
}
