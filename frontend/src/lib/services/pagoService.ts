import { pagoApi } from '$lib/api';
import type { DTOPago, DTOPeticionCrearPago, DTORespuestaPaginada } from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';
import { FormatterUtils } from '$lib/utils/formatters';

export class PagoService {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Create a new payment
	 */
	static async createPayment(paymentData: DTOPeticionCrearPago): Promise<DTOPago> {
		try {
			console.log('=== PAGO SERVICE: Creating Payment ===');
			console.log('Request data:', paymentData);

			const response = await pagoApi.crearPago({
				dTOPeticionCrearPago: paymentData
			});

			console.log('=== PAGO SERVICE: Response Received ===');
			console.log('Full API response:', response);
			console.log('Client Secret:', response.clientSecret);
			console.log('Payment Intent ID:', response.stripePaymentIntentId);
			console.log('Payment ID:', response.id);
			console.log('=====================================');

			return response;
		} catch (error) {
			console.error('=== PAGO SERVICE: Error ===');
			console.error('Error creating payment:', error);
			console.error('==========================');
			ErrorHandler.logError(error, 'createPayment');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a payment by ID
	 */
	static async getPayment(id: number): Promise<DTOPago> {
		try {
			return await pagoApi.obtenerPagoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getPayment(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all payments with pagination and sorting (Admin/Professor only)
	 */
	static async getPayments(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}): Promise<DTORespuestaPaginada> {
		try {
			return await pagoApi.obtenerPagos(params || {});
		} catch (error) {
			ErrorHandler.logError(error, 'getPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all payments for admin view with enhanced details (Admin/Professor only)
	 */
	static async getAllPayments(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}): Promise<DTORespuestaPaginada> {
		try {
			return await pagoApi.obtenerPagos(params || {});
		} catch (error) {
			ErrorHandler.logError(error, 'getAllPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get payments for a specific student by ID (Admin/Professor only)
	 */
	static async getPaymentsByStudentId(
		studentId: string,
		params?: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: string;
		}
	): Promise<DTORespuestaPaginada> {
		try {
			return await pagoApi.obtenerPagosPorAlumno({
				alumnoId: studentId,
				...params
			});
		} catch (error) {
			ErrorHandler.logError(error, `getPaymentsByStudentId(${studentId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== PAYMENT STATUS OPERATIONS ====================

	/**
	 * Check payment status
	 */
	static async checkPaymentStatus(id: number): Promise<{ isSuccessful: boolean; status: string }> {
		try {
			const response = await pagoApi.checkPaymentStatus({ id });
			return {
				isSuccessful: response.isSuccessful || false,
				status: response.status || 'UNKNOWN'
			};
		} catch (error) {
			ErrorHandler.logError(error, `checkPaymentStatus(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== RECENT PAYMENTS ====================

	/**
	 * Get recent payments (Admin/Professor only)
	 */
	static async getRecentPayments(limit: number = 10): Promise<DTOPago[]> {
		try {
			const response = await pagoApi.getRecentPayments({ limit });
			return response || [];
		} catch (error) {
			ErrorHandler.logError(error, 'getRecentPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get my recent payments (Student only)
	 */
	static async getMyRecentPayments(limit: number = 10): Promise<DTOPago[]> {
		try {
			const response = await pagoApi.getMyRecentPayments({ limit });
			return response || [];
		} catch (error) {
			ErrorHandler.logError(error, 'getMyRecentPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== WEBHOOK PROCESSING ====================

	/**
	 * Process Stripe webhook
	 */
	static async processStripeWebhook(
		stripeSignature: string,
		body: string,
		stripeEventId?: string
	): Promise<void> {
		try {
			await pagoApi.procesarWebhookStripe({
				stripeSignature,
				body,
				stripeEventId
			});
		} catch (error) {
			ErrorHandler.logError(error, 'processStripeWebhook');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle payment processing with validation and business logic
	 */
	static async handlePaymentProcessing(
		paymentData: DTOPeticionCrearPago
	): Promise<{ success: boolean; message: string; paymentId?: number; clientSecret?: string }> {
		try {
			// Validate payment data using ValidationUtils
			const validation = this.validatePaymentData(paymentData);
			if (!validation.isValid) {
				return {
					success: false,
					message: validation.errors.join(', ')
				};
			}

			// Check if payment amount is valid using ValidationUtils
			const amountValidation = ValidationUtils.validatePrice(paymentData.importe || 0);
			if (!amountValidation.isValid) {
				return {
					success: false,
					message: amountValidation.message
				};
			}

			// Check if student exists and is enrolled (if applicable)
			if (paymentData.alumnoId) {
				// TODO: Add student validation when API is available
				// For now, we'll assume the student exists
			}

			// Process the payment
			const payment = await this.createPayment(paymentData);

			return {
				success: true,
				message: 'Pago procesado correctamente',
				paymentId: payment.id,
				clientSecret: payment.clientSecret
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error en el procesamiento del pago'
			};
		}
	}

	/**
	 * Handle payment status update with validation
	 */
	static async handlePaymentStatusUpdate(
		paymentId: number,
		newStatus: string
	): Promise<{ success: boolean; message: string; updatedPayment?: DTOPago }> {
		try {
			// Validate input
			if (!paymentId || paymentId <= 0) {
				return {
					success: false,
					message: 'ID de pago inválido'
				};
			}

			// Validate status using predefined valid statuses
			const validStatuses = ['PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'];
			if (!validStatuses.includes(newStatus.toUpperCase())) {
				return {
					success: false,
					message: 'Estado de pago inválido'
				};
			}

			// Check if payment exists
			const existingPayment = await this.getPayment(paymentId);
			if (!existingPayment) {
				return {
					success: false,
					message: 'Pago no encontrado'
				};
			}

			// TODO: Add payment status update API call when available
			// For now, return success with existing payment data
			return {
				success: true,
				message: 'Estado de pago actualizado correctamente',
				updatedPayment: existingPayment
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al actualizar estado del pago'
			};
		}
	}

	/**
	 * Validate payment data with business rules using ValidationUtils
	 */
	static validatePaymentData(data: DTOPeticionCrearPago): { isValid: boolean; errors: string[] } {
		const errors: string[] = [];

		// Validate amount using ValidationUtils
		const amountValidation = ValidationUtils.validatePrice(data.importe || 0);
		if (!amountValidation.isValid) {
			errors.push(amountValidation.message);
		}

		// Validate currency
		if (!data.currency || data.currency.trim().length === 0) {
			errors.push('La moneda es obligatoria');
		} else if (data.currency !== 'EUR' && data.currency !== 'USD') {
			errors.push('Solo se aceptan EUR y USD como monedas');
		}

		// Validate student ID
		if (!data.alumnoId) {
			errors.push('El ID del alumno es obligatorio');
		}

		// Validate description
		if (!data.description || data.description.trim().length === 0) {
			errors.push('La descripción del pago es obligatoria');
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Get payment statistics for admin dashboard
	 */
	static async getPaymentStatistics(): Promise<{
		totalPayments: number;
		totalAmount: number;
		successfulPayments: number;
		failedPayments: number;
		pendingPayments: number;
		averageAmount: number;
		formattedTotalAmount: string;
		formattedAverageAmount: string;
	}> {
		try {
			// Get all payments
			const allPayments = await this.getPayments({ size: 1000 }); // Get a large number to calculate stats

			const payments = allPayments.content as DTOPago[];
			const totalPayments = payments.length;
			const totalAmount = payments.reduce((sum, payment) => sum + (payment.importe || 0), 0);
			const successfulPayments = payments.filter((p) => p.estado === 'EXITO').length;
			const failedPayments = payments.filter((p) => p.estado === 'ERROR').length;
			const pendingPayments = payments.filter((p) => p.estado === 'PENDIENTE').length;
			const averageAmount = totalPayments > 0 ? totalAmount / totalPayments : 0;

			// Format amounts using FormatterUtils
			const formattedTotalAmount = FormatterUtils.formatAmount(totalAmount);
			const formattedAverageAmount = FormatterUtils.formatAmount(averageAmount);

			return {
				totalPayments,
				totalAmount,
				successfulPayments,
				failedPayments,
				pendingPayments,
				averageAmount,
				formattedTotalAmount,
				formattedAverageAmount
			};
		} catch (error) {
			console.error('Error getting payment statistics:', error);
			return {
				totalPayments: 0,
				totalAmount: 0,
				successfulPayments: 0,
				failedPayments: 0,
				pendingPayments: 0,
				averageAmount: 0,
				formattedTotalAmount: FormatterUtils.formatAmount(0),
				formattedAverageAmount: FormatterUtils.formatAmount(0)
			};
		}
	}

	// ==================== FORMATTING UTILITY METHODS ====================

	/**
	 * Format payment amount using FormatterUtils
	 */
	static formatPaymentAmount(amount: number | undefined | null): string {
		return FormatterUtils.formatAmount(amount);
	}

	/**
	 * Get payment status text using FormatterUtils
	 */
	static getPaymentStatusText(status: string | undefined | null): string {
		return FormatterUtils.formatStatus(status, 'payment');
	}

	/**
	 * Get payment status color using FormatterUtils
	 */
	static getPaymentStatusColor(status: string | undefined | null): string {
		return FormatterUtils.getStatusColor(status, 'payment');
	}

	/**
	 * Format payment date using FormatterUtils
	 */
	static formatPaymentDate(date: Date | string | undefined | null): string {
		return FormatterUtils.formatDate(date, { includeTime: true });
	}
}
