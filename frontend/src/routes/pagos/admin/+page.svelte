<script lang="ts">
	import { goto } from '$app/navigation';
	import { PagoService } from '$lib/services/pagoService.js';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOPago } from '$lib/generated/api';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import Pagination from '$lib/components/common/Pagination.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State management
	let payments = $state<DTOPago[]>([]);
	let loading = $state(true);
	let error = $state('');
	let currentPage = $state(0);
	let totalPages = $state(0);
	let totalElements = $state(0);
	let pageSize = $state(20);
	let sortBy = $state('fechaPago');
	let sortDirection = $state('DESC');
	let selectedStudentId = $state('');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		} else if (!authStore.user?.roles?.includes('ROLE_ADMIN')) {
			goto('/pagos');
		}
	});

	// Load payments when parameters change
	$effect(() => {
		loadPayments();
	});

	async function loadPayments() {
		try {
			loading = true;
			error = '';

			const params = {
				page: currentPage,
				size: pageSize,
				sortBy,
				sortDirection
			};

			let response;
			if (selectedStudentId) {
				response = await PagoService.getPaymentsByStudentId(selectedStudentId, params);
			} else {
				response = await PagoService.getAllPayments(params);
			}

			payments = response.content;
			totalPages = response.totalPages;
			totalElements = response.totalElements;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading payments';
		} finally {
			loading = false;
		}
	}

	function handlePageChange(page: number) {
		currentPage = page;
	}

	function handleSort(field: string) {
		if (sortBy === field) {
			sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
		} else {
			sortBy = field;
			sortDirection = 'DESC';
		}
		currentPage = 0; // Reset to first page when sorting
	}

	function handleStudentFilter() {
		currentPage = 0; // Reset to first page when filtering
	}

	function clearStudentFilter() {
		selectedStudentId = '';
		currentPage = 0;
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
</script>

<svelte:head>
	<title>Admin Payment Management</title>
</svelte:head>

<div class="mx-auto max-w-7xl p-8">
	<div class="mb-8 flex items-center justify-between">
		<h1 class="text-4xl font-semibold text-gray-900">Admin Payment Management</h1>
		<button
			class="cursor-pointer rounded-md border-none bg-gray-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-gray-700"
			onclick={() => goto('/pagos')}
		>
			Back to Payments
		</button>
	</div>

	<!-- Filters -->
	<div class="mb-6 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
		<h2 class="mb-4 text-lg font-semibold text-gray-900">Filters</h2>
		<div class="flex flex-wrap items-center gap-4">
			<div class="flex flex-col">
				<label for="studentId" class="mb-1 text-sm font-medium text-gray-700">Student ID</label>
				<input
					id="studentId"
					type="text"
					bind:value={selectedStudentId}
					placeholder="Enter student ID to filter"
					class="rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
				/>
			</div>
			<button
				class="cursor-pointer rounded-md border-none bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors duration-200 hover:bg-blue-700"
				onclick={handleStudentFilter}
			>
				Filter
			</button>
			{#if selectedStudentId}
				<button
					class="cursor-pointer rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors duration-200 hover:bg-gray-50"
					onclick={clearStudentFilter}
				>
					Clear Filter
				</button>
			{/if}
		</div>
	</div>

	<!-- Payment Statistics -->
	<div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-4">
		<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
			<div class="text-sm font-medium text-gray-500">Total Payments</div>
			<div class="text-2xl font-semibold text-gray-900">{totalElements}</div>
		</div>
		<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
			<div class="text-sm font-medium text-gray-500">Current Page</div>
			<div class="text-2xl font-semibold text-gray-900">{currentPage + 1}</div>
		</div>
		<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
			<div class="text-sm font-medium text-gray-500">Page Size</div>
			<div class="text-2xl font-semibold text-gray-900">{pageSize}</div>
		</div>
		<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
			<div class="text-sm font-medium text-gray-500">Total Pages</div>
			<div class="text-2xl font-semibold text-gray-900">{totalPages}</div>
		</div>
	</div>

	{#if error}
		<ErrorDisplay {error} />
	{/if}

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="text-lg text-gray-500">Loading payments...</div>
		</div>
	{:else if payments.length === 0}
		<div class="rounded-lg border border-gray-200 bg-white p-8 text-center shadow-sm">
			<div class="text-lg font-medium text-gray-900">No payments found</div>
			<div class="text-gray-500">
				{#if selectedStudentId}
					No payments found for student ID: {selectedStudentId}
				{:else}
					No payments available
				{/if}
			</div>
		</div>
	{:else}
		<!-- Payments Table -->
		<div class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200">
					<thead class="bg-gray-50">
						<tr>
							<th
								class="cursor-pointer px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase hover:bg-gray-100"
								onclick={() => handleSort('id')}
							>
								ID
								{#if sortBy === 'id'}
									<span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</th>
							<th
								class="cursor-pointer px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase hover:bg-gray-100"
								onclick={() => handleSort('fechaPago')}
							>
								Date & Time
								{#if sortBy === 'fechaPago'}
									<span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</th>
							<th
								class="cursor-pointer px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase hover:bg-gray-100"
								onclick={() => handleSort('importe')}
							>
								Amount
								{#if sortBy === 'importe'}
									<span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</th>
							<th
								class="cursor-pointer px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase hover:bg-gray-100"
								onclick={() => handleSort('metodoPago')}
							>
								Method
								{#if sortBy === 'metodoPago'}
									<span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</th>
							<th
								class="cursor-pointer px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase hover:bg-gray-100"
								onclick={() => handleSort('estado')}
							>
								Status
								{#if sortBy === 'estado'}
									<span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
							>
								Student ID
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
							>
								Actions
							</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-gray-200 bg-white">
						{#each payments as payment (payment.id)}
							<tr class="hover:bg-gray-50">
								<td class="px-6 py-4 text-sm font-medium whitespace-nowrap text-gray-900">
									{payment.id}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{FormatterUtils.formatDate(payment.fechaPago, { includeTime: true })}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{FormatterUtils.formatAmount(payment.importe)}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{getPaymentMethodText(payment.metodoPago)}
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<span
										class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {FormatterUtils.getStatusColor(
											payment.estado || 'UNKNOWN',
											'payment'
										)}"
									>
										{FormatterUtils.formatStatus(payment.estado, 'payment')}
									</span>
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{payment.alumnoId}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									<button
										class="cursor-pointer rounded-md border border-gray-300 bg-white px-3 py-1 text-xs font-medium text-gray-700 transition-colors duration-200 hover:bg-gray-50"
										onclick={() => goto(`/pagos/${payment.id}`)}
									>
										View Details
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>

		<!-- Pagination -->
		{#if totalPages > 1}
			<div class="mt-6">
				<Pagination {currentPage} {totalPages} onPageChange={handlePageChange} />
			</div>
		{/if}
	{/if}
</div>
