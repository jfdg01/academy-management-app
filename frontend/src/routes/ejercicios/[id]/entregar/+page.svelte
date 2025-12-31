<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEjercicio } from '$lib/generated/api';
	import { EjercicioService } from '$lib/services/ejercicioService';

	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';
	import { NavigationUtils } from '$lib/utils/navigation.js';

	// State
	let loading = $state(false);
	let uploading = $state(false);
	let submitting = $state(false);
	let error = $state<string | null>(null);
	let success = $state<string | null>(null);
	let ejercicio = $state<DTOEjercicio | null>(null);

	// Form state
	let selectedFiles = $state<File[]>([]);
	let comments = $state('');
	let uploadedFiles = $state<
		Array<{
			nombreOriginal: string;
			nombreGuardado: string;
			rutaRelativa: string;
			tipoMime: string;
			tamanoBytes: number;
			tamanoFormateado: string;
			fechaSubida: string;
			extension: string;
			esImagen: boolean;
			esPdf: boolean;
		}>
	>([]);

	// Get exercise ID from URL
	const ejercicioId = $derived(Number($page.params.id));

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		// Only students can submit exercises
		if (!authStore.isAlumno) {
			goto('/ejercicios');
			return;
		}
	});

	// Load exercise data
	$effect(() => {
		if (authStore.isAuthenticated && authStore.isAlumno && ejercicioId) {
			loadEjercicio();
		}
	});

	async function loadEjercicio() {
		loading = true;
		error = null;

		try {
			ejercicio = await EjercicioService.getEjercicioById(ejercicioId);

			// Check if exercise is still active
			if (ejercicio.estado === 'EXPIRED') {
				error = 'Este ejercicio ya ha vencido y no se pueden realizar más entregas.';
			}
		} catch (err) {
			console.error('Error loading exercise:', err);
			error = 'Error al cargar el ejercicio. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	function validateFile(file: File): { isValid: boolean; error?: string } {
		const allowedTypes = ['image/png', 'application/pdf'];
		const maxSize = 10 * 1024 * 1024; // 10MB
		const allowedExtensions = ['png', 'pdf'];

		// Check file size
		if (file.size > maxSize) {
			return {
				isValid: false,
				error: 'El archivo es demasiado grande. Tamaño máximo: 10MB'
			};
		}

		// Check file type
		if (!allowedTypes.includes(file.type)) {
			return {
				isValid: false,
				error: 'Tipo de archivo no permitido. Solo se permiten: PNG, PDF'
			};
		}

		// Check file extension
		const extension = file.name.split('.').pop()?.toLowerCase();
		if (!extension || !allowedExtensions.includes(extension)) {
			return {
				isValid: false,
				error: 'Extensión de archivo no permitida. Solo se permiten: .png, .pdf'
			};
		}

		return { isValid: true };
	}

	function handleFileSelect(event: Event) {
		const target = event.target as HTMLInputElement;
		if (target.files) {
			const files = Array.from(target.files);
			const validFiles: File[] = [];
			const errors: string[] = [];

			files.forEach((file) => {
				const validation = validateFile(file);
				if (validation.isValid) {
					validFiles.push(file);
				} else {
					errors.push(`${file.name}: ${validation.error}`);
				}
			});

			if (errors.length > 0) {
				error = `Archivos inválidos:\n${errors.join('\n')}`;
				// Clear the input
				target.value = '';
				return;
			}

			selectedFiles = validFiles;
			error = null;
		}
	}

	function removeFile(index: number) {
		selectedFiles = selectedFiles.filter((_, i) => i !== index);
	}

	function removeUploadedFile(index: number) {
		uploadedFiles = uploadedFiles.filter((_, i) => i !== index);
	}

	async function uploadFiles(): Promise<string[]> {
		if (selectedFiles.length === 0) {
			throw new Error('No hay archivos para subir');
		}

		uploading = true;
		error = null;

		try {
			const formData = new FormData();
			selectedFiles.forEach((file) => {
				formData.append('files', file);
			});
			formData.append('ejercicioId', ejercicioId.toString());

			console.log('Uploading files to:', '/api/entregas/upload-files');
			console.log(
				'Files to upload:',
				selectedFiles.map((f) => f.name)
			);
			console.log('Exercise ID:', ejercicioId);

			const response = await fetch('/api/entregas/upload-files', {
				method: 'POST',
				headers: {
					Authorization: `Bearer ${authStore.token}`
				},
				body: formData
			});

			if (!response.ok) {
				const errorData = await response.json();
				throw new Error(errorData.message || 'Error al subir archivos');
			}

			const uploadedFileData = await response.json();
			uploadedFiles = uploadedFileData;

			// Return the file paths for delivery creation
			return uploadedFileData.map((file: { rutaRelativa: string }) => file.rutaRelativa);
		} catch (err) {
			console.error('Error uploading files:', err);
			throw new Error(err instanceof Error ? err.message : 'Error al subir archivos');
		} finally {
			uploading = false;
		}
	}

	async function createDeliveryWithFiles(filePaths: string[]) {
		if (!authStore.user?.id) {
			throw new Error('Usuario no autenticado');
		}

		const deliveryData = {
			ejercicioId: ejercicioId,
			archivosRutas: filePaths
		};

		console.log('Creating delivery with files:', deliveryData);

		const response = await fetch('/api/entregas/create-with-files', {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${authStore.token}`,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(deliveryData)
		});

		if (!response.ok) {
			const errorData = await response.json();
			throw new Error(errorData.message || 'Error al crear la entrega');
		}

		return await response.json();
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!ejercicio || !authStore.user?.id) {
			error = 'Datos de usuario o ejercicio no válidos.';
			return;
		}

		if (selectedFiles.length === 0 && uploadedFiles.length === 0) {
			error = 'Debes seleccionar al menos un archivo para entregar.';
			return;
		}

		submitting = true;
		error = null;
		success = null;

		try {
			let filePaths: string[] = [];

			// If we have new files to upload, upload them first
			if (selectedFiles.length > 0) {
				filePaths = await uploadFiles();
			} else {
				// Use already uploaded files
				filePaths = uploadedFiles.map((file) => file.rutaRelativa);
			}

			// Create the delivery with the file paths
			await createDeliveryWithFiles(filePaths);

			success = 'Ejercicio entregado correctamente.';

			// Redirect to exercise page after a short delay
			setTimeout(() => {
				NavigationUtils.goToExerciseView(ejercicioId);
			}, 2000);
		} catch (err) {
			console.error('Error submitting exercise:', err);
			error =
				err instanceof Error
					? err.message
					: 'Error al entregar el ejercicio. Por favor, inténtalo de nuevo.';
		} finally {
			submitting = false;
		}
	}

	function handleCancel() {
		NavigationUtils.goToExerciseView(ejercicioId);
	}

	function formatFileSize(bytes: number): string {
		if (bytes === 0) return '0 Bytes';
		const k = 1024;
		const sizes = ['Bytes', 'KB', 'MB', 'GB'];
		const i = Math.floor(Math.log(bytes) / Math.log(k));
		return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
	}
</script>

<svelte:head>
	<title>Entregar Ejercicio - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error && !ejercicio}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
		<div class="text-center">
			<button
				onclick={() => goto('/ejercicios')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Ejercicios
			</button>
		</div>
	{:else if ejercicio}
		<!-- Header -->
		<div class="mb-8">
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold text-gray-900">Entregar Ejercicio</h1>
					<p class="mt-2 text-gray-600">Sube tus archivos para completar la entrega</p>
				</div>
				<button
					onclick={handleCancel}
					class="rounded-lg border border-gray-300 px-4 py-2 font-semibold text-gray-700 hover:bg-gray-50"
				>
					Cancelar
				</button>
			</div>
		</div>

		<!-- Error Display -->
		{#if error}
			<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				<pre class="whitespace-pre-wrap">{error}</pre>
			</div>
		{/if}

		<!-- Success Display -->
		{#if success}
			<div class="mb-6 rounded-lg border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{success}
			</div>
		{/if}

		<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
			<!-- Exercise Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Información del Ejercicio</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">Nombre</span>
						<p class="text-lg font-medium text-gray-900">{ejercicio.name || 'Sin nombre'}</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Enunciado</span>
						<div class="mt-1 rounded-lg bg-gray-50 p-3">
							<p class="whitespace-pre-wrap text-gray-900">
								{ejercicio.statement || 'Sin enunciado'}
							</p>
						</div>
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<span class="text-sm font-medium text-gray-500">Fecha de Inicio</span>
							<p class="text-gray-900">
								{FormatterUtils.formatDate(ejercicio.startDate, { includeTime: true })}
							</p>
						</div>
						<div>
							<span class="text-sm font-medium text-gray-500">Fecha de Fin</span>
							<p class="text-gray-900">
								{FormatterUtils.formatDate(ejercicio.endDate, { includeTime: true })}
							</p>
						</div>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Estado</span>
						<div class="mt-1">
							<span
								class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {FormatterUtils.getExerciseStatusColor(
									ejercicio.estado
								)}"
							>
								{FormatterUtils.getExerciseStatusText(ejercicio.estado)}
							</span>
						</div>
					</div>

					{#if ejercicio.horasRestantes !== undefined}
						<div>
							<span class="text-sm font-medium text-gray-500">Tiempo Restante</span>
							<p class="text-gray-900">{ejercicio.horasRestantes} horas</p>
						</div>
					{/if}
				</div>
			</div>

			<!-- Submission Form -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Entregar Archivos</h2>

				<form onsubmit={handleSubmit} class="space-y-6">
					<!-- File Upload -->
					<div>
						<label for="files" class="block text-sm font-medium text-gray-700">
							Archivos a entregar
						</label>
						<div class="mt-1">
							<input
								type="file"
								id="files"
								multiple
								accept=".pdf,.png"
								onchange={handleFileSelect}
								class="block w-full text-sm text-gray-500 file:mr-4 file:rounded-full file:border-0 file:bg-blue-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-blue-700 hover:file:bg-blue-100"
							/>
						</div>
						<p class="mt-1 text-xs text-gray-500">
							Formatos permitidos: PDF, PNG (máximo 10MB por archivo)
						</p>
					</div>

					<!-- Selected Files -->
					{#if selectedFiles.length > 0}
						<div>
							<div class="mb-2 block text-sm font-medium text-gray-700">
								Archivos seleccionados ({selectedFiles.length})
							</div>
							<div class="space-y-2">
								{#each selectedFiles as file, index (file.name)}
									<div
										class="flex items-center justify-between rounded-lg border border-gray-200 p-3"
									>
										<div class="flex items-center space-x-3">
											<div class="flex-shrink-0">
												<svg
													class="h-6 w-6 text-gray-400"
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
											<div>
												<p class="text-sm font-medium text-gray-900">{file.name}</p>
												<p class="text-xs text-gray-500">{formatFileSize(file.size)}</p>
											</div>
										</div>
										<button
											type="button"
											onclick={() => removeFile(index)}
											class="text-red-600 hover:text-red-900"
											aria-label="Eliminar archivo {file.name}"
										>
											<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M6 18L18 6M6 6l12 12"
												></path>
											</svg>
										</button>
									</div>
								{/each}
							</div>
						</div>
					{/if}

					<!-- Uploaded Files -->
					{#if uploadedFiles.length > 0}
						<div>
							<div class="mb-2 block text-sm font-medium text-gray-700">
								Archivos subidos ({uploadedFiles.length})
							</div>
							<div class="space-y-2">
								{#each uploadedFiles as file, index (file.nombreGuardado)}
									<div
										class="flex items-center justify-between rounded-lg border border-green-200 bg-green-50 p-3"
									>
										<div class="flex items-center space-x-3">
											<div class="flex-shrink-0">
												<svg
													class="h-6 w-6 text-green-500"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M5 13l4 4L19 7"
													></path>
												</svg>
											</div>
											<div>
												<p class="text-sm font-medium text-gray-900">{file.nombreOriginal}</p>
												<p class="text-xs text-gray-500">
													{file.tamanoFormateado} • {file.extension.toUpperCase()}
												</p>
											</div>
										</div>
										<button
											type="button"
											onclick={() => removeUploadedFile(index)}
											class="text-red-600 hover:text-red-900"
											aria-label="Eliminar archivo {file.nombreOriginal}"
										>
											<svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M6 18L18 6M6 6l12 12"
												></path>
											</svg>
										</button>
									</div>
								{/each}
							</div>
						</div>
					{/if}

					<!-- Comments -->
					<div>
						<label for="comments" class="block text-sm font-medium text-gray-700">
							Comentarios (opcional)
						</label>
						<textarea
							id="comments"
							bind:value={comments}
							rows="4"
							maxlength="500"
							placeholder="Añade cualquier comentario adicional sobre tu entrega..."
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
						></textarea>
						<p class="mt-1 text-xs text-gray-500">
							{comments.length}/500 caracteres
						</p>
					</div>

					<!-- Submit Button -->
					<div class="flex justify-end space-x-4">
						<button
							type="button"
							onclick={handleCancel}
							class="rounded-lg border border-gray-300 px-4 py-2 font-semibold text-gray-700 hover:bg-gray-50"
						>
							Cancelar
						</button>
						<button
							type="submit"
							disabled={submitting ||
								uploading ||
								(selectedFiles.length === 0 && uploadedFiles.length === 0)}
							class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
						>
							{#if uploading}
								<div class="flex items-center space-x-2">
									<div class="h-4 w-4 animate-spin rounded-full border-b-2 border-white"></div>
									<span>Subiendo archivos...</span>
								</div>
							{:else if submitting}
								<div class="flex items-center space-x-2">
									<div class="h-4 w-4 animate-spin rounded-full border-b-2 border-white"></div>
									<span>Entregando...</span>
								</div>
							{:else}
								Entregar Ejercicio
							{/if}
						</button>
					</div>
				</form>
			</div>
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
