<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEjercicio, DTOEntregaEjercicio } from '$lib/generated/api';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { EntregaService } from '$lib/services/entregaService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';
	import { NavigationUtils } from '$lib/utils/navigation.js';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let ejercicio = $state<DTOEjercicio | null>(null);
	let entregas = $state<DTOEntregaEjercicio[]>([]);

	let entregasLoading = $state(false);
	let entregasError = $state<string | null>(null);

	// Get exercise ID from URL
	const ejercicioId = $derived(Number($page.params.id));

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load exercise data
	$effect(() => {
		if (authStore.isAuthenticated && ejercicioId) {
			loadEjercicio();
			loadEntregas();
		}
	});

	async function loadEjercicio() {
		loading = true;
		error = null;

		try {
			ejercicio = await EjercicioService.getEjercicioById(ejercicioId);
		} catch (err) {
			console.error('Error loading exercise:', err);
			error = 'Error al cargar el ejercicio. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	async function loadEntregas() {
		entregasLoading = true;
		entregasError = null;

		try {
			const response = await EntregaService.getEntregasByEjercicio(ejercicioId);
			entregas = response.content || [];
		} catch (err) {
			console.error('Error loading deliveries:', err);
			entregasError = 'Error al cargar las entregas. Por favor, inténtalo de nuevo.';
		} finally {
			entregasLoading = false;
		}
	}

	function handleEdit() {
		NavigationUtils.navigateToEditExercise(ejercicioId);
	}

	function handleDelete() {
		// TODO: Implement delete confirmation modal
		console.log('Delete exercise:', ejercicioId);
	}

	function handleCreateEntrega() {
		NavigationUtils.navigateToSubmitExercise(ejercicioId);
	}

	function handleViewEntrega(id: number) {
		NavigationUtils.navigateToDelivery(id);
	}

	function handleGradeEntrega(id: number) {
		NavigationUtils.navigateToGradeDelivery(id);
	}
</script>

<svelte:head>
	<title>Ejercicio - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else if ejercicio}
		<!-- Exercise Header -->
		<div class="mb-8">
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold text-gray-900">{ejercicio.name || 'Sin nombre'}</h1>
					<p class="mt-2 text-gray-600">Detalles del ejercicio</p>
				</div>
				<div class="flex space-x-4">
					{#if authStore.isProfesor || authStore.isAdmin}
						<button
							onclick={handleEdit}
							class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
						>
							Editar Ejercicio
						</button>
						<button
							onclick={handleDelete}
							class="rounded-lg bg-red-600 px-4 py-2 font-semibold text-white hover:bg-red-700"
						>
							Eliminar
						</button>
					{/if}
					{#if authStore.isAlumno}
						<button
							onclick={handleCreateEntrega}
							class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700"
						>
							Entregar Ejercicio
						</button>
					{/if}
				</div>
			</div>
		</div>

		<!-- Exercise Details -->
		<div class="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
			<!-- Main Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Información del Ejercicio</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">Nombre</span>
						<p class="text-lg text-gray-900">{ejercicio.name || 'Sin nombre'}</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Enunciado</span>
						<p class="mt-1 whitespace-pre-wrap text-gray-900">
							{ejercicio.statement || 'Sin enunciado'}
						</p>
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<span class="text-sm font-medium text-gray-500">Fecha de Inicio</span>
							<p class="text-gray-900">
								{FormatterUtils.formatDate(ejercicio.startDate, { includeTime: true })}
							</p>
						</div>
						<div>
							<span class="text-sm font-medium text-gray-500">Fecha de Fin</span>
							<p class="text-gray-900">
								{FormatterUtils.formatDate(ejercicio.endDate, { includeTime: true })}
							</p>
						</div>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Estado</span>
						<div class="mt-1">
							<span
								class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {FormatterUtils.getExerciseStatusColor(
									ejercicio.estado
								)}"
							>
								{FormatterUtils.getExerciseStatusText(ejercicio.estado)}
							</span>
						</div>
					</div>

					{#if ejercicio.horasRestantes !== undefined}
						<div>
							<span class="text-sm font-medium text-gray-500">Tiempo Restante</span>
							<p class="text-gray-900">{ejercicio.horasRestantes} horas</p>
						</div>
					{/if}
				</div>
			</div>

			<!-- Statistics -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Estadísticas</h2>

				<div class="grid grid-cols-2 gap-4">
					<div class="rounded-lg bg-blue-50 p-4 text-center">
						<div class="text-2xl font-bold text-blue-600">{ejercicio.numeroEntregas || 0}</div>
						<div class="text-sm text-gray-600">Total Entregas</div>
					</div>
					<div class="rounded-lg bg-green-50 p-4 text-center">
						<div class="text-2xl font-bold text-green-600">
							{ejercicio.entregasCalificadas || 0}
						</div>
						<div class="text-sm text-gray-600">Entregas Calificadas</div>
					</div>
					<div class="rounded-lg bg-purple-50 p-4 text-center">
						<div class="text-2xl font-bold text-purple-600">
							{ejercicio.porcentajeEntregasCalificadas !== undefined
								? `${ejercicio.porcentajeEntregasCalificadas.toFixed(1)}%`
								: 'N/A'}
						</div>
						<div class="text-sm text-gray-600">% Calificadas</div>
					</div>
					<div class="rounded-lg bg-orange-50 p-4 text-center">
						<div class="text-2xl font-bold text-orange-600">
							{(ejercicio.numeroEntregas || 0) - (ejercicio.entregasCalificadas || 0)}
						</div>
						<div class="text-sm text-gray-600">Pendientes</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Deliveries Section -->
		<div class="rounded-lg bg-white shadow-lg">
			<div class="border-b border-gray-200 px-6 py-4">
				<h2 class="text-xl font-semibold text-gray-900">Entregas del Ejercicio</h2>
			</div>

			{#if entregasLoading}
				<div class="flex items-center justify-center py-12">
					<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
				</div>
			{:else if entregasError}
				<div class="p-6">
					<div class="rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
						{entregasError}
					</div>
				</div>
			{:else if entregas.length === 0}
				<div class="p-6 text-center">
					<div class="mb-4 text-6xl text-gray-400">📝</div>
					<h3 class="mb-2 text-lg font-medium text-gray-900">No hay entregas</h3>
					<p class="text-gray-500">Aún no se han realizado entregas para este ejercicio.</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
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
												onclick={() => handleViewEntrega(entrega.id!)}
												class="text-blue-600 hover:text-blue-900"
											>
												Ver
											</button>
											{#if (authStore.isProfesor || authStore.isAdmin) && entrega.estado === 'ENTREGADO'}
												<button
													onclick={() => handleGradeEntrega(entrega.id!)}
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
				</div>
			{/if}
		</div>
	{:else}
		<!-- Not Found State -->
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📝</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Ejercicio no encontrado</h3>
			<p class="mb-4 text-gray-500">El ejercicio que buscas no existe o ha sido eliminado.</p>
			<button
				onclick={() => goto('/ejercicios')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Ejercicios
			</button>
		</div>
	{/if}
</div>
