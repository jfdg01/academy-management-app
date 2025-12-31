<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOCrearAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// Form state
	let formData: DTOCrearAlumno = $state({
		username: '',
		password: '',
		firstName: '',
		lastName: '',
		dni: '',
		email: '',
		phoneNumber: ''
	});

	// UI state
	let loading = $state(false);
	let errors: string[] = $state([]);
	let fieldErrors: Record<string, string> = $state({});
	let showPassword = $state(false);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isAdmin) {
			goto('/alumnos');
			return;
		}
	});

	// Form validation
	function validateForm(): boolean {
		errors = [];
		fieldErrors = {};

		// Client-side validation - basic checks
		const validationErrors: string[] = [];

		if (!formData.username || formData.username.length < 3) {
			validationErrors.push('El usuario debe tener al menos 3 caracteres');
		}
		if (!formData.password || formData.password.length < 6) {
			validationErrors.push('La contraseña debe tener al menos 6 caracteres');
		}
		if (!formData.firstName || formData.firstName.length < 2) {
			validationErrors.push('El nombre es obligatorio');
		}
		if (!formData.lastName || formData.lastName.length < 2) {
			validationErrors.push('Los apellidos son obligatorios');
		}
		if (!formData.dni || formData.dni.length < 5) {
			validationErrors.push('El DNI es obligatorio');
		}
		if (!formData.email || !formData.email.includes('@')) {
			validationErrors.push('El email debe ser válido');
		}

		if (validationErrors.length > 0) {
			errors = validationErrors;
			return false;
		}

		// Additional custom validations
		if (formData.phoneNumber && !/^\+?[\d\s-()]+$/.test(formData.phoneNumber)) {
			fieldErrors.phoneNumber = 'El formato del teléfono no es válido';
			return false;
		}

		return true;
	}

	async function handleSubmit() {
		if (!validateForm()) {
			return;
		}

		loading = true;
		errors = [];
		fieldErrors = {};

		try {
			const newAlumno = await AlumnoService.createAlumno(formData);

			// Redirect to the new student's profile
			goto(`/alumnos/${newAlumno.id}?created=true`);
		} catch (error: unknown) {
			loading = false;

			// Handle specific field errors from backend
			const errorMessage = error instanceof Error ? error.message : String(error);
			if (errorMessage.includes('usuario')) {
				fieldErrors.usuario = 'El nombre de usuario ya existe';
			} else if (errorMessage.includes('email')) {
				fieldErrors.email = 'El email ya está registrado';
			} else if (errorMessage.includes('dni')) {
				fieldErrors.dni = 'El DNI ya está registrado';
			} else {
				errors = [errorMessage];
			}
		}
	}

	function resetForm() {
		formData = {
			username: '',
			password: '',
			firstName: '',
			lastName: '',
			dni: '',
			email: '',
			phoneNumber: ''
		};
		errors = [];
		fieldErrors = {};
	}

	// Real-time field validation
	function validateField(field: keyof DTOCrearAlumno, value: string) {
		delete fieldErrors[field];

		switch (field) {
			case 'username':
				if (value && (value.length < 3 || value.length > 50)) {
					fieldErrors.username = 'El usuario debe tener entre 3 y 50 caracteres';
				}
				break;
			case 'password':
				if (value && value.length < 6) {
					fieldErrors.password = 'La contraseña debe tener al menos 6 caracteres';
				}
				break;
			case 'email':
				if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
					fieldErrors.email = 'El formato del email no es válido';
				}
				break;
			case 'dni':
				if (value && value.length > 20) {
					fieldErrors.dni = 'El DNI no puede superar 20 caracteres';
				}
				break;
			case 'firstName':
				if (value && value.length > 100) {
					fieldErrors.firstName = 'El nombre no puede superar 100 caracteres';
				}
				break;
			case 'lastName':
				if (value && value.length > 100) {
					fieldErrors.lastName = 'Los apellidos no pueden superar 100 caracteres';
				}
				break;
			case 'phoneNumber':
				if (value && value.length > 15) {
					fieldErrors.phoneNumber = 'El teléfono no puede superar 15 caracteres';
				}
				break;
		}
	}
</script>

<div class="container mx-auto max-w-2xl px-4 py-8">
	<!-- Header -->
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Nuevo Alumno</h1>
		<button onclick={() => goto('/alumnos')} class="text-gray-600 hover:text-gray-800">
			← Volver a Alumnos
		</button>
	</div>

	<!-- Error Messages -->
	{#if errors.length > 0}
		<div class="mb-6 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			<ul class="list-inside list-disc">
				{#each errors as error (error)}
					<li>{error}</li>
				{/each}
			</ul>
		</div>
	{/if}

	<!-- Registration Form -->
	<form
		onsubmit={(e) => {
			e.preventDefault();
			handleSubmit();
		}}
		class="rounded-lg bg-white p-6 shadow-md"
	>
		<!-- Account Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de Cuenta</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- Username -->
				<div>
					<label for="usuario" class="mb-1 block text-sm font-medium text-gray-700">
						Usuario *
					</label>
					<input
						id="username"
						type="text"
						bind:value={formData.username}
						oninput={(e) => validateField('username', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.username
							? 'border-red-500'
							: ''}"
						placeholder="Nombre de usuario único"
					/>
					{#if fieldErrors.username}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.username}</p>
					{/if}
				</div>

				<!-- Password -->
				<div>
					<label for="contraseña" class="mb-1 block text-sm font-medium text-gray-700">
						Contraseña *
					</label>
					<div class="relative">
						<input
							id="contraseña"
							type={showPassword ? 'text' : 'password'}
							bind:value={formData.password}
							oninput={(e) => validateField('password', (e.target as HTMLInputElement).value)}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.password
								? 'border-red-500'
								: ''}"
							placeholder="Mínimo 6 caracteres"
						/>
						<button
							type="button"
							onclick={() => (showPassword = !showPassword)}
							class="absolute inset-y-0 right-0 flex items-center pr-3 text-sm leading-5"
						>
							{showPassword ? '🙈' : '👁️'}
						</button>
					</div>
					{#if fieldErrors.password}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.password}</p>
					{/if}
				</div>
			</div>
		</div>

		<!-- Personal Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información Personal</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- First Name -->
				<div>
					<label for="nombre" class="mb-1 block text-sm font-medium text-gray-700">
						Nombre *
					</label>
					<input
						id="firstName"
						type="text"
						bind:value={formData.firstName}
						oninput={(e) => validateField('firstName', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.firstName
							? 'border-red-500'
							: ''}"
						placeholder="Nombre del alumno"
					/>
					{#if fieldErrors.firstName}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.firstName}</p>
					{/if}
				</div>

				<!-- Last Name -->
				<div>
					<label for="apellidos" class="mb-1 block text-sm font-medium text-gray-700">
						Apellidos *
					</label>
					<input
						id="lastName"
						type="text"
						bind:value={formData.lastName}
						oninput={(e) => validateField('lastName', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.lastName
							? 'border-red-500'
							: ''}"
						placeholder="Apellidos del alumno"
					/>
					{#if fieldErrors.lastName}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.lastName}</p>
					{/if}
				</div>

				<!-- DNI -->
				<div>
					<label for="dni" class="mb-1 block text-sm font-medium text-gray-700"> DNI * </label>
					<input
						id="dni"
						type="text"
						bind:value={formData.dni}
						oninput={(e) => validateField('dni', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.dni
							? 'border-red-500'
							: ''}"
						placeholder="Documento de identidad"
					/>
					{#if fieldErrors.dni}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.dni}</p>
					{/if}
				</div>

				<!-- Email -->
				<div>
					<label for="email" class="mb-1 block text-sm font-medium text-gray-700"> Email * </label>
					<input
						id="email"
						type="email"
						bind:value={formData.email}
						oninput={(e) => validateField('email', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.email
							? 'border-red-500'
							: ''}"
						placeholder="correo@ejemplo.com"
					/>
					{#if fieldErrors.email}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.email}</p>
					{/if}
				</div>
			</div>
		</div>

		<!-- Contact Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de Contacto</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- Phone -->
				<div>
					<label for="telefono" class="mb-1 block text-sm font-medium text-gray-700">
						Número de Teléfono
					</label>
					<input
						id="phoneNumber"
						type="tel"
						bind:value={formData.phoneNumber}
						oninput={(e) => validateField('phoneNumber', (e.target as HTMLInputElement).value)}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.phoneNumber
							? 'border-red-500'
							: ''}"
						placeholder="+34 XXX XXX XXX (opcional)"
					/>
					{#if fieldErrors.phoneNumber}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.phoneNumber}</p>
					{/if}
					<p class="mt-1 text-xs text-gray-500">Campo opcional</p>
				</div>
			</div>
		</div>

		<!-- Form Actions -->
		<div class="flex justify-between space-x-4  border-gray-200 pt-6">
			<div class="flex space-x-2">
				<button
					type="button"
					onclick={resetForm}
					class="rounded-md bg-gray-500 px-4 py-2 text-white hover:bg-gray-600 focus:ring-2 focus:ring-gray-500 focus:outline-none"
				>
					Limpiar Formulario
				</button>
				<button
					type="button"
					onclick={() => goto('/alumnos')}
					class="rounded-md bg-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-400 focus:ring-2 focus:ring-gray-500 focus:outline-none"
				>
					Cancelar
				</button>
			</div>

			<button
				type="submit"
				disabled={loading || Object.keys(fieldErrors).length > 0}
				class="rounded-md bg-blue-600 px-6 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				{#if loading}
					<span class="flex items-center">
						<svg
							class="mr-3 -ml-1 h-5 w-5 animate-spin text-white"
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
						>
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
						Creando Alumno...
					</span>
				{:else}
					Crear Alumno
				{/if}
			</button>
		</div>
	</form>

	<!-- Form Help -->
	<div class="mt-6 rounded-lg border border-blue-200 bg-blue-50 p-4">
		<h3 class="mb-2 text-sm font-medium text-blue-900">Información sobre el formulario:</h3>
		<ul class="space-y-1 text-sm text-blue-700">
			<li>• Los campos marcados con (*) son obligatorios</li>
			<li>• El usuario debe ser único en el sistema</li>
			<li>• El email y DNI también deben ser únicos</li>
			<li>• La contraseña debe tener al menos 6 caracteres</li>
			<li>• El alumno se creará con estado habilitado y no matriculado por defecto</li>
		</ul>
	</div>
</div>
