<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { ClaseService } from '$lib/services/claseService';
	import type { DTOClase } from '$lib/generated/api';

	// State
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let availableClasses = $state<DTOClase[]>([]);

	// Form state
	let form = $state({
		nombre: '',
		enunciado: '',
		fechaInicioPlazo: '',
		fechaFinalPlazo: '',
		claseId: '',
		duracionEnHoras: ''
	});

	// Get class ID from URL params if provided
	const classIdFromUrl = $derived($page.url.searchParams.get('classId'));

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isProfesor && !authStore.isAdmin) {
			goto('/ejercicios');
			return;
		}
	});

	// Load available classes
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadAvailableClasses();
		}
	});

	// Set class ID from URL if provided
	$effect(() => {
		if (classIdFromUrl && availableClasses.length > 0) {
			form.claseId = classIdFromUrl;
		}
	});

	async function loadAvailableClasses() {
		loading = true;
		error = null;

		try {
			if (authStore.isProfesor) {
				availableClasses = await ClaseService.getMyClasses();
			} else if (authStore.isAdmin) {
				// For admins, load all classes
				const response = await ClaseService.getClases({ page: 0, size: 100 });
				availableClasses = response.content || [];
			}
		} catch (err) {
			console.error('Error loading classes:', err);
			error = 'Error al cargar las clases disponibles.';
		} finally {
			loading = false;
		}
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!validateForm()) {
			return;
		}

		saving = true;
		error = null;
		successMessage = null;

		try {
			const ejercicioData = {
				nombre: form.nombre.trim(),
				enunciado: form.enunciado.trim(),
				fechaInicioPlazo: new Date(form.fechaInicioPlazo),
				fechaFinalPlazo: new Date(form.fechaFinalPlazo),
				claseId: parseInt(form.claseId),
				duracionEnHoras: form.duracionEnHoras ? parseInt(form.duracionEnHoras) : undefined
			};

			await EjercicioService.createEjercicio(ejercicioData);

			successMessage = 'Ejercicio creado exitosamente';

			// Redirect back to the class or exercises list
			setTimeout(() => {
				if (form.claseId) {
					goto(`/profesores/mis-clases?selectedClass=${form.claseId}`);
				} else {
					goto('/ejercicios');
				}
			}, 2000);
		} catch (err) {
			console.error('Error creating exercise:', err);
			error = 'Error al crear el ejercicio. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	function validateForm(): boolean {
		if (!form.nombre.trim()) {
			error = 'El nombre del ejercicio es obligatorio';
			return false;
		}

		if (!form.enunciado.trim()) {
			error = 'El enunciado del ejercicio es obligatorio';
			return false;
		}

		if (!form.fechaInicioPlazo) {
			error = 'La fecha de inicio del plazo es obligatoria';
			return false;
		}

		if (!form.fechaFinalPlazo) {
			error = 'La fecha final del plazo es obligatoria';
			return false;
		}

		if (!form.claseId) {
			error = 'Debes seleccionar una clase';
			return false;
		}

		const startDate = new Date(form.fechaInicioPlazo);
		const endDate = new Date(form.fechaFinalPlazo);

		if (startDate >= endDate) {
			error = 'La fecha final del plazo debe ser posterior a la fecha de inicio';
			return false;
		}

		if (startDate < new Date()) {
			error = 'La fecha de inicio del plazo no puede ser anterior a hoy';
			return false;
		}

		if (
			form.duracionEnHoras &&
			(parseInt(form.duracionEnHoras) <= 0 || parseInt(form.duracionEnHoras) > 168)
		) {
			error = 'La duración debe estar entre 1 y 168 horas (1 semana)';
			return false;
		}

		return true;
	}

	function handleCancel() {
		if (form.claseId) {
			goto(`/profesores/mis-clases?selectedClass=${form.claseId}`);
		} else {
			goto('/ejercicios');
		}
	}

	// Set minimum date for date inputs
	const today = new Date().toISOString().slice(0, 16);
</script>

<svelte:head>
	<title>Crear Nuevo Ejercicio - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mx-auto max-w-3xl">
		<!-- Header -->
		<div class="mb-8">
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold text-gray-900">Crear Nuevo Ejercicio</h1>
					<p class="mt-2 text-gray-600">Define un nuevo ejercicio para tus estudiantes</p>
				</div>
				<button
					onclick={handleCancel}
					class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
				>
					Cancelar
				</button>
			</div>
		</div>

		{#if error}
			<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				{error}
			</div>
		{/if}

		{#if successMessage}
			<div class="mb-6 rounded-lg border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else}
			<!-- Exercise Creation Form -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-lg">
				<form onsubmit={handleSubmit} class="space-y-6">
					<!-- Exercise Name -->
					<div>
						<label for="nombre" class="block text-sm font-medium text-gray-700">
							Nombre del Ejercicio *
						</label>
						<input
							type="text"
							id="nombre"
							bind:value={form.nombre}
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: Ejercicio de Álgebra Básica"
							required
						/>
					</div>

					<!-- Exercise Statement -->
					<div>
						<label for="enunciado" class="block text-sm font-medium text-gray-700">
							Enunciado del Ejercicio *
						</label>
						<textarea
							id="enunciado"
							bind:value={form.enunciado}
							rows="6"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							placeholder="Describe detalladamente el ejercicio que deben realizar los estudiantes..."
							required
						></textarea>
					</div>

					<!-- Class Selection -->
					<div>
						<label for="claseId" class="block text-sm font-medium text-gray-700">
							Clase Asignada *
						</label>
						<select
							id="claseId"
							bind:value={form.claseId}
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							required
						>
							<option value="">Selecciona una clase...</option>
							{#each availableClasses as clase (clase.id)}
								<option value={clase.id}>{clase.titulo}</option>
							{/each}
						</select>
					</div>

					<!-- Date Range -->
					<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
						<div>
							<label for="fechaInicioPlazo" class="block text-sm font-medium text-gray-700">
								Fecha de Inicio del Plazo *
							</label>
							<input
								type="datetime-local"
								id="fechaInicioPlazo"
								bind:value={form.fechaInicioPlazo}
								min={today}
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								required
							/>
						</div>
						<div>
							<label for="fechaFinalPlazo" class="block text-sm font-medium text-gray-700">
								Fecha Final del Plazo *
							</label>
							<input
								type="datetime-local"
								id="fechaFinalPlazo"
								bind:value={form.fechaFinalPlazo}
								min={form.fechaInicioPlazo || today}
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								required
							/>
						</div>
					</div>

					<!-- Duration (Optional) -->
					<div>
						<label for="duracionEnHoras" class="block text-sm font-medium text-gray-700">
							Duración Estimada (horas) - Opcional
						</label>
						<input
							type="number"
							id="duracionEnHoras"
							bind:value={form.duracionEnHoras}
							min="1"
							max="168"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: 4 (para 4 horas)"
						/>
						<p class="mt-1 text-xs text-gray-500">
							Estima cuánto tiempo debería tomar a un estudiante completar este ejercicio
						</p>
					</div>

					<!-- Form Actions -->
					<div class="flex justify-end space-x-3 pt-6">
						<button
							type="button"
							onclick={handleCancel}
							class="rounded-lg bg-gray-600 px-6 py-2 font-semibold text-white hover:bg-gray-700"
							disabled={saving}
						>
							Cancelar
						</button>
						<button
							type="submit"
							class="rounded-lg bg-blue-600 px-6 py-2 font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
							disabled={saving}
						>
							{saving ? 'Creando...' : 'Crear Ejercicio'}
						</button>
					</div>
				</form>
			</div>
		{/if}
	</div>
</div>
