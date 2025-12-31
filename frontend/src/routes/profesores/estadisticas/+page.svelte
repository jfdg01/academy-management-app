<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';

	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let totalProfesores = $state(0);
	let profesoresActivos = $state(0);
	let profesoresInactivos = $state(0);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAdmin && !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(() => {
		loadBasicStats();
	});

	async function loadBasicStats() {
		loading = true;
		error = null;

		try {
			// For now, we'll just show placeholder data
			// In a real implementation, you would call the backend API
			totalProfesores = 25;
			profesoresActivos = 22;
			profesoresInactivos = 3;
		} catch (err) {
			error = `Error al cargar estadísticas: ${err}`;
			console.error('Error loading statistics:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<h1 class="mb-2 text-3xl font-bold text-gray-900">Estadísticas de Profesores</h1>
		<p class="text-gray-600">Resumen general del cuerpo docente</p>
	</div>

	{#if error}
		<div class="mb-4 rounded-md border border-red-200 bg-red-50 p-4">
			<div class="flex">
				<div class="flex-shrink-0">
					<svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
						<path
							fill-rule="evenodd"
							d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
							clip-rule="evenodd"
						/>
					</svg>
				</div>
				<div class="ml-3">
					<h3 class="text-sm font-medium text-red-800">Error</h3>
					<div class="mt-2 text-sm text-red-700">{error}</div>
				</div>
			</div>
		</div>
	{/if}

	{#if loading}
		<div class="flex h-64 items-center justify-center">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else}
		<!-- Basic Statistics Grid -->
		<div class="grid grid-cols-1 gap-6 md:grid-cols-3">
			<!-- Total Professors -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<svg
							class="h-8 w-8 text-blue-600"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
							/>
						</svg>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Total de Profesores</p>
						<p class="text-2xl font-semibold text-gray-900">{totalProfesores}</p>
					</div>
				</div>
			</div>

			<!-- Active Professors -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<svg
							class="h-8 w-8 text-green-600"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
							/>
						</svg>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Profesores Activos</p>
						<p class="text-2xl font-semibold text-gray-900">{profesoresActivos}</p>
					</div>
				</div>
			</div>

			<!-- Inactive Professors -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<svg class="h-8 w-8 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
							/>
						</svg>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Profesores Inactivos</p>
						<p class="text-2xl font-semibold text-gray-900">{profesoresInactivos}</p>
					</div>
				</div>
			</div>
		</div>

		<!-- Additional Statistics Section -->
		<div class="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
			<!-- Recent Activity -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<h3 class="mb-4 text-lg font-medium text-gray-900">Actividad Reciente</h3>
				<div class="space-y-3">
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Nuevos profesores este mes</span>
						<span class="text-sm font-medium text-gray-900">3</span>
					</div>
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Clases impartidas</span>
						<span class="text-sm font-medium text-gray-900">156</span>
					</div>
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Horas de enseñanza</span>
						<span class="text-sm font-medium text-gray-900">1,248</span>
					</div>
				</div>
			</div>

			<!-- Performance Metrics -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<h3 class="mb-4 text-lg font-medium text-gray-900">Métricas de Rendimiento</h3>
				<div class="space-y-3">
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Promedio de calificación</span>
						<span class="text-sm font-medium text-gray-900">4.8/5.0</span>
					</div>
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Tasa de asistencia</span>
						<span class="text-sm font-medium text-gray-900">95%</span>
					</div>
					<div class="flex items-center justify-between">
						<span class="text-sm text-gray-600">Satisfacción estudiantil</span>
						<span class="text-sm font-medium text-gray-900">92%</span>
					</div>
				</div>
			</div>
		</div>

		<!-- Quick Actions -->
		<div class="mt-8 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
			<h3 class="mb-4 text-lg font-medium text-gray-900">Acciones Rápidas</h3>
			<div class="flex flex-wrap gap-3">
				<a
					href="/profesores"
					class="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
				>
					Ver todos los profesores
				</a>
				<a
					href="/profesores/nuevo"
					class="inline-flex items-center rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
				>
					Agregar profesor
				</a>
				<button
					onclick={loadBasicStats}
					class="inline-flex items-center rounded-md bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
				>
					Actualizar estadísticas
				</button>
			</div>
		</div>
	{/if}
</div>
