<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { EntregaService } from '$lib/services/entregaService';
	import type { DTOEjercicio } from '$lib/generated/api';
	import type { DTOEntregaEjercicio } from '$lib/generated/api';

	// Get exercise ID from route params
	const ejercicioId = parseInt($page.params.id);

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let ejercicio = $state<DTOEjercicio | null>(null);
	let entregas = $state<DTOEntregaEjercicio[]>([]);
	let filteredEntregas = $state<DTOEntregaEjercicio[]>([]);

	// Filter state
	let statusFilter = $state('all');
	let searchQuery = $state('');
	let sortBy = $state('fechaEntrega');
	let sortDirection = $state<'asc' | 'desc'>('desc');

	// Grading state
	let selectedEntrega = $state<DTOEntregaEjercicio | null>(null);
	let showGradingModal = $state(false);
	let gradingForm = $state({
		nota: 0,
		comentarios: ''
	});

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(() => {
		loadExercise();
		loadEntregas();
	});

	async function loadExercise() {
		try {
			ejercicio = await EjercicioService.getEjercicioById(ejercicioId);
		} catch (err) {
			error = `Error al cargar el ejercicio: ${err}`;
			console.error('Error loading exercise:', err);
		}
	}

	async function loadEntregas() {
		try {
			const response = await EntregaService.getEntregasByEjercicio(ejercicioId);
			entregas = response.content || [];
			applyFilters();
		} catch (err) {
			error = `Error al cargar las entregas: ${err}`;
			console.error('Error loading entregas:', err);
		} finally {
			loading = false;
		}
	}

	function applyFilters() {
		let filtered = [...entregas];

		// Apply status filter
		if (statusFilter !== 'all') {
			filtered = filtered.filter((entrega) => entrega.estado === statusFilter);
		}

		// Apply search filter
		if (searchQuery.trim()) {
			const query = searchQuery.toLowerCase();
			filtered = filtered.filter(
				(entrega) =>
					entrega.comentarios?.toLowerCase().includes(query) ||
					entrega.notaFormateada?.toLowerCase().includes(query)
			);
		}

		// Apply sorting
		filtered.sort((a, b) => {
			let aValue: string | number | Date, bValue: string | number | Date;

			switch (sortBy) {
				case 'fechaEntrega':
					aValue = new Date(a.fechaEntrega || 0);
					bValue = new Date(b.fechaEntrega || 0);
					break;
				case 'alumnoId':
					aValue = a.alumnoId || 0;
					bValue = b.alumnoId || 0;
					break;
				case 'nota':
					aValue = a.nota || 0;
					bValue = b.nota || 0;
					break;
				case 'estado':
					aValue = a.estado || '';
					bValue = b.estado || '';
					break;
				default:
					aValue = new Date(a.fechaEntrega || 0);
					bValue = new Date(b.fechaEntrega || 0);
			}

			if (sortDirection === 'asc') {
				return aValue > bValue ? 1 : -1;
			} else {
				return aValue < bValue ? 1 : -1;
			}
		});

		filteredEntregas = filtered;
	}

	function openGradingModal(entrega: DTOEntregaEjercicio) {
		selectedEntrega = entrega;
		gradingForm = {
			nota: entrega.nota || 0,
			comentarios: entrega.comentarios || ''
		};
		showGradingModal = true;
	}

	function closeGradingModal() {
		showGradingModal = false;
		selectedEntrega = null;
		gradingForm = { nota: 0, comentarios: '' };
	}

	async function gradeEntrega() {
		if (!selectedEntrega) return;

		try {
			await EntregaService.gradeEntrega(selectedEntrega.id!, {
				nota: gradingForm.nota,
				comentarios: gradingForm.comentarios
			});

			// Update local state
			selectedEntrega.nota = gradingForm.nota;
			selectedEntrega.comentarios = gradingForm.comentarios;
			selectedEntrega.estado = 'CALIFICADO';

			// Reload entregas to get updated data
			await loadEntregas();
			closeGradingModal();
		} catch (err) {
			error = `Error al calificar la entrega: ${err}`;
			console.error('Error grading entrega:', err);
		}
	}

	function getStatusColor(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'bg-yellow-100 text-yellow-800';
			case 'CALIFICADO':
				return 'bg-green-100 text-green-800';
			case 'ENTREGADO':
				return 'bg-blue-100 text-blue-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function formatStatus(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'Pendiente';
			case 'CALIFICADO':
				return 'Calificado';
			case 'ENTREGADO':
				return 'Entregado';
			default:
				return 'Desconocido';
		}
	}

	function goBack() {
		goto('/profesores/mis-clases');
	}

	// Watch for filter changes
	$effect(() => {
		applyFilters();
	});
</script>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<button
					onclick={goBack}
					class="mb-4 inline-flex items-center text-blue-600 hover:text-blue-800"
				>
					← Volver a Mis Clases
				</button>
				<h1 class="text-3xl font-bold text-gray-900">Entregas del Ejercicio</h1>
				{#if ejercicio}
					<p class="mt-2 text-gray-600">{ejercicio.name}</p>
				{/if}
			</div>
		</div>
	</div>

	{#if error}
		<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{/if}

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else}
		<!-- Filters and Controls -->
		<div class="mb-6 rounded-lg border border-gray-200 bg-white p-4">
			<div class="grid grid-cols-1 gap-4 md:grid-cols-4">
				<!-- Search -->
				<div>
					<label for="search" class="block text-sm font-medium text-gray-700">Buscar</label>
					<input
						id="search"
						type="text"
						bind:value={searchQuery}
						placeholder="Comentarios..."
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					/>
				</div>

				<!-- Status Filter -->
				<div>
					<label for="status" class="block text-sm font-medium text-gray-700">Estado</label>
					<select
						id="status"
						bind:value={statusFilter}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					>
						<option value="all">Todos los estados</option>
						<option value="PENDIENTE">Pendiente</option>
						<option value="ENTREGADO">Entregado</option>
						<option value="CALIFICADO">Calificado</option>
					</select>
				</div>

				<!-- Sort By -->
				<div>
					<label for="sortBy" class="block text-sm font-medium text-gray-700">Ordenar por</label>
					<select
						id="sortBy"
						bind:value={sortBy}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					>
						<option value="fechaEntrega">Fecha de entrega</option>
						<option value="alumnoId">ID Alumno</option>
						<option value="nota">Nota</option>
						<option value="estado">Estado</option>
					</select>
				</div>

				<!-- Sort Direction -->
				<div>
					<label for="sortDirection" class="block text-sm font-medium text-gray-700"
						>Dirección</label
					>
					<select
						id="sortDirection"
						bind:value={sortDirection}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					>
						<option value="desc">Descendente</option>
						<option value="asc">Ascendente</option>
					</select>
				</div>
			</div>
		</div>

		<!-- Statistics -->
		<div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-4">
			<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
				<div class="text-2xl font-bold text-blue-600">{entregas.length}</div>
				<div class="text-sm text-gray-600">Total Entregas</div>
			</div>
			<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
				<div class="text-2xl font-bold text-yellow-600">
					{entregas.filter((e) => e.estado === 'PENDIENTE').length}
				</div>
				<div class="text-sm text-gray-600">Pendientes</div>
			</div>
			<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
				<div class="text-2xl font-bold text-green-600">
					{entregas.filter((e) => e.estado === 'CALIFICADO').length}
				</div>
				<div class="text-sm text-gray-600">Calificadas</div>
			</div>
			<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
				<div class="text-2xl font-bold text-purple-600">
					{entregas.filter((e) => e.estado === 'ENTREGADO').length}
				</div>
				<div class="text-sm text-gray-600">Entregadas</div>
			</div>
		</div>

		<!-- Entregas List -->
		<div class="rounded-lg border border-gray-200 bg-white">
			{#if filteredEntregas.length === 0}
				<div class="p-8 text-center">
					<p class="text-gray-500">
						{#if entregas.length === 0}
							No hay entregas para este ejercicio.
						{:else}
							No se encontraron entregas con los filtros aplicados.
						{/if}
					</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th
									class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
								>
									ID Alumno
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
									Comentarios
								</th>
								<th
									class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
								>
									Acciones
								</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-gray-200 bg-white">
							{#each filteredEntregas as entrega (entrega.id)}
								<tr class="hover:bg-gray-50">
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">
											ID: {entrega.alumnoId}
										</div>
									</td>
									<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
										{entrega.fechaEntrega
											? new Date(entrega.fechaEntrega).toLocaleDateString()
											: 'N/A'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<span
											class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium {getStatusColor(
												entrega.estado
											)}"
										>
											{formatStatus(entrega.estado)}
										</span>
									</td>
									<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
										{#if entrega.nota !== undefined && entrega.nota !== null}
											<span class="font-medium">{entrega.nota}/10</span>
										{:else}
											<span class="text-gray-400">-</span>
										{/if}
									</td>
									<td class="px-6 py-4 text-sm text-gray-900">
										<div class="max-w-xs truncate">
											{entrega.comentarios || 'Sin comentarios'}
										</div>
									</td>
									<td class="px-6 py-4 text-sm font-medium whitespace-nowrap">
										<div class="flex space-x-2">
											{#if entrega.estado !== 'CALIFICADO'}
												<button
													onclick={() => openGradingModal(entrega)}
													class="rounded-lg bg-blue-600 px-3 py-1 text-xs font-medium text-white hover:bg-blue-700"
												>
													📝 Calificar
												</button>
											{:else}
												<button
													onclick={() => openGradingModal(entrega)}
													class="rounded-lg bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700"
												>
													✏️ Editar
												</button>
											{/if}
											<button
												onclick={() => goto(`/entregas/${entrega.id}`)}
												class="rounded-lg bg-gray-600 px-3 py-1 text-xs font-medium text-white hover:bg-gray-700"
											>
												👁️ Ver
											</button>
										</div>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}
		</div>
	{/if}
</div>

<!-- Grading Modal -->
{#if showGradingModal && selectedEntrega}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
			<div class="mb-6">
				<h3 class="text-lg font-semibold text-gray-900">
					{#if selectedEntrega.estado === 'CALIFICADO'}
						Editar Calificación
					{:else}
						Calificar Entrega
					{/if}
				</h3>
				<p class="mt-1 text-sm text-gray-600">
					Alumno ID: {selectedEntrega.alumnoId}
				</p>
			</div>

			<div class="space-y-4">
				<div>
					<label for="nota" class="block text-sm font-medium text-gray-700">Nota</label>
					<input
						id="nota"
						type="number"
						min="0"
						max="10"
						step="0.1"
						bind:value={gradingForm.nota}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					/>
				</div>

				<div>
					<label for="comentarios" class="block text-sm font-medium text-gray-700"
						>Comentarios</label
					>
					<textarea
						id="comentarios"
						bind:value={gradingForm.comentarios}
						rows="3"
						placeholder="Comentarios sobre la entrega..."
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					></textarea>
				</div>

				<div class="flex justify-end space-x-3">
					<button
						onclick={closeGradingModal}
						class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
					>
						Cancelar
					</button>
					<button
						onclick={gradeEntrega}
						class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
					>
						{#if selectedEntrega.estado === 'CALIFICADO'}
							Actualizar
						{:else}
							Calificar
						{/if}
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}
