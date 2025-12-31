<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { PagoService } from '$lib/services/pagoService.js';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOPago } from '$lib/generated/api';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State management
	let payment = $state<DTOPago | null>(null);
	let loading = $state(true);
	let error = $state('');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	// Load payment details
	$effect(() => {
		const paymentId = $page.params.id;
		if (paymentId) {
			loadPayment(parseInt(paymentId));
		}
	});

	async function loadPayment(id: number) {
		try {
			loading = true;
			error = '';
			payment = await PagoService.getPayment(id);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading payment details';
		} finally {
			loading = false;
		}
	}

	function getPaymentMethodText(method: string | undefined): string {
		if (!method) return 'Unknown';
		switch (method) {
			case 'STRIPE':
				return 'Stripe';
			case 'EFECTIVO':
				return 'Cash';
			case 'TRANSFERENCIA':
				return 'Bank Transfer';
			default:
				return method;
		}
	}

	function canViewPayment(): boolean {
		if (!payment || !authStore.user) return false;

		// Admin and professors can view any payment
		if (
			authStore.user.roles?.includes('ROLE_ADMIN') ||
			authStore.user.roles?.includes('ROLE_PROFESOR')
		) {
			return true;
		}

		// Students can only view their own payments
		if (authStore.user.roles?.includes('ROLE_ALUMNO')) {
			return payment.alumnoId === authStore.user.id?.toString();
		}

		return false;
	}
</script>

<svelte:head>
	<title>Payment Details - {payment?.id || 'Loading...'}</title>
</svelte:head>

<div class="mx-auto max-w-4xl p-8">
	<div class="mb-8 flex items-center justify-between">
		<h1 class="text-4xl font-semibold text-gray-900">Payment Details</h1>
		<button
			class="cursor-pointer rounded-md border-none bg-gray-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-gray-700"
			onclick={() => goto('/pagos')}
		>
			Back to Payments
		</button>
	</div>

	{#if error}
		<ErrorDisplay {error} />
	{/if}

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="text-lg text-gray-500">Loading payment details...</div>
		</div>
	{:else if !payment}
		<div class="rounded-lg border border-gray-200 bg-white p-8 text-center shadow-sm">
			<div class="text-lg font-medium text-gray-900">Payment not found</div>
			<div class="text-gray-500">The requested payment could not be found.</div>
		</div>
	{:else if !canViewPayment()}
		<div class="rounded-lg border border-gray-200 bg-white p-8 text-center shadow-sm">
			<div class="text-lg font-medium text-gray-900">Access Denied</div>
			<div class="text-gray-500">You don't have permission to view this payment.</div>
		</div>
	{:else}
		<!-- Payment Information -->
		<div class="grid gap-6">
			<!-- Basic Payment Info -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Payment Information</h2>
				<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
					<div>
						<div class="text-sm font-medium text-gray-500">Payment ID</div>
						<p class="text-lg font-semibold text-gray-900">{payment.id}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Amount</div>
						<p class="text-lg font-semibold text-gray-900">
							{FormatterUtils.formatAmount(payment.importe)}
						</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Date & Time</div>
						<p class="text-lg text-gray-900">
							{FormatterUtils.formatDate(payment.fechaPago, {
								includeTime: true,
								includeSeconds: true
							})}
						</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Payment Method</div>
						<p class="text-lg text-gray-900">{getPaymentMethodText(payment.metodoPago)}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Status</div>
						<p class="text-lg">
							<span
								class="inline-flex rounded-full px-3 py-1 text-sm font-semibold {FormatterUtils.getStatusColor(
									payment.estado || 'UNKNOWN',
									'payment'
								)}"
							>
								{FormatterUtils.formatStatus(payment.estado, 'payment')}
							</span>
						</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Student ID</div>
						<p class="text-lg text-gray-900">{payment.alumnoId}</p>
					</div>
				</div>
			</div>

			<!-- Stripe Information (if applicable) -->
			{#if payment.metodoPago === 'STRIPE'}
				<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
					<h2 class="mb-4 text-xl font-semibold text-gray-900">Stripe Information</h2>
					<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
						{#if payment.stripePaymentIntentId}
							<div>
								<div class="text-sm font-medium text-gray-500">Payment Intent ID</div>
								<p class="font-mono text-sm text-gray-900">{payment.stripePaymentIntentId}</p>
							</div>
						{/if}
						{#if payment.stripeChargeId}
							<div>
								<div class="text-sm font-medium text-gray-500">Charge ID</div>
								<p class="font-mono text-sm text-gray-900">{payment.stripeChargeId}</p>
							</div>
						{/if}
					</div>
				</div>
			{/if}

			<!-- Error Information (if applicable) -->
			{#if payment.failureReason}
				<div class="rounded-lg border border-red-200 bg-red-50 p-6 shadow-sm">
					<h2 class="mb-4 text-xl font-semibold text-red-900">Error Information</h2>
					<div>
						<div class="text-sm font-medium text-red-700">Failure Reason</div>
						<p class="text-red-900">{payment.failureReason}</p>
					</div>
				</div>
			{/if}

			<!-- Payment Items -->
			{#if payment.items && payment.items.length > 0}
				<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
					<h2 class="mb-4 text-xl font-semibold text-gray-900">Payment Items</h2>
					<div class="overflow-x-auto">
						<table class="min-w-full divide-y divide-gray-200">
							<thead class="bg-gray-50">
								<tr>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Item ID
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Description
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Quantity
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Unit Price
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Total
									</th>
								</tr>
							</thead>
							<tbody class="divide-y divide-gray-200 bg-white">
								{#each payment.items as item (item.id)}
									<tr>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">{item.id}</td>
										<td class="px-6 py-4 text-sm text-gray-900">{item.concepto}</td>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900"
											>{item.cantidad}</td
										>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{FormatterUtils.formatAmount(item.precioUnitario)}
										</td>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{FormatterUtils.formatAmount(item.importeTotal)}
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				</div>
			{/if}

			<!-- Invoice Information -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Invoice Information</h2>
				<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
					<div>
						<div class="text-sm font-medium text-gray-500">Invoice Created</div>
						<p class="text-lg text-gray-900">
							{payment.facturaCreada ? 'Yes' : 'No'}
						</p>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>
