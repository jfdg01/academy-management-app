<script lang="ts">
	import { PagoService } from '$lib/services/pagoService.js';
	import type { DTOPago } from '$lib/generated/api';

	// State using Svelte 5 syntax
	let payments = $state<DTOPago[]>([]);
	let loading = $state(true);
	let error = $state('');

	// Load payments using $effect
	$effect(() => {
		loadPayments();
	});

	async function loadPayments() {
		try {
			// Get recent payments
			payments = await PagoService.getRecentPayments(10);
		} catch (err: unknown) {
			error = err instanceof Error ? err.message : 'An unknown error occurred';
		} finally {
			loading = false;
		}
	}

	function formatDate(dateString: string | Date | undefined): string {
		if (!dateString) return 'N/A';
		return new Date(dateString).toLocaleDateString();
	}

	function getStatusColor(status: string | undefined): string {
		switch (status) {
			case 'EXITO':
				return '#2e7d32';
			case 'PENDIENTE':
				return '#f57c00';
			case 'PROCESANDO':
				return '#1976d2';
			case 'ERROR':
				return '#d32f2f';
			case 'REEMBOLSADO':
				return '#7b1fa2';
			default:
				return '#666';
		}
	}
</script>

<div class="mx-auto max-w-4xl p-4">
	<h2 class="mb-6 text-2xl font-semibold text-gray-900">Payment History</h2>

	{#if loading}
		<p class="text-gray-600">Loading payments...</p>
	{:else if error}
		<p class="text-sm text-red-600">{error}</p>
	{:else if payments.length === 0}
		<p class="text-gray-600">No payments found.</p>
	{:else}
		<div class="flex flex-col gap-4">
			{#each payments as payment (payment.id)}
				<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
					<div class="mb-3 flex items-center justify-between border-b border-gray-100 pb-3">
						<span class="font-semibold text-gray-700">#{payment.id}</span>
						<span
							class="text-sm font-medium uppercase"
							style="color: {getStatusColor(payment.estado)}"
						>
							{payment.estado}
						</span>
					</div>
					<div class="space-y-1">
						<p class="text-sm text-gray-600">
							<strong class="text-gray-700">Amount:</strong> €{payment.importe}
						</p>
						<p class="text-sm text-gray-600">
							<strong class="text-gray-700">Date:</strong>
							{formatDate(payment.fechaPago)}
						</p>
						<p class="text-sm text-gray-600">
							<strong class="text-gray-700">Method:</strong>
							{payment.metodoPago}
						</p>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
