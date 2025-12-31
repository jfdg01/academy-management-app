<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type {
		DTOClase,
		DTOAlumno,
		DTORespuestaAlumnosClase,
		DTOProfesor,
		DTOEjercicio
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let clase = $state<DTOClase | null>(null);
	let claseDetalles = $state<DTOClase | null>(null);
	let numeroAlumnos = $state(0);
	let numeroProfesores = $state(0);
	let enrollmentStatus = $state<{
		isEnrolled: boolean;
		claseId?: number;
		alumnoId?: number;
	} | null>(null);
	let enrollmentLoading = $state(false);
	let enrollmentError = $state<string | null>(null);

	// Students management state
	let students = $state<DTOAlumno[]>([]);
	let studentsLoading = $state(false);
	let studentsError = $state<string | null>(null);
	let unenrollLoading = $state<number | null>(null);

	// Professor profile state
	let profesores = $state<DTOProfesor[]>([]);
	let profesorLoading = $state(false);
	let profesorError = $state<string | null>(null);

	// Exercises state
	let ejercicios = $state<DTOEjercicio[]>([]);
	let ejerciciosLoading = $state(false);
	let ejerciciosError = $state<string | null>(null);

	// Get class ID from URL
	const claseId = $derived(Number($page.params.id));

	// Check if current user is the teacher of this class
	let isClassTeacher = $state(false);

	$effect(() => {
		if ((!clase && !claseDetalles) || !authStore.user?.id) {
			isClassTeacher = false;
		} else {
			const currentClase = clase || claseDetalles;
			isClassTeacher = currentClase?.profesoresId?.includes(authStore.user.id.toString()) || false;
		}
	});

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load class data
	$effect(() => {
		if (authStore.isAuthenticated && claseId) {
			loadClase();
			if (authStore.isAlumno) {
				checkEnrollmentStatus();
			}
			if (authStore.isProfesor || authStore.isAdmin) {
				loadStudents();
			}
		}
	});

	// Load professor data when class is loaded (only for teachers/admins)
	$effect(() => {
		if (clase?.id && (authStore.isProfesor || authStore.isAdmin)) {
			loadProfesores();
		}
	});

	// Load exercises when class is loaded
	$effect(() => {
		if (currentClase?.id) {
			loadEjercicios();
		}
	});

	async function loadClase() {
		loading = true;
		error = null;

		try {
			// Use the optimized endpoint that loads all relationships
			clase = await ClaseService.getClaseDetallada(claseId);

			// Set statistics from the detailed response
			if (clase?.id) {
				numeroAlumnos = clase.numeroAlumnos || 0;
				numeroProfesores = clase.numeroProfesores || 0;
			}
		} catch (err) {
			error = `Error al cargar la clase: ${err}`;
			console.error('Error loading class:', err);
		} finally {
			loading = false;
		}
	}

	async function loadProfesores() {
		if (!claseId) return;

		profesorLoading = true;
		profesorError = null;

		try {
			// Use the new API endpoint to get professors for this class
			profesores = await ClaseService.getProfesoresPorClase(claseId);
		} catch (err) {
			profesorError = `Error al cargar la información de los profesores: ${err}`;
			console.error('Error loading professors:', err);
		} finally {
			profesorLoading = false;
		}
	}

	async function checkEnrollmentStatus() {
		if (!claseId) return;

		try {
			const status = await EnrollmentService.checkMyEnrollmentStatus(claseId);
			enrollmentStatus = {
				isEnrolled: status.isEnrolled || false,
				claseId: status.claseId,
				alumnoId: status.alumnoId
			};
		} catch (err) {
			console.warn('Error checking enrollment status:', err);
			// Don't show error to user, just assume not enrolled
			enrollmentStatus = { isEnrolled: false };
		}
	}

	async function loadStudents() {
		if (!claseId) return;

		studentsLoading = true;
		studentsError = null;

		try {
			const response: DTORespuestaAlumnosClase =
				await EnrollmentService.getStudentsInClass(claseId);
			students = response.content || [];
		} catch (err) {
			studentsError = `Error al cargar los estudiantes: ${err}`;
			console.error('Error loading students:', err);
		} finally {
			studentsLoading = false;
		}
	}

	async function loadEjercicios() {
		if (!claseId) return;

		ejerciciosLoading = true;
		ejerciciosError = null;

		try {
			const response = await EjercicioService.getEjercicios({ classId: claseId.toString() });
			ejercicios = response.content || [];
		} catch (err) {
			ejerciciosError = `Error al cargar los ejercicios: ${err}`;
			console.error('Error loading exercises:', err);
		} finally {
			ejerciciosLoading = false;
		}
	}

	async function handleUnenrollStudent(studentId: number) {
		if (!claseId) return;

		unenrollLoading = studentId;

		try {
			await EnrollmentService.unenrollStudentFromClass(studentId, claseId);

			// Remove student from local state
			students = students.filter((student) => student.id !== studentId);

			// Update student count
			if (clase) {
				numeroAlumnos = Math.max(0, numeroAlumnos - 1);
			}
		} catch (err) {
			studentsError = `Error al dar de baja al estudiante: ${err}`;
			console.error('Error unenrolling student:', err);
		} finally {
			unenrollLoading = null;
		}
	}

	async function handleEnrollment() {
		if (!claseId) return;

		enrollmentLoading = true;
		enrollmentError = null;

		try {
			const currentClase = clase || claseDetalles;
			if (!currentClase) {
				throw new Error('No se pudo obtener la información de la clase');
			}

			const result = await EnrollmentService.handleEnrollmentAction(claseId, currentClase);

			if (result.action === 'redirect') {
				goto(result.redirectUrl!);
				return;
			} else if (result.action === 'already_enrolled') {
				// Show message for already enrolled students
				enrollmentError = result.message || 'Ya estás inscrito en esta clase';
				return;
			}

			// Refresh student count - use the class details from the response
			if (claseDetalles?.id) {
				// Reload the class to get updated statistics
				const updatedClaseDetalles = await EnrollmentService.getMyClassDetails(claseDetalles.id);
				numeroAlumnos = updatedClaseDetalles.alumnosCount || 0;
			} else if (clase?.id) {
				// Reload the class to get updated statistics
				const updatedClase = await ClaseService.getClaseById(clase.id);
				numeroAlumnos = updatedClase.numeroAlumnos || 0;
			}
		} catch (err) {
			enrollmentError = `Error al inscribirse: ${err}`;
		} finally {
			enrollmentLoading = false;
		}
	}

	// Get the current class data (either from clase or claseDetalles)
	let currentClase = $derived(clase || claseDetalles);
</script>

<svelte:head>
	<title>{currentClase?.titulo || 'Clase'} - Academia</title>
</svelte:head>

<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="mb-8 flex items-center justify-between">
			<div>
				<h1
					class="montserrat-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-4xl font-bold text-transparent"
				>
					Detalles de Clase
				</h1>
				<p class="mt-2 font-medium text-gray-600">
					Información completa de la clase y sus recursos
				</p>
			</div>
			<button
				onclick={() => goto('/clases')}
				class="flex items-center font-medium text-gray-600 transition-all duration-200 hover:text-blue-600"
			>
				<svg class="mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"
					></path>
				</svg>
				Volver a Clases
			</button>
		</div>

		<!-- Loading State -->
		{#if loading}
			<div class="py-16 text-center">
				<div
					class="mx-auto h-16 w-16 animate-spin rounded-full border-4 border-blue-200 -blue-600"
				></div>
				<p class="mt-6 text-lg font-medium text-gray-600">Cargando detalles de la clase...</p>
			</div>
		{:else if error}
			<!-- Error State -->
			<div class="mb-6 rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700 shadow-sm">
				<div class="flex items-center">
					<svg class="mr-3 h-5 w-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
						<path
							fill-rule="evenodd"
							d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
							clip-rule="evenodd"
						></path>
					</svg>
					{error}
				</div>
			</div>
		{:else if currentClase}
			<div class="space-y-8">
				<!-- Class Header Card -->
				<div
					class="overflow-hidden rounded-2xl border border-gray-200/50 bg-white/80 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
				>
					<!-- Header with Image -->
					<div class="relative h-64 bg-gradient-to-r from-blue-500 to-purple-600">
						{#if currentClase.imagenPortada}
							<img
								src={currentClase.imagenPortada}
								alt={currentClase.titulo}
								class="h-full w-full object-cover"
							/>
						{/if}
						<div class="bg-opacity-40 absolute inset-0 bg-black"></div>
						<div class="absolute right-0 bottom-0 left-0 p-8 text-white">
							<h2 class="mb-4 text-4xl font-bold">{currentClase.titulo || 'Sin título'}</h2>
							<div class="flex items-center space-x-4">
								<span class="text-2xl font-bold"
									>{FormatterUtils.formatPrice(currentClase.precio)}</span
								>
								<span class="rounded-full bg-blue-100 px-3 py-1 text-sm font-medium text-blue-800">
									{FormatterUtils.formatNivel(currentClase.nivel)}
								</span>
								<span
									class="rounded-full bg-purple-100 px-3 py-1 text-sm font-medium text-purple-800"
								>
									{FormatterUtils.getPresencialidadText(currentClase.presencialidad)}
								</span>
							</div>
						</div>
					</div>

					<!-- Description -->
					<div class="p-8">
						<h3
							class="montserrat-semibold mb-4 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							Descripción
						</h3>
						<p class="leading-relaxed text-gray-700">
							{currentClase.descripcion || 'No hay descripción disponible para esta clase.'}
						</p>
					</div>
				</div>

				<!-- Statistics Grid -->
				<div class="grid grid-cols-1 gap-6 md:grid-cols-4">
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<div class="mb-2 text-3xl font-bold text-blue-600">{numeroAlumnos}</div>
						<div class="text-sm text-gray-600">Alumnos inscritos</div>
					</div>
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<div class="mb-2 text-3xl font-bold text-green-600">{numeroProfesores}</div>
						<div class="text-sm text-gray-600">Profesores</div>
					</div>
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<div class="mb-2 text-3xl font-bold text-purple-600">
							{'numeroMateriales' in currentClase
								? currentClase.numeroMateriales
								: currentClase.material?.length || 0}
						</div>
						<div class="text-sm text-gray-600">Materiales</div>
					</div>
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<div class="mb-2 text-3xl font-bold text-orange-600">
							{'numeroEjercicios' in currentClase
								? currentClase.numeroEjercicios
								: currentClase.ejerciciosId?.length || 0}
						</div>
						<div class="text-sm text-gray-600">Ejercicios</div>
					</div>
				</div>

				<!-- Professor Profile Section -->
				{#if (authStore.isProfesor || authStore.isAdmin) && (profesores.length > 0 || profesorLoading)}
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<h3
							class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							{profesores.length === 1 ? 'Profesor de la Clase' : 'Profesores de la Clase'}
						</h3>

						{#if profesorLoading}
							<div class="py-8 text-center">
								<div
									class="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-200 -blue-600"
								></div>
								<p class="mt-4 text-gray-600">Cargando profesores...</p>
							</div>
						{:else if profesorError}
							<div class="rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700">
								{profesorError}
							</div>
						{:else if profesores.length > 0}
							<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
								{#each profesores as profesor (profesor.id)}
									<div
										class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md"
									>
										<div class="mb-4 flex items-center space-x-4">
											<!-- Professor Avatar -->
											<div class="flex-shrink-0">
												<div
													class="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-purple-600"
												>
													<span class="text-lg font-bold text-white">
														{profesor.firstName?.[0]}{profesor.lastName?.[0]}
													</span>
												</div>
											</div>

											<!-- Professor Information -->
											<div class="min-w-0 flex-1">
												<h4 class="text-base font-semibold text-gray-900">
													{profesor.firstName}
													{profesor.lastName}
												</h4>
												<p class="text-sm text-gray-600">
													<a
														href="mailto:{profesor.email}"
														class="text-blue-600 hover:text-blue-800"
													>
														{profesor.email}
													</a>
												</p>
											</div>
										</div>

										<!-- View Profile Button -->
										{#if authStore.isAdmin || authStore.isProfesor}
											<a
												href="/profesores/{profesor.id}"
												class="block w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
											>
												Ver perfil completo
											</a>
										{/if}
									</div>
								{/each}
							</div>
						{/if}
					</div>
				{/if}

				<!-- Materials Section -->
				{#if currentClase.material && currentClase.material.length > 0}
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<h3
							class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							Materiales
						</h3>
						<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
							{#each currentClase.material as material (material.id)}
								<div
									class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md"
								>
									<h4 class="mb-3 font-semibold text-gray-900">
										{material.name || 'Sin nombre'}
									</h4>
									{#if material.url}
										<a
											href={material.url}
											target="_blank"
											rel="noopener noreferrer"
											class="inline-block w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
										>
											Ver material
										</a>
									{/if}
								</div>
							{/each}
						</div>
					</div>
				{/if}

				<!-- Exercises Section -->
				<div
					class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
				>
					<div class="mb-6 flex items-center justify-between">
						<h3
							class="montserrat-semibold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							Ejercicios
						</h3>
						{#if authStore.isProfesor || authStore.isAdmin}
							<a
								href="/ejercicios?classId={currentClase.id}"
								class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
							>
								Ver todos los ejercicios
							</a>
						{/if}
					</div>

					{#if ejerciciosLoading}
						<div class="py-8 text-center">
							<div
								class="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-200 -blue-600"
							></div>
							<p class="mt-4 text-gray-600">Cargando ejercicios...</p>
						</div>
					{:else if ejerciciosError}
						<div class="rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700">
							{ejerciciosError}
						</div>
					{:else if ejercicios.length === 0}
						<div class="rounded-xl border border-gray-200 bg-gray-50 p-8 text-center">
							<div class="mb-4 text-6xl">📝</div>
							<h4 class="mb-2 text-lg font-medium text-gray-900">Ejercicios de la clase</h4>
							<p class="mb-4 text-gray-600">
								Esta clase tiene {'numeroEjercicios' in currentClase
									? currentClase.numeroEjercicios
									: 0} ejercicios asignados.
							</p>
							<a
								href="/ejercicios?classId={currentClase.id}"
								class="inline-block transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
							>
								Ver ejercicios
							</a>
						</div>
					{:else}
						<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
							{#each ejercicios as ejercicio (ejercicio.id)}
								<div
									class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md"
								>
									<div class="mb-4 flex items-start justify-between">
										<h4 class="line-clamp-2 font-semibold text-gray-900">
											{ejercicio.name || 'Sin nombre'}
										</h4>
										<span
											class="ml-2 flex-shrink-0 rounded-full px-2 py-1 text-xs font-medium {FormatterUtils.getExerciseStatusColor(
												ejercicio.estado
											)}"
										>
											{ejercicio.estado || 'N/A'}
										</span>
									</div>

									{#if ejercicio.statement}
										<p class="mb-4 line-clamp-3 text-sm text-gray-600">
											{ejercicio.statement}
										</p>
									{/if}

									<div class="mb-4 space-y-2 text-xs text-gray-500">
										{#if ejercicio.startDate}
											<div class="flex items-center">
												<svg
													class="mr-2 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
													/>
												</svg>
												Inicio: {FormatterUtils.formatDate(ejercicio.startDate)}
											</div>
										{/if}
										{#if ejercicio.endDate}
											<div class="flex items-center">
												<svg
													class="mr-2 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
													/>
												</svg>
												Fin: {FormatterUtils.formatDate(ejercicio.endDate)}
											</div>
										{/if}
										{#if ejercicio.numeroEntregas !== undefined}
											<div class="flex items-center">
												<svg
													class="mr-2 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
													/>
												</svg>
												Entregas: {ejercicio.numeroEntregas}
											</div>
										{/if}
									</div>

									<a
										href="/ejercicios/{ejercicio.id}"
										class="block w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
									>
										Ver ejercicio
									</a>
								</div>
							{/each}
						</div>
					{/if}
				</div>

				<!-- Class Type Information -->
				{#if currentClase.tipoClase}
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<h3
							class="montserrat-semibold mb-4 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							Tipo de Clase
						</h3>
						<div class="rounded-xl border border-orange-200 bg-orange-50 p-4">
							<span
								class="rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-800"
							>
								{currentClase.tipoClase}
							</span>
						</div>
					</div>
				{/if}

				<!-- Students Section (for teachers and admins) -->
				{#if (authStore.isProfesor && isClassTeacher) || authStore.isAdmin}
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<h3
							class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-semibold text-transparent"
						>
							Estudiantes Inscritos
						</h3>

						{#if studentsLoading}
							<div class="py-8 text-center">
								<div
									class="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-200 -blue-600"
								></div>
								<p class="mt-4 text-gray-600">Cargando estudiantes...</p>
							</div>
						{:else if studentsError}
							<div class="rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700">
								{studentsError}
							</div>
						{:else if students.length === 0}
							<div class="rounded-xl border border-gray-200 bg-gray-50 p-8 text-center">
								<div class="mb-4 text-6xl">👥</div>
								<h4 class="mb-2 text-lg font-medium text-gray-900">No hay estudiantes inscritos</h4>
								<p class="text-gray-600">Aún no se ha inscrito ningún estudiante en esta clase.</p>
							</div>
						{:else}
							<div class="overflow-hidden rounded-xl border border-gray-200">
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
												Email
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												DNI
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Teléfono
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Estado
											</th>
											<th
												class="px-6 py-3 text-right text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Acciones
											</th>
										</tr>
									</thead>
									<tbody class="divide-y divide-gray-200 bg-white">
										{#each students as student (student.id)}
											<tr class="hover:bg-gray-50">
												<td class="px-6 py-4 whitespace-nowrap">
													<div class="flex items-center">
														<div class="h-10 w-10 flex-shrink-0">
															<div
																class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100"
															>
																<span class="text-sm font-medium text-blue-600">
																	{student.firstName?.[0]}{student.lastName?.[0]}
																</span>
															</div>
														</div>
														<div class="ml-4">
															<div class="text-sm font-medium text-gray-900">
																{student.firstName}
																{student.lastName}
															</div>
															<div class="text-sm text-gray-500">@{student.username}</div>
														</div>
													</div>
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.email}
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.dni}
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.phoneNumber || 'N/A'}
												</td>
												<td class="px-6 py-4 whitespace-nowrap">
													<span
														class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {student.enabled
															? 'bg-green-100 text-green-800'
															: 'bg-red-100 text-red-800'}"
													>
														{student.enabled ? 'Activo' : 'Inactivo'}
													</span>
												</td>
												<td class="px-6 py-4 text-right text-sm font-medium whitespace-nowrap">
													<div class="flex items-center justify-end space-x-2">
														<a
															href="/alumnos/{student.id}"
															class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-1 text-xs font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
														>
															Ver
														</a>
														{#if authStore.isAdmin}
															<button
																onclick={() => handleUnenrollStudent(student.id)}
																disabled={unenrollLoading === student.id}
																class="transform rounded-lg bg-gradient-to-r from-red-600 to-pink-600 px-3 py-1 text-xs font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-red-700 hover:to-pink-700 hover:shadow-lg disabled:opacity-50"
															>
																{unenrollLoading === student.id ? 'Cargando...' : 'Dar de baja'}
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
				{/if}

				<!-- Action Buttons -->
				<div
					class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
				>
					<div class="flex flex-col gap-4 sm:flex-row">
						{#if authStore.isAlumno}
							<button
								onclick={handleEnrollment}
								class="flex-1 transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg disabled:opacity-50"
								disabled={enrollmentLoading || enrollmentStatus?.isEnrolled}
							>
								{enrollmentLoading
									? 'Cargando...'
									: enrollmentStatus?.isEnrolled
										? 'Ya inscrito'
										: 'Inscribirse en la clase'}
							</button>
						{/if}
						{#if authStore.isAdmin || authStore.isProfesor}
							<a
								href="/clases/{currentClase.id}/editar"
								class="flex-1 transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-3 text-center font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
							>
								Editar Clase
							</a>
							<button
								class="flex-1 transform rounded-lg bg-gradient-to-r from-red-600 to-pink-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-red-700 hover:to-pink-700 hover:shadow-lg"
							>
								Eliminar Clase
							</button>
						{/if}
					</div>

					<!-- Enrollment Error Message -->
					{#if enrollmentError}
						<div class="mt-6 rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700">
							{enrollmentError}
						</div>
					{/if}
				</div>
			</div>
		{:else}
			<!-- Not Found State -->
			<div class="py-16 text-center">
				<div
					class="mx-auto max-w-md rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl"
				>
					<svg
						class="mx-auto mb-4 h-16 w-16 text-gray-400"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
						></path>
					</svg>
					<p class="mb-6 text-lg font-medium text-gray-600">Clase no encontrada</p>
					<p class="mb-6 text-gray-500">La clase que buscas no existe o ha sido eliminada.</p>
					<button
						onclick={() => goto('/clases')}
						class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
					>
						Volver a Clases
					</button>
				</div>
			</div>
		{/if}
	</div>
</div>
