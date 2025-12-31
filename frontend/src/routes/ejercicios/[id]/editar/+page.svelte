<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEjercicio } from '$lib/generated/api';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let ejercicio = $state<DTOEjercicio | null>(null);

	// Form state
	let form = $state({
		name: '',
		statement: '',
		startDate: '',
		endDate: '',
		classId: ''
	});

	// Get exercise ID from URL
	const ejercicioId = $derived(Number($page.params.id));

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

	// Load exercise data
	$effect(() => {
		if (authStore.isAuthenticated && ejercicioId) {
			loadEjercicio();
		}
	});

	async function loadEjercicio() {
		loading = true;
		error = null;

		try {
			ejercicio = await EjercicioService.getEjercicioById(ejercicioId);

			// Populate form with current values
			form = {
				name: ejercicio.name || '',
				statement: ejercicio.statement || '',
				startDate: ejercicio.startDate
					? new Date(ejercicio.startDate).toISOString().slice(0, 16)
					: '',
				endDate: ejercicio.endDate ? new Date(ejercicio.endDate).toISOString().slice(0, 16) : '',
				classId: ejercicio.classId || ''
			};
		} catch (err) {
			console.error('Error loading exercise:', err);
			error = 'Error al cargar el ejercicio. Por favor, inténtalo de nuevo.';
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
			const updateData = {
				name: form.name.trim(),
				statement: form.statement.trim(),
				startDate: new Date(form.startDate),
				endDate: new Date(form.endDate),
				classId: parseInt(form.classId.trim())
			};

			await EjercicioService.updateEjercicio(ejercicioId, updateData);

			successMessage = 'Ejercicio actualizado exitosamente';
			setTimeout(() => {
				goto(`/ejercicios/${ejercicioId}`);
			}, 1500);
		} catch (err) {
			console.error('Error updating exercise:', err);
			error = 'Error al actualizar el ejercicio. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	function validateForm(): boolean {
		if (!form.name.trim()) {
			error = 'El nombre del ejercicio es obligatorio';
			return false;
		}

		if (!form.statement.trim()) {
			error = 'El enunciado del ejercicio es obligatorio';
			return false;
		}

		if (!form.startDate) {
			error = 'La fecha de inicio es obligatoria';
			return false;
		}

		if (!form.endDate) {
			error = 'La fecha de fin es obligatoria';
			return false;
		}

		if (!form.classId.trim()) {
			error = 'El ID de la clase es obligatorio';
			return false;
		}

		const startDate = new Date(form.startDate);
		const endDate = new Date(form.endDate);

		if (endDate <= startDate) {
			error = 'La fecha de fin debe ser posterior a la fecha de inicio';
			return false;
		}

		return true;
	}

	function handleCancel() {
		goto(`/ejercicios/${ejercicioId}`);
	}
</script>

<svelte:head>
	<title>Editar Ejercicio - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Editar Ejercicio</h1>
				<p class="mt-2 text-gray-600">Modifica los detalles del ejercicio</p>
			</div>
			<button
				onclick={handleCancel}
				class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
			>
				Cancelar
			</button>
		</div>
	</div>

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error && !ejercicio}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else if ejercicio}
		<!-- Success Message -->
		{#if successMessage}
			<div class="mb-4 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		<!-- Error Message -->
		{#if error}
			<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				{error}
			</div>
		{/if}

		<!-- Edit Form -->
		<div class="rounded-lg bg-white p-6 shadow-lg">
			<form onsubmit={handleSubmit} class="space-y-6">
				<!-- Name -->
				<div>
					<label for="name" class="block text-sm font-medium text-gray-700">
						Nombre del Ejercicio *
					</label>
					<input
						type="text"
						id="name"
						bind:value={form.name}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						placeholder="Ingresa el nombre del ejercicio"
						required
					/>
				</div>

				<!-- Statement -->
				<div>
					<label for="statement" class="block text-sm font-medium text-gray-700">
						Enunciado *
					</label>
					<textarea
						id="statement"
						bind:value={form.statement}
						rows="6"
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						placeholder="Describe el ejercicio y los requisitos..."
						required
					></textarea>
				</div>

				<!-- Dates -->
				<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
					<div>
						<label for="startDate" class="block text-sm font-medium text-gray-700">
							Fecha de Inicio *
						</label>
						<input
							type="datetime-local"
							id="startDate"
							bind:value={form.startDate}
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							required
						/>
					</div>

					<div>
						<label for="endDate" class="block text-sm font-medium text-gray-700">
							Fecha de Fin *
						</label>
						<input
							type="datetime-local"
							id="endDate"
							bind:value={form.endDate}
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							required
						/>
					</div>
				</div>

				<!-- Class ID -->
				<div>
					<label for="classId" class="block text-sm font-medium text-gray-700">
						ID de la Clase *
					</label>
					<input
						type="text"
						id="classId"
						bind:value={form.classId}
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						placeholder="Ingresa el ID de la clase"
						required
					/>
				</div>

				<!-- Submit Button -->
				<div class="flex justify-end space-x-4">
					<button
						type="button"
						onclick={handleCancel}
						class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
						disabled={saving}
					>
						Cancelar
					</button>
					<button
						type="submit"
						class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
						disabled={saving}
					>
						{saving ? 'Guardando...' : 'Guardar Cambios'}
					</button>
				</div>
			</form>
		</div>
	{:else}
		<!-- Not Found State -->
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📝</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Ejercicio no encontrado</h3>
			<p class="mb-4 text-gray-500">El ejercicio que buscas no existe o ha sido eliminado.</p>
			<button
				onclick={() => goto('/ejercicios')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Ejercicios
			</button>
		</div>
	{/if}
</div>
