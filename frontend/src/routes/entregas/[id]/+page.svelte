<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEntregaEjercicio, FileInfo, DTOAlumno, DTOEjercicio } from '$lib/generated/api';
	import { EntregaService, type FileOperation } from '$lib/services/entregaService';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let entrega = $state<DTOEntregaEjercicio | null>(null);

	// Additional data state
	let student = $state<DTOAlumno | null>(null);
	let exercise = $state<DTOEjercicio | null>(null);

	// File viewer state
	let fileViewerOpen = $state(false);
	let selectedFile = $state<string | null>(null);
	let originalFilePath = $state<string | null>(null);
	let fileError = $state<string | null>(null);
	let fileMetadata = $state<FileInfo[]>([]);

	// Grading form state
	let gradingMode = $state(false);
	let gradeForm = $state({
		nota: '',
		comentarios: ''
	});

	// New modification state
	let modificationMode = $state(false);
	let modificationForm = $state({
		comentarios: '',
		fileOperations: [] as FileOperation[]
	});
	let selectedFiles = $state<File[]>([]);
	let fileInputRef = $state<HTMLInputElement | null>(null);

	// File rename state
	let renamingFile = $state<string | null>(null);
	let newFileName = $state('');

	// Get delivery ID from URL
	const entregaId = $derived(Number($page.params.id));

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
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

			// Load additional data if delivery was loaded successfully
			if (entrega) {
				await Promise.all([loadStudentData(), loadExerciseData()]);
			}

			// Load file metadata if there are files
			if (entrega?.archivosEntregados && entrega.archivosEntregados.length > 0) {
				await loadFileMetadata();
			}
		} catch (err) {
			console.error('Error loading delivery:', err);
			error = 'Error al cargar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	async function loadStudentData() {
		if (!entrega?.alumnoId) return;

		try {
			student = await AlumnoService.getAlumno(entrega.alumnoId);
		} catch (err) {
			console.error('Error loading student data:', err);
			// Don't show error to user as this is optional metadata
		}
	}

	async function loadExerciseData() {
		if (!entrega?.ejercicioId) return;

		try {
			exercise = await EjercicioService.getEjercicioById(entrega.ejercicioId);
		} catch (err) {
			console.error('Error loading exercise data:', err);
			// Don't show error to user as this is optional metadata
		}
	}

	async function loadFileMetadata() {
		try {
			const response = await fetch(`/api/files/info/${entregaId}`, {
				headers: {
					Authorization: `Bearer ${authStore.token}`
				}
			});

			if (response.ok) {
				fileMetadata = await response.json();
			}
		} catch (err) {
			console.error('Error loading file metadata:', err);
			// Don't show error to user as this is optional metadata
		}
	}

	function getFileExtension(filename: string): string {
		return filename.split('.').pop()?.toLowerCase() || '';
	}

	function isImageFile(filename: string): boolean {
		// First try to find file metadata
		const fileInfo = fileMetadata.find((f) => f.filePath === filename);
		if (fileInfo?.isImage !== undefined) {
			return fileInfo.isImage;
		}

		// Fallback to extension-based detection
		const ext = getFileExtension(filename);
		return ext === 'png' || ext === 'jpg' || ext === 'jpeg' || ext === 'gif';
	}

	function isPdfFile(filename: string): boolean {
		// First try to find file metadata
		const fileInfo = fileMetadata.find((f) => f.filePath === filename);
		if (fileInfo?.isPdf !== undefined) {
			return fileInfo.isPdf;
		}

		// Fallback to extension-based detection
		return getFileExtension(filename) === 'pdf';
	}

	function getFileIcon(filename: string): string {
		if (isImageFile(filename)) return '🖼️';
		if (isPdfFile(filename)) return '📄';
		return '📎';
	}

	function getFileTypeLabel(filename: string): string {
		if (isImageFile(filename)) return 'Imagen';
		if (isPdfFile(filename)) return 'PDF';
		return 'Documento';
	}

	function getFileDisplayName(filePath: string): string {
		// First try to find file metadata with original file name
		const fileInfo = fileMetadata.find((f) => f.filePath === filePath);
		console.log(`Looking for file: ${filePath}`);
		console.log(
			`Available metadata:`,
			fileMetadata.map((f) => ({ filePath: f.filePath, originalFileName: f.originalFileName }))
		);
		console.log(`Found fileInfo:`, fileInfo);

		if (fileInfo?.originalFileName) {
			console.log(`Using original file name for ${filePath}: ${fileInfo.originalFileName}`);
			return fileInfo.originalFileName;
		}

		// Fallback to extracting filename from path
		const fileName = filePath.split('/').pop() || filePath.split('\\').pop() || filePath;
		console.log(`Using fallback file name for ${filePath}: ${fileName}`);
		return fileName;
	}

	async function openFileViewer(filePath: string) {
		console.log('Opening file viewer for:', filePath);
		console.log('File extension:', getFileExtension(filePath));
		console.log('Is PDF?', isPdfFile(filePath));
		console.log('Is Image?', isImageFile(filePath));

		originalFilePath = filePath;
		selectedFile = filePath;
		fileViewerOpen = true;
		fileError = null;

		// For PDFs and images, we need to fetch with authentication and create a blob URL
		if (isPdfFile(filePath) || isImageFile(filePath)) {
			const fileType = isPdfFile(filePath) ? 'PDF' : 'Image';
			try {
				console.log(
					`Fetching ${fileType} from:`,
					`/api/files/view?path=${encodeURIComponent(filePath)}&deliveryId=${entregaId}`
				);
				console.log('Using token:', authStore.token ? 'Token present' : 'No token');

				const response = await fetch(
					`/api/files/view?path=${encodeURIComponent(filePath)}&deliveryId=${entregaId}`,
					{
						headers: {
							Authorization: `Bearer ${authStore.token}`
						}
					}
				);

				console.log('Response status:', response.status);
				console.log('Response ok:', response.ok);

				if (!response.ok) {
					const errorText = await response.text();
					console.error('Response error:', errorText);
					throw new Error(
						`Error al cargar el ${fileType}: ${response.status} ${response.statusText}`
					);
				}

				const blob = await response.blob();
				console.log('Blob created, size:', blob.size);
				const blobUrl = URL.createObjectURL(blob);
				console.log('Blob URL created:', blobUrl);
				selectedFile = blobUrl; // Store the blob URL instead of the file path
			} catch (err) {
				console.error(`Error loading ${fileType}:`, err);
				fileError = `Error al cargar el ${fileType}. Por favor, inténtalo de nuevo.`;
			}
		}
	}

	function closeFileViewer() {
		fileViewerOpen = false;
		// Clean up blob URL if it exists
		if (selectedFile && selectedFile.startsWith('blob:')) {
			URL.revokeObjectURL(selectedFile);
		}
		selectedFile = null;
		originalFilePath = null;
		fileError = null;
	}

	// New modification functions
	function startModification() {
		modificationMode = true;
		modificationForm = {
			comentarios: entrega?.comentarios || '',
			fileOperations: []
		};
	}

	function cancelModification() {
		modificationMode = false;
		modificationForm = {
			comentarios: '',
			fileOperations: []
		};
		selectedFiles = [];
		renamingFile = null;
		newFileName = '';
	}

	function addFileOperation(operation: FileOperation) {
		modificationForm.fileOperations = [...modificationForm.fileOperations, operation];
	}

	function removeFileOperation(index: number) {
		modificationForm.fileOperations = modificationForm.fileOperations.filter((_, i) => i !== index);
	}

	function startRenameFile(filePath: string) {
		renamingFile = filePath;
		const fileName = filePath.split('/').pop() || '';
		newFileName = fileName;
	}

	function cancelRename() {
		renamingFile = null;
		newFileName = '';
	}

	function confirmRename() {
		if (newFileName.trim() && renamingFile) {
			addFileOperation({
				tipo: 'RENOMBRAR',
				rutaArchivo: renamingFile,
				nuevoNombre: newFileName.trim()
			});
			cancelRename();
		}
	}

	function deleteFile(filePath: string) {
		addFileOperation({
			tipo: 'ELIMINAR',
			rutaArchivo: filePath
		});
	}

	function handleFileSelect(event: Event) {
		const target = event.target as HTMLInputElement;
		if (target.files) {
			selectedFiles = Array.from(target.files);
		}
	}

	async function handleModificationSubmit(event: Event) {
		event.preventDefault();

		if (!entrega?.id) {
			error = 'No se puede modificar la entrega: ID no válido';
			return;
		}

		saving = true;
		error = null;
		successMessage = null;

		try {
			// First, add new files if any
			if (selectedFiles.length > 0) {
				await EntregaService.addFilesToDelivery(entrega.id, selectedFiles);
			}

			// Then, perform modifications if any
			if (
				modificationForm.comentarios !== entrega.comentarios ||
				modificationForm.fileOperations.length > 0
			) {
				const result = await EntregaService.handleDeliveryModification(
					entrega.id,
					{
						comentarios: modificationForm.comentarios,
						operacionesArchivos: modificationForm.fileOperations
					},
					authStore.user!,
					entrega.alumnoId
				);

				if (!result.success) {
					throw new Error(result.message);
				}

				// Update local state with the result
				if (result.result) {
					entrega = {
						...entrega,
						comentarios: result.result.comentarios || entrega.comentarios,
						archivosEntregados: result.result.archivosEntregados || entrega.archivosEntregados,
						numeroArchivos: result.result.numeroArchivos || entrega.numeroArchivos
					};
				}
			}

			successMessage = 'Entrega modificada exitosamente';
			modificationMode = false;
			cancelModification();

			// Reload delivery data to get updated metadata
			await loadEntrega();

			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			console.error('Error modifying delivery:', err);
			error =
				err instanceof Error
					? err.message
					: 'Error al modificar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	async function deleteAllFiles() {
		if (!entrega?.id) {
			error = 'No se pueden eliminar los archivos: ID de entrega no válido';
			return;
		}

		if (!confirm('¿Estás seguro de que quieres eliminar todos los archivos de esta entrega?')) {
			return;
		}

		try {
			await EntregaService.deleteAllFiles(entrega.id);

			// Reload delivery data to reflect the changes
			await loadEntrega();
			successMessage = 'Todos los archivos eliminados correctamente';

			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			console.error('Error deleting all files:', err);
			error =
				err instanceof Error
					? err.message
					: 'Error al eliminar los archivos. Por favor, inténtalo de nuevo.';
		}
	}

	async function downloadFile(filePath: string, originalName?: string) {
		try {
			// Create a download link for the file using the new API endpoint
			const response = await fetch(
				`/api/files/download?path=${encodeURIComponent(filePath)}&deliveryId=${entregaId}`,
				{
					headers: {
						Authorization: `Bearer ${authStore.token}`
					}
				}
			);

			if (!response.ok) {
				const errorData = await response.json().catch(() => ({}));
				throw new Error(errorData.message || 'Error al descargar el archivo');
			}

			const blob = await response.blob();
			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = originalName || filePath.split('/').pop() || 'archivo';
			document.body.appendChild(a);
			a.click();
			window.URL.revokeObjectURL(url);
			document.body.removeChild(a);
		} catch (err) {
			console.error('Error downloading file:', err);
			error =
				err instanceof Error
					? err.message
					: 'Error al descargar el archivo. Por favor, inténtalo de nuevo.';
		}
	}

	function startGrading() {
		gradingMode = true;
		gradeForm = {
			nota: entrega?.nota?.toString() || '',
			comentarios: entrega?.comentarios || ''
		};
	}

	function cancelGrading() {
		gradingMode = false;
		gradeForm = {
			nota: '',
			comentarios: ''
		};
	}

	async function handleGradeSubmit(event: Event) {
		event.preventDefault();

		if (!validateGradeForm()) {
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
			gradingMode = false;

			// Reload delivery data
			await loadEntrega();

			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			console.error('Error grading delivery:', err);
			error = 'Error al calificar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	function validateGradeForm(): boolean {
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

	function handleBack() {
		if (entrega?.ejercicioId) {
			goto(`/ejercicios/${entrega.ejercicioId}`);
		} else {
			goto('/entregas');
		}
	}

	// Check if user can modify this delivery
	function canModifyDelivery(): boolean {
		if (!entrega || !authStore.user) return false;

		// Check if delivery is not graded
		if (entrega.estado === 'CALIFICADO') return false;

		// Check permissions
		return EntregaService.canModifyDelivery(authStore.user, entrega.alumnoId);
	}
</script>

<svelte:head>
	<title>Detalle de Entrega - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Detalle de Entrega</h1>
				<p class="mt-2 text-gray-600">Información completa de la entrega</p>
			</div>
			<div class="flex space-x-4">
				<button
					onclick={handleBack}
					class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
				>
					Volver
				</button>
				{#if canModifyDelivery()}
					<button
						onclick={startModification}
						class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
					>
						Modificar Entrega
					</button>
				{/if}
				{#if (authStore.isProfesor || authStore.isAdmin) && entrega?.estado === 'ENTREGADO'}
					<button
						onclick={startGrading}
						class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700"
					>
						Calificar Entrega
					</button>
				{/if}
			</div>
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

		<!-- Delivery Information -->
		<div class="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
			<!-- Main Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de la Entrega</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">Estudiante</span>
						<p class="text-lg text-gray-900">
							{student ? `${student.firstName} ${student.lastName}` : entrega?.alumnoId || 'N/A'}
						</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Ejercicio</span>
						<p class="text-lg text-gray-900">
							{exercise?.name || entrega?.ejercicioId || 'N/A'}
						</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Fecha de Entrega</span>
						<p class="text-gray-900">
							{FormatterUtils.formatDate(entrega.fechaEntrega, { includeTime: true })}
						</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Estado</span>
						<div class="mt-1">
							<span
								class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {FormatterUtils.getStatusColor(
									entrega.estado,
									'delivery'
								)}"
							>
								{FormatterUtils.formatStatus(entrega.estado, 'delivery')}
							</span>
						</div>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Número de Archivos</span>
						<p class="text-gray-900">{entrega.numeroArchivos || 0} archivos</p>
					</div>
				</div>
			</div>

			<!-- Grading Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Calificación</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">Nota</span>
						<p class="text-2xl font-bold {FormatterUtils.getGradeColor(entrega.nota)}">
							{FormatterUtils.formatGrade(entrega.nota)}
						</p>
					</div>

					{#if entrega.comentarios && entrega.comentarios.trim()}
						<div>
							<span class="text-sm font-medium text-gray-500">Comentarios</span>
							<p class="mt-1 whitespace-pre-wrap text-gray-900">
								{entrega.comentariosFormateados || entrega.comentarios}
							</p>
						</div>
					{/if}

					{#if entrega.estadoDescriptivo}
						<div>
							<span class="text-sm font-medium text-gray-500">Estado Descriptivo</span>
							<p class="text-gray-900">{entrega.estadoDescriptivo}</p>
						</div>
					{/if}

					{#if entrega.notaFormateada}
						<div>
							<span class="text-sm font-medium text-gray-500">Nota Formateada</span>
							<p class="text-gray-900">{entrega.notaFormateada}</p>
						</div>
					{/if}
				</div>
			</div>
		</div>

		<!-- Files Section -->
		{#if entrega.archivosEntregados && entrega.archivosEntregados.length > 0}
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<div class="mb-4 flex items-center justify-between">
					<h2 class="text-xl font-semibold text-gray-900">Archivos Entregados</h2>
					{#if canModifyDelivery()}
						<button
							onclick={deleteAllFiles}
							class="rounded-lg bg-red-600 px-3 py-1 text-sm font-medium text-white hover:bg-red-700"
						>
							Eliminar Todos
						</button>
					{/if}
				</div>
				<div class="space-y-3">
					{#each entrega.archivosEntregados as archivo (archivo)}
						{@const fileInfo = fileMetadata.find((f) => f.filePath === archivo)}
						{@const isPendingOperation = modificationForm.fileOperations.some(
							(op) => op.rutaArchivo === archivo && op.tipo === 'ELIMINAR'
						)}
						{#if !isPendingOperation}
							<div class="flex items-center justify-between rounded-lg border border-gray-200 p-4">
								<div class="flex items-center space-x-3">
									<div class="text-2xl">{getFileIcon(archivo)}</div>
									<div>
										<p class="font-medium text-gray-900">
											{getFileDisplayName(archivo)}
										</p>
										<p class="text-sm text-gray-500">
											{fileInfo
												? `${fileInfo.formattedSize || 'Unknown size'} • ${(fileInfo.extension || 'unknown').toUpperCase()}`
												: getFileTypeLabel(archivo)}
										</p>
									</div>
								</div>
								<div class="flex space-x-2">
									{#if isImageFile(archivo) || isPdfFile(archivo)}
										<button
											onclick={() => openFileViewer(archivo)}
											class="rounded-lg bg-blue-600 px-3 py-1 text-sm font-medium text-white hover:bg-blue-700"
										>
											Ver Archivo
										</button>
									{/if}
									<button
										onclick={() => downloadFile(archivo, fileInfo?.originalFileName)}
										class="rounded-lg bg-green-600 px-3 py-1 text-sm font-medium text-white hover:bg-green-700"
									>
										Descargar
									</button>
									{#if canModifyDelivery()}
										<button
											onclick={() => startRenameFile(archivo)}
											class="rounded-lg bg-yellow-600 px-3 py-1 text-sm font-medium text-white hover:bg-yellow-700"
										>
											Renombrar
										</button>
										<button
											onclick={() => deleteFile(archivo)}
											class="rounded-lg bg-red-600 px-3 py-1 text-sm font-medium text-white hover:bg-red-700"
										>
											Eliminar
										</button>
									{/if}
								</div>
							</div>
						{/if}
					{/each}
				</div>
			</div>
		{/if}

		<!-- Pending Operations Section -->
		{#if modificationForm.fileOperations.length > 0}
			<div class="rounded-lg bg-yellow-50 p-6 shadow-lg">
				<h3 class="mb-4 text-lg font-semibold text-yellow-800">Operaciones Pendientes</h3>
				<div class="space-y-2">
					{#each modificationForm.fileOperations as operation, index (index)}
						<div class="flex items-center justify-between rounded-lg bg-yellow-100 p-3">
							<div>
								<span class="font-medium text-yellow-800">
									{operation.tipo === 'ELIMINAR' ? '🗑️ Eliminar' : '✏️ Renombrar'}:
								</span>
								<span class="text-yellow-700">{operation.rutaArchivo.split('/').pop()}</span>
								{#if operation.tipo === 'RENOMBRAR' && operation.nuevoNombre}
									<span class="text-yellow-700"> → {operation.nuevoNombre}</span>
								{/if}
							</div>
							<button
								onclick={() => removeFileOperation(index)}
								class="rounded bg-red-500 px-2 py-1 text-xs text-white hover:bg-red-600"
							>
								Cancelar
							</button>
						</div>
					{/each}
				</div>
			</div>
		{/if}

		<!-- File Viewer Modal -->
		{#if fileViewerOpen && selectedFile}
			<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-sm">
				<div
					class="relative max-h-[90vh] max-w-[95vw] min-w-[500px] rounded-lg bg-white p-6 shadow-xl"
				>
					<!-- Header -->
					<div class="mb-4 flex items-center justify-between">
						<h3 class="text-lg font-semibold text-gray-900">
							{(() => {
								const fileInfo = fileMetadata.find((f) => f.filePath === selectedFile);
								return fileInfo?.originalFileName || selectedFile.split('/').pop();
							})()}
						</h3>
						<div class="flex space-x-2">
							<button
								onclick={() => {
									if (selectedFile) {
										if (selectedFile.startsWith('blob:')) {
											// For blob URLs, we need to get the original file path
											const originalFilePath = entrega?.archivosEntregados?.find(
												(archivo) =>
													archivo === selectedFile ||
													(selectedFile && selectedFile.includes(archivo.split('/').pop() || ''))
											);
											if (originalFilePath) {
												downloadFile(originalFilePath);
											}
										} else {
											downloadFile(selectedFile);
										}
									}
								}}
								class="rounded-lg bg-green-600 px-3 py-1 text-sm font-medium text-white hover:bg-green-700"
							>
								Descargar
							</button>
							<button
								onclick={closeFileViewer}
								class="rounded-lg bg-gray-600 px-3 py-1 text-sm font-medium text-white hover:bg-gray-700"
							>
								Cerrar
							</button>
						</div>
					</div>

					<!-- File Content -->
					<div class="max-h-[70vh] overflow-auto">
						{#if selectedFile.startsWith('blob:')}
							<!-- File loaded as blob URL -->
							{#if originalFilePath && isPdfFile(originalFilePath)}
								<!-- PDF loaded as blob URL -->
								<object
									data={selectedFile}
									type="application/pdf"
									class="h-[70vh] w-full min-w-[500px] rounded-lg border"
									title="PDF Viewer"
								>
									<p>
										Tu navegador no puede mostrar PDFs. <a href={selectedFile} target="_blank"
											>Abrir PDF</a
										>
									</p>
								</object>
							{:else}
								<!-- Image loaded as blob URL -->
								<img
									src={selectedFile}
									alt="Archivo de imagen"
									class="mx-auto max-h-full max-w-full rounded-lg"
									onerror={() => (fileError = 'Error al cargar la imagen')}
								/>
							{/if}
						{:else if isPdfFile(selectedFile)}
							<!-- PDF loaded directly from API (fallback) -->
							<div class="flex flex-col items-center justify-center space-y-4">
								<div class="text-6xl text-gray-400">📄</div>
								<p class="text-lg font-medium text-gray-900">Error al cargar el PDF</p>
								<p class="text-sm text-gray-500">
									No se pudo cargar el archivo PDF. Por favor, inténtalo de nuevo.
								</p>
							</div>
						{:else}
							<div class="flex h-32 items-center justify-center text-gray-500">
								Vista previa no disponible para este tipo de archivo
							</div>
						{/if}
					</div>

					{#if fileError}
						<div class="mt-4 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
							{fileError}
						</div>
					{/if}
				</div>
			</div>
		{/if}

		<!-- Modification Form Modal -->
		{#if modificationMode}
			<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
				<div class="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Modificar Entrega</h3>

					<form onsubmit={handleModificationSubmit} class="space-y-6">
						<!-- Comments Section -->
						<div>
							<label for="comentarios" class="block text-sm font-medium text-gray-700">
								Comentarios
							</label>
							<textarea
								id="comentarios"
								bind:value={modificationForm.comentarios}
								rows="4"
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="Comentarios sobre la entrega..."
							></textarea>
							<p class="mt-1 text-xs text-gray-500">
								{modificationForm.comentarios.length}/1000 caracteres
							</p>
						</div>

						<!-- Add Files Section -->
						<div>
							<label for="files" class="block text-sm font-medium text-gray-700">
								Agregar Archivos
							</label>
							<input
								bind:this={fileInputRef}
								type="file"
								id="files"
								multiple
								onchange={handleFileSelect}
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
							/>
							{#if selectedFiles.length > 0}
								<div class="mt-2">
									<p class="text-sm font-medium text-gray-700">Archivos seleccionados:</p>
									<ul class="mt-1 space-y-1">
										{#each selectedFiles as file (file.name)}
											<li class="text-sm text-gray-600">
												{file.name} ({(file.size / 1024).toFixed(1)} KB)
											</li>
										{/each}
									</ul>
								</div>
							{/if}
						</div>

						<!-- File Operations Summary -->
						{#if modificationForm.fileOperations.length > 0}
							<div>
								<h4 class="text-sm font-medium text-gray-700">Operaciones de archivos:</h4>
								<div class="mt-2 space-y-1">
									{#each modificationForm.fileOperations as operation, index (index)}
										<div class="text-sm text-gray-600">
											{operation.tipo === 'ELIMINAR' ? '🗑️' : '✏️'}
											{operation.rutaArchivo.split('/').pop()}
											{#if operation.tipo === 'RENOMBRAR' && operation.nuevoNombre}
												→ {operation.nuevoNombre}
											{/if}
										</div>
									{/each}
								</div>
							</div>
						{/if}

						<div class="flex justify-end space-x-3">
							<button
								type="button"
								onclick={cancelModification}
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
			</div>
		{/if}

		<!-- Rename File Modal -->
		{#if renamingFile}
			<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
				<div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Renombrar Archivo</h3>

					<div class="space-y-4">
						<div>
							<label for="newFileName" class="block text-sm font-medium text-gray-700">
								Nuevo nombre del archivo
							</label>
							<input
								type="text"
								id="newFileName"
								bind:value={newFileName}
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="nuevo-nombre.pdf"
							/>
						</div>

						<div class="flex justify-end space-x-3">
							<button
								type="button"
								onclick={cancelRename}
								class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
							>
								Cancelar
							</button>
							<button
								type="button"
								onclick={confirmRename}
								class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700"
								disabled={!newFileName.trim()}
							>
								Renombrar
							</button>
						</div>
					</div>
				</div>
			</div>
		{/if}

		<!-- Grading Form Modal -->
		{#if gradingMode}
			<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
				<div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Calificar Entrega</h3>

					<form onsubmit={handleGradeSubmit} class="space-y-4">
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
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="8.5"
								required
							/>
						</div>

						<div>
							<label for="comentarios" class="block text-sm font-medium text-gray-700">
								Comentarios
							</label>
							<textarea
								id="comentarios"
								bind:value={gradeForm.comentarios}
								rows="4"
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="Comentarios sobre la entrega..."
							></textarea>
							<p class="mt-1 text-xs text-gray-500">
								{gradeForm.comentarios.length}/1000 caracteres
							</p>
						</div>

						<div class="flex justify-end space-x-3">
							<button
								type="button"
								onclick={cancelGrading}
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
		{/if}
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
