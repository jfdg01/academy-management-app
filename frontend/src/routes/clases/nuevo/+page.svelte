<script lang="ts">
	import { goto } from '$app/navigation';
	import type {
		DTOPeticionCrearCurso,
		DTOPeticionCrearCursoNivelEnum,
		DTOPeticionCrearCursoPresencialidadEnum,
		DTOPeticionCrearTaller,
		DTOPeticionCrearTallerNivelEnum,
		DTOPeticionCrearTallerPresencialidadEnum,
		DTOProfesor
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { ProfesorService } from '$lib/services/profesorService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);

	// Form data
	let tipoClase = $state<'curso' | 'taller'>('curso');
	let titulo = $state('');
	let descripcion = $state('');
	let precio = $state<number>(25);
	let presencialidad = $state<
		DTOPeticionCrearCursoPresencialidadEnum | DTOPeticionCrearTallerPresencialidadEnum
	>('ONLINE');
	let nivel = $state<DTOPeticionCrearCursoNivelEnum | DTOPeticionCrearTallerNivelEnum>(
		'PRINCIPIANTE'
	);
	let imagenPortada = $state('');
	let fechaInicio = $state('');
	let fechaFin = $state('');
	let duracionHoras = $state<number>(2);
	let fechaRealizacion = $state('');
	let horaComienzo = $state('');
	let profesoresSeleccionados = $state<number[]>([]);

	// Available professors
	let profesoresDisponibles = $state<DTOProfesor[]>([]);
	let loadingProfesores = $state(false);

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

	// Load available professors
	async function cargarProfesores() {
		loadingProfesores = true;
		try {
			profesoresDisponibles = await ProfesorService.getAllProfesores({ enabled: true });
		} catch (err) {
			error = `Error al cargar profesores: ${err}`;
			console.error('Error loading professors:', err);
		} finally {
			loadingProfesores = false;
		}
	}

	// Load professors when component mounts
	$effect(() => {
		if (authStore.isAuthenticated && (authStore.isAdmin || authStore.isProfesor)) {
			cargarProfesores();
		}
	});

	// Toggle professor selection
	function toggleProfesor(profesorId: number) {
		if (profesoresSeleccionados.includes(profesorId)) {
			profesoresSeleccionados = profesoresSeleccionados.filter((id) => id !== profesorId);
		} else {
			profesoresSeleccionados = [...profesoresSeleccionados, profesorId];
		}
	}

	// Form validation
	function validarFormulario(): boolean {
		if (!titulo.trim()) {
			error = 'El título es obligatorio';
			return false;
		}
		if (precio <= 0) {
			error = 'El precio debe ser mayor a 0';
			return false;
		}

		if (tipoClase === 'curso') {
			if (!fechaInicio) {
				error = 'La fecha de inicio es obligatoria';
				return false;
			}
			if (!fechaFin) {
				error = 'La fecha de fin es obligatoria';
				return false;
			}
			if (new Date(fechaInicio) >= new Date(fechaFin)) {
				error = 'La fecha de fin debe ser posterior a la fecha de inicio';
				return false;
			}
		} else {
			if (!fechaRealizacion) {
				error = 'La fecha de realización es obligatoria';
				return false;
			}
			if (!horaComienzo) {
				error = 'La hora de comienzo es obligatoria';
				return false;
			}
			if (duracionHoras <= 0) {
				error = 'La duración debe ser mayor a 0';
				return false;
			}
		}
		return true;
	}

	// Submit form
	async function submitForm() {
		if (!validarFormulario()) return;

		loading = true;
		error = null;

		try {
			if (tipoClase === 'curso') {
				const cursoData: DTOPeticionCrearCurso = {
					titulo: titulo.trim(),
					descripcion: descripcion.trim() || undefined,
					precio,
					presencialidad: presencialidad as DTOPeticionCrearCursoPresencialidadEnum,
					nivel: nivel as DTOPeticionCrearCursoNivelEnum,
					fechaInicio: new Date(fechaInicio),
					fechaFin: new Date(fechaFin),
					imagenPortada: imagenPortada.trim() || undefined,
					profesoresId: profesoresSeleccionados.length > 0 ? profesoresSeleccionados : undefined
				};

				await ClaseService.crearCurso(cursoData);
			} else {
				const tallerData: DTOPeticionCrearTaller = {
					titulo: titulo.trim(),
					descripcion: descripcion.trim() || undefined,
					precio,
					presencialidad: presencialidad as DTOPeticionCrearTallerPresencialidadEnum,
					nivel: nivel as DTOPeticionCrearTallerNivelEnum,
					duracionHoras,
					fechaRealizacion: new Date(fechaRealizacion),
					horaComienzo: horaComienzo,
					imagenPortada: imagenPortada.trim() || undefined,
					profesoresId: profesoresSeleccionados.length > 0 ? profesoresSeleccionados : undefined
				};

				await ClaseService.crearTaller(tallerData);
			}

			successMessage = `${tipoClase === 'curso' ? 'Curso' : 'Taller'} creado correctamente`;
			setTimeout(() => {
				goto('/clases');
			}, 2000);
		} catch (err) {
			error = `Error al crear ${tipoClase}: ${err}`;
			console.error('Error creating class:', err);
		} finally {
			loading = false;
		}
	}

	// Reset form
	function resetForm() {
		titulo = '';
		descripcion = '';
		precio = 25;
		presencialidad = 'ONLINE';
		nivel = 'PRINCIPIANTE';
		imagenPortada = '';
		fechaInicio = '';
		fechaFin = '';
		duracionHoras = 2;
		fechaRealizacion = '';
		horaComienzo = '';
		profesoresSeleccionados = [];
		error = null;
		successMessage = null;
	}
</script>

<svelte:head>
	<title>Nueva Clase - Academia</title>
</svelte:head>

<!-- Main Container with Gradient Background -->
<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8 lg:py-16">
		<div class="mx-auto max-w-4xl">
			<!-- Header Section -->
			<div class="mb-8 sm:mb-12">
				<div
					class="mb-6 flex flex-col gap-4 sm:mb-8 sm:flex-row sm:items-center sm:justify-between"
				>
					<div>
						<h1
							class="montserrat-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-bold text-transparent sm:text-3xl lg:text-4xl"
						>
							Crear Nueva Clase
						</h1>
						<p class="mt-2 text-sm text-gray-600 sm:text-base">
							Completa el formulario para crear un nuevo curso o taller
						</p>
					</div>
					<button
						onclick={() => goto('/clases')}
						class="inline-flex items-center rounded-lg px-4 py-2 text-sm font-medium text-blue-600 transition-all duration-200 hover:bg-blue-50 hover:text-blue-700"
					>
						<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M15 19l-7-7 7-7"
							></path>
						</svg>
						Volver a Clases
					</button>
				</div>
			</div>

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

			<!-- Form Card with Glass Morphism -->
			<form
				onsubmit={(e) => {
					e.preventDefault();
					submitForm();
				}}
				class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl sm:p-8"
			>
				<!-- Class Type Selection -->
				<div class="mb-8">
					<fieldset>
						<legend class="montserrat-semibold mb-4 block text-lg font-semibold text-gray-900"
							>Tipo de Clase</legend
						>
						<div class="flex flex-col gap-4 sm:flex-row">
							<label
								class="flex cursor-pointer items-center rounded-lg border-2 border-gray-200 p-4 transition-all duration-200 hover:border-blue-300 {tipoClase ===
								'curso'
									? 'border-blue-500 bg-blue-50'
									: ''}"
							>
								<input
									type="radio"
									bind:group={tipoClase}
									value="curso"
									class="mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500"
								/>
								<div>
									<div class="font-medium text-gray-900">Curso</div>
									<div class="text-sm text-gray-600">Programa de formación continuada</div>
								</div>
							</label>
							<label
								class="flex cursor-pointer items-center rounded-lg border-2 border-gray-200 p-4 transition-all duration-200 hover:border-blue-300 {tipoClase ===
								'taller'
									? 'border-blue-500 bg-blue-50'
									: ''}"
							>
								<input
									type="radio"
									bind:group={tipoClase}
									value="taller"
									class="mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500"
								/>
								<div>
									<div class="font-medium text-gray-900">Taller</div>
									<div class="text-sm text-gray-600">Sesión práctica intensiva</div>
								</div>
							</label>
						</div>
					</fieldset>
				</div>

				<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
					<!-- Basic Information -->
					<div class="space-y-6">
						<div>
							<label for="titulo" class="mb-2 block text-sm font-medium text-gray-700">
								Título *
							</label>
							<input
								id="titulo"
								type="text"
								bind:value={titulo}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
								placeholder="Título de la clase"
							/>
						</div>

						<div>
							<label for="descripcion" class="mb-2 block text-sm font-medium text-gray-700">
								Descripción
							</label>
							<textarea
								id="descripcion"
								bind:value={descripcion}
								rows="4"
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
								placeholder="Descripción de la clase"
							></textarea>
						</div>

						<div>
							<label for="precio" class="mb-2 block text-sm font-medium text-gray-700">
								Precio (€) *
							</label>
							<input
								id="precio"
								type="number"
								bind:value={precio}
								min="0"
								step="0.01"
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
								placeholder="0.00"
							/>
						</div>

						<div>
							<label for="imagenPortada" class="mb-2 block text-sm font-medium text-gray-700">
								URL de Imagen de Portada
							</label>
							<input
								id="imagenPortada"
								type="url"
								bind:value={imagenPortada}
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
								placeholder="https://ejemplo.com/imagen.jpg"
							/>
						</div>
					</div>

					<!-- Class Settings -->
					<div class="space-y-6">
						<div>
							<label for="nivel" class="mb-2 block text-sm font-medium text-gray-700">
								Nivel *
							</label>
							<select
								id="nivel"
								bind:value={nivel}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							>
								<option value="BASICO">Básico</option>
								<option value="PRINCIPIANTE">Principiante</option>
								<option value="INTERMEDIO">Intermedio</option>
								<option value="AVANZADO">Avanzado</option>
							</select>
						</div>

						<div>
							<label for="presencialidad" class="mb-2 block text-sm font-medium text-gray-700">
								Presencialidad *
							</label>
							<select
								id="presencialidad"
								bind:value={presencialidad}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							>
								<option value="ONLINE">Online</option>
								<option value="PRESENCIAL">Presencial</option>
								<option value="HIBRIDO">Híbrido</option>
							</select>
						</div>

						<div>
							<label for="fechaInicio" class="mb-2 block text-sm font-medium text-gray-700">
								Fecha de Inicio *
							</label>
							<input
								id="fechaInicio"
								type="date"
								bind:value={fechaInicio}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<div>
							<label for="fechaFin" class="mb-2 block text-sm font-medium text-gray-700">
								Fecha de Fin *
							</label>
							<input
								id="fechaFin"
								type="date"
								bind:value={fechaFin}
								required={tipoClase === 'curso'}
								disabled={tipoClase === 'taller'}
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100"
							/>
						</div>
					</div>
				</div>

				<!-- Workshop-specific fields -->
				{#if tipoClase === 'taller'}
					<div class="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
						<div>
							<label for="fechaRealizacion" class="mb-2 block text-sm font-medium text-gray-700">
								Fecha de Realización *
							</label>
							<input
								id="fechaRealizacion"
								type="date"
								bind:value={fechaRealizacion}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<div>
							<label for="horaComienzo" class="mb-2 block text-sm font-medium text-gray-700">
								Hora de Comienzo *
							</label>
							<input
								id="horaComienzo"
								type="time"
								bind:value={horaComienzo}
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<div>
							<label for="duracionHoras" class="mb-2 block text-sm font-medium text-gray-700">
								Duración (horas) *
							</label>
							<input
								id="duracionHoras"
								type="number"
								bind:value={duracionHoras}
								min="1"
								max="24"
								required
								class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
							/>
						</div>
					</div>
				{/if}

				<!-- Professors Section -->
				<div class="mt-8">
					<h3 class="montserrat-semibold mb-4 text-lg font-semibold text-gray-900">Profesores</h3>
					<div class="space-y-4">
						{#if loadingProfesores}
							<div class="flex items-center justify-center py-8">
								<svg class="h-6 w-6 animate-spin text-blue-600" fill="none" viewBox="0 0 24 24">
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
								<span class="ml-2 text-gray-600">Cargando profesores...</span>
							</div>
						{:else if profesoresDisponibles.length === 0}
							<div class="py-8 text-center text-gray-500">
								<p>No hay profesores disponibles</p>
							</div>
						{:else}
							<div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
								{#each profesoresDisponibles as profesor (profesor.id)}
									<label
										class="flex cursor-pointer items-center rounded-lg border-2 border-gray-200 p-3 transition-all duration-200 hover:border-blue-300 {profesoresSeleccionados.includes(
											profesor.id!
										)
											? 'border-blue-500 bg-blue-50'
											: ''}"
									>
										<input
											type="checkbox"
											checked={profesoresSeleccionados.includes(profesor.id!)}
											onchange={() => toggleProfesor(profesor.id!)}
											class="mr-3 h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
										/>
										<div class="flex-1">
											<div class="font-medium text-gray-900">
												{profesor.firstName}
												{profesor.lastName}
											</div>
											<div class="text-sm text-gray-600">{profesor.email}</div>
										</div>
									</label>
								{/each}
							</div>
							{#if profesoresSeleccionados.length > 0}
								<div class="mt-4 rounded-lg bg-blue-50 p-3">
									<p class="text-sm text-blue-800">
										<strong>Profesores seleccionados:</strong>
										{profesoresSeleccionados.length}
									</p>
								</div>
							{/if}
						{/if}
					</div>
				</div>

				<!-- Form Actions -->
				<div
					class="mt-8 flex flex-col justify-end space-y-3  border-gray-200 pt-6 sm:flex-row sm:space-y-0 sm:space-x-4"
				>
					<button
						type="button"
						onclick={resetForm}
						class="rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
					>
						Limpiar
					</button>
					<button
						type="submit"
						disabled={loading}
						class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg disabled:transform-none disabled:cursor-not-allowed disabled:opacity-50"
					>
						{#if loading}
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
								Creando...
							</div>
						{:else}
							Crear {tipoClase === 'curso' ? 'Curso' : 'Taller'}
						{/if}
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
