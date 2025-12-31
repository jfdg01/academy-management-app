<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOProfesor } from '$lib/generated/api';
	import { ProfesorService } from '$lib/services/profesorService';
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
	let profesores = $state<DTOProfesor[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);
	let sortBy = $state('id');
	let sortDirection = $state<'ASC' | 'DESC'>('ASC');

	// Search configuration
	const searchConfig = getSearchConfig('profesores');

	// Search and filters
	let currentFilters = $state<EntityFilters>({
		searchMode: 'simple',
		q: '',
		firstName: '',
		lastName: '',
		dni: '',
		email: '',
		enabled: ''
	});

	// Modal state
	let showDeleteModal = $state(false);
	let profesorToDelete: DTOProfesor | null = $state(null);

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
			loadProfesores();
		}
	});

	// Data loading with new search parameters
	async function loadProfesores() {
		loading = true;
		error = null;

		try {
			const filters: Record<string, unknown> = {};

			// Add search parameters
			if (currentFilters.q && typeof currentFilters.q === 'string' && currentFilters.q.trim()) {
				filters.q = currentFilters.q.trim();
			}
			if (
				currentFilters.firstName &&
				typeof currentFilters.firstName === 'string' &&
				currentFilters.firstName.trim()
			) {
				filters.firstName = currentFilters.firstName.trim();
			}
			if (
				currentFilters.lastName &&
				typeof currentFilters.lastName === 'string' &&
				currentFilters.lastName.trim()
			) {
				filters.lastName = currentFilters.lastName.trim();
			}
			if (
				currentFilters.dni &&
				typeof currentFilters.dni === 'string' &&
				currentFilters.dni.trim()
			) {
				filters.dni = currentFilters.dni.trim();
			}
			if (
				currentFilters.email &&
				typeof currentFilters.email === 'string' &&
				currentFilters.email.trim()
			) {
				filters.email = currentFilters.email.trim();
			}
			if (currentFilters.enabled !== '' && currentFilters.enabled !== null) {
				filters.enabled = currentFilters.enabled === 'true';
			}

			const response = await ProfesorService.getProfesoresPaginados(filters, {
				page: currentPage,
				size: pageSize,
				sortBy: sortBy,
				sortDirection: sortDirection
			});

			profesores = response.content || [];
			totalElements = response.totalElements || 0;
			totalPages = response.totalPages || 0;
			// currentPage is managed locally, not returned from API
		} catch (err) {
			error = `Error al cargar profesores: ${err}`;
			console.error('Error loading profesores:', err);
		} finally {
			loading = false;
		}
	}

	// Navigation functions - convert 1-based UI to 0-based API
	function goToPage(page: number) {
		// Convert from 1-based UI to 0-based API
		currentPage = page - 1;
		loadProfesores();
	}

	function changePageSize(newSize: number) {
		pageSize = newSize;
		currentPage = 0; // Reset to first page (0-based)
		loadProfesores();
	}

	// Search and filter functions
	function updateFilters(filters: Record<string, unknown>) {
		Object.assign(currentFilters, filters);
		currentPage = 0; // Reset to first page (0-based)
		loadProfesores();
	}

	function clearFilters() {
		currentFilters = {
			searchMode: 'simple',
			q: '',
			firstName: '',
			lastName: '',
			dni: '',
			email: '',
			enabled: ''
		};
		currentPage = 0; // Reset to first page (0-based)
		loadProfesores();
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		currentFilters.searchMode = mode;
	}

	// Professor actions
	async function toggleAccountStatus(profesor: DTOProfesor) {
		if (!authStore.isAdmin) return;

		try {
			loading = true;
			const result = await ProfesorService.handleStatusChange(profesor.id!, !profesor.enabled);
			if (result.success && result.updatedProfesor) {
				successMessage = `Cuenta ${result.updatedProfesor.enabled ? 'habilitada' : 'deshabilitada'}`;
				loadProfesores();
			} else {
				error = result.message;
			}
		} catch (err) {
			error = `Error al cambiar estado de cuenta: ${err}`;
		} finally {
			loading = false;
		}
	}

	function openDeleteModal(profesor: DTOProfesor) {
		profesorToDelete = profesor;
		showDeleteModal = true;
	}

	async function deleteProfesor() {
		if (!profesorToDelete || !authStore.isAdmin) return;

		try {
			loading = true;
			await ProfesorService.deleteProfesor(profesorToDelete.id!);
			successMessage = 'Profesor eliminado correctamente';
			showDeleteModal = false;
			profesorToDelete = null;
			loadProfesores();
		} catch (err) {
			error = `Error al eliminar profesor: ${err}`;
		} finally {
			loading = false;
		}
	}

	// Enhanced table configuration using utilities
	const tableColumns = createColumns({
		id: commonColumns.teacher.id,
		name: commonColumns.teacher.name,
		email: commonColumns.teacher.email,
		dni: commonColumns.teacher.dni,
		enabled: commonColumns.teacher.enabled,
		createdAt: commonColumns.teacher.createdAt
	});

	const tableActions = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (profesor: DTOProfesor) => goto(`/profesores/${profesor.id}`),
			condition: () => true
		},
		{
			label: 'Habilitar/Deshabilitar',
			dynamicLabel: (profesor: DTOProfesor) => (profesor.enabled ? 'Deshabilitar' : 'Habilitar'),
			color: 'yellow',
			hoverColor: 'yellow',
			condition: () => !!authStore.isAdmin,
			action: toggleAccountStatus
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red',
			condition: () => !!authStore.isAdmin,
			action: openDeleteModal
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
		{ value: 'username', label: 'Usuario' },
		{ value: 'firstName', label: 'Nombre' },
		{ value: 'lastName', label: 'Apellidos' },
		{ value: 'dni', label: 'DNI' },
		{ value: 'email', label: 'Email' },
		{ value: 'enabled', label: 'Habilitado' },
		{ value: 'createdAt', label: 'Fecha Creación' }
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
		loadProfesores();
	}

	function exportResults() {
		// Implement export functionality
		console.log('Export functionality to be implemented');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Profesores</h1>
		{#if authStore.isAdmin}
			<button
				onclick={() => goto('/profesores/nuevo')}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
			>
				Nuevo Profesor
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
			content: profesores as unknown as Record<string, unknown>[],
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
			content: profesores as unknown as Record<string, unknown>[],
			totalElements,
			totalPages,
			currentPage
		} as PaginatedEntities<Record<string, unknown>>}
		{currentPagination}
		{authStore}
		columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
		actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
		entityName="profesor"
		entityNamePlural="profesores"
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
	entity={profesorToDelete as unknown as Record<string, unknown>}
	entityName="profesor"
	entityNameCapitalized="Profesor"
	displayNameField="fullName"
	on:cancelDelete={() => {
		showDeleteModal = false;
		profesorToDelete = null;
	}}
	on:confirmDelete={deleteProfesor}
/>
