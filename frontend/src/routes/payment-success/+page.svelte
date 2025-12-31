<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { PagoService } from '$lib/services/pagoService.js';
	import { EnrollmentService } from '$lib/services/enrollmentService.js';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOPago } from '$lib/generated/api';

	// State management
	let paymentStatus = $state('checking');
	let payment = $state<DTOPago | null>(null);
	let error = $state('');
	let enrollmentStatus = $state<string>('');
	let classId = $state<string | null>(null);

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	// Payment processing with retry logic
	$effect(() => {
		const paymentId = $page.url.searchParams.get('payment_id');
		classId = $page.url.searchParams.get('classId');

		if (!paymentId) {
			paymentStatus = 'error';
			error = 'No payment ID found';
			return;
		}

		// Use an IIFE for async operations with retry logic
		(async () => {
			const maxRetries = 10; // Increased from 5 to 10
			let retryCount = 0;

			const checkPayment = async (): Promise<boolean> => {
				try {
					// Check payment status
					const status = await PagoService.checkPaymentStatus(parseInt(paymentId));

					if (status.isSuccessful) {
						paymentStatus = 'success';
						payment = await PagoService.getPayment(parseInt(paymentId));

						// If this was a class enrollment payment, enroll the student
						if (classId && authStore.isAlumno) {
							try {
								await EnrollmentService.enrollInClass(parseInt(classId));
								enrollmentStatus = 'success';
							} catch (enrollmentError) {
								console.error('Error enrolling in class:', enrollmentError);
								enrollmentStatus = 'error';
							}
						}

						return true;
					} else if (status.status === 'PENDIENTE' || status.status === 'PROCESANDO') {
						// Payment is still processing, retry
						return false;
					} else {
						paymentStatus = 'failed';
						return true;
					}
				} catch {
					return false;
				}
			};

			// Initial check
			let resolved = await checkPayment();

			// Retry logic for pending payments
			while (!resolved && retryCount < maxRetries) {
				retryCount++;

				// Wait 3 seconds before retry (increased from 2 seconds)
				await new Promise((resolve) => setTimeout(resolve, 3000));
				resolved = await checkPayment();
			}

			// If still not resolved after retries, show error
			if (!resolved) {
				paymentStatus = 'error';
				error = 'Unable to verify payment status. Please contact support.';
			}
		})();
	});
</script>

<svelte:head>
	<title>Payment Result</title>
</svelte:head>

<div class="mx-auto my-12 max-w-2xl p-8 text-center">
	{#if paymentStatus === 'checking'}
		<div class="rounded-lg border border-blue-200 bg-blue-50 p-8 shadow-sm">
			<h2 class="mb-4 text-2xl font-semibold text-blue-700">Processing Payment...</h2>
			<p class="text-blue-600">Please wait while we confirm your payment.</p>
		</div>
	{:else if paymentStatus === 'success'}
		<div class="rounded-lg border border-green-200 bg-green-50 p-8 shadow-sm">
			<h2 class="mb-4 text-2xl font-semibold text-green-700">Payment Successful! 🎉</h2>
			{#if payment}
				<div class="my-6 rounded-md bg-white p-4 text-left">
					<p class="mb-2 text-gray-700"><strong>Amount:</strong> €{payment.importe}</p>
					<p class="mb-2 text-gray-700">
						<strong>Date:</strong>
						{new Date(payment.fechaPago || '').toLocaleDateString()}
					</p>
					<p class="text-gray-700"><strong>Status:</strong> {payment.estado}</p>
				</div>
			{/if}

			{#if classId && enrollmentStatus === 'success'}
				<div class="my-4 rounded-md bg-green-100 p-4 text-green-800">
					<p class="font-semibold">✅ Enrollment Successful!</p>
					<p>You have been successfully enrolled in the class.</p>
				</div>
			{:else if classId && enrollmentStatus === 'error'}
				<div class="my-4 rounded-md bg-yellow-100 p-4 text-yellow-800">
					<p class="font-semibold">⚠️ Payment Successful, Enrollment Issue</p>
					<p>
						Your payment was successful, but there was an issue with your enrollment. Please contact
						support.
					</p>
				</div>
			{/if}

			<div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
				{#if classId}
					<a
						href="/clases/{classId}"
						class="inline-block rounded-md bg-green-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-green-700"
					>
						View Class
					</a>
				{/if}
				<a
					href="/clases"
					class="inline-block rounded-md bg-blue-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-blue-700"
				>
					Browse More Classes
				</a>
			</div>
		</div>
	{:else if paymentStatus === 'failed'}
		<div class="rounded-lg border border-red-200 bg-red-50 p-8 shadow-sm">
			<h2 class="mb-4 text-2xl font-semibold text-red-700">Payment Failed</h2>
			<p class="mb-4 text-red-600">Your payment could not be processed. Please try again.</p>
			<a
				href="/payment"
				class="text-decoration-none inline-block rounded-md bg-blue-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-blue-700"
			>
				Try Again
			</a>
		</div>
	{:else if paymentStatus === 'error'}
		<div class="rounded-lg border border-red-200 bg-red-50 p-8 shadow-sm">
			<h2 class="mb-4 text-2xl font-semibold text-red-700">Error</h2>
			<p class="mb-4 text-red-600">{error}</p>
			<a
				href="/payment"
				class="text-decoration-none inline-block rounded-md bg-blue-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-blue-700"
			>
				Back to Payment
			</a>
		</div>
	{/if}
</div>
