<script lang="ts">
	import { getStripe } from '$lib/stripe.js';
	import type { Stripe, StripeElements, StripePaymentElement } from '@stripe/stripe-js';
	import { PagoService } from '$lib/services/pagoService.js';

	// Props using Svelte 5 syntax
	const {
		amount = 0,
		description = '',
		studentId = ''
	} = $props<{
		amount?: number;
		description?: string;
		studentId?: string;
	}>();

	// State using Svelte 5 syntax
	let loading = $state(false);
	let error = $state('');
	let stripeInstance = $state<Stripe | null>(null);
	let stripeLoaded = $state(false);
	let elements = $state<StripeElements | null>(null);
	let paymentElement = $state<StripePaymentElement | null>(null);
	let paymentForm: HTMLFormElement;

	// Load Stripe on component mount
	$effect(() => {
		loadStripe();
	});

	async function loadStripe() {
		try {
			stripeInstance = await getStripe();
			stripeLoaded = true;
		} catch (err) {
			console.error('Failed to load Stripe:', err);
			error = 'Payment system is currently unavailable. Please try again later.';
		}
	}

	// Derived values
	const isDisabled = $derived(loading || !stripeLoaded);
	const buttonText = $derived(loading ? 'Processing...' : `Pay €${amount.toFixed(2)}`);

	// Initialize Stripe elements using $effect
	$effect(() => {
		if (stripeLoaded && stripeInstance) {
			initializeStripeElements();
		}

		// Cleanup function
		return () => {
			if (paymentElement) {
				paymentElement.destroy();
			}
		};
	});

	async function initializeStripeElements() {
		if (!stripeInstance) return;

		try {
			// Create payment intent first
			const payment = await PagoService.createPayment({
				importe: amount,
				alumnoId: studentId,
				description,
				currency: 'EUR'
			});

			// Create elements
			elements = stripeInstance.elements({
				clientSecret: payment.clientSecret || '',
				appearance: {
					theme: 'stripe',
					variables: {
						colorPrimary: '#6772e5'
					}
				}
			});

			// Create payment element
			paymentElement = elements.create('payment');
			paymentElement.mount('#payment-element');
		} catch (err: unknown) {
			error = err instanceof Error ? err.message : 'An unknown error occurred';
		}
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!stripeInstance || !elements) {
			return;
		}

		loading = true;
		error = '';

		const { error: submitError } = await elements.submit();

		if (submitError) {
			error = submitError.message || 'Payment submission failed';
			loading = false;
			return;
		}

		// Confirm payment
		const { error: confirmError } = await stripeInstance.confirmPayment({
			elements,
			confirmParams: {
				return_url: `${window.location.origin}/payment-success`
			}
		});

		if (confirmError) {
			error = confirmError.message || 'Payment confirmation failed';
			loading = false;
		}
	}
</script>

<form onsubmit={handleSubmit} bind:this={paymentForm}>
	<div class="mx-auto max-w-2xl rounded-lg border border-gray-200 bg-white p-8 shadow-sm">
		<h2 class="mb-6 text-center text-2xl font-semibold text-gray-900">Payment Details</h2>

		<div class="mb-6 rounded-md bg-gray-50 p-4">
			<p class="mb-2 text-gray-700"><strong>Amount:</strong> €{amount.toFixed(2)}</p>
			<p class="text-gray-700"><strong>Description:</strong> {description}</p>
		</div>

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
