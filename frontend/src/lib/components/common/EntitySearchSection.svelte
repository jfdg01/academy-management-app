<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { EntityFilters, PaginatedEntities } from './types';

	const dispatch = createEventDispatcher();

	const {
		currentFilters,
		paginatedData = null,
		loading = false,
		entityNamePlural = 'elementos',
		advancedFields = [],
		statusField = null,
		entityType = 'generic' // 'alumnos', 'profesores', 'clases', 'generic'
	} = $props<{
		currentFilters: EntityFilters;
		paginatedData?: PaginatedEntities<Record<string, unknown>> | null;
		loading?: boolean;
		entityNamePlural?: string;
		advancedFields?: Array<{
			key: string;
			label: string;
			type?: 'text' | 'email' | 'number' | 'tel' | 'date' | 'select' | string;
			placeholder?: string;
			options?: Array<{ value: string | number | boolean; label: string }>;
		}>;
		statusField?: {
			key: string;
			label: string;
			options: Array<{ value: string | number | boolean; label: string }>;
		} | null;
		entityType?: 'alumnos' | 'profesores' | 'clases' | 'materiales' | 'generic';
	}>();

	// Local state for input values to prevent focus loss
	let localFilters = $state({ ...currentFilters });

	// Update localFilters when currentFilters changes, but preserve object reference for binding
	$effect(() => {
		Object.assign(localFilters, currentFilters);
	});

	// Debounced search
	let searchTimeout: ReturnType<typeof setTimeout> | null = null;

	function debouncedSearch(value: string, filterKey: string) {
		if (searchTimeout) {
			clearTimeout(searchTimeout);
		}

		searchTimeout = setTimeout(() => {
			updateFilters({ [filterKey]: value });
		}, 500); // 500ms delay
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		dispatch('switchSearchMode', mode);
	}

	function updateFilters(filters: Record<string, unknown>) {
		dispatch('updateFilters', filters);
	}

	function clearFilters() {
		dispatch('clearFilters');
	}

	function exportResults() {
		dispatch('exportResults');
	}

	function handleSubmit(event: Event) {
		event.preventDefault();
		updateFilters(localFilters);
	}

	// Get entity-specific placeholder text
	function getGeneralSearchPlaceholder(): string {
		switch (entityType) {
			case 'alumnos':
				return 'Buscar por nombre, apellidos, DNI, email...';
			case 'profesores':
				return 'Buscar por nombre, apellidos, email, usuario, DNI...';
			case 'clases':
				return 'Buscar por título, descripción...';
			case 'materiales':
				return 'Buscar por nombre, URL...';
			default:
				return 'Buscar...';
		}
	}

	// Get entity-specific help text
	function getGeneralSearchHelpText(): string {
		switch (entityType) {
			case 'alumnos':
				return 'Búsqueda general: nombre, apellidos, DNI, email';
			case 'profesores':
				return 'Búsqueda general: nombre, apellidos, email, usuario, DNI';
			case 'clases':
				return 'Búsqueda general: título, descripción';
			case 'materiales':
				return 'Búsqueda general: nombre, URL';
			default:
				return 'Búsqueda general en todos los campos relevantes';
		}
	}
</script>

<div class="mb-6 rounded-lg bg-white p-6 shadow-md">
	<!-- Search Mode Toggle -->
	<div class="mb-6 flex items-center justify-between">
		<h2 class="text-lg font-semibold">Búsqueda de {entityNamePlural}</h2>
		<div class="flex space-x-2">
			<button
				onclick={() => switchSearchMode('simple')}
				class="rounded-md px-3 py-1 text-sm transition-colors {currentFilters.searchMode ===
				'simple'
					? 'bg-blue-600 text-white'
					: 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
			>
				🔍 Búsqueda Simple
			</button>
			<button
				onclick={() => switchSearchMode('advanced')}
				class="rounded-md px-3 py-1 text-sm transition-colors {currentFilters.searchMode ===
				'advanced'
					? 'bg-blue-600 text-white'
					: 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
			>
				⚙️ Búsqueda Avanzada
			</button>
		</div>
	</div>

	<form onsubmit={handleSubmit}>
		{#if currentFilters.searchMode === 'simple'}
			<!-- Simple Search Mode -->
			<div class="space-y-4">
				<div>
					<label for="busquedaGeneral" class="mb-2 block text-sm font-medium text-gray-700">
						Búsqueda General
					</label>
					<div class="relative">
						<input
							id="busquedaGeneral"
							type="text"
							bind:value={localFilters.q}
							oninput={() => {
								debouncedSearch(localFilters.q || '', 'q');
							}}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									if (searchTimeout) clearTimeout(searchTimeout);
									updateFilters({ q: localFilters.q });
								}
							}}
							onblur={() => {
								if (searchTimeout) clearTimeout(searchTimeout);
								updateFilters({ q: localFilters.q });
							}}
							class="w-full rounded-lg border border-gray-300 py-3 pr-4 pl-10 focus:ransparent focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder={getGeneralSearchPlaceholder()}
						/>
						<div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
							<svg
								class="h-5 w-5 text-gray-400"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
								></path>
							</svg>
						</div>
					</div>
					<p class="mt-1 text-xs text-gray-500">{getGeneralSearchHelpText()}</p>
				</div>

				<!-- Status Filter -->
				{#if statusField}
					<div>
						<label for="statusFilter" class="mb-1 block text-sm font-medium text-gray-700"
							>{statusField.label}</label
						>
						<select
							id="statusFilter"
							bind:value={localFilters[statusField.key]}
							onchange={() => updateFilters({ [statusField.key]: localFilters[statusField.key] })}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						>
							{#each statusField.options as option (option.value)}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>
				{/if}
			</div>
		{:else}
			<!-- Advanced Search Mode -->
			<div class="space-y-6">
				<!-- General Search in Advanced Mode -->
				<div>
					<h3 class="text-md mb-3 font-medium text-gray-800">🔍 Búsqueda General</h3>
					<div>
						<label
							for="busquedaGeneralAdvanced"
							class="mb-1 block text-sm font-medium text-gray-700"
						>
							Término de búsqueda general
						</label>
						<input
							id="busquedaGeneralAdvanced"
							type="text"
							bind:value={localFilters.q}
							onblur={() => updateFilters({ q: localFilters.q })}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									updateFilters({ q: localFilters.q });
								}
							}}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder={getGeneralSearchPlaceholder()}
						/>
						<p class="mt-1 text-xs text-gray-500">{getGeneralSearchHelpText()}</p>
					</div>
				</div>

				<!-- Specific Fields -->
				{#if advancedFields.length > 0}
					<div>
						<h3 class="text-md mb-3 font-medium text-gray-800">📝 Campos Específicos</h3>
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#each advancedFields as field (field.key)}
								<div>
									<label for={field.key} class="mb-1 block text-sm font-medium text-gray-700"
										>{field.label}</label
									>
									{#if field.type === 'select' && field.options}
										<select
											id={field.key}
											bind:value={localFilters[field.key]}
											onchange={() => updateFilters({ [field.key]: localFilters[field.key] })}
											class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
										>
											<option value="">Todos</option>
											{#each field.options as option (option.value)}
												<option value={option.value}>{option.label}</option>
											{/each}
										</select>
									{:else}
										<input
											id={field.key}
											type={field.type || 'text'}
											bind:value={localFilters[field.key]}
											onblur={() => updateFilters({ [field.key]: localFilters[field.key] })}
											onkeydown={(e) => {
												if (e.key === 'Enter') {
													updateFilters({ [field.key]: localFilters[field.key] });
												}
											}}
											class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
											placeholder={field.placeholder || `Ej: ${field.label}`}
										/>
									{/if}
								</div>
							{/each}
						</div>
					</div>
				{/if}

				<!-- Status Filter in Advanced Mode -->
				{#if statusField}
					<div>
						<h3 class="text-md mb-3 font-medium text-gray-800">⚙️ Estados</h3>
						<div>
							<label for="statusFilterAdvanced" class="mb-1 block text-sm font-medium text-gray-700"
								>{statusField.label}</label
							>
							<select
								id="statusFilterAdvanced"
								bind:value={localFilters[statusField.key]}
								onchange={() => updateFilters({ [statusField.key]: localFilters[statusField.key] })}
								class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							>
								{#each statusField.options as option (option.value)}
									<option value={option.value}>{option.label}</option>
								{/each}
							</select>
						</div>
					</div>
				{/if}
			</div>
		{/if}
	</form>

	<!-- Action Buttons -->
	<div class="mt-6 flex flex-wrap gap-3  pt-4">
		<button
			onclick={clearFilters}
			class="rounded-md bg-gray-500 px-4 py-2 text-white transition-colors hover:bg-gray-600 focus:ring-2 focus:ring-gray-500 focus:outline-none"
		>
			🗑️ Limpiar Filtros
		</button>
		{#if paginatedData?.content && paginatedData.content.length > 0}
			<button
				onclick={exportResults}
				disabled={loading}
				class="rounded-md px-4 py-2 text-white transition-colors focus:ring-2 focus:outline-none {loading
					? 'cursor-not-allowed bg-gray-400'
					: 'bg-green-500 hover:bg-green-600 focus:ring-green-500'}"
			>
				{#if loading}
					<span class="inline-flex items-center">
						<svg
							class="mr-2 -ml-1 h-4 w-4 animate-spin text-white"
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
						>
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
						Exportando...
					</span>
				{:else}
					📥 Exportar Todos los Filtrados
				{/if}
			</button>
		{/if}
	</div>
</div>
