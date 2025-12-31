<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOEntregaEjercicio, DTORespuestaPaginada } from '$lib/generated/api';
	import { EntregaService } from '$lib/services/entregaService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';
	import { NavigationUtils } from '$lib/utils/navigation.js';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let entregas = $state<DTOEntregaEjercicio[]>([]);
	let pagination = $state<DTORespuestaPaginada | null>(null);

	// Search and filter state
	let selectedStatus = $state<string>('');
	let selectedEjercicioId = $state<string>('');
	let currentPage = $state(0);
	let pageSize = $state(20);
	let sortBy = $state('fechaEntrega');
	let sortDirection = $state('desc');

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load deliveries when parameters change
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadEntregas();
		}
	});

	async function loadEntregas() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage,
				size: pageSize,
				sortBy,
				sortDirection
			};

			if (selectedStatus) {
				params.estado = selectedStatus;
			}

			if (selectedEjercicioId) {
				params.ejercicioId = selectedEjercicioId;
			}

			// For students, only show their own deliveries
			if (authStore.isAlumno) {
				params.alumnoId = authStore.user?.id;
			}

			const response = await EntregaService.getEntregas(params);
			entregas = response.content || [];
			pagination = response;
		} catch (err) {
			console.error('Error loading deliveries:', err);
			error = 'Error al cargar las entregas. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	function handleStatusFilter(status: string) {
		selectedStatus = status;
		currentPage = 0;
		loadEntregas();
	}

	function handlePageChange(page: number) {
		currentPage = page;
		loadEntregas();
	}

	function handleView(id: number) {
		NavigationUtils.goToDeliveryView(id);
	}

	function handleGrade(id: number) {
		NavigationUtils.goToDeliveryGrade(id);
	}
</script>

<svelte:head>
	<title>Entregas - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Entregas de Ejercicios</h1>
				<p class="mt-2 text-gray-600">
					{#if authStore.isAlumno}
						Mis entregas de ejercicios
					{:else}
						Gestiona las entregas de ejercicios
					{/if}
				</p>
			</div>
		</div>
	</div>

	<!-- Search and Filters -->
	<div class="mb-6 rounded-lg bg-white p-6 shadow-md">
		<div class="flex flex-col gap-4 md:flex-row md:items-center">
			<div class="flex gap-4">
				<select
					bind:value={selectedStatus}
					onchange={(e) => handleStatusFilter(e.currentTarget.value)}
					class="rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none"
				>
					<option value="">Todos los estados</option>
					<option value="PENDIENTE">Pendientes</option>
					<option value="ENTREGADO">Entregadas</option>
					<option value="CALIFICADO">Calificadas</option>
				</select>
			</div>
		</div>
	</div>

	<!-- Error Display -->
	{#if error}
		<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{/if}

	<!-- Deliveries Table -->
	<div class="overflow-hidden rounded-lg bg-white shadow-lg">
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else if entregas.length === 0}
			<div class="py-12 text-center">
				<div class="mb-4 text-6xl text-gray-400">📝</div>
				<h3 class="mb-2 text-lg font-medium text-gray-900">No hay entregas</h3>
				<p class="text-gray-500">
					{#if selectedStatus}
						No se encontraron entregas con el estado seleccionado.
					{:else if authStore.isAlumno}
						Aún no has realizado ninguna entrega.
					{:else}
						Aún no se han realizado entregas.
					{/if}
				</p>
			</div>
		{:else}
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Estudiante
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Ejercicio
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Fecha de Entrega
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Estado
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Nota
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Archivos
						</th>
						<th
							class="px-6 py-3 text-right text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Acciones
						</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					{#each entregas as entrega (entrega.id)}
						<tr class="hover:bg-gray-50">
							<td class="px-6 py-4">
								<div class="text-sm font-medium text-gray-900">
									{entrega.alumnoId || 'N/A'}
								</div>
							</td>
							<td class="px-6 py-4">
								<div class="text-sm text-gray-900">
									{entrega.ejercicioId || 'N/A'}
								</div>
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{FormatterUtils.formatDate(entrega.fechaEntrega, { includeTime: true })}
							</td>
							<td class="px-6 py-4">
								<span
									class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {FormatterUtils.getStatusColor(
										entrega.estado,
										'delivery'
									)}"
								>
									{FormatterUtils.formatStatus(entrega.estado, 'delivery')}
								</span>
							</td>
							<td class="px-6 py-4">
								<span class="text-sm font-medium {FormatterUtils.getGradeColor(entrega.nota)}">
									{FormatterUtils.formatGrade(entrega.nota)}
								</span>
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{entrega.numeroArchivos || 0} archivos
							</td>
							<td class="px-6 py-4 text-right text-sm font-medium">
								<div class="flex space-x-2">
									<button
										onclick={() => handleView(entrega.id!)}
										class="text-blue-600 hover:text-blue-900"
									>
										Ver
									</button>
									{#if (authStore.isProfesor || authStore.isAdmin) && entrega.estado === 'ENTREGADO'}
										<button
											onclick={() => handleGrade(entrega.id!)}
											class="text-green-600 hover:text-green-900"
										>
											Calificar
										</button>
									{/if}
								</div>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		{/if}
	</div>

	<!-- Pagination -->
	{#if pagination && pagination.totalElements > 0}
		<div class="mt-6 flex items-center justify-between">
			<div class="text-sm text-gray-700">
				Mostrando {pagination!.content.length} de {pagination!.totalElements} entregas
			</div>
			<div class="flex space-x-2">
				<button
					onclick={() => handlePageChange(pagination!.page - 1)}
					disabled={pagination!.isFirst}
					class="rounded px-3 py-1 text-sm font-medium text-gray-500 hover:bg-gray-100 disabled:opacity-50"
				>
					Anterior
				</button>
				<span class="px-3 py-1 text-sm text-gray-700">
					Página {pagination!.page + 1} de {pagination!.totalPages}
				</span>
				<button
					onclick={() => handlePageChange(pagination!.page + 1)}
					disabled={pagination!.isLast}
					class="rounded px-3 py-1 text-sm font-medium text-gray-500 hover:bg-gray-100 disabled:opacity-50"
				>
					Siguiente
				</button>
			</div>
		</div>
	{/if}
</div>
