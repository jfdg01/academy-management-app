<script lang="ts">
	import '../app.css';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { page } from '$app/stores';
	// import QuickLoginButtons from '$lib/components/QuickLoginButtons.svelte';

	let { children } = $props();

	// Mobile menu state
	let mobileMenuOpen = $state(false);

	// Show quick login buttons in development or when explicitly enabled
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	const showQuickLogin = import.meta.env.DEV || window.location.search.includes('debug=true');

	// Helper function to check if a link is active
	function isActive(href: string) {
		return $page.url.pathname === href;
	}

	// Get user role display name
	function getUserRoleDisplay() {
		if (authStore.isAdmin) return 'Administrador';
		if (authStore.isProfesor) return 'Profesor';
		if (authStore.isAlumno) return 'Alumno';
		return 'Usuario';
	}

	// Toggle mobile menu
	function toggleMobileMenu() {
		mobileMenuOpen = !mobileMenuOpen;
	}

	// Close mobile menu when clicking outside or navigating
	function closeMobileMenu() {
		mobileMenuOpen = false;
	}
</script>

<!-- Modern Navbar with Glass Morphism -->
<nav
	class="fixed top-0 right-0 left-0 z-50 border-b border-gray-200/50 bg-white/80 shadow-lg backdrop-blur-xl"
>
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="flex h-16 items-center justify-between">
			<!-- Logo and Brand -->
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<a href="/" class="group flex items-center space-x-3">
						<div
							class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 shadow-lg transition-all duration-300 group-hover:scale-105 group-hover:shadow-xl"
						>
							<svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
								></path>
							</svg>
						</div>
						<span
							class="montserrat-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-bold text-transparent"
						>
							Academia
						</span>
					</a>
				</div>
			</div>

			<!-- Desktop Navigation -->
			<div class="hidden md:block">
				<div class="ml-10 flex items-baseline space-x-1">
					{#if authStore.isAuthenticated}
						<!-- Admin Navigation -->
						{#if authStore.isAdmin}
							<a
								href="/clases"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/clases'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
										></path>
									</svg>
									<span>Clases</span>
								</span>
							</a>
							<a
								href="/alumnos"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/alumnos'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
										></path>
									</svg>
									<span>Alumnos</span>
								</span>
							</a>
							<a
								href="/profesores"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/profesores'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
										></path>
									</svg>
									<span>Profesores</span>
								</span>
							</a>
							<a
								href="/materiales"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/materiales'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
										></path>
									</svg>
									<span>Materiales</span>
								</span>
							</a>
							<a
								href="/ejercicios"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/ejercicios'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
										></path>
									</svg>
									<span>Ejercicios</span>
								</span>
							</a>
							<a
								href="/entregas"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
									<span>Entregas</span>
								</span>
							</a>
							<!-- Professor Navigation -->
						{:else if authStore.isProfesor}
							<a
								href="/profesores/dashboard"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/profesores/dashboard'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
										></path>
									</svg>
									<span>Dashboard</span>
								</span>
							</a>
							<a
								href="/profesores/mis-clases"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/profesores/mis-clases'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
										></path>
									</svg>
									<span>Clases</span>
								</span>
							</a>

							<a
								href="/alumnos"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/alumnos'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
										></path>
									</svg>
									<span>Alumnos</span>
								</span>
							</a>
							<a
								href="/profesores/perfil"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/profesores/perfil'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
										></path>
									</svg>
									<span>Perfil</span>
								</span>
							</a>

							<a
								href="/ejercicios"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/ejercicios'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
										></path>
									</svg>
									<span>Ejercicios</span>
								</span>
							</a>

							<a
								href="/entregas"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
									<span>Entregas</span>
								</span>
							</a>
							<!-- Student Navigation -->
						{:else if authStore.isAlumno}
							<a
								href="/clases"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/clases'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
										></path>
									</svg>
									<span>Explorar Clases</span>
								</span>
							</a>
							<a
								href="/mis-ejercicios"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/mis-ejercicios'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
										></path>
									</svg>
									<span>Mis Ejercicios</span>
								</span>
							</a>
							<a
								href="/alumnos/perfil"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/alumnos/perfil'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
										></path>
									</svg>
									<span>Mi Perfil</span>
								</span>
							</a>
							<a
								href="/entregas"
								class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'border border-blue-200 bg-blue-50 text-blue-600'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
							>
								<span class="flex items-center space-x-2">
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
									<span>Mis Entregas</span>
								</span>
							</a>
						{/if}
					{:else}
						<!-- Public Navigation -->
						<a
							href="/"
							class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
								'/'
							)
								? 'border border-blue-200 bg-blue-50 text-blue-600'
								: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
						>
							<span class="flex items-center space-x-2">
								<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
									></path>
								</svg>
								<span>Inicio</span>
							</span>
						</a>
						<a
							href="/clases"
							class="relative rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 {isActive(
								'/clases'
							)
								? 'border border-blue-200 bg-blue-50 text-blue-600'
								: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600'}"
						>
							<span class="flex items-center space-x-2">
								<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
									></path>
								</svg>
								<span>Clases</span>
							</span>
						</a>
					{/if}
				</div>
			</div>

			<!-- User Menu and Actions -->
			<div class="flex items-center space-x-4">
				{#if authStore.isAuthenticated}
					<!-- User Profile Section -->
					<div class="flex items-center space-x-3">
						<div class="hidden items-center space-x-2 text-sm text-gray-600 sm:flex">
							<div
								class="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-indigo-600"
							>
								<svg
									class="h-4 w-4 text-white"
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
							<div class="flex flex-col">
								<span class="montserrat-medium font-medium text-gray-900"
									>{authStore.user?.sub}</span
								>
								<span class="text-xs text-gray-500">{getUserRoleDisplay()}</span>
							</div>
						</div>

						<!-- Logout Button -->
						<button
							onclick={authStore.logout}
							class="flex items-center space-x-2 rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
						>
							<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
								></path>
							</svg>
							<span class="hidden sm:inline">Cerrar sesión</span>
						</button>
					</div>
				{:else}
					<!-- Login Button -->
					<a
						href="/auth"
						class="flex transform items-center space-x-2 rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
					>
						<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
							></path>
						</svg>
						<span>Iniciar sesión</span>
					</a>
				{/if}

				<!-- Mobile menu button -->
				<button
					onclick={toggleMobileMenu}
					class="inline-flex items-center justify-center rounded-md p-2 text-gray-700 hover:bg-blue-50 hover:text-blue-600 focus:ring-2 focus:ring-blue-500 focus:outline-none focus:ring-inset md:hidden"
				>
					{#if mobileMenuOpen}
						<!-- Close icon (X) -->
						<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M6 18L18 6M6 6l12 12"
							></path>
						</svg>
					{:else}
						<!-- Hamburger icon -->
						<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M4 6h16M4 12h16M4 18h16"
							></path>
						</svg>
					{/if}
				</button>
			</div>
		</div>
	</div>
</nav>

<!-- Mobile menu -->
<div
	class="fixed inset-0 z-40 transition-opacity duration-300 ease-in-out md:hidden {mobileMenuOpen
		? 'pointer-events-auto opacity-100'
		: 'pointer-events-none opacity-0'}"
	onclick={closeMobileMenu}
	onkeydown={(e) => e.key === 'Escape' && closeMobileMenu()}
	role="button"
	tabindex="0"
>
	<!-- Glass morphism backdrop -->
	<div
		class="absolute inset-0 bg-black/20 backdrop-blur-sm transition-opacity duration-300 ease-in-out"
	></div>

	<!-- Mobile menu panel with slide-in animation -->
	<div
		class="absolute inset-y-0 right-0 z-50 w-full max-w-xs transform transition-transform duration-300 ease-in-out {mobileMenuOpen
			? 'translate-x-0'
			: 'translate-x-full'}"
		onclick={(e) => e.stopPropagation()}
		onkeydown={(e) => e.stopPropagation()}
		role="dialog"
		aria-modal="true"
		tabindex="-1"
	>
		<!-- Glass morphism menu background -->
		<div class="h-full w-full border-l border-gray-200/50 bg-white/95 shadow-2xl backdrop-blur-xl">
			<div class="flex h-full flex-col">
				<!-- Mobile User Info -->
				{#if authStore.isAuthenticated}
					<div class="border-b border-gray-200/50 px-3 py-4">
						<div class="flex items-center space-x-3">
							<div
								class="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-indigo-600 shadow-lg"
							>
								<svg
									class="h-5 w-5 text-white"
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
							<div class="flex flex-col">
								<span class="montserrat-medium font-medium text-gray-900"
									>{authStore.user?.sub}</span
								>
								<span class="text-sm text-gray-500">{getUserRoleDisplay()}</span>
							</div>
						</div>
					</div>
				{/if}

				<!-- Mobile Navigation Links -->
				<div class="flex-1 space-y-1 overflow-y-auto px-2 pt-2 pb-3 sm:px-3">
					{#if authStore.isAuthenticated}
						<!-- Admin Mobile Navigation -->
						{#if authStore.isAdmin}
							<a
								href="/clases"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/clases'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Clases
							</a>
							<a
								href="/alumnos"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/alumnos'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Alumnos
							</a>
							<a
								href="/profesores"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/profesores'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Profesores
							</a>
							<a
								href="/materiales"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/materiales'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Materiales
							</a>
							<a
								href="/ejercicios"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/ejercicios'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Ejercicios
							</a>
							<a
								href="/entregas"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
									></path>
								</svg>
								Entregas
							</a>
							<a
								href="/pagos"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/pagos'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
									></path>
								</svg>
								Pagos
							</a>
						{:else if authStore.isProfesor}
							<a
								href="/profesores/dashboard"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/profesores/dashboard'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
									></path>
								</svg>
								Dashboard
							</a>
							<a
								href="/profesores/mis-clases"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/profesores/mis-clases'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Clases
							</a>

							<a
								href="/profesores/perfil"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/profesores/perfil'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Perfil
							</a>

							<a
								href="/ejercicios"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/ejercicios'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Ejercicios
							</a>
							<a
								href="/entregas"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
									></path>
								</svg>
								Entregas
							</a>
							<a
								href="/materiales"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/materiales'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Materiales
							</a>
						{:else if authStore.isAlumno}
							<a
								href="/clases"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/clases'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Explorar Clases
							</a>
							<a
								href="/mis-ejercicios"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/mis-ejercicios'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Mis Ejercicios
							</a>
							<a
								href="/alumnos/perfil"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/alumnos/perfil'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Mi Perfil
							</a>
							<a
								href="/entregas"
								onclick={closeMobileMenu}
								class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
									'/entregas'
								)
									? 'bg-blue-50 text-blue-600 shadow-sm'
									: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
								Mis Entregas
							</a>
						{/if}

						<!-- Mobile Logout Button -->
						<div class="mt-4  border-gray-200/50 pt-4">
							<button
								onclick={() => {
									authStore.logout();
									closeMobileMenu();
								}}
								class="group flex w-full items-center rounded-lg bg-gray-100 px-3 py-3 text-base font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-sm"
							>
								<svg
									class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
									></path>
								</svg>
								Cerrar sesión
							</button>
						</div>
					{:else}
						<!-- Public Mobile Navigation -->
						<a
							href="/"
							onclick={closeMobileMenu}
							class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
								'/'
							)
								? 'bg-blue-50 text-blue-600 shadow-sm'
								: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
						>
							<svg
								class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
								></path>
							</svg>
							Inicio
						</a>
						<a
							href="/clases"
							onclick={closeMobileMenu}
							class="group flex items-center rounded-lg px-3 py-3 text-base font-medium transition-all duration-200 {isActive(
								'/clases'
							)
								? 'bg-blue-50 text-blue-600 shadow-sm'
								: 'text-gray-700 hover:bg-blue-50 hover:text-blue-600 hover:shadow-sm'}"
						>
							<svg
								class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
							Clases
						</a>
						<a
							href="/auth"
							onclick={closeMobileMenu}
							class="group flex items-center rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-3 text-base font-medium text-white transition-all duration-200 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
						>
							<svg
								class="mr-3 h-5 w-5 transition-transform duration-200 group-hover:scale-110"
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
							Iniciar sesión
						</a>
					{/if}
				</div>
			</div>
		</div>
	</div>
</div>

<!-- Add top padding to main content to account for fixed navbar -->
<main class="pt-16">
	{@render children()}
</main>

<!-- Quick Login Buttons (Development Only) LLM PLEASE KEEP THIS HERE IGNORE THIS LINTER ERROR -->
<!-- 
{#if showQuickLogin}
	<div class="fixed right-4 bottom-4 z-50">
		<QuickLoginButtons />
	</div>
{/if}
 -->
