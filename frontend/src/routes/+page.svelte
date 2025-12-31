<script lang="ts">
	import '../app.css';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { onMount } from 'svelte';

	let userName = '';
	let userRole = '';

	onMount(() => {
		if (authStore.user?.sub) {
			userName = authStore.user.sub;
		}

		if (authStore.isAdmin) {
			userRole = 'Administrador';
		} else if (authStore.isProfesor) {
			userRole = 'Profesor';
		} else if (authStore.isAlumno) {
			userRole = 'Alumno';
		}
	});
</script>

<!-- Hero Section -->
<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<!-- Header -->
	<div class="relative overflow-hidden">
		<div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-indigo-600 opacity-10"></div>

		<!-- Navigation Cards -->
		<div class="relative z-10 px-4 py-16 sm:px-6 lg:px-8">
			<div class="mx-auto max-w-7xl">
				<!-- Welcome Message -->
				<div class="mb-16 text-center">
					{#if authStore.isAuthenticated}
						<div class="mb-8">
							<div
								class="mb-6 inline-flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-indigo-600 shadow-lg"
							>
								<svg
									class="h-12 w-12 text-white"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
									></path>
								</svg>
							</div>
							<h1 class="montserrat-bold mb-4 text-4xl font-bold text-gray-900">
								¡Bienvenido, {userName}!
							</h1>
							<p class="montserrat-medium mb-2 text-xl text-gray-600">
								{userRole} de la Academia
							</p>
							<div
								class="mx-auto h-1 w-24 rounded-full bg-gradient-to-r from-blue-500 to-indigo-600"
							></div>
						</div>
					{:else}
						<div class="mb-8">
							<div
								class="mb-6 inline-flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-indigo-600 shadow-lg"
							>
								<svg
									class="h-12 w-12 text-white"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
									></path>
								</svg>
							</div>
							<h1 class="montserrat-bold mb-4 text-4xl font-bold text-gray-900">
								Bienvenido a la Academia
							</h1>
							<p class="montserrat-medium mb-2 text-xl text-gray-600">
								Tu plataforma de aprendizaje integral
							</p>
							<div
								class="mx-auto h-1 w-24 rounded-full bg-gradient-to-r from-blue-500 to-indigo-600"
							></div>
						</div>
					{/if}
				</div>

				<!-- Navigation Cards Grid -->
				<div class="mb-16 grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3">
					{#if authStore.isAuthenticated}
						<!-- Admin Navigation -->
						{#if authStore.isAdmin}
							<a href="/clases" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-purple-500 to-pink-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Gestión de Clases
									</h3>
									<p class="mb-4 text-gray-600">
										Administra todas las clases, horarios y asignaciones
									</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/alumnos" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-green-500 to-teal-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Gestión de Alumnos
									</h3>
									<p class="mb-4 text-gray-600">
										Gestiona el registro y seguimiento de estudiantes
									</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/profesores" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-orange-500 to-red-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Gestión de Profesores
									</h3>
									<p class="mb-4 text-gray-600">
										Administra el personal docente y sus asignaciones
									</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/materiales" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-yellow-500 to-orange-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Materiales
									</h3>
									<p class="mb-4 text-gray-600">Gestiona recursos educativos y documentación</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/ejercicios" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Ejercicios
									</h3>
									<p class="mb-4 text-gray-600">Administra tareas y evaluaciones</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/entregas" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-pink-500 to-rose-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Entregas
									</h3>
									<p class="mb-4 text-gray-600">Revisa y evalúa trabajos de estudiantes</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<!-- Professor Navigation -->
						{:else if authStore.isProfesor}
							<a href="/clases" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-purple-500 to-pink-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Mis Clases
									</h3>
									<p class="mb-4 text-gray-600">Gestiona tus clases y horarios</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/alumnos" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-green-500 to-teal-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Mis Alumnos
									</h3>
									<p class="mb-4 text-gray-600">Gestiona tus estudiantes</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/profesores/perfil" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-blue-500 to-indigo-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Mi Perfil
									</h3>
									<p class="mb-4 text-gray-600">Gestiona tu información personal</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/ejercicios" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Ejercicios
									</h3>
									<p class="mb-4 text-gray-600">Crea y gestiona tareas</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/entregas" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-pink-500 to-rose-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Entregas
									</h3>
									<p class="mb-4 text-gray-600">Revisa trabajos de estudiantes</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<!-- Student Navigation -->
						{:else if authStore.isAlumno}
							<a href="/clases" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-purple-500 to-pink-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Explorar Clases
									</h3>
									<p class="mb-4 text-gray-600">Descubre nuevas clases disponibles</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/alumnos/perfil" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-blue-500 to-indigo-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Mi Perfil
									</h3>
									<p class="mb-4 text-gray-600">Gestiona tu información personal</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>

							<a href="/entregas" class="group">
								<div
									class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
								>
									<div
										class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-pink-500 to-rose-500 transition-transform duration-300 group-hover:scale-110"
									>
										<svg
											class="h-8 w-8 text-white"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
											></path>
										</svg>
									</div>
									<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
										Mis Entregas
									</h3>
									<p class="mb-4 text-gray-600">Revisa tus trabajos y calificaciones</p>
									<div class="flex items-center font-medium text-blue-600">
										<span>Acceder</span>
										<svg
											class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 5l7 7-7 7"
											></path>
										</svg>
									</div>
								</div>
							</a>
						{/if}
					{:else}
						<!-- Public Navigation -->
						<a href="/clases" class="group">
							<div
								class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
							>
								<div
									class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-purple-500 to-pink-500 transition-transform duration-300 group-hover:scale-110"
								>
									<svg
										class="h-8 w-8 text-white"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
										></path>
									</svg>
								</div>
								<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
									Explorar Clases
								</h3>
								<p class="mb-4 text-gray-600">Descubre nuestras clases disponibles</p>
								<div class="flex items-center font-medium text-blue-600">
									<span>Explorar</span>
									<svg
										class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 5l7 7-7 7"
										></path>
									</svg>
								</div>
							</div>
						</a>

						<a href="/auth" class="group">
							<div
								class="transform rounded-2xl border border-gray-100 bg-white p-8 shadow-lg transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl"
							>
								<div
									class="mb-6 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-r from-green-500 to-teal-500 transition-transform duration-300 group-hover:scale-110"
								>
									<svg
										class="h-8 w-8 text-white"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
										></path>
									</svg>
								</div>
								<h3 class="montserrat-semibold mb-3 text-xl font-semibold text-gray-900">
									Iniciar Sesión
								</h3>
								<p class="mb-4 text-gray-600">Accede a tu cuenta para continuar</p>
								<div class="flex items-center font-medium text-blue-600">
									<span>Acceder</span>
									<svg
										class="ml-2 h-4 w-4 transition-transform duration-300 group-hover:translate-x-1"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 5l7 7-7 7"
										></path>
									</svg>
								</div>
							</div>
						</a>
					{/if}
				</div>

				<!-- Quick Stats Section (for authenticated users) -->
				{#if authStore.isAuthenticated}
					<div class="mb-16 rounded-2xl bg-white p-8 shadow-lg">
						<h2 class="montserrat-bold mb-6 text-center text-2xl font-bold text-gray-900">
							Resumen Rápido
						</h2>
						<div class="grid grid-cols-1 gap-6 md:grid-cols-3">
							<div class="text-center">
								<div
									class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-indigo-600"
								>
									<svg
										class="h-8 w-8 text-white"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
								</div>
								<h3 class="montserrat-semibold mb-2 text-lg font-semibold text-gray-900">
									Clases Activas
								</h3>
								<p class="text-3xl font-bold text-blue-600">12</p>
							</div>
							<div class="text-center">
								<div
									class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-r from-green-500 to-teal-600"
								>
									<svg
										class="h-8 w-8 text-white"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
								</div>
								<h3 class="montserrat-semibold mb-2 text-lg font-semibold text-gray-900">
									Tareas Completadas
								</h3>
								<p class="text-3xl font-bold text-green-600">8</p>
							</div>
							<div class="text-center">
								<div
									class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-r from-purple-500 to-pink-600"
								>
									<svg
										class="h-8 w-8 text-white"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
										></path>
									</svg>
								</div>
								<h3 class="montserrat-semibold mb-2 text-lg font-semibold text-gray-900">
									Progreso General
								</h3>
								<p class="text-3xl font-bold text-purple-600">85%</p>
							</div>
						</div>
					</div>
				{/if}

				<!-- Call to Action Section -->
				<div class="text-center">
					<div class="rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 p-8 shadow-xl">
						<h2 class="montserrat-bold mb-4 text-3xl font-bold text-white">
							{#if authStore.isAuthenticated}
								¡Comienza tu día de aprendizaje!
							{:else}
								¡Únete a nuestra comunidad educativa!
							{/if}
						</h2>
						<p class="montserrat-medium mb-6 text-xl text-blue-100">
							{#if authStore.isAuthenticated}
								Explora las nuevas funcionalidades y mantén tu progreso al día.
							{:else}
								Descubre un mundo de posibilidades educativas con nuestra plataforma.
							{/if}
						</p>
						{#if !authStore.isAuthenticated}
							<a
								href="/auth"
								class="inline-flex transform items-center rounded-lg bg-white px-8 py-3 font-semibold text-blue-600 shadow-lg transition-colors duration-300 hover:-translate-y-1 hover:bg-gray-50 hover:shadow-xl"
							>
								<span>Empezar Ahora</span>
								<svg class="ml-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M13 7l5 5m0 0l-5 5m5-5H6"
									></path>
								</svg>
							</a>
						{/if}
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
