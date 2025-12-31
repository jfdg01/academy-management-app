<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import type { DTOProfesor, DTOActualizacionProfesor } from '$lib/generated/api';
	import { ProfesorService } from '$lib/services/profesorService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ProfileHeader, ProfileCard } from '$lib/components/common';

	// Props and derived state
	const profesorId = $derived(parseInt($page.params.id));
	const isCreated = $derived($page.url.searchParams.get('created') === 'true');

	// State management
	let profesor: DTOProfesor | null = $state(null);
	let editMode = $state(false);
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);

	// Edit form state
	let editForm: DTOActualizacionProfesor = $state({});

	// Permissions
	const canEdit = $derived(() => {
		if (!profesor) return false;

		// Admin can edit anyone
		if (authStore.isAdmin) return true;

		// Professor can only edit their own profile
		if (
			authStore.isProfesor &&
			(authStore.user?.usuario === profesor.username || authStore.user?.sub === profesor.username)
		)
			return true;

		return false;
	});

	const canChangeStatus = $derived(() => authStore.isAdmin);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Show success message if just created
	$effect(() => {
		if (isCreated) {
			successMessage = 'Profesor creado exitosamente';
			setTimeout(() => (successMessage = null), 5000);
		}
	});

	onMount(() => {
		loadProfesor();
	});

	async function loadProfesor() {
		if (!profesorId || isNaN(profesorId)) {
			error = 'ID de profesor inválido';
			return;
		}

		loading = true;
		error = null;

		try {
			profesor = await ProfesorService.getProfesorById(profesorId);

			// Check if current user can access this profile
			if (!authStore.isAdmin) {
				if (
					!authStore.isProfesor ||
					(authStore.user?.sub !== profesor.username && authStore.user?.sub !== profesor.username)
				) {
					error = 'No tienes permisos para ver este perfil';
					return;
				}
			}
		} catch (err) {
			error = `Error al cargar el profesor: ${err}`;
		} finally {
			loading = false;
		}
	}

	function startEdit() {
		if (!profesor || !canEdit) return;

		editForm = {
			firstName: profesor.firstName || '',
			lastName: profesor.lastName || '',
			dni: profesor.dni || '',
			email: profesor.email || '',
			phoneNumber: profesor.phoneNumber || ''
		};
		editMode = true;
	}

	function cancelEdit() {
		editMode = false;
		editForm = {};
		error = null;
	}

	// ==================== VALIDATION FUNCTIONS ====================

	/**
	 * Valida nombres y apellidos: solo letras, acentos, espacios, máximo 100 caracteres
	 */
	function validateName(name: string): { isValid: boolean; message: string } {
		if (!name || name.trim().length === 0) {
			return { isValid: false, message: 'Este campo es obligatorio' };
		}
		if (name.length > 100) {
			return { isValid: false, message: 'Máximo 100 caracteres' };
		}
		const nameRegex = /^[a-zA-ZáéíóúñÁÉÍÓÚÑ\s]+$/;
		if (!nameRegex.test(name)) {
			return { isValid: false, message: 'Solo se permiten letras, acentos y espacios' };
		}
		return { isValid: true, message: '✓ Válido' };
	}

	/**
	 * Valida DNI español: 8 números + 1 letra calculada
	 */
	function validateDNI(dni: string): { isValid: boolean; message: string } {
		if (!dni || dni.trim().length === 0) {
			return { isValid: false, message: 'El DNI es obligatorio' };
		}

		const dniRegex = /^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$/i;
		if (!dniRegex.test(dni)) {
			return { isValid: false, message: 'Formato: 8 números + 1 letra (ej: 12345678Z)' };
		}

		// Calcular letra correcta
		const numbers = dni.substring(0, 8);
		const letter = dni.substring(8, 9).toUpperCase();
		const correctLetters = 'TRWAGMYFPDXBNJZSQVHLCKE';
		const correctLetter = correctLetters[parseInt(numbers) % 23];

		if (letter !== correctLetter) {
			return { isValid: false, message: `La letra debería ser ${correctLetter}` };
		}

		return { isValid: true, message: '✓ DNI válido' };
	}

	/**
	 * Valida email con límites específicos
	 */
	function validateEmail(email: string): { isValid: boolean; message: string } {
		if (!email || email.trim().length === 0) {
			return { isValid: false, message: 'El email es obligatorio' };
		}

		if (email.length > 254) {
			return { isValid: false, message: 'Máximo 254 caracteres' };
		}

		const [localPart] = email.split('@');
		if (localPart && localPart.length > 64) {
			return { isValid: false, message: 'La parte local no puede superar 64 caracteres' };
		}

		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			return { isValid: false, message: 'Formato de email inválido' };
		}

		// Verificar puntos consecutivos
		if (email.includes('..')) {
			return { isValid: false, message: 'No se permiten puntos consecutivos' };
		}

		return { isValid: true, message: '✓ Email válido' };
	}

	/**
	 * Valida teléfono: 6-14 dígitos, prefijos internacionales, caracteres permitidos
	 */
	function validatePhoneNumber(phone: string): { isValid: boolean; message: string } {
		if (!phone || phone.trim().length === 0) {
			return { isValid: true, message: 'Campo opcional' }; // Es opcional
		}

		// Caracteres permitidos: números, espacios, guiones, puntos, paréntesis, +
		const allowedCharsRegex = /^[0-9+\-\s().]+$/;
		if (!allowedCharsRegex.test(phone)) {
			return { isValid: false, message: 'Solo números, espacios, guiones, puntos, paréntesis y +' };
		}

		// Extraer solo los dígitos
		const digits = phone.replace(/[^0-9]/g, '');

		if (digits.length < 6) {
			return { isValid: false, message: 'Mínimo 6 dígitos' };
		}

		if (digits.length > 14) {
			return { isValid: false, message: 'Máximo 14 dígitos' };
		}

		return { isValid: true, message: '✓ Teléfono válido' };
	}

	/**
	 * Verifica si un campo debe enviarse en el PATCH (no vacío)
	 */
	function shouldIncludeField(value: string | undefined): boolean {
		return value !== undefined && value !== null && value.trim() !== '';
	}

	/**
	 * Verifica si el formulario completo es válido
	 */
	function isFormValid(): boolean {
		// Verificar que todos los campos que tienen contenido sean válidos
		if (editForm.firstName && !validateName(editForm.firstName).isValid) return false;
		if (editForm.lastName && !validateName(editForm.lastName).isValid) return false;
		if (editForm.dni && !validateDNI(editForm.dni).isValid) return false;
		if (editForm.email && !validateEmail(editForm.email).isValid) return false;
		if (editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid) return false;

		// Verificar que al menos un campo tenga contenido para enviar
		return (
			shouldIncludeField(editForm.firstName) ||
			shouldIncludeField(editForm.lastName) ||
			shouldIncludeField(editForm.dni) ||
			shouldIncludeField(editForm.email) ||
			shouldIncludeField(editForm.phoneNumber)
		);
	}

	/**
	 * Verifica si hay errores de validación en el formulario
	 */
	function hasFormErrors(): boolean {
		return Boolean(
			(editForm.firstName && !validateName(editForm.firstName).isValid) ||
				(editForm.lastName && !validateName(editForm.lastName).isValid) ||
				(editForm.dni && !validateDNI(editForm.dni).isValid) ||
				(editForm.email && !validateEmail(editForm.email).isValid) ||
				(editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid)
		);
	}

	async function saveChanges() {
		if (!profesor) return;

		saving = true;
		error = null;

		try {
			// Validar todos los campos que van a enviarse
			const validationErrors: string[] = [];

			// Solo incluir campos que no estén vacíos (PATCH parcial)
			const updateData: DTOActualizacionProfesor = {};

			// Validar y incluir nombre si está presente
			if (shouldIncludeField(editForm.firstName)) {
				const nameValidation = validateName(editForm.firstName!);
				if (!nameValidation.isValid) {
					validationErrors.push(`Nombre: ${nameValidation.message}`);
				} else {
					updateData.firstName = editForm.firstName;
				}
			}

			// Validar y incluir apellidos si está presente
			if (shouldIncludeField(editForm.lastName)) {
				const apellidosValidation = validateName(editForm.lastName!);
				if (!apellidosValidation.isValid) {
					validationErrors.push(`Apellidos: ${apellidosValidation.message}`);
				} else {
					updateData.lastName = editForm.lastName;
				}
			}

			// Validar y incluir DNI si está presente
			if (shouldIncludeField(editForm.dni)) {
				const dniValidation = validateDNI(editForm.dni!);
				if (!dniValidation.isValid) {
					validationErrors.push(`DNI: ${dniValidation.message}`);
				} else {
					updateData.dni = editForm.dni;
				}
			}

			// Validar y incluir email si está presente
			if (shouldIncludeField(editForm.email)) {
				const emailValidation = validateEmail(editForm.email!);
				if (!emailValidation.isValid) {
					validationErrors.push(`Email: ${emailValidation.message}`);
				} else {
					updateData.email = editForm.email;
				}
			}

			// Validar y incluir teléfono si está presente
			if (shouldIncludeField(editForm.phoneNumber)) {
				const phoneValidation = validatePhoneNumber(editForm.phoneNumber!);
				if (!phoneValidation.isValid) {
					validationErrors.push(`Teléfono: ${phoneValidation.message}`);
				} else {
					updateData.phoneNumber = editForm.phoneNumber;
				}
			}

			// Si hay errores de validación, mostrarlos
			if (validationErrors.length > 0) {
				error = validationErrors.join('. ');
				return;
			}

			// Si no hay datos para actualizar
			if (Object.keys(updateData).length === 0) {
				error = 'No hay cambios para guardar';
				return;
			}

			const updatedProfesor = await ProfesorService.updateProfesor(profesor.id!, updateData);
			profesor = updatedProfesor;
			editMode = false;
			editForm = {};
			successMessage = 'Perfil actualizado correctamente';
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al actualizar el perfil: ${err}`;
		} finally {
			saving = false;
		}
	}

	async function toggleAccountStatus() {
		if (!profesor || !canChangeStatus) return;

		try {
			const result = await ProfesorService.handleStatusChange(profesor.id!, !profesor.enabled);
			if (result.success && result.updatedProfesor) {
				profesor = result.updatedProfesor;
				successMessage = `Cuenta ${result.updatedProfesor.enabled ? 'habilitada' : 'deshabilitada'} correctamente`;
			} else {
				error = result.message;
			}
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al cambiar estado de cuenta: ${err}`;
		}
	}

	function formatDate(date: Date | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}
</script>

<svelte:head>
	<title>Perfil de Profesor - Academia</title>
</svelte:head>

<!-- Main Container with Gradient Background -->
<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8 lg:py-16">
		<div class="mx-auto max-w-4xl">
			<!-- Header Section -->
			<ProfileHeader
				title="Perfil de Profesor"
				subtitle="Gestiona tu información personal y configuración de cuenta"
				backUrl="/profesores"
				backText="Volver a Profesores"
				showBackButton={true}
			/>

			<!-- Teacher Flow Navigation -->
			{#if authStore.isProfesor && (authStore.user?.sub === profesor?.username || authStore.user?.usuario === profesor?.username)}
				<ProfileCard
					title="Acciones del Profesor"
					subtitle="Acceso rápido a las herramientas del profesor"
				>
					<div class="flex flex-wrap gap-3">
						<button
							onclick={() => goto('/profesores/dashboard')}
							class="inline-flex transform items-center rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
						>
							📊 Dashboard
						</button>
						<button
							onclick={() => goto('/profesores/mis-clases')}
							class="inline-flex transform items-center rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
						>
							📚 Mis Clases
						</button>
						<button
							onclick={() => goto('/entregas')}
							class="inline-flex transform items-center rounded-lg bg-gradient-to-r from-purple-600 to-violet-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-purple-700 hover:to-violet-700 hover:shadow-lg"
						>
							📝 Entregas
						</button>
						<button
							onclick={() => goto('/materiales')}
							class="inline-flex transform items-center rounded-lg bg-gradient-to-r from-orange-600 to-red-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-orange-700 hover:to-red-700 hover:shadow-lg"
						>
							📁 Materiales
						</button>
					</div>
				</ProfileCard>
			{/if}

			<!-- Messages -->
			{#if error}
				<div class="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
					<div class="flex items-center">
						<svg class="mr-2 h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
							<path
								fill-rule="evenodd"
								d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
								clip-rule="evenodd"
							></path>
						</svg>
						{error}
					</div>
				</div>
			{/if}

			{#if successMessage}
				<div class="mb-6 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-green-700">
					<div class="flex items-center">
						<svg class="mr-2 h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
							<path
								fill-rule="evenodd"
								d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
								clip-rule="evenodd"
							></path>
						</svg>
						{successMessage}
					</div>
				</div>
			{/if}

			<!-- Loading State -->
			{#if loading}
				<div class="flex justify-center py-12">
					<div class="text-center">
						<div class="mb-4 text-6xl">👨‍🏫</div>
						<p class="text-gray-600">Cargando perfil...</p>
					</div>
				</div>
			{:else if profesor}
				<div class="space-y-8">
					<!-- Personal Information -->
					<ProfileCard
						title="Información Personal"
						subtitle="Datos personales y académicos del profesor"
						actions={canEdit() && !editMode
							? [
									{
										label: 'Editar',
										onClick: startEdit,
										icon: 'edit',
										variant: 'secondary'
									}
								]
							: []}
					>
						{#if editMode}
							<!-- Edit Form -->
							<form
								onsubmit={(e) => {
									e.preventDefault();
									saveChanges();
								}}
								class="space-y-6"
							>
								<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
									<!-- NOMBRE -->
									<div>
										<label for="firstName" class="mb-2 block text-sm font-medium text-gray-700">
											Nombre <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="firstName"
												type="text"
												bind:value={editForm.firstName}
												maxlength="100"
												class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {editForm.firstName
													? validateName(editForm.firstName).isValid
														? 'border-green-500 bg-green-50'
														: 'border-red-500 bg-red-50'
													: 'border-gray-300'}"
												placeholder="Ej: Juan Carlos"
											/>
											{#if editForm.firstName}
												<div class="absolute inset-y-0 right-0 flex items-center pr-3">
													{#if validateName(editForm.firstName).isValid}
														<span class="text-green-500">✓</span>
													{:else}
														<span class="text-red-500">✗</span>
													{/if}
												</div>
											{/if}
										</div>
										{#if editForm.firstName}
											<p
												class="mt-1 text-xs {validateName(editForm.firstName).isValid
													? 'text-green-600'
													: 'text-red-600'}"
											>
												{validateName(editForm.firstName).message}
											</p>
										{/if}
									</div>

									<!-- APELLIDOS -->
									<div>
										<label for="lastName" class="mb-2 block text-sm font-medium text-gray-700">
											Apellidos <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="lastName"
												type="text"
												bind:value={editForm.lastName}
												maxlength="100"
												class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {editForm.lastName
													? validateName(editForm.lastName).isValid
														? 'border-green-500 bg-green-50'
														: 'border-red-500 bg-red-50'
													: 'border-gray-300'}"
												placeholder="Ej: García López"
											/>
											{#if editForm.lastName}
												<div class="absolute inset-y-0 right-0 flex items-center pr-3">
													{#if validateName(editForm.lastName).isValid}
														<span class="text-green-500">✓</span>
													{:else}
														<span class="text-red-500">✗</span>
													{/if}
												</div>
											{/if}
										</div>
										{#if editForm.lastName}
											<p
												class="mt-1 text-xs {validateName(editForm.lastName).isValid
													? 'text-green-600'
													: 'text-red-600'}"
											>
												{validateName(editForm.lastName).message}
											</p>
										{/if}
									</div>

									<!-- DNI -->
									<div>
										<label for="dni" class="mb-2 block text-sm font-medium text-gray-700">
											DNI <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="dni"
												type="text"
												bind:value={editForm.dni}
												maxlength="9"
												class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {editForm.dni
													? validateDNI(editForm.dni).isValid
														? 'border-green-500 bg-green-50'
														: 'border-red-500 bg-red-50'
													: 'border-gray-300'}"
												placeholder="12345678Z"
												style="text-transform: uppercase;"
											/>
											{#if editForm.dni}
												<div class="absolute inset-y-0 right-0 flex items-center pr-3">
													{#if validateDNI(editForm.dni).isValid}
														<span class="text-green-500">✓</span>
													{:else}
														<span class="text-red-500">✗</span>
													{/if}
												</div>
											{/if}
										</div>
										{#if editForm.dni}
											<p
												class="mt-1 text-xs {validateDNI(editForm.dni).isValid
													? 'text-green-600'
													: 'text-red-600'}"
											>
												{validateDNI(editForm.dni).message}
											</p>
										{/if}
									</div>

									<!-- EMAIL -->
									<div>
										<label for="email" class="mb-2 block text-sm font-medium text-gray-700">
											Email <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="email"
												type="email"
												bind:value={editForm.email}
												maxlength="254"
												class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {editForm.email
													? validateEmail(editForm.email).isValid
														? 'border-green-500 bg-green-50'
														: 'border-red-500 bg-red-50'
													: 'border-gray-300'}"
												placeholder="usuario@universidad.es"
											/>
											{#if editForm.email}
												<div class="absolute inset-y-0 right-0 flex items-center pr-3">
													{#if validateEmail(editForm.email).isValid}
														<span class="text-green-500">✓</span>
													{:else}
														<span class="text-red-500">✗</span>
													{/if}
												</div>
											{/if}
										</div>
										{#if editForm.email}
											<p
												class="mt-1 text-xs {validateEmail(editForm.email).isValid
													? 'text-green-600'
													: 'text-red-600'}"
											>
												{validateEmail(editForm.email).message}
											</p>
										{/if}
									</div>

									<!-- TELÉFONO -->
									<div class="md:col-span-2">
										<label for="phoneNumber" class="mb-2 block text-sm font-medium text-gray-700">
											Teléfono <span class="text-gray-400">(Opcional)</span>
										</label>
										<div class="relative">
											<input
												id="phoneNumber"
												type="tel"
												bind:value={editForm.phoneNumber}
												class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {editForm.phoneNumber
													? validatePhoneNumber(editForm.phoneNumber).isValid
														? 'border-green-500 bg-green-50'
														: 'border-red-500 bg-red-50'
													: 'border-gray-300'}"
												placeholder="Ej: +34 123 456 789, (555) 123-4567, 123456789"
											/>
											{#if editForm.phoneNumber}
												<div class="absolute inset-y-0 right-0 flex items-center pr-3">
													{#if validatePhoneNumber(editForm.phoneNumber).isValid}
														<span class="text-green-500">✓</span>
													{:else}
														<span class="text-red-500">✗</span>
													{/if}
												</div>
											{/if}
										</div>
										{#if editForm.phoneNumber}
											<p
												class="mt-1 text-xs {validatePhoneNumber(editForm.phoneNumber).isValid
													? 'text-green-600'
													: 'text-red-600'}"
											>
												{validatePhoneNumber(editForm.phoneNumber).message}
											</p>
										{/if}
									</div>
								</div>

								<!-- Form Actions -->
								<div
									class="flex flex-col justify-end space-y-3  border-gray-200 pt-6 sm:flex-row sm:space-y-0 sm:space-x-4"
								>
									<button
										type="button"
										onclick={cancelEdit}
										class="rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
										disabled={saving}
									>
										Cancelar
									</button>
									<button
										type="submit"
										disabled={saving || !isFormValid()}
										class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg disabled:transform-none disabled:cursor-not-allowed disabled:opacity-50"
									>
										{#if saving}
											<div class="flex items-center">
												<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
													<circle
														class="opacity-25"
														cx="12"
														cy="12"
														r="10"
														stroke="currentColor"
														stroke-width="4"
													></circle>
													<path
														class="opacity-75"
														fill="currentColor"
														d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
													></path>
												</svg>
												Guardando...
											</div>
										{:else}
											Guardar Cambios
										{/if}
									</button>
								</div>

								<!-- Form Validation Summary -->
								{#if hasFormErrors()}
									<div class="mt-4 rounded-lg border border-yellow-200 bg-yellow-50 p-4">
										<h4 class="mb-2 text-sm font-medium text-yellow-800">⚠️ Campos con errores:</h4>
										<ul class="space-y-1 text-xs text-yellow-700">
											{#if editForm.firstName && !validateName(editForm.firstName).isValid}
												<li>• Nombre: {validateName(editForm.firstName).message}</li>
											{/if}
											{#if editForm.lastName && !validateName(editForm.lastName).isValid}
												<li>• Apellidos: {validateName(editForm.lastName).message}</li>
											{/if}
											{#if editForm.dni && !validateDNI(editForm.dni).isValid}
												<li>• DNI: {validateDNI(editForm.dni).message}</li>
											{/if}
											{#if editForm.email && !validateEmail(editForm.email).isValid}
												<li>• Email: {validateEmail(editForm.email).message}</li>
											{/if}
											{#if editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid}
												<li>• Teléfono: {validatePhoneNumber(editForm.phoneNumber).message}</li>
											{/if}
										</ul>
									</div>
								{/if}
							</form>
						{:else}
							<!-- View Mode -->
							<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
								<div>
									<h3 class="mb-4 text-sm font-medium tracking-wide text-gray-500 uppercase">
										Información Personal
									</h3>
									<dl class="space-y-3">
										<div>
											<dt class="text-sm font-medium text-gray-700">Nombre Completo</dt>
											<dd class="text-gray-900">{profesor.firstName} {profesor.lastName}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-700">DNI</dt>
											<dd class="text-gray-900">{profesor.dni}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-700">Email</dt>
											<dd class="text-gray-900">{profesor.email}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-700">Teléfono</dt>
											<dd class="text-gray-900">{profesor.phoneNumber || 'No especificado'}</dd>
										</div>
									</dl>
								</div>

								<div>
									<h3 class="mb-4 text-sm font-medium tracking-wide text-gray-500 uppercase">
										Información Académica
									</h3>
									<dl class="space-y-3">
										<div>
											<dt class="text-sm font-medium text-gray-700">Usuario</dt>
											<dd class="text-gray-900">@{profesor.username}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-700">Fecha de Registro</dt>
											<dd class="text-gray-900">{formatDate(profesor.createdAt)}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-700">Estado de Cuenta</dt>
											<dd>
												<span
													class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {profesor.enabled
														? 'bg-green-100 text-green-800'
														: 'bg-red-100 text-red-800'}"
												>
													{profesor.enabled ? 'Activo' : 'Inactivo'}
												</span>
											</dd>
										</div>
									</dl>
								</div>
							</div>
						{/if}
					</ProfileCard>

					<!-- Admin Actions -->
					{#if canChangeStatus()}
						<ProfileCard
							title="Acciones de Administrador"
							subtitle="Gestionar el estado de la cuenta del profesor"
						>
							<div class="space-y-4">
								<button
									onclick={toggleAccountStatus}
									class="w-full bg-gradient-to-r px-6 py-3 {profesor.enabled
										? 'from-red-600 to-red-700 hover:from-red-700 hover:to-red-800'
										: 'from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700'} transform rounded-lg font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg"
								>
									{profesor.enabled ? 'Deshabilitar' : 'Habilitar'} Cuenta
								</button>
							</div>
						</ProfileCard>
					{/if}

					<!-- Quick Stats -->
					<ProfileCard title="Información Rápida" subtitle="Datos básicos del perfil del profesor">
						<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
							<div class="flex items-center justify-between rounded-lg bg-gray-50 p-3">
								<span class="text-sm font-medium text-gray-700">ID del Profesor:</span>
								<span class="text-sm font-semibold text-gray-900">#{profesor.id}</span>
							</div>
							<div class="flex items-center justify-between rounded-lg bg-gray-50 p-3">
								<span class="text-sm font-medium text-gray-700">Estado:</span>
								<span
									class="text-sm font-semibold {profesor.enabled
										? 'text-green-600'
										: 'text-red-600'}"
								>
									{profesor.enabled ? 'Activo' : 'Inactivo'}
								</span>
							</div>
						</div>
					</ProfileCard>
				</div>
			{:else if !loading}
				<div class="py-12 text-center">
					<div class="mb-4 text-6xl text-gray-400">👤</div>
					<h3 class="mb-2 text-lg font-medium text-gray-900">Perfil no encontrado</h3>
					<p class="mb-4 text-gray-500">No se pudo cargar la información del perfil.</p>
					<button
						onclick={() => goto('/profesores')}
						class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
					>
						Volver a Profesores
					</button>
				</div>
			{/if}
		</div>
	</div>
</div>
