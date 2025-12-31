<script lang="ts">
	import { getStripe } from '$lib/stripe.js';
	import type { Stripe, StripeElements, StripePaymentElement } from '@stripe/stripe-js';
	import { PagoService } from '$lib/services/pagoService.js';

	// Generate unique component ID for debugging
	const componentId = Math.random().toString(36).substring(2, 9);
	console.log(`PaymentForm component ${componentId} initialized`);

	// Props using Svelte 5 syntax
	const {
		amount = 0,
		description = '',
		studentId = '',
		classId = null,
		onError = () => {}
	} = $props<{
		amount?: number;
		description?: string;
		studentId?: string;
		classId?: number | null;
		onError?: (error: Error) => void;
	}>();

	// State using Svelte 5 syntax
	let loading = $state(false);
	let error = $state('');
	let stripeInstance = $state<Stripe | null>(null);
	let stripeLoaded = $state(false);
	let elements = $state<StripeElements | null>(null);
	let paymentElement = $state<StripePaymentElement | null>(null);
	let paymentForm = $state<HTMLFormElement | null>(null);
	let paymentElementReady = $state(false);
	let currentPaymentId = $state<string | null>(null);
	let paymentVerificationInterval = $state<NodeJS.Timeout | null>(null);

	// Load Stripe on component mount
	$effect(() => {
		loadStripe();
	});

	async function loadStripe(retryCount = 0) {
		const maxRetries = 3;

		try {
			stripeInstance = await getStripe();
			stripeLoaded = true;
		} catch (err) {
			console.error(
				`PaymentForm ${componentId}: Failed to load Stripe (attempt ${retryCount + 1}):`,
				err
			);

			if (retryCount < maxRetries) {
				console.log(`PaymentForm ${componentId}: Retrying in 1 second...`);
				setTimeout(() => loadStripe(retryCount + 1), 1000);
			} else {
				error = 'Payment system is currently unavailable. Please refresh the page and try again.';
			}
		}
	}

	// Derived values
	const isDisabled = $derived(loading || !stripeLoaded || !paymentElementReady);
	const buttonText = $derived(loading ? 'Procesando...' : `Pagar €${amount.toFixed(2)}`);

	async function initializePayment() {
		if (!stripeInstance) {
			error = 'Payment system not loaded';
			return;
		}

		try {
			// 1. Create payment on your backend
			const paymentData = {
				importe: amount,
				alumnoId: studentId,
				description,
				currency: 'EUR',
				classId: classId // Include classId for enrollment payments
			};

			const payment = await PagoService.createPayment(paymentData);

			// 2. Create Stripe Elements

			elements = stripeInstance.elements({
				clientSecret: payment.clientSecret || '',
				appearance: {
					theme: 'stripe',
					variables: {
						colorPrimary: '#3b82f6'
					}
				}
			});

			// 3. Create and mount payment element
			paymentElement = elements.create('payment');
			paymentElement.mount('#payment-element');

			// Store the payment ID for use in handleSubmit
			currentPaymentId = payment.id?.toString() || null;
			// Mark payment element as ready
			paymentElementReady = true;
			// Don't call onSuccess here - only call it after successful payment submission
		} catch (err: unknown) {
			console.error(`PaymentForm ${componentId}: Error initializing payment:`, err);
			const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
			error = errorMessage;
			onError(err instanceof Error ? err : new Error(errorMessage));
		}
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!stripeInstance || !elements) {
			error = 'Payment system not ready';
			return;
		}

		loading = true;
		error = '';

		try {
			// 1. Submit the payment element
			const { error: submitError } = await elements.submit();

			if (submitError) {
				throw new Error(submitError.message);
			}

			// 2. Confirm the payment
			const returnUrl = `${window.location.origin}/payment-success?payment_id=${currentPaymentId}${classId ? `&classId=${classId}` : ''}`;

			const { error: confirmError } = await stripeInstance.confirmPayment({
				elements,
				confirmParams: {
					return_url: returnUrl
				}
			});

			// If no error, Stripe will automatically redirect to return_url
			if (!confirmError) {
				console.log(`PaymentForm ${componentId}: Payment successful, redirecting to ${returnUrl}`);
			}

			if (confirmError) {
				console.error(`PaymentForm ${componentId}: Confirmation error:`, confirmError);
				console.error(`PaymentForm ${componentId}: Error details:`, {
					type: confirmError.type,
					code: confirmError.code,
					message: confirmError.message,
					payment_intent: confirmError.payment_intent
				});

				// Handle specific error types
				switch (confirmError.type) {
					case 'card_error':
					case 'validation_error':
						throw new Error(confirmError.message || 'Payment validation failed');
					case 'authentication_error':
						throw new Error('Payment authentication failed. Please try again.');
					default:
						throw new Error(confirmError.message || 'Payment failed. Please try again.');
				}
			}

			// Start payment verification polling (as backup to webhooks)
			if (currentPaymentId) {
				startPaymentVerificationPolling(parseInt(currentPaymentId));
			}

			// Note: User will be redirected to return_url automatically by Stripe
			// No need to call onSuccess here as the redirect will happen automatically
		} catch (err: unknown) {
			console.error(`PaymentForm ${componentId}: Payment error:`, err);
			const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
			error = errorMessage;
			onError(err instanceof Error ? err : new Error(errorMessage));
		} finally {
			loading = false;
		}
	}

	// Initialize payment when component is ready
	$effect(() => {
		if (stripeLoaded && stripeInstance) {
			initializePayment();
		}
	});

	// Payment verification polling function
	async function startPaymentVerificationPolling(paymentId: number) {
		const maxAttempts = 10; // 10 attempts with 2-second intervals = 20 seconds
		let attempts = 0;

		const poll = async () => {
			if (attempts >= maxAttempts) {
				return;
			}

			try {
				const status = await PagoService.checkPaymentStatus(paymentId);
				if (status.isSuccessful) {
					stopPaymentVerificationPolling();
					return;
				}
			} catch {
				// Silent fail for polling
			}

			attempts++;
			paymentVerificationInterval = setTimeout(poll, 2000); // Poll every 2 seconds
		};

		// Start polling after a short delay to allow webhooks to process first
		paymentVerificationInterval = setTimeout(poll, 1000);
	}

	function stopPaymentVerificationPolling() {
		if (paymentVerificationInterval) {
			clearTimeout(paymentVerificationInterval);
			paymentVerificationInterval = null;
		}
	}

	// Cleanup on unmount
	$effect(() => {
		return () => {
			if (paymentElement) {
				paymentElement.destroy();
			}
			stopPaymentVerificationPolling();
		};
	});
</script>

<form onsubmit={handleSubmit} bind:this={paymentForm}>
	<div class="mx-auto max-w-lg rounded-lg border border-gray-200 bg-white p-8 shadow-sm">
		<h2 class="mb-6 text-center text-2xl font-semibold text-gray-900">
			Vas a inscribirte en {description}
		</h2>

		<div class="mb-6 rounded-md bg-gray-50 p-4">
			<p class="mb-2 text-gray-700"><strong>Importe:</strong> €{amount.toFixed(2)}</p>
			<p class="text-gray-700"><strong>Descripción:</strong> {description}</p>
		</div>

		{#if !paymentElementReady}
			<div class="mb-6 rounded-md border border-gray-200 bg-gray-50 p-8 text-center text-gray-500">
				<p>Cargando el formulario de pago...</p>
			</div>
		{/if}

		<div id="payment-element" class="mb-6">
			<!-- Stripe Elements will be mounted here -->
		</div>

		{#if error}
			<div class="mb-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
				{error}
			</div>
		{/if}

		<button
			type="submit"
			disabled={isDisabled}
			class="w-full cursor-pointer rounded-md border-none bg-blue-600 px-6 py-3 text-base font-medium text-white transition-colors duration-200 hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-400"
		>
			{buttonText}
		</button>
	</div>
</form>

<!-- Stripe Elements styling with Tailwind classes -->
<div class="stripe-elements-styles">
	<style>
		/* Stripe Elements styling */
		:global(.StripeElement) {
			padding: 0.75rem;
			border: 1px solid #d1d5db;
			border-radius: 0.375rem;
			background-color: white;
		}

		:global(.StripeElement--focus) {
			border-color: #3b82f6;
			box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
		}

		:global(.StripeElement--invalid) {
			border-color: #dc2626;
		}
	</style>
</div>
