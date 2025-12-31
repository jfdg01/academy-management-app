<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import type {
		DTOAlumno,
		DTOActualizarAlumno,
		DTOClase,
		DTOEntregaEjercicio
	} from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { EntregaService } from '$lib/services/entregaService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { PermissionUtils } from '$lib/utils/permissions';
	import { alumnoApi } from '$lib/api';
	import { ProfileHeader, ProfileCard } from '$lib/components/common';

	// Props and derived state
	const studentId = $derived(parseInt($page.params.id));
	const isCreated = $derived($page.url.searchParams.get('created') === 'true');

	// State management
	let alumno: DTOAlumno | null = $state(null);
	let editMode = $state(false);
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let enrolledClasses = $state<DTOClase[]>([]);
	let loadingClasses = $state(false);
	let entregas = $state<DTOEntregaEjercicio[]>([]);
	let loadingEntregas = $state(false);

	// Edit form state
	let editForm: DTOActualizarAlumno = $state({});

	// Permissions
	const canEdit = $derived(() => {
		if (!alumno) return false;

		// Admin can edit anyone
		if (authStore.isAdmin) return true;

		// Student can only edit their own profile
		if (
			authStore.isAlumno &&
			(authStore.user?.usuario === alumno.username || authStore.user?.sub === alumno.username)
		)
			return true;

		return false;
	});

	const canChangeStatus = $derived(() => authStore.isAdmin);

	// Check if current user is viewing their own profile
	const isOwnProfile = $derived(() => {
		if (!alumno || !authStore.user) return false;
		return authStore.user.usuario === alumno.username || authStore.user.sub === alumno.username;
	});

	// Check if user can view deliveries
	const canViewDeliveries = $derived(() => {
		if (!alumno || !authStore.user) return false;

		// Admin and teachers can view any student's deliveries
		if (PermissionUtils.canViewAllDeliveries(authStore.user)) return true;

		// Students can only view their own deliveries
		if (isOwnProfile()) return true;

		return false;
	});

	// Check if user can view enrolled classes
	const canViewEnrolledClasses = $derived(() => {
		if (!alumno || !authStore.user) return false;

		// Students can view their own enrolled classes
		if (isOwnProfile()) return true;

		// Admin and teachers can view any student's enrolled classes
		if (authStore.isAdmin || authStore.isProfesor) return true;

		return false;
	});

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
			successMessage = 'Alumno creado exitosamente';
			setTimeout(() => (successMessage = null), 5000);
		}
	});

	onMount(() => {
		loadAlumno();
	});

	// Load enrolled classes if user has permission
	$effect(() => {
		if (alumno && canViewEnrolledClasses()) {
			loadEnrolledClasses();
		}
	});

	// Load deliveries if user has permission
	$effect(() => {
		if (alumno && canViewDeliveries()) {
			loadEntregas();
		}
	});

	async function loadAlumno() {
		if (!studentId || isNaN(studentId)) {
			error = 'ID de alumno inválido';
			return;
		}

		loading = true;
		error = null;

		try {
			// Use the optimized endpoint that loads all relationships
			alumno = await AlumnoService.getAlumnoCompleto(studentId);
		} catch (err) {
			error = `Error al cargar alumno: ${err}`;
			console.error('Error loading alumno:', err);
		} finally {
			loading = false;
		}
	}

	async function loadEnrolledClasses() {
		if (!alumno?.id) return;

		try {
			loadingClasses = true;

			// Use different endpoints based on who is viewing the profile
			if (isOwnProfile()) {
				// Student viewing their own profile - use the enrollment service
				enrolledClasses = await EnrollmentService.getMyEnrolledClasses();
			} else if (authStore.isAdmin || authStore.isProfesor) {
				// Admin or teacher viewing any student's profile - use the student API directly
				enrolledClasses = await alumnoApi.obtenerClasesAlumno({ id: alumno.id });
			}
		} catch (err) {
			console.error('Error loading enrolled classes:', err);
		} finally {
			loadingClasses = false;
		}
	}

	async function loadEntregas() {
		if (!alumno?.id) return;

		try {
			loadingEntregas = true;
			const response = await EntregaService.getEntregasByAlumno(alumno.id);
			entregas = response.content || [];
		} catch (err) {
			console.error('Error loading entregas:', err);
		} finally {
			loadingEntregas = false;
		}
	}

	function startEdit() {
		if (!alumno || !canEdit) return;

		editForm = {
			firstName: alumno.firstName || '',
			lastName: alumno.lastName || '',
			dni: alumno.dni || '',
			email: alumno.email || '',
			phoneNumber: alumno.phoneNumber || ''
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
		if (!alumno) return;

		saving = true;
		error = null;

		try {
			// Validar todos los campos que van a enviarse
			const validationErrors: string[] = [];

			// Solo incluir campos que no estén vacíos (PATCH parcial)
			const updateData: DTOActualizarAlumno = {};

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

			const updatedAlumno = await AlumnoService.updateAlumno(alumno.id!, updateData);
			alumno = updatedAlumno;
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

	async function toggleEnrollmentStatus() {
		if (!alumno || !canChangeStatus) return;

		try {
			console.log('Before toggle - enrolled:', alumno.enrolled);
			const updatedAlumno = await AlumnoService.changeEnrollmentStatus(
				alumno.id!,
				!alumno.enrolled
			);
			console.log('After toggle - updatedAlumno:', updatedAlumno);
			alumno = updatedAlumno;
			console.log('After assignment - alumno.enrolled:', alumno.enrolled);

			// Force a reload of the student data to ensure UI is updated
			await loadAlumno();

			successMessage = `Estado de matrícula ${updatedAlumno.enrolled ? 'activado' : 'desactivado'} correctamente`;
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
		}
	}

	async function toggleAccountStatus() {
		if (!alumno || !canChangeStatus) return;

		try {
			console.log('Before toggle - enabled:', alumno.enabled);
			const updatedAlumno = await AlumnoService.toggleEnabled(alumno.id!, !alumno.enabled);
			console.log('After toggle - updatedAlumno:', updatedAlumno);
			alumno = updatedAlumno;
			console.log('After assignment - alumno.enabled:', alumno.enabled);

			// Force a reload of the student data to ensure UI is updated
			await loadAlumno();

			successMessage = `Cuenta ${updatedAlumno.enabled ? 'habilitada' : 'deshabilitada'} correctamente`;
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

	function getEstadoColor(estado: string | undefined): string {
		switch (estado) {
			case 'PENDIENTE':
				return 'bg-yellow-100 text-yellow-800';
			case 'ENTREGADO':
				return 'bg-blue-100 text-blue-800';
			case 'CALIFICADO':
				return 'bg-green-100 text-green-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function getEstadoText(estado: string | undefined): string {
		switch (estado) {
			case 'PENDIENTE':
				return 'Pendiente';
			case 'ENTREGADO':
				return 'Entregado';
			case 'CALIFICADO':
				return 'Calificado';
			default:
				return 'Desconocido';
		}
	}

	// Check if current teacher is teaching a specific class
	function isTeacherOfClass(clase: DTOClase): boolean {
		if (!authStore.isProfesor || !authStore.user?.id || !clase.profesoresId) {
			return false;
		}

		// Check if current teacher's ID is in the class's professors array
		return clase.profesoresId.includes(authStore.user.id.toString());
	}
</script>

<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<div class="mx-auto max-w-4xl">
			<ProfileHeader
				title="Perfil de Alumno"
				subtitle="Información personal, clases inscritas y entregas"
				backUrl="/alumnos"
				backText="Volver a Alumnos"
				actions={[
					{
						label: 'Editar',
						onClick: startEdit,
						variant: 'primary',
						icon: '✏️'
					}
				]}
			/>

			<!-- Success/Error Messages -->
			{#if successMessage}
				<div
					class="mb-6 rounded-xl border border-green-200 bg-green-50 px-6 py-4 text-green-700 shadow-sm"
				>
					<div class="flex items-center">
						<svg class="mr-3 h-5 w-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
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

			{#if error}
				<div
					class="mb-6 rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700 shadow-sm"
				>
					<div class="flex items-center justify-between">
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
						<button
							onclick={() => (error = null)}
							class="text-red-500 transition-colors duration-200 hover:text-red-700"
							aria-label="Cerrar mensaje de error"
						>
							<svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
								<path
									fill-rule="evenodd"
									d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
									clip-rule="evenodd"
								></path>
							</svg>
						</button>
					</div>
				</div>
			{/if}

			<!-- Loading State -->
			{#if loading}
				<div class="py-16 text-center">
					<div
						class="mx-auto h-16 w-16 animate-spin rounded-full border-4 border-blue-200 -blue-600"
					></div>
					<p class="mt-6 text-lg font-medium text-gray-600">Cargando perfil del alumno...</p>
				</div>
			{:else if alumno}
				<div class="space-y-8">
					<!-- Personal Information -->
					<ProfileCard
						title={`${alumno.firstName} ${alumno.lastName}`}
						subtitle={`@${alumno.username}`}
						actions={canEdit() && !editMode
							? [
									{
										label: 'Editar Datos',
										onClick: startEdit,
										variant: 'primary',
										icon: '✏️'
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
								<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
									<!-- NOMBRE -->
									<div>
										<label for="nombre" class="mb-1 block text-sm font-medium text-gray-700">
											Nombre <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="firstName"
												type="text"
												bind:value={editForm.firstName}
												maxlength="100"
												class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.firstName
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
										<p class="mt-1 text-xs text-gray-500">
											Solo letras, acentos y espacios. Máximo 100 caracteres.
										</p>
									</div>

									<!-- APELLIDOS -->
									<div>
										<label for="apellidos" class="mb-1 block text-sm font-medium text-gray-700">
											Apellidos <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="lastName"
												type="text"
												bind:value={editForm.lastName}
												maxlength="100"
												class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.lastName
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
										<p class="mt-1 text-xs text-gray-500">
											Solo letras, acentos y espacios. Máximo 100 caracteres.
										</p>
									</div>

									<!-- DNI -->
									<div>
										<label for="dni" class="mb-1 block text-sm font-medium text-gray-700">
											DNI <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="dni"
												type="text"
												bind:value={editForm.dni}
												maxlength="9"
												class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.dni
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
										<p class="mt-1 text-xs text-gray-500">
											8 números seguidos de 1 letra. Ej: 12345678Z
										</p>
									</div>

									<!-- EMAIL -->
									<div>
										<label for="email" class="mb-1 block text-sm font-medium text-gray-700">
											Email <span class="text-red-500">*</span>
										</label>
										<div class="relative">
											<input
												id="email"
												type="email"
												bind:value={editForm.email}
												maxlength="254"
												class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.email
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
										<p class="mt-1 text-xs text-gray-500">
											Máximo 254 caracteres. Parte local máximo 64 caracteres.
										</p>
									</div>

									<!-- TELÉFONO -->
									<div class="md:col-span-2">
										<label for="phoneNumber" class="mb-1 block text-sm font-medium text-gray-700">
											Teléfono <span class="text-gray-400">(Opcional)</span>
										</label>
										<div class="relative">
											<input
												id="phoneNumber"
												type="tel"
												bind:value={editForm.phoneNumber}
												class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.phoneNumber
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
										<p class="mt-1 text-xs text-gray-500">
											6-14 dígitos. Permitidos: números, espacios, guiones, puntos, paréntesis y +
										</p>
									</div>
								</div>

								<div class="flex justify-end space-x-4  pt-8">
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
										class="rounded-lg px-6 py-3 font-semibold transition-all duration-200 {isFormValid() &&
										!saving
											? 'transform bg-gradient-to-r from-blue-600 to-indigo-600 text-white hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg'
											: 'cursor-not-allowed bg-gray-400 text-gray-200'}"
									>
										{saving
											? '🔄 Guardando...'
											: isFormValid()
												? '✓ Guardar Cambios'
												: '⚠️ Corregir Errores'}
									</button>
								</div>

								<!-- Form Validation Summary -->
								{#if hasFormErrors()}
									<div class="mt-4 rounded-md border border-yellow-200 bg-yellow-50 p-3">
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
							<div class="grid gap-6 md:grid-cols-2">
								<div>
									<h3 class="mb-4 text-lg font-semibold text-gray-900">Información Personal</h3>
									<div class="space-y-4">
										<div>
											<div class="text-sm font-medium text-gray-500">Nombre Completo</div>
											<p class="font-medium text-gray-900">{alumno.firstName} {alumno.lastName}</p>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">DNI</div>
											<p class="text-gray-900">{alumno.dni}</p>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">Email</div>
											<p class="text-gray-900">{alumno.email}</p>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">Teléfono</div>
											<p class="text-gray-900">{alumno.phoneNumber || 'No especificado'}</p>
										</div>
									</div>
								</div>

								<div>
									<h3 class="mb-4 text-lg font-semibold text-gray-900">Información Académica</h3>
									<div class="space-y-4">
										<div>
											<div class="text-sm font-medium text-gray-500">Usuario</div>
											<p class="text-gray-900">@{alumno.username}</p>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">Fecha de Inscripción</div>
											<p class="text-gray-900">{formatDate(alumno.enrollmentDate)}</p>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">Estado de Matrícula</div>
											<span
												class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enrolled
													? 'bg-green-100 text-green-800'
													: 'bg-yellow-100 text-yellow-800'}"
											>
												{alumno.enrolled ? 'Matriculado' : 'No Matriculado'}
											</span>
										</div>
										<div>
											<div class="text-sm font-medium text-gray-500">Estado de Cuenta</div>
											<span
												class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
													? 'bg-blue-100 text-blue-800'
													: 'bg-red-100 text-red-800'}"
											>
												{alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
											</span>
										</div>
									</div>
								</div>
							</div>

							<!-- Admin Actions -->
							{#if canChangeStatus()}
								<div class=" pt-6">
									<h3 class="mb-4 text-lg font-semibold text-gray-900">
										Acciones de Administrador
									</h3>
									<div class="flex gap-4">
										<button
											onclick={toggleEnrollmentStatus}
											class="transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
										>
											{alumno.enrolled ? 'Desmatricular' : 'Matricular'} Alumno
										</button>

										<button
											onclick={toggleAccountStatus}
											class="transform rounded-lg px-6 py-3 font-semibold transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg {alumno.enabled
												? 'bg-gradient-to-r from-red-600 to-pink-600 text-white hover:from-red-700 hover:to-pink-700'
												: 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white hover:from-blue-700 hover:to-indigo-700'}"
										>
											{alumno.enabled ? 'Deshabilitar' : 'Habilitar'} Cuenta
										</button>
									</div>
								</div>
							{/if}
						{/if}
					</ProfileCard>

					<!-- Enrolled Classes Section -->
					{#if canViewEnrolledClasses()}
						<ProfileCard
							title={isOwnProfile()
								? 'Mis Clases Inscritas'
								: authStore.isProfesor
									? 'Clases Inscritas del Alumno'
									: 'Clases Inscritas'}
							subtitle={`${enrolledClasses.length} clase${enrolledClasses.length !== 1 ? 's' : ''} inscrita${enrolledClasses.length !== 1 ? 's' : ''}`}
							loading={loadingClasses}
							empty={enrolledClasses.length === 0}
							emptyIcon="📚"
							emptyTitle={isOwnProfile()
								? 'No tienes clases inscritas'
								: 'El alumno no tiene clases inscritas'}
							emptyDescription={isOwnProfile()
								? 'Explora nuestras clases disponibles y comienza tu aprendizaje.'
								: 'El alumno aún no se ha inscrito en ninguna clase.'}
							emptyAction={isOwnProfile()
								? {
										label: 'Explorar Clases',
										onClick: () => goto('/clases')
									}
								: null}
							actions={isOwnProfile()
								? [
										{
											label: 'Explorar Clases',
											onClick: () => goto('/clases'),
											variant: 'primary'
										}
									]
								: []}
						>
							{#if !loadingClasses && enrolledClasses.length > 0}
								<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
									{#each enrolledClasses as clase (clase.id)}
										<div
											class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md"
										>
											<div class="mb-4">
												<h3 class="mb-2 font-semibold text-gray-900">{clase.titulo}</h3>
												<p class="line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>
											</div>

											<div class="mb-4 space-y-3">
												<div class="flex items-center justify-between">
													<span class="text-sm text-gray-500">Precio:</span>
													<span class="font-semibold text-green-600">{clase.precio}€</span>
												</div>
												<div class="flex items-center justify-between">
													<span class="text-sm text-gray-500">Nivel:</span>
													<span
														class="inline-flex rounded-full bg-blue-100 px-2 py-1 text-xs font-semibold text-blue-800"
													>
														{clase.nivel}
													</span>
												</div>
												<div class="flex items-center justify-between">
													<span class="text-sm text-gray-500">Modalidad:</span>
													<span
														class="inline-flex rounded-full bg-purple-100 px-2 py-1 text-xs font-semibold text-purple-800"
													>
														{clase.presencialidad}
													</span>
												</div>
											</div>

											{#if !authStore.isProfesor || isTeacherOfClass(clase)}
												<a
													href="/clases/{clase.id}"
													class="block w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
												>
													Ver Clase
												</a>
											{/if}
										</div>
									{/each}
								</div>
							{/if}
						</ProfileCard>
					{/if}

					<!-- Entregas Section -->
					{#if canViewDeliveries()}
						<ProfileCard
							title={isOwnProfile() ? 'Mis Entregas' : 'Entregas del Alumno'}
							subtitle={`${entregas.length} entrega${entregas.length !== 1 ? 's' : ''}`}
							loading={loadingEntregas}
							empty={entregas.length === 0}
							emptyIcon="📝"
							emptyTitle={isOwnProfile()
								? 'No tienes entregas registradas'
								: 'El alumno no tiene entregas registradas'}
							emptyDescription={isOwnProfile()
								? 'Comienza a trabajar en los ejercicios disponibles.'
								: 'El alumno aún no ha realizado ninguna entrega.'}
							emptyAction={isOwnProfile()
								? {
										label: 'Ver Ejercicios',
										onClick: () => goto('/ejercicios')
									}
								: null}
							actions={isOwnProfile()
								? [
										{
											label: 'Ver Ejercicios',
											onClick: () => goto('/ejercicios'),
											variant: 'primary'
										}
									]
								: []}
						>
							{#if !loadingEntregas && entregas.length > 0}
								<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
									{#each entregas as entrega (entrega.id)}
										<div
											class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md"
										>
											<div class="mb-4">
												<div class="mb-2 flex items-center justify-between">
													<h3 class="font-semibold text-gray-900">
														Ejercicio #{entrega.ejercicioId}
													</h3>
													<span
														class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {getEstadoColor(
															entrega.estado
														)}"
													>
														{getEstadoText(entrega.estado)}
													</span>
												</div>

												{#if entrega.fechaEntrega}
													<p class="text-sm text-gray-600">
														Entregado: {formatDate(entrega.fechaEntrega)}
													</p>
												{/if}

												{#if entrega.nota !== undefined && entrega.nota !== null}
													<p class="text-sm text-gray-600">
														Nota: <span class="font-medium text-green-600">{entrega.nota}/10</span>
														{#if entrega.notaFormateada}
															<span class="text-gray-500">({entrega.notaFormateada})</span>
														{/if}
													</p>
												{/if}

												{#if entrega.numeroArchivos && entrega.numeroArchivos > 0}
													<p class="text-sm text-gray-600">
														📎 {entrega.numeroArchivos} archivo{entrega.numeroArchivos !== 1
															? 's'
															: ''}
													</p>
												{/if}

												{#if entrega.comentarios}
													<p class="mt-2 text-sm text-gray-600">
														💬 {entrega.comentarios}
													</p>
												{/if}
											</div>

											<div class="flex gap-3">
												<a
													href="/entregas/{entrega.id}"
													class="flex-1 transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
												>
													Ver Detalles
												</a>

												{#if entrega.ejercicioId}
													<a
														href="/ejercicios/{entrega.ejercicioId}"
														class="flex-1 transform rounded-lg bg-gray-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-gray-700 hover:shadow-lg"
													>
														Ver Ejercicio
													</a>
												{/if}
											</div>
										</div>
									{/each}
								</div>

								<!-- Summary Stats -->
								<div
									class="mt-8 grid grid-cols-3 gap-6 rounded-xl border border-blue-200/50 bg-gradient-to-r from-blue-50 to-indigo-50 p-6"
								>
									<div class="text-center">
										<div class="text-2xl font-bold text-gray-900">{entregas.length}</div>
										<div class="text-sm text-gray-600">Total Entregas</div>
									</div>
									<div class="text-center">
										<div class="text-2xl font-bold text-yellow-600">
											{entregas.filter((e) => e.estado === 'PENDIENTE').length}
										</div>
										<div class="text-sm text-gray-600">Pendientes</div>
									</div>
									<div class="text-center">
										<div class="text-2xl font-bold text-green-600">
											{entregas.filter((e) => e.estado === 'CALIFICADO').length}
										</div>
										<div class="text-sm text-gray-600">Calificadas</div>
									</div>
								</div>
							{/if}
						</ProfileCard>
					{/if}
				</div>
			{:else if !loading}
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
						<p class="mb-6 text-lg font-medium text-gray-600">Alumno no encontrado</p>
						<button
							onclick={() => goto('/alumnos')}
							class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
						>
							Volver a Alumnos
						</button>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
