<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ClaseService } from '$lib/services/claseService';
	import { EntregaService } from '$lib/services/entregaService';
	import type { DTOClase } from '$lib/generated/api';
	import type { DTOEntregaEjercicio } from '$lib/generated/api';
	import { FormatterUtils } from '$lib/utils/formatters';

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let myClasses = $state<DTOClase[]>([]);
	let recentDeliveries = $state<DTOEntregaEjercicio[]>([]);
	let totalDeliveries = $state(0);
	let pendingDeliveries = $state(0);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(() => {
		loadTeacherDashboard();
	});

	async function loadTeacherDashboard() {
		loading = true;
		error = null;

		try {
			// Load teacher's classes
			myClasses = await ClaseService.getMyClasses();

			// Load recent deliveries for all classes
			const deliveriesResponse = await EntregaService.getTeacherDeliveries({
				page: 0,
				size: 10,
				sortBy: 'fechaEntrega',
				sortDirection: 'DESC'
			});

			recentDeliveries = deliveriesResponse.content || [];
			totalDeliveries = deliveriesResponse.totalElements || 0;

			// Count pending deliveries
			pendingDeliveries = recentDeliveries.filter((d) => d.estado === 'ENTREGADO').length;
		} catch (err) {
			error = `Error al cargar el dashboard: ${err}`;
			console.error('Error loading teacher dashboard:', err);
		} finally {
			loading = false;
		}
	}

	function goToClass(claseId: number) {
		goto(`/clases/${claseId}`);
	}

	function goToDeliveries() {
		goto('/entregas');
	}

	function goToMaterials() {
		goto('/materiales');
	}

	function goToNewClass() {
		goto('/clases/nuevo');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-gray-900">Dashboard del Profesor</h1>
		<p class="mt-2 text-gray-600">Bienvenido, {authStore.user?.sub}</p>
	</div>

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else}
		<!-- Quick Stats -->
		<div class="mb-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div class="flex h-8 w-8 items-center justify-center rounded-md bg-blue-500 text-white">
							📚
						</div>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Mis Clases</p>
						<p class="text-2xl font-semibold text-gray-900">{myClasses.length}</p>
					</div>
				</div>
			</div>

			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div
							class="flex h-8 w-8 items-center justify-center rounded-md bg-green-500 text-white"
						>
							📝
						</div>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Total Entregas</p>
						<p class="text-2xl font-semibold text-gray-900">{totalDeliveries}</p>
					</div>
				</div>
			</div>

			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div
							class="flex h-8 w-8 items-center justify-center rounded-md bg-yellow-500 text-white"
						>
							⏳
						</div>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Pendientes</p>
						<p class="text-2xl font-semibold text-gray-900">{pendingDeliveries}</p>
					</div>
				</div>
			</div>

			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div
							class="flex h-8 w-8 items-center justify-center rounded-md bg-purple-500 text-white"
						>
							📊
						</div>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Promedio</p>
						<p class="text-2xl font-semibold text-gray-900">
							{recentDeliveries.length > 0
								? (
										recentDeliveries.reduce((sum, d) => sum + (d.nota || 0), 0) /
										recentDeliveries.filter((d) => d.nota).length
									).toFixed(1)
								: '0.0'}
						</p>
					</div>
				</div>
			</div>
		</div>

		<!-- Quick Actions -->
		<div class="mb-8">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Acciones Rápidas</h2>
			<div class="flex flex-wrap gap-4">
				<button
					onclick={goToNewClass}
					class="inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
				>
					➕ Nueva Clase
				</button>
				<button
					onclick={goToDeliveries}
					class="inline-flex items-center rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:outline-none"
				>
					📝 Ver Entregas
				</button>
				<button
					onclick={goToMaterials}
					class="inline-flex items-center rounded-lg bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700 focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 focus:outline-none"
				>
					📚 Gestionar Materiales
				</button>
			</div>
		</div>

		<!-- My Classes -->
		<div class="mb-8">
			<div class="mb-4 flex items-center justify-between">
				<h2 class="text-xl font-semibold text-gray-900">Mis Clases</h2>
				<a href="/clases" class="text-sm text-blue-600 hover:text-blue-800">Ver todas</a>
			</div>

			{#if myClasses.length === 0}
				<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
					<p class="text-gray-500">No tienes clases asignadas aún.</p>
					<button
						onclick={goToNewClass}
						class="mt-4 inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
					>
						Crear mi primera clase
					</button>
				</div>
			{:else}
				<div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
					{#each myClasses as clase (clase.id)}
						<div
							class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-shadow hover:shadow-md"
						>
							{#if clase.imagenPortada}
								<img
									src={clase.imagenPortada}
									alt="Portada de {clase.titulo}"
									class="h-48 w-full object-cover"
								/>
							{:else}
								<div
									class="flex h-48 w-full items-center justify-center bg-gradient-to-br from-blue-100 to-purple-100"
								>
									<span class="text-4xl">📚</span>
								</div>
							{/if}

							<div class="p-6">
								<h3 class="mb-2 text-lg font-semibold text-gray-900">{clase.titulo}</h3>
								<p class="mb-4 line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>

								<div class="mb-4 flex items-center justify-between text-sm text-gray-500">
									<span class="flex items-center">
										<span class="mr-1">👥</span>
										{clase.numeroAlumnos || 0} alumnos
									</span>
									<span class="flex items-center">
										<span class="mr-1">💰</span>
										{FormatterUtils.formatPrice(clase.precio || 0)}
									</span>
								</div>

								<button
									onclick={() => goToClass(clase.id!)}
									class="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
								>
									Ver Clase
								</button>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<!-- Recent Deliveries -->
		<div class="mb-8">
			<div class="mb-4 flex items-center justify-between">
				<h2 class="text-xl font-semibold text-gray-900">Entregas Recientes</h2>
				<a href="/entregas" class="text-sm text-blue-600 hover:text-blue-800">Ver todas</a>
			</div>

			{#if recentDeliveries.length === 0}
				<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
					<p class="text-gray-500">No hay entregas recientes.</p>
				</div>
			{:else}
				<div class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
					<div class="overflow-x-auto">
						<table class="min-w-full divide-y divide-gray-200">
							<thead class="bg-gray-50">
								<tr>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Alumno
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Ejercicio
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
										Fecha
									</th>
									<th
										class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
									>
										Acciones
									</th>
								</tr>
							</thead>
							<tbody class="divide-y divide-gray-200 bg-white">
								{#each recentDeliveries as entrega (entrega.id)}
									<tr class="hover:bg-gray-50">
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{entrega.alumnoId}
										</td>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{entrega.ejercicioId}
										</td>
										<td class="px-6 py-4 whitespace-nowrap">
											<span
												class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {entrega.estado ===
												'CALIFICADO'
													? 'bg-green-100 text-green-800'
													: entrega.estado === 'ENTREGADO'
														? 'bg-yellow-100 text-yellow-800'
														: 'bg-gray-100 text-gray-800'}"
											>
												{entrega.estado}
											</span>
										</td>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{entrega.nota ? `${entrega.nota}/10` : '-'}
										</td>
										<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
											{entrega.fechaEntrega ? FormatterUtils.formatDate(entrega.fechaEntrega) : '-'}
										</td>
										<td class="px-6 py-4 text-sm font-medium whitespace-nowrap">
											<a href="/entregas/{entrega.id}" class="text-blue-600 hover:text-blue-900">
												Ver
											</a>
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				</div>
			{/if}
		</div>
	{/if}
</div>
