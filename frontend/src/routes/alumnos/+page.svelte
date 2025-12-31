<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import {
		EntitySearchSection,
		EnhancedDataTable,
		EntityPaginationControls,
		EntityMessages,
		EntityDeleteModal,
		createColumns,
		commonColumns
	} from '$lib/components/common';
	import type {
		EntityFilters,
		PaginatedEntities,
		EntityColumn,
		EntityAction
	} from '$lib/components/common/types';
	import { getSearchConfig } from '$lib/components/common/searchConfigs';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let alumnos = $state<DTOAlumno[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);
	let sortBy = $state('id');
	let sortDirection = $state<'ASC' | 'DESC'>('ASC');

	// Search configuration
	const searchConfig = getSearchConfig('alumnos');

	// Search and filters
	let currentFilters = $state<EntityFilters>({
		searchMode: 'simple',
		q: '',
		firstName: '',
		lastName: '',
		dni: '',
		email: '',
		enrolled: '',
		enabled: ''
	});

	// Modal state
	let showDeleteModal = $state(false);
	let alumnoToDelete: DTOAlumno | null = $state(null);

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

	// Load data when component mounts or filters change
	$effect(() => {
		if (authStore.isAuthenticated && (authStore.isAdmin || authStore.isProfesor)) {
			loadAlumnos();
		}
	});

	// Data loading with new search parameters
	async function loadAlumnos() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage,
				size: pageSize,
				sortBy: sortBy,
				sortDirection: sortDirection
			};

			// Add search parameters
			if (currentFilters.q && typeof currentFilters.q === 'string' && currentFilters.q.trim()) {
				params.q = currentFilters.q.trim();
			}
			if (
				currentFilters.firstName &&
				typeof currentFilters.firstName === 'string' &&
				currentFilters.firstName.trim()
			) {
				params.firstName = currentFilters.firstName.trim();
			}
			if (
				currentFilters.lastName &&
				typeof currentFilters.lastName === 'string' &&
				currentFilters.lastName.trim()
			) {
				params.lastName = currentFilters.lastName.trim();
			}
			if (
				currentFilters.dni &&
				typeof currentFilters.dni === 'string' &&
				currentFilters.dni.trim()
			) {
				params.dni = currentFilters.dni.trim();
			}
			if (
				currentFilters.email &&
				typeof currentFilters.email === 'string' &&
				currentFilters.email.trim()
			) {
				params.email = currentFilters.email.trim();
			}
			if (currentFilters.enrolled !== '' && currentFilters.enrolled !== null) {
				params.enrolled = currentFilters.enrolled === 'true';
			}
			if (currentFilters.enabled !== '' && currentFilters.enabled !== null) {
				params.enabled = currentFilters.enabled === 'true';
			}

			const response = await AlumnoService.getAlumnos(params);

			alumnos = response.content || [];
			totalElements = response.totalElements || 0;
			totalPages = response.totalPages || 0;
			// currentPage is managed locally, not returned from API
		} catch (err) {
			error = `Error al cargar alumnos: ${err}`;
			console.error('Error loading alumnos:', err);
		} finally {
			loading = false;
		}
	}

	// Navigation functions - convert 1-based UI to 0-based API
	function goToPage(page: number) {
		// Convert from 1-based UI to 0-based API
		currentPage = page - 1;
		loadAlumnos();
	}

	function changePageSize(newSize: number) {
		pageSize = newSize;
		currentPage = 0; // Reset to first page (0-based)
		loadAlumnos();
	}

	// Search and filter functions
	function updateFilters(filters: Record<string, unknown>) {
		Object.assign(currentFilters, filters);
		currentPage = 0; // Reset to first page (0-based)
		loadAlumnos();
	}

	function clearFilters() {
		currentFilters = {
			searchMode: 'simple',
			q: '',
			firstName: '',
			lastName: '',
			dni: '',
			email: '',
			enrolled: '',
			enabled: ''
		};
		currentPage = 0; // Reset to first page (0-based)
		loadAlumnos();
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		currentFilters.searchMode = mode;
	}

	// Student actions
	async function toggleEnrollmentStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.changeEnrollmentStatus(alumno.id || 0, !alumno.enrolled);
			successMessage = `Alumno ${alumno.enrolled ? 'desmatriculado' : 'matriculado'} exitosamente`;
			loadAlumnos();
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
		} finally {
			loading = false;
		}
	}

	async function toggleEnabledStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.toggleEnabled(alumno.id || 0, !alumno.enabled);
			successMessage = `Alumno ${alumno.enabled ? 'deshabilitado' : 'habilitado'} exitosamente`;
			loadAlumnos();
		} catch (err) {
			error = `Error al cambiar estado del alumno: ${err}`;
		} finally {
			loading = false;
		}
	}

	function openDeleteModal(alumno: DTOAlumno) {
		alumnoToDelete = alumno;
		showDeleteModal = true;
	}

	async function deleteAlumno() {
		if (!alumnoToDelete || !authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.deleteAlumno(alumnoToDelete.id || 0);
			successMessage = 'Alumno eliminado exitosamente';
			showDeleteModal = false;
			alumnoToDelete = null;
			loadAlumnos();
		} catch (err) {
			error = `Error al eliminar alumno: ${err}`;
		} finally {
			loading = false;
		}
	}

	// Enhanced table configuration using utilities
	const tableColumns = createColumns({
		id: commonColumns.student.id,
		name: commonColumns.student.name,
		email: commonColumns.student.email,
		dni: commonColumns.student.dni,
		enrollment: commonColumns.student.enrollment,
		enabled: commonColumns.student.enabled
	});

	const tableActions = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (alumno: DTOAlumno) => goto(`/alumnos/${alumno.id}`),
			condition: () => true
		},
		{
			label: 'Matricular',
			color: 'green',
			hoverColor: 'green',
			action: toggleEnrollmentStatus,
			condition: (alumno: DTOAlumno) => !alumno.enrolled && authStore.isAdmin
		},
		{
			label: 'Desmatricular',
			color: 'yellow',
			hoverColor: 'yellow',
			action: toggleEnrollmentStatus,
			condition: (alumno: DTOAlumno) => alumno.enrolled && authStore.isAdmin
		},
		{
			label: 'Activar',
			color: 'green',
			hoverColor: 'green',
			action: toggleEnabledStatus,
			condition: (alumno: DTOAlumno) => !alumno.enabled && authStore.isAdmin
		},
		{
			label: 'Desactivar',
			color: 'red',
			hoverColor: 'red',
			action: toggleEnabledStatus,
			condition: (alumno: DTOAlumno) => alumno.enabled && authStore.isAdmin
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red',
			action: openDeleteModal,
			condition: () => authStore.isAdmin
		}
	];

	// Pagination configuration - use proper pageDisplayInfo with 1-based indexing for UI
	const pageDisplayInfo = $derived({
		currentPage: currentPage + 1, // Convert to 1-based for UI
		totalPages,
		totalItems: totalElements,
		startItem: totalElements > 0 ? currentPage * pageSize + 1 : 0,
		endItem: Math.min(totalElements, (currentPage + 1) * pageSize),
		hasNext: currentPage < totalPages - 1,
		hasPrevious: currentPage > 0,
		isFirstPage: currentPage === 0,
		isLastPage: currentPage >= totalPages - 1
	});

	const currentPagination = $derived({
		page: currentPage, // Keep 0-based for API
		size: pageSize,
		sortBy: sortBy,
		sortDirection: sortDirection
	});

	const sortFields = [
		{ value: 'id', label: 'ID' },
		{ value: 'firstName', label: 'Nombre' },
		{ value: 'lastName', label: 'Apellidos' },
		{ value: 'dni', label: 'DNI' },
		{ value: 'email', label: 'Email' },
		{ value: 'enrolled', label: 'Estado de Matrícula' },
		{ value: 'enabled', label: 'Estado' }
	];

	const pageSizeOptions = [10, 20, 50, 100];

	function changeSorting(field: string | { value: string; direction: 'ASC' | 'DESC' }) {
		if (typeof field === 'string') {
			// Toggle direction if same field, otherwise set to ASC
			if (sortBy === field) {
				sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
			} else {
				sortBy = field;
				sortDirection = 'ASC';
			}
		} else {
			sortBy = field.value;
			sortDirection = field.direction;
		}

		currentPage = 0; // Reset to first page (0-based) when sorting
		loadAlumnos();
	}

	function exportResults() {
		// Implement export functionality
		console.log('Export functionality to be implemented');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Alumnos</h1>
		{#if authStore.isAdmin}
			<button
				onclick={() => goto('/alumnos/nuevo')}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
			>
				Nuevo Alumno
			</button>
		{/if}
	</div>

	<EntityMessages
		{successMessage}
		{error}
		on:clearSuccess={() => (successMessage = null)}
		on:clearError={() => (error = null)}
	/>

	<EntitySearchSection
		{currentFilters}
		paginatedData={{
			content: alumnos as unknown as Record<string, unknown>[],
			totalElements,
			totalPages,
			currentPage
		} as PaginatedEntities<Record<string, unknown>>}
		{loading}
		entityNamePlural={searchConfig.entityNamePlural}
		advancedFields={searchConfig.advancedFields}
		statusField={searchConfig.statusField}
		entityType={searchConfig.entityType}
		on:updateFilters={(e) => updateFilters(e.detail)}
		on:clearFilters={clearFilters}
		on:switchSearchMode={(e) => switchSearchMode(e.detail)}
		on:exportResults={exportResults}
	/>

	<div class="pt-10 pb-5">
		<EntityPaginationControls
			{pageDisplayInfo}
			{currentPagination}
			{sortFields}
			{pageSizeOptions}
			on:goToPage={(e) => goToPage(e.detail)}
			on:changePageSize={(e) => changePageSize(e.detail)}
			on:changeSorting={(e) => changeSorting(e.detail)}
		/>
	</div>

	<EnhancedDataTable
		{loading}
		paginatedData={{
			content: alumnos as unknown as Record<string, unknown>[],
			totalElements,
			totalPages,
			currentPage
		} as PaginatedEntities<Record<string, unknown>>}
		{currentPagination}
		{authStore}
		columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
		actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
		entityName="alumno"
		entityNamePlural="alumnos"
		theme="modern"
		showRowNumbers={true}
		on:changeSorting={(e) => changeSorting(e.detail)}
	/>

	{#if pageDisplayInfo && pageDisplayInfo.totalPages > 1}
		<div class="mt-6 flex flex-col items-center gap-4">
			<EntityPaginationControls
				{pageDisplayInfo}
				{currentPagination}
				{sortFields}
				{pageSizeOptions}
				justifyContent="center"
				showSortAndSize={false}
				on:goToPage={(e) => goToPage(e.detail)}
				on:changePageSize={(e) => changePageSize(e.detail)}
				on:changeSorting={(e) => changeSorting(e.detail)}
			/>
		</div>
	{/if}
</div>

<EntityDeleteModal
	showModal={showDeleteModal}
	entity={alumnoToDelete as unknown as Record<string, unknown>}
	entityName="alumno"
	entityNameCapitalized="Alumno"
	displayNameField="nombreCompleto"
	on:cancelDelete={() => {
		showDeleteModal = false;
		alumnoToDelete = null;
	}}
	on:confirmDelete={deleteAlumno}
/>
