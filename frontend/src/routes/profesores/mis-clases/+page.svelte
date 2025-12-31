<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ClaseService } from '$lib/services/claseService';
	import { MaterialService } from '$lib/services/materialService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { EntregaService } from '$lib/services/entregaService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import type { DTOClase } from '$lib/generated/api';
	import type { Material } from '$lib/generated/api/models/Material';
	import type {
		DTOPeticionCrearClase,
		DTOEjercicio,
		DTOEntregaEjercicio,
		DTOAlumno,
		DTOMaterial
	} from '$lib/generated/api';
	import { FormatterUtils } from '$lib/utils/formatters';

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let myClasses = $state<DTOClase[]>([]);
	let selectedClass = $state<DTOClase | null>(null);
	let classMaterials = $state<Material[]>([]);
	let availableMaterials = $state<Material[]>([]);
	let classExercises = $state<DTOEjercicio[]>([]);
	let selectedExercise = $state<DTOEjercicio | null>(null);
	let exerciseDeliveries = $state<DTOEntregaEjercicio[]>([]);
	let classStudents = $state<DTOAlumno[]>([]);
	let studentsLoading = $state(false);

	// Active tab state
	let activeTab = $state<'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos'>(
		'overview'
	);

	// Function to get initial tab from URL hash
	function getInitialTab(): 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos' {
		if (typeof window !== 'undefined') {
			const hash = window.location.hash.slice(1); // Remove the # symbol
			if (['overview', 'materials', 'exercises', 'grading', 'alumnos'].includes(hash)) {
				return hash as 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos';
			}
		}
		return 'overview';
	}

	// Function to update URL hash when tab changes
	function updateUrlHash(tab: string) {
		if (typeof window !== 'undefined') {
			window.location.hash = tab;
		}
	}

	// Edit mode state
	let editMode = $state(false);
	let editForm = $state<DTOPeticionCrearClase>({
		titulo: '',
		descripcion: '',
		precio: 0,
		presencialidad: 'PRESENCIAL' as const,
		nivel: 'PRINCIPIANTE' as const
	});

	// Material management state
	let showMaterialModal = $state(false);
	let newMaterial = $state({
		name: '',
		url: ''
	});
	// Removed unused variable

	// Grading state
	let showGradingModal = $state(false);
	let selectedDelivery = $state<DTOEntregaEjercicio | null>(null);
	let gradingForm = $state({
		nota: '',
		comentarios: ''
	});

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	// Reactive effect to handle class changes and reload dependent data
	$effect(() => {
		if (selectedClass && activeTab) {
			// Clear dependent data when class changes
			if (activeTab === 'materials') {
				loadClassMaterials(selectedClass.id!);
			} else if (activeTab === 'exercises' || activeTab === 'grading') {
				loadClassExercises(selectedClass.id!);
			} else if (activeTab === 'alumnos') {
				loadClassStudents(selectedClass.id!);
			}
		}
	});

	onMount(() => {
		// Initialize active tab from URL hash
		activeTab = getInitialTab();

		// Listen for hash changes (browser back/forward buttons)
		const handleHashChange = () => {
			const newTab = getInitialTab();
			if (newTab !== activeTab) {
				activeTab = newTab;
				// Load data for the new tab if a class is selected
				if (selectedClass) {
					handleTabChange(newTab);
				}
			}
		};

		window.addEventListener('hashchange', handleHashChange);

		loadMyClasses();

		// Cleanup event listener
		return () => {
			window.removeEventListener('hashchange', handleHashChange);
		};
	});

	async function loadMyClasses() {
		loading = true;
		error = null;

		try {
			myClasses = await ClaseService.getMyClasses();
			if (myClasses.length > 0) {
				selectClass(myClasses[0]);
			}
		} catch (err) {
			error = `Error al cargar mis clases: ${err}`;
			console.error('Error loading my classes:', err);
		} finally {
			loading = false;
		}
	}

	async function selectClass(clase: DTOClase) {
		selectedClass = clase;
		selectedExercise = null;
		exerciseDeliveries = [];
		// Clear students data when switching classes
		classStudents = [];
		editForm = {
			titulo: clase.titulo || '',
			descripcion: clase.descripcion || '',
			precio: clase.precio || 0,
			presencialidad: clase.presencialidad || 'PRESENCIAL',
			nivel: clase.nivel || 'PRINCIPIANTE'
		};

		// Load data based on active tab
		if (activeTab === 'materials') {
			await loadClassMaterials(clase.id!);
		} else if (activeTab === 'exercises') {
			await loadClassExercises(clase.id!);
		} else if (activeTab === 'grading') {
			await loadClassExercises(clase.id!);
		} else if (activeTab === 'alumnos') {
			await loadClassStudents(clase.id!);
		}
	}

	async function loadClassMaterials(claseId: number) {
		try {
			const detailedClass = await ClaseService.getClaseDetallada(claseId);
			// Convert DTOMaterial to Material, filtering out invalid entries
			classMaterials = (detailedClass.material || [])
				.filter((material: DTOMaterial) => material.name && material.url) // Only include materials with required fields
				.map((material: DTOMaterial) => ({
					id: material.id,
					name: material.name!,
					url: material.url!
				}));
		} catch (err) {
			console.error('Error loading class materials:', err);
			classMaterials = [];
		}
	}

	async function loadAvailableMaterials() {
		try {
			const response = await MaterialService.getMaterials({ page: 0, size: 100 });
			availableMaterials = response.content || [];
		} catch (err) {
			console.error('Error loading available materials:', err);
		}
	}

	async function loadClassExercises(claseId: number) {
		try {
			const response = await EjercicioService.getEjercicios({ classId: claseId.toString() });
			classExercises = response.content || [];
		} catch (err) {
			console.error('Error loading class exercises:', err);
			classExercises = [];
		}
	}

	async function selectExercise(ejercicio: DTOEjercicio) {
		selectedExercise = ejercicio;
		await loadExerciseDeliveries(ejercicio.id!);
	}

	async function loadExerciseDeliveries(ejercicioId: number) {
		try {
			const response = await EntregaService.getEntregasByEjercicio(ejercicioId);
			exerciseDeliveries = response.content || [];
		} catch (err) {
			console.error('Error loading exercise deliveries:', err);
			exerciseDeliveries = [];
		}
	}

	async function loadClassStudents(claseId: number) {
		studentsLoading = true;
		try {
			const response = await EnrollmentService.getStudentsInClass(claseId, 0, 100);
			classStudents = response.content || [];
		} catch (err) {
			console.error('Error loading class students:', err);
			classStudents = [];
		} finally {
			studentsLoading = false;
		}
	}

	async function handleTabChange(
		tab: 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos'
	) {
		activeTab = tab;

		// Update URL hash to persist tab state
		updateUrlHash(tab);

		if (selectedClass) {
			if (tab === 'materials') {
				await loadClassMaterials(selectedClass.id!);
			} else if (tab === 'exercises' || tab === 'grading') {
				await loadClassExercises(selectedClass.id!);
			} else if (tab === 'alumnos') {
				await loadClassStudents(selectedClass.id!);
			}
		}
	}

	function startEdit() {
		editMode = true;
	}

	function cancelEdit() {
		editMode = false;
		editForm = {
			titulo: selectedClass?.titulo || '',
			descripcion: selectedClass?.descripcion || '',
			precio: selectedClass?.precio || 0,
			presencialidad: selectedClass?.presencialidad || 'PRESENCIAL',
			nivel: selectedClass?.nivel || 'PRINCIPIANTE'
		};
	}

	async function saveChanges() {
		if (!selectedClass) return;

		try {
			await ClaseService.updateClassParameters(selectedClass.id!, editForm);
			// Create a new object with the updated properties, preserving the original structure
			selectedClass = {
				...selectedClass,
				titulo: editForm.titulo,
				descripcion: editForm.descripcion,
				precio: editForm.precio,
				presencialidad: editForm.presencialidad,
				nivel: editForm.nivel
			};
			editMode = false;

			// Reload classes to get updated data
			await loadMyClasses();
		} catch (err) {
			error = `Error al actualizar la clase: ${err}`;
			console.error('Error updating class:', err);
		}
	}

	function openMaterialModal() {
		showMaterialModal = true;
		loadAvailableMaterials();
	}

	function closeMaterialModal() {
		showMaterialModal = false;
		newMaterial = { name: '', url: '' };
	}

	async function createNewMaterial() {
		if (!selectedClass || !newMaterial.name || !newMaterial.url) return;

		try {
			const material = await MaterialService.createMaterial(newMaterial.name, newMaterial.url);
			// Convert DTOMaterial to Material type for the addMaterialToClass method
			const materialForClass: Material = {
				id: material.id,
				name: material.name || '',
				url: material.url || ''
			};
			await ClaseService.addMaterialToClass(selectedClass.id!, materialForClass);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
			closeMaterialModal();
		} catch (err) {
			error = `Error al crear el material: ${err}`;
			console.error('Error creating material:', err);
		}
	}

	async function addExistingMaterial(material: Material) {
		if (!selectedClass) return;

		try {
			await ClaseService.addMaterialToClass(selectedClass.id!, material);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
			closeMaterialModal();
		} catch (err) {
			error = `Error al agregar el material: ${err}`;
			console.error('Error adding material:', err);
		}
	}

	async function removeMaterial(materialId: number) {
		if (!selectedClass) return;

		if (!confirm('¿Estás seguro de que quieres quitar este material de la clase?')) {
			return;
		}

		try {
			await ClaseService.removeMaterialFromClass(selectedClass.id!, materialId);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
		} catch (err) {
			error = `Error al quitar el material: ${err}`;
			console.error('Error removing material:', err);
		}
	}

	function openGradingModal(delivery: DTOEntregaEjercicio) {
		selectedDelivery = delivery;
		gradingForm = {
			nota: delivery.nota?.toString() || '',
			comentarios: delivery.comentarios || ''
		};
		showGradingModal = true;
	}

	function closeGradingModal() {
		showGradingModal = false;
		selectedDelivery = null;
		gradingForm = { nota: '', comentarios: '' };
	}

	async function handleGradeSubmit(event: Event) {
		event.preventDefault();

		if (!selectedDelivery || !validateGradingForm()) {
			return;
		}

		try {
			const nota = parseFloat(gradingForm.nota);
			const updateData: {
				nota: number;
				comentarios?: string;
			} = { nota };

			if (gradingForm.comentarios && gradingForm.comentarios.trim()) {
				updateData.comentarios = gradingForm.comentarios.trim();
			}

			await EntregaService.updateEntrega(selectedDelivery.id!, updateData);

			// Update local state
			selectedDelivery.nota = nota;
			selectedDelivery.comentarios = updateData.comentarios;
			selectedDelivery.estado = 'CALIFICADO';

			// Reload deliveries to get updated data
			if (selectedExercise) {
				await loadExerciseDeliveries(selectedExercise.id!);
			}

			closeGradingModal();
		} catch (err) {
			error = `Error al calificar la entrega: ${err}`;
			console.error('Error grading delivery:', err);
		}
	}

	function validateGradingForm(): boolean {
		if (!gradingForm.nota || gradingForm.nota.toString().trim() === '') {
			error = 'La nota es obligatoria';
			return false;
		}

		const nota = parseFloat(gradingForm.nota);
		if (isNaN(nota) || nota < 0 || nota > 10) {
			error = 'La nota debe ser un número entre 0 y 10';
			return false;
		}

		if (gradingForm.comentarios && gradingForm.comentarios.trim().length > 1000) {
			error = 'Los comentarios no pueden exceder 1000 caracteres';
			return false;
		}

		return true;
	}

	function createNewExercise() {
		if (selectedClass) {
			goto(`/ejercicios/nuevo?classId=${selectedClass.id}`);
		} else {
			goto('/ejercicios/nuevo');
		}
	}

	function goToClassDetail(claseId: number) {
		goto(`/clases/${claseId}`);
	}

	function goToNewClass() {
		goto('/clases/nuevo');
	}

	function getStatusColor(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'bg-yellow-100 text-yellow-800';
			case 'CALIFICADO':
				return 'bg-green-100 text-green-800';
			case 'ENTREGADO':
				return 'bg-blue-100 text-blue-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function formatStatus(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'Pendiente';
			case 'CALIFICADO':
				return 'Calificado';
			case 'ENTREGADO':
				return 'Entregado';
			default:
				return 'Desconocido';
		}
	}

	function getGradeColor(nota: number | undefined): string {
		if (nota === undefined || nota === null) return 'text-gray-500';
		if (nota >= 9) return 'text-green-600 font-bold';
		if (nota >= 7) return 'text-blue-600 font-semibold';
		if (nota >= 5) return 'text-yellow-600 font-semibold';
		return 'text-red-600 font-semibold';
	}

	function formatGrade(nota: number | undefined): string {
		if (nota === undefined || nota === null) return 'N/A';
		return nota.toFixed(1);
	}
</script>

<svelte:head>
	<title>Gestión de Clases - Academia</title>
</svelte:head>

<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="mb-8 flex items-center justify-between">
			<div>
				<h1
					class="montserrat-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-4xl font-bold text-transparent"
				>
					Gestión de Clases
				</h1>
				<p class="mt-2 font-medium text-gray-600">
					Gestiona tus clases, materiales, ejercicios y calificaciones
				</p>
			</div>
		</div>

		{#if error}
			<div class="mb-6 rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-red-700 shadow-sm">
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
			</div>
		{/if}

		{#if loading}
			<div class="py-16 text-center">
				<div
					class="mx-auto h-16 w-16 animate-spin rounded-full border-4 border-blue-200 -blue-600"
				></div>
				<p class="mt-6 text-lg font-medium text-gray-600">Cargando tus clases...</p>
			</div>
		{:else}
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-4">
				<!-- Classes List -->
				<div class="lg:col-span-1">
					<div
						class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
					>
						<div class="mb-6 flex items-center justify-between">
							<h2
								class="montserrat-semibold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
							>
								Mis Clases
							</h2>
							<button
								onclick={goToNewClass}
								class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
							>
								➕ Nueva
							</button>
						</div>

						{#if myClasses.length === 0}
							<div class="rounded-xl border border-gray-200 bg-gray-50 p-8 text-center">
								<div class="mb-4 text-6xl">📚</div>
								<p class="mb-4 text-gray-600">No tienes clases asignadas aún.</p>
								<button
									onclick={goToNewClass}
									class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
								>
									Crear mi primera clase
								</button>
							</div>
						{:else}
							<div class="space-y-4">
								{#each myClasses as clase (clase.id)}
									<button
										onclick={() => selectClass(clase)}
										class="w-full rounded-xl border border-gray-200 bg-white p-4 text-left transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:shadow-md {selectedClass?.id ===
										clase.id
											? 'border-blue-500 bg-blue-50 shadow-md'
											: ''}"
									>
										<h3 class="font-semibold text-gray-900">{clase.titulo}</h3>
										<p class="mt-2 line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>
										<div class="mt-3 flex items-center justify-between text-xs text-gray-500">
											<span>{clase.numeroAlumnos || 0} alumnos</span>
											<span>{FormatterUtils.formatPrice(clase.precio || 0)}</span>
										</div>
									</button>
								{/each}
							</div>
						{/if}
					</div>
				</div>

				<!-- Class Details and Management -->
				<div class="lg:col-span-3">
					{#if selectedClass}
						<div
							class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
						>
							<!-- Class Header -->
							<div class="mb-8 flex items-start justify-between">
								<div>
									<h2
										class="montserrat-bold mb-2 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-3xl font-bold text-transparent"
									>
										{selectedClass.titulo}
									</h2>
									<p class="font-medium text-gray-600">{selectedClass.descripcion}</p>
								</div>
								<div class="flex space-x-3">
									{#if editMode}
										<button
											onclick={saveChanges}
											class="transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
										>
											💾 Guardar
										</button>
										<button
											onclick={cancelEdit}
											class="rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
										>
											❌ Cancelar
										</button>
									{:else}
										<button
											onclick={startEdit}
											class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
										>
											✏️ Editar
										</button>
										<button
											onclick={() => selectedClass && goToClassDetail(selectedClass.id!)}
											class="rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
										>
											👁️ Ver Detalle
										</button>
									{/if}
								</div>
							</div>

							<!-- Tab Navigation -->
							<div class="mb-8 border-b border-gray-200">
								<nav class="scrollbar-hide -mb-px flex space-x-8 overflow-x-auto">
									<button
										onclick={() => handleTabChange('overview')}
										class="flex-shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition-all duration-200 {activeTab ===
										'overview'
											? 'rounded-t-lg border-blue-500 bg-blue-50 text-blue-600'
											: 'rounded-t-lg ransparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'}"
									>
										📊 Resumen
									</button>
									<button
										onclick={() => handleTabChange('materials')}
										class="flex-shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition-all duration-200 {activeTab ===
										'materials'
											? 'rounded-t-lg border-blue-500 bg-blue-50 text-blue-600'
											: 'rounded-t-lg ransparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'}"
									>
										📚 Materiales
									</button>
									<button
										onclick={() => handleTabChange('exercises')}
										class="flex-shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition-all duration-200 {activeTab ===
										'exercises'
											? 'rounded-t-lg border-blue-500 bg-blue-50 text-blue-600'
											: 'rounded-t-lg ransparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'}"
									>
										✍️ Ejercicios
									</button>
									<button
										onclick={() => handleTabChange('grading')}
										class="flex-shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition-all duration-200 {activeTab ===
										'grading'
											? 'rounded-t-lg border-blue-500 bg-blue-50 text-blue-600'
											: 'rounded-t-lg ransparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'}"
									>
										📝 Calificaciones
									</button>
									<button
										onclick={() => handleTabChange('alumnos')}
										class="flex-shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition-all duration-200 {activeTab ===
										'alumnos'
											? 'rounded-t-lg border-blue-500 bg-blue-50 text-blue-600'
											: 'rounded-t-lg ransparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'}"
									>
										👥 Alumnos
									</button>
								</nav>
							</div>

							<!-- Tab Content -->
							{#if activeTab === 'overview'}
								<!-- Overview Tab -->
								<div class="space-y-8">
									<!-- Class Information -->
									<div class="grid grid-cols-1 gap-8 md:grid-cols-2">
										{#if editMode}
											<!-- Edit Form -->
											<div
												class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
											>
												<h3
													class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
												>
													✏️ Editar Información de la Clase
												</h3>
												<div class="space-y-6">
													<div>
														<label
															for="titulo"
															class="mb-2 block text-sm font-medium text-gray-700"
														>
															Título de la Clase
														</label>
														<input
															id="titulo"
															type="text"
															bind:value={editForm.titulo}
															class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
															placeholder="Título de la clase"
															required
														/>
													</div>

													<div>
														<label
															for="descripcion"
															class="mb-2 block text-sm font-medium text-gray-700"
														>
															Descripción
														</label>
														<textarea
															id="descripcion"
															bind:value={editForm.descripcion}
															rows="3"
															class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
															placeholder="Descripción de la clase"
														></textarea>
													</div>

													<div class="grid grid-cols-2 gap-4">
														<div>
															<label
																for="precio"
																class="mb-2 block text-sm font-medium text-gray-700"
															>
																Precio
															</label>
															<input
																id="precio"
																type="number"
																step="0.01"
																min="0"
																bind:value={editForm.precio}
																class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
																placeholder="0.00"
																required
															/>
														</div>

														<div>
															<label
																for="presencialidad"
																class="mb-2 block text-sm font-medium text-gray-700"
															>
																Modalidad
															</label>
															<select
																id="presencialidad"
																bind:value={editForm.presencialidad}
																class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
															>
																<option value="PRESENCIAL">Presencial</option>
																<option value="ONLINE">Online</option>
																<option value="HIBRIDO">Híbrido</option>
															</select>
														</div>
													</div>

													<div>
														<label for="nivel" class="mb-2 block text-sm font-medium text-gray-700">
															Nivel
														</label>
														<select
															id="nivel"
															bind:value={editForm.nivel}
															class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
														>
															<option value="PRINCIPIANTE">Principiante</option>
															<option value="BASICO">Básico</option>
															<option value="INTERMEDIO">Intermedio</option>
															<option value="AVANZADO">Avanzado</option>
														</select>
													</div>
												</div>
											</div>
										{:else}
											<!-- Display Class Information -->
											<div
												class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
											>
												<h3
													class="montserrat-semibold mb-4 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
												>
													Información de la Clase
												</h3>
												<div class="space-y-3 text-sm text-gray-600">
													<p><strong>Título:</strong> {selectedClass.titulo}</p>
													<p>
														<strong>Precio:</strong>
														{FormatterUtils.formatPrice(selectedClass.precio || 0)}
													</p>
													<p><strong>Presencialidad:</strong> {selectedClass.presencialidad}</p>
													<p><strong>Nivel:</strong> {selectedClass.nivel}</p>
													<p><strong>Alumnos:</strong> {selectedClass.numeroAlumnos || 0}</p>
												</div>
											</div>
										{/if}

										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<h3
												class="montserrat-semibold mb-4 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
											>
												Acciones Rápidas
											</h3>
											<div class="space-y-3">
												<button
													onclick={() => handleTabChange('materials')}
													class="w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
												>
													📚 Gestionar Materiales
												</button>
												<button
													onclick={() => handleTabChange('exercises')}
													class="w-full transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-4 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
												>
													✍️ Gestionar Ejercicios
												</button>
												<button
													onclick={() => handleTabChange('grading')}
													class="w-full transform rounded-lg bg-gradient-to-r from-purple-600 to-pink-600 px-4 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-purple-700 hover:to-pink-700 hover:shadow-lg"
												>
													📝 Revisar Entregas
												</button>
												<button
													onclick={() => handleTabChange('alumnos')}
													class="w-full transform rounded-lg bg-gradient-to-r from-orange-600 to-red-600 px-4 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-orange-700 hover:to-red-700 hover:shadow-lg"
												>
													👥 Gestionar Alumnos
												</button>
											</div>
										</div>
									</div>

									<!-- Quick Stats -->
									<div class="grid grid-cols-1 gap-6 md:grid-cols-3">
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-2 text-3xl font-bold text-blue-600">
												{selectedClass.numeroAlumnos || 0}
											</div>
											<div class="text-sm text-gray-600">Alumnos Inscritos</div>
										</div>
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-2 text-3xl font-bold text-green-600">
												{selectedClass.precio
													? FormatterUtils.formatPrice(selectedClass.precio)
													: 'Gratis'}
											</div>
											<div class="text-sm text-gray-600">Precio por Clase</div>
										</div>
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-2 text-3xl font-bold text-purple-600">
												{selectedClass.presencialidad}
											</div>
											<div class="text-sm text-gray-600">Modalidad</div>
										</div>
									</div>
								</div>
							{:else if activeTab === 'materials'}
								<!-- Materials Tab -->
								<div class="space-y-8">
									<div class="flex items-center justify-between">
										<h3
											class="montserrat-semibold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
										>
											Materiales de la Clase
										</h3>
										<button
											onclick={openMaterialModal}
											class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
										>
											➕ Agregar Material
										</button>
									</div>

									{#if classMaterials.length === 0}
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-4 text-6xl">📚</div>
											<p class="mb-4 text-gray-600">No hay materiales en esta clase.</p>
											<button
												onclick={openMaterialModal}
												class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
											>
												Agregar primer material
											</button>
										</div>
									{:else}
										<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
											{#each classMaterials as material (material.id)}
												<div
													class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
												>
													<h4 class="mb-3 font-semibold text-gray-900">{material.name}</h4>
													<a
														href={material.url}
														target="_blank"
														rel="noopener noreferrer"
														class="mb-3 inline-block w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-center text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
													>
														Ver Material
													</a>
													<button
														onclick={() => removeMaterial(material.id!)}
														class="w-full transform rounded-lg bg-gradient-to-r from-red-600 to-pink-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-red-700 hover:to-pink-700 hover:shadow-lg"
													>
														❌ Quitar
													</button>
												</div>
											{/each}
										</div>
									{/if}
								</div>
							{:else if activeTab === 'exercises'}
								<!-- Exercises Tab -->
								<div class="space-y-8">
									<div class="flex items-center justify-between">
										<h3
											class="montserrat-semibold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
										>
											Ejercicios de la Clase
										</h3>
										<button
											onclick={createNewExercise}
											class="transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
										>
											➕ Nuevo Ejercicio
										</button>
									</div>

									{#if classExercises.length === 0}
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-4 text-6xl">✍️</div>
											<p class="mb-4 text-gray-600">No hay ejercicios en esta clase.</p>
											<button
												onclick={createNewExercise}
												class="transform rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-green-700 hover:to-emerald-700 hover:shadow-lg"
											>
												Crear primer ejercicio
											</button>
										</div>
									{:else}
										<div class="grid gap-6 md:grid-cols-2">
											{#each classExercises as ejercicio (ejercicio.id)}
												<div
													class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
												>
													<h4 class="mb-3 font-semibold text-gray-900">{ejercicio.name}</h4>
													<p class="mb-4 line-clamp-3 text-sm text-gray-600">
														{ejercicio.statement}
													</p>
													<div class="flex items-center justify-between">
														<span class="text-xs text-gray-500">
															{ejercicio.startDate
																? FormatterUtils.formatDate(ejercicio.startDate)
																: 'Sin fecha'}
														</span>
														<button
															onclick={() => goto(`/ejercicios/${ejercicio.id}`)}
															class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
														>
															Ver Detalles
														</button>
													</div>
												</div>
											{/each}
										</div>
									{/if}
								</div>
							{:else if activeTab === 'grading'}
								<!-- Grading Tab -->
								<div class="space-y-8">
									<div class="grid grid-cols-1 gap-8 lg:grid-cols-2">
										<!-- Exercises List -->
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<h3
												class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
											>
												Ejercicios
											</h3>
											{#if classExercises.length === 0}
												<div class="rounded-xl border border-gray-200 bg-gray-50 p-6 text-center">
													<p class="text-gray-600">No hay ejercicios en esta clase.</p>
												</div>
											{:else}
												<div class="space-y-4">
													{#each classExercises as ejercicio (ejercicio.id)}
														<div
															class="rounded-xl border border-gray-200 bg-white p-4 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:shadow-md {selectedExercise?.id ===
															ejercicio.id
																? 'border-blue-500 bg-blue-50 shadow-md'
																: ''}"
														>
															<div class="flex items-center justify-between">
																<button
																	onclick={() => selectExercise(ejercicio)}
																	class="flex-1 text-left"
																>
																	<h4 class="font-semibold text-gray-900">{ejercicio.name}</h4>
																	<p class="mt-2 line-clamp-2 text-sm text-gray-600">
																		{ejercicio.statement}
																	</p>
																</button>
																<button
																	onclick={() => goto(`/ejercicios/${ejercicio.id}`)}
																	class="ml-3 transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-1 text-xs font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
																>
																	Ver Detalles
																</button>
															</div>
														</div>
													{/each}
												</div>
											{/if}
										</div>

										<!-- Deliveries List -->
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<h3
												class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
											>
												Entregas {selectedExercise ? `- ${selectedExercise.name}` : ''}
											</h3>
											{#if !selectedExercise}
												<div class="rounded-xl border border-gray-200 bg-gray-50 p-6 text-center">
													<p class="text-gray-600">
														Selecciona un ejercicio para ver sus entregas.
													</p>
												</div>
											{:else if exerciseDeliveries.length === 0}
												<div class="rounded-xl border border-gray-200 bg-gray-50 p-6 text-center">
													<p class="text-gray-600">No hay entregas para este ejercicio.</p>
												</div>
											{:else}
												<div class="space-y-4">
													{#each exerciseDeliveries as entrega (entrega.id)}
														<div
															class="rounded-xl border border-gray-200 bg-white p-4 transition-all duration-200 hover:shadow-md"
														>
															<div class="flex items-center justify-between">
																<div>
																	<p class="font-semibold text-gray-900">
																		Alumno ID: {entrega.alumnoId}
																	</p>
																	<p class="mt-1 text-sm text-gray-600">
																		Estado:
																		<span
																			class="inline-flex rounded-full px-2 text-xs font-semibold {getStatusColor(
																				entrega.estado
																			)}"
																		>
																			{formatStatus(entrega.estado)}
																		</span>
																	</p>
																	<p class="text-sm text-gray-600">
																		Nota:
																		<span class={getGradeColor(entrega.nota)}>
																			{formatGrade(entrega.nota)}
																		</span>
																	</p>
																</div>
																<div class="flex space-x-2">
																	<button
																		onclick={() => goto(`/entregas/${entrega.id}`)}
																		class="rounded-lg bg-gray-100 px-3 py-2 text-sm font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
																	>
																		👁️ Ver Detalles
																	</button>
																	<button
																		onclick={() => openGradingModal(entrega)}
																		class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
																	>
																		{entrega.nota ? '✏️ Editar' : '📝 Calificar'}
																	</button>
																</div>
															</div>
														</div>
													{/each}
												</div>
											{/if}
										</div>
									</div>
								</div>
							{:else if activeTab === 'alumnos'}
								<!-- Alumnos Tab -->
								<div class="space-y-8">
									<div class="flex items-center justify-between">
										<h3
											class="montserrat-semibold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
										>
											Alumnos Inscritos
										</h3>
										<div class="text-sm text-gray-600">
											Total: {selectedClass?.numeroAlumnos || 0} alumnos
										</div>
									</div>

									{#if studentsLoading}
										<div class="py-16 text-center">
											<div
												class="mx-auto h-16 w-16 animate-spin rounded-full border-4 border-blue-200 -blue-600"
											></div>
											<p class="mt-6 text-lg font-medium text-gray-600">Cargando alumnos...</p>
										</div>
									{:else if classStudents.length === 0}
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-8 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-4 text-6xl">👥</div>
											<p class="mb-4 text-gray-600">No hay alumnos inscritos en esta clase aún.</p>
											<p class="text-sm text-gray-500">
												Los alumnos aparecerán aquí una vez se inscriban a la clase.
											</p>
										</div>
									{:else}
										<!-- Student List -->
										<div
											class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
										>
											<div class="mb-6 flex items-center justify-between">
												<h4 class="text-lg font-semibold text-gray-900">Lista de Alumnos</h4>
												<span class="text-sm text-gray-600">
													{classStudents.length} inscritos
												</span>
											</div>

											<div class="space-y-4">
												{#each classStudents as student (student.id)}
													<div
														class="rounded-xl border border-gray-200 bg-white p-4 transition-all duration-200 hover:shadow-md"
													>
														<div class="flex items-center justify-between">
															<div class="flex items-center space-x-4">
																<div
																	class="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100"
																>
																	<span class="text-sm font-medium text-blue-600">
																		{student.firstName?.charAt(0)}{student.lastName?.charAt(0)}
																	</span>
																</div>
																<div>
																	<h5 class="font-semibold text-gray-900">
																		{student.firstName}
																		{student.lastName}
																	</h5>
																	<p class="text-sm text-gray-600">{student.email}</p>
																	{#if student.phoneNumber}
																		<p class="text-xs text-gray-500">📞 {student.phoneNumber}</p>
																	{/if}
																</div>
															</div>
															<div class="text-right">
																<div class="mb-2 text-sm text-gray-600">
																	<span
																		class="inline-flex items-center rounded-full px-2 text-xs font-semibold {student.enabled
																			? 'bg-green-100 text-green-800'
																			: 'bg-red-100 text-red-800'}"
																	>
																		{student.enabled ? 'Activo' : 'Inactivo'}
																	</span>
																</div>
																{#if student.enrollmentDate}
																	<p class="mb-2 text-xs text-gray-500">
																		📅 {FormatterUtils.formatDate(student.enrollmentDate)}
																	</p>
																{/if}
																<button
																	onclick={() => goto(`/alumnos/${student.id}`)}
																	class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-1 text-xs font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
																>
																	👤 Ver Perfil
																</button>
															</div>
														</div>
													</div>
												{/each}
											</div>
										</div>

										<!-- Quick Stats -->
										<div class="grid grid-cols-1 gap-6 md:grid-cols-3">
											<div
												class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
											>
												<div class="mb-2 text-3xl font-bold text-blue-600">
													{classStudents.length}
												</div>
												<div class="text-sm text-gray-600">Total Inscritos</div>
											</div>
											<div
												class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
											>
												<div class="mb-2 text-3xl font-bold text-green-600">
													{classStudents.filter((s) => s.enabled).length}
												</div>
												<div class="text-sm text-gray-600">Activos</div>
											</div>
											<div
												class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
											>
												<div class="mb-2 text-3xl font-bold text-purple-600">
													{classStudents.filter(
														(s) => s.submissionIds && s.submissionIds.length > 0
													).length}
												</div>
												<div class="text-sm text-gray-600">Con Entregas</div>
											</div>
										</div>
									{/if}
								</div>
							{/if}
						</div>
					{:else}
						<div
							class="rounded-2xl border border-gray-200/50 bg-white/80 p-12 text-center shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl"
						>
							<div class="mb-4 text-6xl">📚</div>
							<p class="mb-4 text-gray-600">Selecciona una clase para comenzar a gestionar.</p>
						</div>
					{/if}
				</div>
			</div>
		{/if}
	</div>
</div>

<!-- Material Modal -->
{#if showMaterialModal}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
		<div
			class="w-full max-w-md rounded-2xl border border-gray-200/50 bg-white/90 p-8 shadow-2xl backdrop-blur-xl"
		>
			<h3
				class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
			>
				Agregar Material
			</h3>

			<!-- Create New Material -->
			<div class="mb-8">
				<h4 class="mb-4 font-semibold text-gray-900">Crear Nuevo Material</h4>
				<div class="space-y-4">
					<input
						type="text"
						bind:value={newMaterial.name}
						placeholder="Nombre del material"
						class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
					/>
					<input
						type="url"
						bind:value={newMaterial.url}
						placeholder="URL del material"
						class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
					/>
					<button
						onclick={createNewMaterial}
						disabled={!newMaterial.name || !newMaterial.url}
						class="w-full transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
					>
						Crear y Agregar
					</button>
				</div>
			</div>

			<!-- Add Existing Material -->
			<div class="mb-8">
				<h4 class="mb-4 font-semibold text-gray-900">Agregar Material Existente</h4>
				{#if availableMaterials.length > 0}
					<div class="max-h-40 space-y-3 overflow-y-auto">
						{#each availableMaterials as material (material.id)}
							<button
								onclick={() => addExistingMaterial(material)}
								class="w-full rounded-xl border border-gray-200 bg-white p-4 text-left transition-all duration-200 hover:bg-gray-50 hover:shadow-md"
							>
								<div class="font-medium text-gray-900">{material.name}</div>
								<div class="text-sm text-gray-600">{material.url}</div>
							</button>
						{/each}
					</div>
				{:else}
					<p class="text-sm text-gray-500">No hay materiales disponibles.</p>
				{/if}
			</div>

			<button
				onclick={closeMaterialModal}
				class="w-full rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
			>
				Cerrar
			</button>
		</div>
	</div>
{/if}

<!-- Grading Modal -->
{#if showGradingModal}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
		<div
			class="w-full max-w-md rounded-2xl border border-gray-200/50 bg-white/90 p-8 shadow-2xl backdrop-blur-xl"
		>
			<h3
				class="montserrat-semibold mb-6 bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-xl font-semibold text-transparent"
			>
				Calificar Entrega
			</h3>

			<form onsubmit={handleGradeSubmit} class="space-y-6">
				<div>
					<label for="nota" class="mb-2 block text-sm font-medium text-gray-700">Nota (0-10)</label>
					<input
						id="nota"
						type="number"
						min="0"
						max="10"
						step="0.1"
						bind:value={gradingForm.nota}
						class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
						required
					/>
				</div>

				<div>
					<label for="comentarios" class="mb-2 block text-sm font-medium text-gray-700"
						>Comentarios (opcional)</label
					>
					<textarea
						id="comentarios"
						bind:value={gradingForm.comentarios}
						rows="3"
						maxlength="1000"
						class="w-full rounded-lg border border-gray-200 px-4 py-3 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
						placeholder="Comentarios sobre la entrega..."
					></textarea>
				</div>

				<div class="flex space-x-4">
					<button
						type="submit"
						class="flex-1 transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
					>
						Guardar Calificación
					</button>
					<button
						type="button"
						onclick={closeGradingModal}
						class="flex-1 rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
					>
						Cancelar
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
