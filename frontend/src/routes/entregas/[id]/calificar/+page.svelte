<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEntregaEjercicio } from '$lib/generated/api';
	import { EntregaService } from '$lib/services/entregaService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let entrega = $state<DTOEntregaEjercicio | null>(null);

	// Grading form state
	let gradeForm = $state({
		nota: '',
		comentarios: ''
	});

	// Get delivery ID from URL
	const entregaId = $derived(Number($page.params.id));

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isProfesor && !authStore.isAdmin) {
			goto('/entregas');
			return;
		}
	});

	// Load delivery data
	$effect(() => {
		if (authStore.isAuthenticated && entregaId) {
			loadEntrega();
		}
	});

	async function loadEntrega() {
		loading = true;
		error = null;

		try {
			entrega = await EntregaService.getEntregaById(entregaId);

			// Check if delivery can be graded
			if (entrega.estado !== 'ENTREGADO') {
				error =
					'Esta entrega no puede ser calificada. Solo se pueden calificar entregas con estado "Entregado".';
				return;
			}

			// Pre-populate form with existing data if available
			gradeForm = {
				nota: entrega.nota?.toString() || '',
				comentarios: entrega.comentarios || ''
			};
		} catch (err) {
			console.error('Error loading delivery:', err);
			error = 'Error al cargar la entrega. Por favor, inténtalo de nuevo.';
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
			const nota = parseFloat(gradeForm.nota);
			const updateData: {
				nota: number;
				comentarios?: string;
			} = { nota };

			// Only include comments if they are provided
			if (gradeForm.comentarios && gradeForm.comentarios.trim()) {
				updateData.comentarios = gradeForm.comentarios.trim();
			}

			await EntregaService.updateEntrega(entregaId, updateData);

			successMessage = 'Entrega calificada exitosamente';

			setTimeout(() => {
				// Redirect back to delivery detail or exercise
				if (entrega?.ejercicioId) {
					goto(`/ejercicios/${entrega.ejercicioId}`);
				} else {
					goto(`/entregas/${entregaId}`);
				}
			}, 2000);
		} catch (err) {
			console.error('Error grading delivery:', err);
			error = 'Error al calificar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	function validateForm(): boolean {
		if (!gradeForm.nota || gradeForm.nota.toString().trim() === '') {
			error = 'La nota es obligatoria';
			return false;
		}

		const nota = parseFloat(gradeForm.nota);
		if (isNaN(nota) || nota < 0 || nota > 10) {
			error = 'La nota debe ser un número entre 0 y 10';
			return false;
		}

		if (gradeForm.comentarios && gradeForm.comentarios.trim().length > 1000) {
			error = 'Los comentarios no pueden exceder 1000 caracteres';
			return false;
		}

		return true;
	}

	function handleCancel() {
		if (entrega?.ejercicioId) {
			goto(`/ejercicios/${entrega.ejercicioId}`);
		} else {
			goto(`/entregas/${entregaId}`);
		}
	}

	// Format functions
	function formatDate(date: Date | string | undefined): string {
		if (!date) return 'N/A';
		return new Date(date).toLocaleDateString('es-ES', {
			year: 'numeric',
			month: 'long',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	function getGradeColor(nota: number | undefined): string {
		if (nota === undefined || nota === null) return 'text-gray-500';
		if (nota >= 9) return 'text-green-600 font-bold';
		if (nota >= 7) return 'text-blue-600 font-semibold';
		if (nota >= 5) return 'text-yellow-600 font-semibold';
		return 'text-red-600 font-semibold';
	}

	function getGradeDescription(nota: number): string {
		if (nota >= 9) return 'Excelente - Trabajo sobresaliente';
		if (nota >= 7) return 'Bueno - Trabajo satisfactorio';
		if (nota >= 5) return 'Aprobado - Trabajo aceptable';
		return 'Suspenso - Necesita mejorar';
	}
</script>

<svelte:head>
	<title>Calificar Entrega - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Calificar Entrega</h1>
				<p class="mt-2 text-gray-600">Evalúa el trabajo del estudiante</p>
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
	{:else if error && !entrega}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else if entrega}
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

		<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
			<!-- Delivery Information -->
			<div class="space-y-6">
				<div class="rounded-lg bg-white p-6 shadow-lg">
					<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de la Entrega</h2>

					<div class="space-y-4">
						<div>
							<span class="text-sm font-medium text-gray-500">Estudiante</span>
							<p class="text-lg text-gray-900">{entrega.alumnoId || 'N/A'}</p>
						</div>

						<div>
							<span class="text-sm font-medium text-gray-500">Ejercicio ID</span>
							<p class="text-lg text-gray-900">{entrega.ejercicioId || 'N/A'}</p>
						</div>

						<div>
							<span class="text-sm font-medium text-gray-500">Fecha de Entrega</span>
							<p class="text-gray-900">{formatDate(entrega.fechaEntrega)}</p>
						</div>

						<div>
							<span class="text-sm font-medium text-gray-500">Estado</span>
							<p class="text-gray-900">{entrega.estadoDescriptivo || entrega.estado || 'N/A'}</p>
						</div>

						<div>
							<span class="text-sm font-medium text-gray-500">Archivos Entregados</span>
							<p class="text-gray-900">{entrega.numeroArchivos || 0} archivos</p>
						</div>

						<!-- Show existing grade if available -->
						{#if entrega.nota !== undefined && entrega.nota !== null}
							<div>
								<span class="text-sm font-medium text-gray-500">Calificación Actual</span>
								<p class="text-lg {getGradeColor(entrega.nota)}">
									{entrega.notaFormateada || entrega.nota}
								</p>
								{#if entrega.calificacionCualitativa}
									<p class="text-sm text-gray-600">{entrega.calificacionCualitativa}</p>
								{/if}
							</div>
						{/if}

						<!-- Show existing comments if available -->
						{#if entrega.comentarios && entrega.comentarios.trim()}
							<div>
								<span class="text-sm font-medium text-gray-500">Comentarios Actuales</span>
								<div class="mt-2 rounded-lg bg-blue-50 p-3">
									<p class="text-sm text-gray-800">
										{entrega.comentariosFormateados || entrega.comentarios}
									</p>
								</div>
							</div>
						{/if}
					</div>
				</div>

				<!-- Files Section -->
				{#if entrega.archivosEntregados && entrega.archivosEntregados.length > 0}
					<div class="rounded-lg bg-white p-6 shadow-lg">
						<h2 class="mb-4 text-xl font-semibold text-gray-900">Archivos Entregados</h2>
						<div class="space-y-2">
							{#each entrega.archivosEntregados as archivo (archivo)}
								<div
									class="flex items-center justify-between rounded-lg border border-gray-200 p-3"
								>
									<div class="flex items-center space-x-3">
										<div class="text-blue-600">📎</div>
										<span class="text-gray-900">{archivo}</span>
									</div>
									<a
										href={archivo}
										target="_blank"
										rel="noopener noreferrer"
										class="text-blue-600 hover:text-blue-800"
									>
										Ver archivo
									</a>
								</div>
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<!-- Grading Form -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Calificación</h2>

				<form onsubmit={handleSubmit} class="space-y-6">
					<!-- Grade -->
					<div>
						<label for="nota" class="block text-sm font-medium text-gray-700">
							Nota (0-10) *
						</label>
						<input
							type="number"
							id="nota"
							bind:value={gradeForm.nota}
							min="0"
							max="10"
							step="0.1"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-lg shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							placeholder="8.5"
							required
						/>
						{#if gradeForm.nota && !isNaN(parseFloat(gradeForm.nota))}
							<p class="mt-1 text-sm {getGradeColor(parseFloat(gradeForm.nota))}">
								{getGradeDescription(parseFloat(gradeForm.nota))}
							</p>
						{/if}
					</div>

					<!-- Comments -->
					<div>
						<label for="comentarios" class="block text-sm font-medium text-gray-700">
							Comentarios de Calificación
						</label>
						<textarea
							id="comentarios"
							bind:value={gradeForm.comentarios}
							rows="6"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							placeholder="Proporciona comentarios detallados sobre el trabajo del estudiante..."
						></textarea>
						<p class="mt-1 text-xs text-gray-500">
							{gradeForm.comentarios.length}/1000 caracteres
						</p>
					</div>

					<!-- Grading Guidelines -->
					<div class="rounded-lg bg-blue-50 p-4">
						<h3 class="mb-2 text-sm font-semibold text-blue-900">Guía de Calificación</h3>
						<div class="space-y-1 text-xs text-blue-800">
							<p><strong>9-10:</strong> Excelente - Trabajo sobresaliente</p>
							<p><strong>7-8.9:</strong> Bueno - Trabajo satisfactorio</p>
							<p><strong>5-6.9:</strong> Aprobado - Trabajo aceptable</p>
							<p><strong>0-4.9:</strong> Suspenso - Necesita mejorar</p>
						</div>
					</div>

					<!-- Submit Buttons -->
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
							class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700 disabled:opacity-50"
							disabled={saving}
						>
							{saving ? 'Guardando...' : 'Guardar Calificación'}
						</button>
					</div>
				</form>
			</div>
		</div>
	{:else}
		<!-- Not Found State -->
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📝</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Entrega no encontrada</h3>
			<p class="mb-4 text-gray-500">La entrega que buscas no existe o ha sido eliminada.</p>
			<button
				onclick={() => goto('/entregas')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Entregas
			</button>
		</div>
	{/if}
</div>
