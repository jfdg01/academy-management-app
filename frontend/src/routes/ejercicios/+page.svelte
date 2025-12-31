<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOEjercicio, DTORespuestaPaginada } from '$lib/generated/api';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let ejercicios = $state<DTOEjercicio[]>([]);
	let pagination = $state<DTORespuestaPaginada | null>(null);

	// Search and filter state
	let searchQuery = $state('');
	let selectedStatus = $state<string>('');
	let currentPage = $state(0);
	let pageSize = $state(20);
	let sortBy = $state('name');
	let sortDirection = $state('asc');

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load exercises when parameters change
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadEjercicios();
		}
	});

	async function loadEjercicios() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage,
				size: pageSize,
				sortBy,
				sortDirection
			};

			if (searchQuery) {
				params.q = searchQuery;
			}

			if (selectedStatus) {
				params.status = selectedStatus;
			}

			const response = await EjercicioService.getEjercicios(params);
			ejercicios = response.content || [];
			pagination = response;
		} catch (err) {
			console.error('Error loading exercises:', err);
			error = 'Error al cargar los ejercicios. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	function handleSearch() {
		currentPage = 0;
		loadEjercicios();
	}

	function handleStatusFilter(status: string) {
		selectedStatus = status;
		currentPage = 0;
		loadEjercicios();
	}

	function handlePageChange(page: number) {
		currentPage = page;
		loadEjercicios();
	}

	function handleCreate() {
		goto('/ejercicios/nuevo');
	}

	function handleEdit(id: number) {
		goto(`/ejercicios/${id}`);
	}

	function handleDelete(id: number) {
		// TODO: Implement delete confirmation modal
		console.log('Delete exercise:', id);
	}

	function handleView(id: number) {
		goto(`/ejercicios/${id}`);
	}

	// Format functions
	function formatDate(date: Date | string | undefined): string {
		if (!date) return 'N/A';
		return new Date(date).toLocaleDateString('es-ES');
	}

	function formatStatus(status: string | undefined): string {
		if (!status) return 'N/A';
		const statusMap: Record<string, string> = {
			ACTIVE: 'Activo',
			EXPIRED: 'Vencido',
			FUTURE: 'Futuro',
			WITH_DELIVERIES: 'Con entregas',
			WITHOUT_DELIVERIES: 'Sin entregas'
		};
		return statusMap[status] || status;
	}

	function getStatusColor(status: string | undefined): string {
		if (!status) return 'bg-gray-100 text-gray-800';
		const colorMap: Record<string, string> = {
			ACTIVE: 'bg-green-100 text-green-800',
			EXPIRED: 'bg-red-100 text-red-800',
			FUTURE: 'bg-blue-100 text-blue-800',
			WITH_DELIVERIES: 'bg-purple-100 text-purple-800',
			WITHOUT_DELIVERIES: 'bg-yellow-100 text-yellow-800'
		};
		return colorMap[status] || 'bg-gray-100 text-gray-800';
	}
</script>

<svelte:head>
	<title>Ejercicios - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Ejercicios</h1>
				<p class="mt-2 text-gray-600">Gestiona los ejercicios de las clases</p>
			</div>
			{#if authStore.isProfesor || authStore.isAdmin}
				<button
					onclick={handleCreate}
					class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
				>
					Crear Ejercicio
				</button>
			{/if}
		</div>
	</div>

	<!-- Search and Filters -->
	<div class="mb-6 rounded-lg bg-white p-6 shadow-md">
		<div class="flex flex-col gap-4 md:flex-row md:items-center">
			<div class="flex-1">
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Buscar ejercicios por nombre o enunciado..."
					class="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none"
					oninput={handleSearch}
				/>
			</div>
			<div class="flex gap-4">
				<select
					bind:value={selectedStatus}
					onchange={(e) => handleStatusFilter(e.currentTarget.value)}
					class="rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none"
				>
					<option value="">Todos los estados</option>
					<option value="ACTIVE">Activos</option>
					<option value="EXPIRED">Vencidos</option>
					<option value="FUTURE">Futuros</option>
					<option value="WITH_DELIVERIES">Con entregas</option>
					<option value="WITHOUT_DELIVERIES">Sin entregas</option>
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

	<!-- Exercises Table -->
	<div class="overflow-hidden rounded-lg bg-white shadow-lg">
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else if ejercicios.length === 0}
			<div class="py-12 text-center">
				<div class="mb-4 text-6xl text-gray-400">📝</div>
				<h3 class="mb-2 text-lg font-medium text-gray-900">No hay ejercicios</h3>
				<p class="text-gray-500">
					{#if searchQuery || selectedStatus}
						No se encontraron ejercicios con los filtros aplicados.
					{:else}
						Aún no se han creado ejercicios.
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
							Nombre
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Enunciado
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Fecha Inicio
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Fecha Fin
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Estado
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Entregas
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Calificadas
						</th>
						<th
							class="px-6 py-3 text-right text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Acciones
						</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					{#each ejercicios as ejercicio (ejercicio.id)}
						<tr class="hover:bg-gray-50">
							<td class="px-6 py-4">
								<div class="text-sm font-medium text-gray-900">
									{ejercicio.name || 'Sin nombre'}
								</div>
							</td>
							<td class="px-6 py-4">
								<div class="max-w-xs truncate text-sm text-gray-900">
									{ejercicio.statement || 'Sin enunciado'}
								</div>
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{formatDate(ejercicio.startDate)}
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{formatDate(ejercicio.endDate)}
							</td>
							<td class="px-6 py-4">
								<span
									class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {getStatusColor(
										ejercicio.estado
									)}"
								>
									{formatStatus(ejercicio.estado)}
								</span>
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{ejercicio.numeroEntregas || 0}
							</td>
							<td class="px-6 py-4 text-sm text-gray-900">
								{ejercicio.entregasCalificadas || 0}
							</td>
							<td class="px-6 py-4 text-right text-sm font-medium">
								<div class="flex space-x-2">
									<button
										onclick={() => handleView(ejercicio.id!)}
										class="text-blue-600 hover:text-blue-900"
									>
										Ver
									</button>
									{#if authStore.isProfesor || authStore.isAdmin}
										<button
											onclick={() => handleEdit(ejercicio.id!)}
											class="text-green-600 hover:text-green-900"
										>
											Editar
										</button>
										<button
											onclick={() => handleDelete(ejercicio.id!)}
											class="text-red-600 hover:text-red-900"
										>
											Eliminar
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
				Mostrando {pagination!.content.length} de {pagination!.totalElements} ejercicios
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
