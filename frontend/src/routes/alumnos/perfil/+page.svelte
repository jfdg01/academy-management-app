<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOAlumno, DTOClase } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';
	import { ProfileHeader, ProfileCard } from '$lib/components/common/index.js';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let alumno = $state<DTOAlumno | null>(null);
	let enrolledClasses = $state<DTOClase[]>([]);
	let enrolledClassesLoading = $state(false);
	let enrolledClassesError = $state<string | null>(null);

	// Get student ID from URL or use current user's ID
	const studentId = $derived(Number($page.params.id) || authStore.user?.id || 0);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load student data
	$effect(() => {
		if (authStore.isAuthenticated && studentId) {
			loadStudentData();
			loadEnrolledClasses();
		}
	});

	async function loadStudentData() {
		loading = true;
		error = null;

		try {
			alumno = await AlumnoService.getAlumno(studentId);
		} catch (err) {
			error = `Error al cargar los datos del alumno: ${err}`;
			console.error('Error loading student data:', err);
		} finally {
			loading = false;
		}
	}

	async function loadEnrolledClasses() {
		enrolledClassesLoading = true;
		enrolledClassesError = null;

		try {
			enrolledClasses = await EnrollmentService.getMyEnrolledClasses();
		} catch (err) {
			enrolledClassesError = `Error al cargar las clases inscritas: ${err}`;
			console.error('Error loading enrolled classes:', err);
		} finally {
			enrolledClassesLoading = false;
		}
	}
</script>

<svelte:head>
	<title>Mi Perfil - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	{#if loading}
		<div class="flex justify-center py-12">
			<div class="text-center">
				<div class="mb-4 text-6xl">📚</div>
				<p class="text-gray-600">Cargando perfil...</p>
			</div>
		</div>
	{:else if error}
		<div class="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
			<h3 class="mb-2 text-lg font-medium text-red-800">Error</h3>
			<p class="text-red-600">{error}</p>
			<button
				onclick={() => goto('/clases')}
				class="mt-4 rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Clases
			</button>
		</div>
	{:else if alumno}
		<div class="space-y-8">
			<!-- Header -->
			<ProfileHeader
				title="Mi Perfil"
				subtitle="Gestiona tu información personal y clases inscritas"
				backUrl="/clases"
				backText="Volver a Clases"
				showBackButton={true}
				actions={[
					{
						label: 'Explorar Clases',
						onClick: () => goto('/clases'),
						variant: 'primary'
					}
				]}
			/>

			<!-- Personal Information -->
			<ProfileCard
				title="Información Personal"
				subtitle="Datos personales y académicos del estudiante"
			>
				<div class="grid gap-4 md:grid-cols-2">
					<div>
						<div class="text-sm font-medium text-gray-500">Nombre</div>
						<p class="text-gray-900">{alumno.firstName} {alumno.lastName}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Usuario</div>
						<p class="text-gray-900">@{alumno.username}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Email</div>
						<p class="text-gray-900">{alumno.email}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">DNI</div>
						<p class="text-gray-900">{alumno.dni}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Teléfono</div>
						<p class="text-gray-900">{alumno.phoneNumber || 'No especificado'}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Estado</div>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
								? 'bg-green-100 text-green-800'
								: 'bg-red-100 text-red-800'}"
						>
							{alumno.enabled ? 'Activo' : 'Inactivo'}
						</span>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Fecha de Matrícula</div>
						<p class="text-gray-900">{FormatterUtils.formatDate(alumno.enrollmentDate)}</p>
					</div>
					<div>
						<div class="text-sm font-medium text-gray-500">Matriculado</div>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enrolled
								? 'bg-green-100 text-green-800'
								: 'bg-yellow-100 text-yellow-800'}"
						>
							{alumno.enrolled ? 'Sí' : 'No'}
						</span>
					</div>
				</div>
			</ProfileCard>

			<!-- Enrolled Classes -->
			<ProfileCard
				title="Mis Clases Inscritas"
				subtitle="Clases en las que estás matriculado"
				loading={enrolledClassesLoading}
				empty={!enrolledClassesLoading && enrolledClasses.length === 0}
				emptyIcon="📚"
				emptyTitle="No tienes clases inscritas"
				emptyDescription="Explora nuestras clases disponibles y comienza tu aprendizaje."
				emptyAction={{
					label: 'Explorar Clases',
					onClick: () => goto('/clases')
				}}
				actions={[
					{
						label: 'Ver clases disponibles',
						onClick: () => goto('/clases'),
						variant: 'secondary'
					}
				]}
			>
				{#if enrolledClassesError}
					<div class="rounded-lg border border-red-200 bg-red-50 p-4">
						<p class="text-red-600">{enrolledClassesError}</p>
					</div>
				{:else}
					<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
						{#each enrolledClasses as clase (clase.id)}
							<div
								class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
							>
								<div class="mb-3">
									<h3 class="font-semibold text-gray-900">{clase.titulo}</h3>
									<p class="line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>
								</div>

								<div class="mb-3 space-y-2">
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Precio:</span>
										<span class="font-semibold text-green-600"
											>{FormatterUtils.formatPrice(clase.precio)}</span
										>
									</div>
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Nivel:</span>
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {FormatterUtils.getNivelColor(
												clase.nivel
											)}"
										>
											{FormatterUtils.formatNivel(clase.nivel)}
										</span>
									</div>
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Modalidad:</span>
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {FormatterUtils.getPresencialidadColor(
												clase.presencialidad
											)}"
										>
											{FormatterUtils.getPresencialidadText(clase.presencialidad)}
										</span>
									</div>
								</div>

								<div class="flex gap-2">
									<a
										href="/clases/{clase.id}"
										class="flex-1 rounded bg-blue-600 px-3 py-2 text-center text-sm font-medium text-white hover:bg-blue-700"
									>
										Ver Clase
									</a>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</ProfileCard>
		</div>
	{:else}
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">👤</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Perfil no encontrado</h3>
			<p class="mb-4 text-gray-500">No se pudo cargar la información del perfil.</p>
			<button
				onclick={() => goto('/clases')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Clases
			</button>
		</div>
	{/if}
</div>
