<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOMaterial } from '$lib/generated/api';
	import { MaterialService } from '$lib/services/materialService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import {
		EntitySearchSection,
		EnhancedDataTable,
		EntityPaginationControls,
		EntityMessages,
		EntityDeleteModal,
		createColumns
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
	let materials = $state<DTOMaterial[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0); // 0-based for API
	let pageSize = $state(20);
	let sortBy = $state('name');
	let sortDirection = $state<'ASC' | 'DESC'>('ASC');

	// Search configuration
	const searchConfig = getSearchConfig('materiales');

	// Search and filters
	let currentFilters = $state<EntityFilters>({
		searchMode: 'simple',
		q: '',
		name: '',
		url: '',
		type: ''
	});

	// Modal state
	let showDeleteModal = $state(false);
	let materialToDelete: DTOMaterial | null = $state(null);

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
			loadMaterials();
		}
	});

	// Data loading with new search parameters
	async function loadMaterials() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage, // 0-based for API
				size: pageSize,
				sortBy: sortBy,
				sortDirection: sortDirection
			};

			// Add search parameters
			if (currentFilters.q && typeof currentFilters.q === 'string' && currentFilters.q.trim()) {
				params.q = currentFilters.q.trim();
			}
			if (
				currentFilters.name &&
				typeof currentFilters.name === 'string' &&
				currentFilters.name.trim()
			) {
				params.name = currentFilters.name.trim();
			}
			if (
				currentFilters.url &&
				typeof currentFilters.url === 'string' &&
				currentFilters.url.trim()
			) {
				params.url = currentFilters.url.trim();
			}
			if (
				currentFilters.type &&
				typeof currentFilters.type === 'string' &&
				currentFilters.type.trim()
			) {
				params.type = currentFilters.type.trim();
			}

			console.log('Loading materials with params:', params);
			const response = await MaterialService.getMaterials(params);
			console.log('Materials response:', response);

			materials = response.content || [];
			totalElements = response.totalElements || 0;
			totalPages = response.totalPages || 0;
		} catch (err) {
			error = `Error al cargar materiales: ${err}`;
			console.error('Error loading materials:', err);
		} finally {
			loading = false;
		}
	}

	// Navigation functions - convert 1-based UI to 0-based API
	function goToPage(page: number) {
		// Convert from 1-based UI to 0-based API
		currentPage = page - 1;
		loadMaterials();
	}

	function changePageSize(newSize: number) {
		pageSize = newSize;
		currentPage = 0; // Reset to first page (0-based)
		loadMaterials();
	}

	// Search and filter functions
	function updateFilters(filters: Record<string, unknown>) {
		Object.assign(currentFilters, filters);
		currentPage = 0; // Reset to first page (0-based)
		loadMaterials();
	}

	function clearFilters() {
		currentFilters = {
			searchMode: 'simple',
			q: '',
			name: '',
			url: '',
			type: ''
		};
		currentPage = 0; // Reset to first page (0-based)
		loadMaterials();
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		currentFilters.searchMode = mode;
	}

	function openDeleteModal(material: DTOMaterial) {
		materialToDelete = material;
		showDeleteModal = true;
	}

	async function deleteMaterial() {
		if (!materialToDelete || !authStore.isAdmin) return;

		try {
			loading = true;
			await MaterialService.deleteMaterial(materialToDelete.id!);
			successMessage = 'Material eliminado exitosamente';
			showDeleteModal = false;
			materialToDelete = null;
			loadMaterials();
		} catch (err) {
			error = `Error al eliminar material: ${err}`;
		} finally {
			loading = false;
		}
	}

	// Enhanced table configuration using utilities
	const tableColumns = createColumns({
		name: {
			key: 'name',
			header: 'Name',
			sortable: true,
			formatter: (value) => value as string
		},
		url: {
			key: 'url',
			header: 'URL',
			sortable: true,
			formatter: (value) => {
				const url = value as string;
				return url.length > 50 ? url.substring(0, 50) + '...' : url;
			}
		},
		type: {
			key: 'materialType',
			header: 'Type',
			sortable: false, // Disable sorting for now since it's computed
			formatter: (value, entity) => {
				return MaterialService.getMaterialTypeLabel(entity as DTOMaterial);
			}
		},
		icon: {
			key: 'icon',
			header: 'Icon',
			sortable: false,
			cell: (entity) => {
				return MaterialService.getMaterialIcon(entity as DTOMaterial);
			}
		}
	});

	const tableActions = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (material: DTOMaterial) => goto(`/materiales/${material.id}`),
			condition: () => true
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
		{ value: 'name', label: 'Name' },
		{ value: 'url', label: 'URL' }
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
		loadMaterials();
	}

	function exportResults() {
		// Implement export functionality
		console.log('Export functionality to be implemented');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Materials</h1>
		{#if authStore.isAdmin}
			<button
				onclick={() => goto('/materiales/nuevo')}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
			>
				Add Material
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
			content: materials as unknown as Record<string, unknown>[],
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
			content: materials as unknown as Record<string, unknown>[],
			totalElements,
			totalPages,
			currentPage
		} as PaginatedEntities<Record<string, unknown>>}
		{currentPagination}
		{authStore}
		columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
		actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
		entityName="material"
		entityNamePlural="materiales"
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
	entity={materialToDelete as unknown as Record<string, unknown>}
	entityName="material"
	entityNameCapitalized="Material"
	displayNameField="name"
	on:cancelDelete={() => {
		showDeleteModal = false;
		materialToDelete = null;
	}}
	on:confirmDelete={deleteMaterial}
/>
