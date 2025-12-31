<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let totalAlumnos = $state(0);
	let matriculados = $state(0);
	let noMatriculados = $state(0);

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
			totalAlumnos = 150;
			matriculados = 120;
			noMatriculados = 30;
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
		<h1 class="mb-2 text-3xl font-bold text-gray-900">Estadísticas de Alumnos</h1>
		<p class="text-gray-600">Resumen general de la matrícula estudiantil</p>
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
			<!-- Total Students -->
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
								d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
							/>
						</svg>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">Total de Alumnos</p>
						<p class="text-2xl font-semibold text-gray-900">{totalAlumnos}</p>
					</div>
				</div>
			</div>

			<!-- Enrolled Students -->
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
						<p class="text-sm font-medium text-gray-500">Matriculados</p>
						<p class="text-2xl font-semibold text-gray-900">{matriculados}</p>
					</div>
				</div>
			</div>

			<!-- Non-enrolled Students -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<svg
							class="h-8 w-8 text-yellow-600"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
							/>
						</svg>
					</div>
					<div class="ml-4">
						<p class="text-sm font-medium text-gray-500">No Matriculados</p>
						<p class="text-2xl font-semibold text-gray-900">{noMatriculados}</p>
					</div>
				</div>
			</div>
		</div>

		<!-- Enrollment Rate -->
		<div class="mt-8 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
			<h3 class="mb-4 text-lg font-medium text-gray-900">Tasa de Matrícula</h3>
			<div class="flex items-center">
				<div class="flex-1">
					<div class="flex justify-between text-sm text-gray-600">
						<span>Progreso</span>
						<span>{Math.round((matriculados / totalAlumnos) * 100)}%</span>
					</div>
					<div class="mt-2 h-2 w-full rounded-full bg-gray-200">
						<div
							class="h-2 rounded-full bg-green-600"
							style="width: {(matriculados / totalAlumnos) * 100}%"
						></div>
					</div>
				</div>
			</div>
		</div>

		<!-- Quick Actions -->
		<div class="mt-8 flex space-x-4">
			<a
				href="/alumnos"
				class="inline-flex items-center rounded-md border ransparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
			>
				Ver Todos los Alumnos
			</a>
			{#if authStore.isAdmin}
				<a
					href="/alumnos/nuevo"
					class="inline-flex items-center rounded-md border ransparent bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:outline-none"
				>
					Nuevo Alumno
				</a>
			{/if}
		</div>
	{/if}
</div>
