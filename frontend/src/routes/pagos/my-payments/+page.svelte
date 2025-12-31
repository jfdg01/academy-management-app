<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { PagoService } from '$lib/services/pagoService';
	import type { DTOPago } from '$lib/generated/api';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	let payments: DTOPago[] = [];
	let loading = true;
	let error: string | null = null;

	async function loadMyPayments() {
		try {
			loading = true;
			error = null;
			payments = await PagoService.getMyRecentPayments(20); // Get last 20 payments
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading payment history';
		} finally {
			loading = false;
		}
	}

	function getPaymentMethodText(method: string | undefined): string {
		if (!method) return 'Unknown';
		switch (method) {
			case 'STRIPE':
				return 'Credit Card';
			case 'EFECTIVO':
				return 'Cash';
			case 'DEBITO':
				return 'Debit Card';
			default:
				return method;
		}
	}

	onMount(() => {
		loadMyPayments();
	});
</script>

<svelte:head>
	<title>My Payment History - Academia App</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6">
		<button
			on:click={() => goto('/pagos')}
			class="mb-4 inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
		>
			← Back to Payments
		</button>
		<h1 class="text-3xl font-bold text-gray-900">My Payment History</h1>
		<p class="mt-2 text-gray-600">View your recent payment transactions and their status.</p>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	{#if loading}
		<div class="py-12 text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
			></div>
			<p class="mt-4 text-gray-600">Loading your payment history...</p>
		</div>
	{:else if payments.length === 0}
		<div class="py-12 text-center">
			<div class="mx-auto h-12 w-12 text-gray-400">
				<svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
					/>
				</svg>
			</div>
			<h3 class="mt-2 text-sm font-medium text-gray-900">No payments found</h3>
			<p class="mt-1 text-sm text-gray-500">You haven't made any payments yet.</p>
			<div class="mt-6">
				<button
					on:click={() => goto('/pagos')}
					class="inline-flex items-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
				>
					Make a Payment
				</button>
			</div>
		</div>
	{:else}
		<div class="overflow-hidden bg-white shadow sm:rounded-md">
			<ul role="list" class="divide-y divide-gray-200">
				{#each payments as payment (payment.id)}
					<li>
						<div class="px-4 py-4 sm:px-6">
							<div class="flex items-center justify-between">
								<div class="flex items-center">
									<div class="flex-shrink-0">
										<div
											class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100"
										>
											<svg
												class="h-6 w-6 text-blue-600"
												fill="none"
												viewBox="0 0 24 24"
												stroke="currentColor"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
												/>
											</svg>
										</div>
									</div>
									<div class="ml-4">
										<div class="flex items-center">
											<p class="text-sm font-medium text-gray-900">
												Payment #{payment.id}
											</p>
											<span
												class="ml-2 inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium {FormatterUtils.getStatusColor(
													payment.estado || '',
													'payment'
												)}"
											>
												{FormatterUtils.formatStatus(payment.estado, 'payment')}
											</span>
										</div>
										<div class="mt-1 flex items-center text-sm text-gray-500">
											<p>
												{getPaymentMethodText(payment.metodoPago || '')} • {FormatterUtils.formatDate(
													payment.fechaPago,
													{ includeTime: true }
												)}
											</p>
										</div>
										{#if payment.descripcionEstado}
											<p class="mt-1 text-sm text-gray-500">{payment.descripcionEstado}</p>
										{/if}
									</div>
								</div>
								<div class="flex flex-col items-end">
									<p class="text-lg font-semibold text-gray-900">
										{FormatterUtils.formatAmount(payment.importe)}
									</p>
									{#if payment.numeroItems && payment.numeroItems > 0}
										<p class="text-sm text-gray-500">{payment.numeroItems} item(s)</p>
									{/if}
								</div>
							</div>

							{#if payment.items && payment.items.length > 0}
								<div class="mt-4  border-gray-200 pt-4">
									<h4 class="mb-2 text-sm font-medium text-gray-900">Items:</h4>
									<div class="space-y-2">
										{#each payment.items as item (item.id)}
											<div class="flex justify-between text-sm">
												<span class="text-gray-600">{item.concepto}</span>
												<span class="text-gray-900">
													{FormatterUtils.formatAmount(item.importeTotal)}
												</span>
											</div>
										{/each}
									</div>
								</div>
							{/if}

							{#if payment.failureReason}
								<div class="mt-4  border-gray-200 pt-4">
									<div class="flex items-center">
										<svg class="mr-2 h-4 w-4 text-red-400" fill="currentColor" viewBox="0 0 20 20">
											<path
												fill-rule="evenodd"
												d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
												clip-rule="evenodd"
											/>
										</svg>
										<span class="text-sm text-red-600">Failed: {payment.failureReason}</span>
									</div>
								</div>
							{/if}
						</div>
					</li>
				{/each}
			</ul>
		</div>

		<div class="mt-6 text-center">
			<button
				on:click={() => goto('/pagos')}
				class="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
			>
				Make New Payment
			</button>
		</div>
	{/if}
</div>
