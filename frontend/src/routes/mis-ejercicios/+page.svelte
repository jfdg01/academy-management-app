<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOClaseInscrita, DTOEjercicioConEntrega } from '$lib/generated/api';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let enrolledClasses = $state<DTOClaseInscrita[]>([]);
	let allExercises = $state<Array<DTOEjercicioConEntrega & { className: string; classId: number }>>(
		[]
	);
	let filteredExercises = $state<
		Array<DTOEjercicioConEntrega & { className: string; classId: number }>
	>([]);

	// Filter state
	let currentFilter = $state<'all' | 'active' | 'urgent' | 'expired' | 'submitted' | 'graded'>(
		'all'
	);
	let searchQuery = $state('');

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAlumno) {
			goto('/clases');
			return;
		}
	});

	// Load data when component mounts
	$effect(() => {
		if (authStore.isAuthenticated && authStore.isAlumno) {
			loadMyExercises();
		}
	});

	// Filter exercises when filter or search changes
	$effect(() => {
		filterExercises();
	});

	async function loadMyExercises() {
		loading = true;
		error = null;

		try {
			// Get all enrolled classes
			enrolledClasses = await EnrollmentService.getMyEnrolledClasses();

			// Get exercises with delivery information for each enrolled class
			const exercisesWithClassInfo: Array<
				DTOEjercicioConEntrega & { className: string; classId: number }
			> = [];

			for (const enrolledClass of enrolledClasses) {
				if (enrolledClass.id) {
					try {
						const response = await EjercicioService.getEjerciciosConEntrega({
							classId: enrolledClass.id.toString()
						});

						const exercises = response.content || [];
						const exercisesWithClass = exercises.map((exercise) => ({
							...exercise,
							className: enrolledClass.titulo || 'Clase sin título',
							classId: enrolledClass.id || 0
						}));

						exercisesWithClassInfo.push(...exercisesWithClass);
					} catch (err) {
						console.warn(`Error loading exercises for class ${enrolledClass.id}:`, err);
						// Continue with other classes even if one fails
					}
				}
			}

			// Sort exercises by deadline proximity (closest first)
			allExercises = exercisesWithClassInfo.sort((a, b) => {
				const aDeadline = new Date(a.endDate || 0);
				const bDeadline = new Date(b.endDate || 0);
				const now = new Date();

				// If both are expired, sort by most recently expired
				if (aDeadline < now && bDeadline < now) {
					return bDeadline.getTime() - aDeadline.getTime();
				}

				// If one is expired and one isn't, expired goes last
				if (aDeadline < now) return 1;
				if (bDeadline < now) return -1;

				// Both are active, sort by closest deadline
				return aDeadline.getTime() - bDeadline.getTime();
			});

			filteredExercises = [...allExercises];
		} catch (err) {
			error = `Error al cargar ejercicios: ${err}`;
			console.error('Error loading exercises:', err);
		} finally {
			loading = false;
		}
	}

	function filterExercises() {
		let filtered = [...allExercises];

		// Apply status filter
		if (currentFilter !== 'all') {
			const now = new Date();
			filtered = filtered.filter((exercise) => {
				const deadline = new Date(exercise.endDate || 0);
				const hoursUntilDeadline = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60);

				switch (currentFilter) {
					case 'active':
						return deadline > now && !exercise.tieneEntrega;
					case 'urgent':
						return deadline > now && hoursUntilDeadline <= 24 && !exercise.tieneEntrega;
					case 'expired':
						return deadline < now && !exercise.tieneEntrega;
					case 'submitted':
						return exercise.tieneEntrega && exercise.estadoEntrega !== 'CALIFICADO';
					case 'graded':
						return exercise.tieneEntrega && exercise.estadoEntrega === 'CALIFICADO';
					default:
						return true;
				}
			});
		}

		// Apply search filter
		if (searchQuery.trim()) {
			const query = searchQuery.toLowerCase().trim();
			filtered = filtered.filter(
				(exercise) =>
					exercise.name?.toLowerCase().includes(query) ||
					exercise.statement?.toLowerCase().includes(query) ||
					exercise.className.toLowerCase().includes(query)
			);
		}

		filteredExercises = filtered;
	}

	function getExerciseStatus(exercise: DTOEjercicioConEntrega) {
		// If student has submitted, show delivery status
		if (exercise.tieneEntrega) {
			switch (exercise.estadoEntrega) {
				case 'CALIFICADO':
					return {
						status: 'graded',
						label: 'Calificado',
						color: 'text-green-600 bg-green-50',
						icon: '✅'
					};
				case 'ENTREGADO':
					return {
						status: 'pending',
						label: 'Pendiente de calificación',
						color: 'text-yellow-600 bg-yellow-50',
						icon: '📤'
					};
				default:
					return {
						status: 'submitted',
						label: 'Entregado',
						color: 'text-blue-600 bg-blue-50',
						icon: '📤'
					};
			}
		}

		// If no submission, show exercise status
		const deadline = new Date(exercise.endDate || 0);
		const now = new Date();
		const hoursUntilDeadline = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60);

		if (deadline < now) {
			return {
				status: 'expired',
				label: 'Vencido',
				color: 'text-red-600 bg-red-50',
				icon: '❌'
			};
		} else if (hoursUntilDeadline <= 24) {
			return {
				status: 'urgent',
				label: 'Urgente',
				color: 'text-orange-600 bg-orange-50',
				icon: '⏰'
			};
		} else if (hoursUntilDeadline <= 72) {
			return {
				status: 'soon',
				label: 'Próximo',
				color: 'text-yellow-600 bg-yellow-50',
				icon: '📝'
			};
		} else {
			return {
				status: 'active',
				label: 'Disponible',
				color: 'text-green-600 bg-green-50',
				icon: '📝'
			};
		}
	}

	function formatDeadline(exercise: DTOEjercicioConEntrega) {
		const deadline = new Date(exercise.endDate || 0);
		const now = new Date();
		const diffMs = deadline.getTime() - now.getTime();
		const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
		const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

		if (diffMs < 0) {
			const daysAgo = Math.abs(diffDays);
			return `Vencido hace ${daysAgo} día${daysAgo !== 1 ? 's' : ''}`;
		} else if (diffDays > 0) {
			return `Faltan ${diffDays} día${diffDays !== 1 ? 's' : ''} y ${diffHours} hora${diffHours !== 1 ? 's' : ''}`;
		} else {
			return `Faltan ${diffHours} hora${diffHours !== 1 ? 's' : ''}`;
		}
	}

	function clearFilters() {
		currentFilter = 'all';
		searchQuery = '';
	}

	function getFilterCount(
		filter: 'all' | 'active' | 'urgent' | 'expired' | 'submitted' | 'graded'
	) {
		if (filter === 'all') return allExercises.length;

		const now = new Date();
		return allExercises.filter((exercise) => {
			const deadline = new Date(exercise.endDate || 0);
			const hoursUntilDeadline = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60);

			switch (filter) {
				case 'active':
					return deadline > now && !exercise.tieneEntrega;
				case 'urgent':
					return deadline > now && hoursUntilDeadline <= 24 && !exercise.tieneEntrega;
				case 'expired':
					return deadline < now && !exercise.tieneEntrega;
				case 'submitted':
					return exercise.tieneEntrega && exercise.estadoEntrega !== 'CALIFICADO';
				case 'graded':
					return exercise.tieneEntrega && exercise.estadoEntrega === 'CALIFICADO';
				default:
					return true;
			}
		}).length;
	}
</script>

<svelte:head>
	<title>Mis Ejercicios - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Mis Ejercicios</h1>
		<button
			onclick={() => goto('/clases')}
			class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
		>
			Explorar Clases
		</button>
	</div>

	<!-- Messages -->
	{#if successMessage}
		<div class="mb-4 rounded-lg bg-green-50 p-4 text-green-700">
			{successMessage}
		</div>
	{/if}

	{#if error}
		<div class="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
			{error}
		</div>
	{/if}

	<!-- Filters and Search -->
	<div class="mb-6 rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
		<div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
			<!-- Filter Tabs -->
			<div class="flex flex-wrap gap-2">
				<button
					onclick={() => (currentFilter = 'all')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter === 'all'
						? 'bg-blue-100 text-blue-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Todos ({getFilterCount('all')})
				</button>
				<button
					onclick={() => (currentFilter = 'active')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter ===
					'active'
						? 'bg-green-100 text-green-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Disponibles ({getFilterCount('active')})
				</button>
				<button
					onclick={() => (currentFilter = 'urgent')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter ===
					'urgent'
						? 'bg-orange-100 text-orange-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Urgentes ({getFilterCount('urgent')})
				</button>
				<button
					onclick={() => (currentFilter = 'submitted')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter ===
					'submitted'
						? 'bg-yellow-100 text-yellow-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Entregados ({getFilterCount('submitted')})
				</button>
				<button
					onclick={() => (currentFilter = 'graded')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter ===
					'graded'
						? 'bg-green-100 text-green-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Calificados ({getFilterCount('graded')})
				</button>
				<button
					onclick={() => (currentFilter = 'expired')}
					class="rounded-lg px-3 py-2 text-sm font-medium transition-colors {currentFilter ===
					'expired'
						? 'bg-red-100 text-red-700'
						: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
				>
					Vencidos ({getFilterCount('expired')})
				</button>
			</div>

			<!-- Search and Clear -->
			<div class="flex items-center gap-2">
				<input
					type="text"
					placeholder="Buscar ejercicios..."
					bind:value={searchQuery}
					class="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
				/>
				<button
					onclick={clearFilters}
					class="rounded-lg bg-gray-100 px-3 py-2 text-sm text-gray-700 hover:bg-gray-200"
				>
					Limpiar
				</button>
			</div>
		</div>
	</div>

	<!-- Loading State -->
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="text-center">
				<div
					class="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-blue-600 -transparent"
				></div>
				<p class="text-gray-600">Cargando ejercicios...</p>
			</div>
		</div>
	{:else if filteredExercises.length === 0}
		<!-- Empty State -->
		<div class="py-12 text-center">
			<div class="mx-auto mb-4 h-16 w-16 text-gray-400">
				<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
					></path>
				</svg>
			</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">
				{#if allExercises.length === 0}
					No tienes ejercicios asignados
				{:else}
					No se encontraron ejercicios
				{/if}
			</h3>
			<p class="text-gray-600">
				{#if allExercises.length === 0}
					Inscríbete en clases para ver ejercicios aquí
				{:else}
					Intenta cambiar los filtros de búsqueda
				{/if}
			</p>
			{#if allExercises.length === 0}
				<button
					onclick={() => goto('/clases')}
					class="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
				>
					Explorar Clases
				</button>
			{/if}
		</div>
	{:else}
		<!-- Exercises List -->
		<div class="space-y-4">
			{#each filteredExercises as exercise (exercise.id)}
				<div
					class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm transition-shadow hover:shadow-md"
				>
					<div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
						<!-- Exercise Info -->
						<div class="flex-1">
							<div class="mb-2 flex items-center gap-2">
								<h3 class="text-lg font-semibold text-gray-900">
									{exercise.name || 'Ejercicio sin título'}
								</h3>
								<span
									class="flex items-center gap-1 rounded-full px-2 py-1 text-xs font-medium {getExerciseStatus(
										exercise
									).color}"
								>
									<span>{getExerciseStatus(exercise).icon}</span>
									{getExerciseStatus(exercise).label}
								</span>
							</div>

							<p class="mb-3 line-clamp-2 text-gray-600">
								{exercise.statement || 'Sin descripción'}
							</p>

							<div class="flex flex-wrap items-center gap-4 text-sm text-gray-500">
								<span class="flex items-center gap-1">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
										></path>
									</svg>
									{exercise.className}
								</span>

								<span class="flex items-center gap-1">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
										></path>
									</svg>
									{formatDeadline(exercise)}
								</span>

								{#if exercise.numeroEntregas !== undefined}
									<span class="flex items-center gap-1">
										<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
											></path>
										</svg>
										{exercise.numeroEntregas} entrega{exercise.numeroEntregas !== 1 ? 's' : ''}
									</span>
								{/if}

								<!-- Delivery Information -->
								{#if exercise.tieneEntrega}
									<span class="flex items-center gap-1">
										<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
											></path>
										</svg>
										Entregado el {new Date(exercise.fechaEntrega || '').toLocaleDateString('es-ES')}
									</span>

									{#if exercise.estadoEntrega === 'CALIFICADO' && exercise.notaFormateada}
										<span class="flex items-center gap-1 font-medium text-green-600">
											<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
												></path>
											</svg>
											Nota: {exercise.notaFormateada}
										</span>
									{/if}
								{/if}
							</div>
						</div>

						<!-- Actions -->
						<div class="flex gap-2">
							{#if exercise.tieneEntrega}
								<button
									onclick={() => goto(`/entregas/${exercise.entregaId}`)}
									class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
								>
									Ver Entrega
								</button>
							{:else}
								{@const deadline = new Date(exercise.endDate || 0)}
								{@const now = new Date()}
								{@const isExpired = deadline < now}
								<button
									onclick={() => goto(`/ejercicios/${exercise.id}`)}
									disabled={isExpired}
									class="rounded-lg px-4 py-2 text-sm font-medium text-white focus:ring-2 focus:ring-blue-500 focus:outline-none {isExpired
										? 'cursor-not-allowed bg-gray-400'
										: 'bg-green-600 hover:bg-green-700'}"
								>
									{isExpired ? 'Vencido' : 'Entregar'}
								</button>
							{/if}
							<button
								onclick={() => goto(`/clases/${exercise.classId}`)}
								class="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							>
								Ver Clase
							</button>
						</div>
					</div>
				</div>
			{/each}
		</div>

		<!-- Results Summary -->
		<div class="mt-6 text-center text-sm text-gray-500">
			Mostrando {filteredExercises.length} de {allExercises.length} ejercicio{allExercises.length !==
			1
				? 's'
				: ''}
		</div>
	{/if}
</div>
