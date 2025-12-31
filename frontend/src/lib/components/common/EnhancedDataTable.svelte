<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type {
		EntityAction,
		EntityColumn,
		EntityPagination,
		PaginatedEntities,
		AuthStoreType
	} from './types';

	const dispatch = createEventDispatcher();

	export let loading: boolean = false;
	export let paginatedData: PaginatedEntities<Record<string, unknown>> | null = null;
	export let currentPagination: EntityPagination;
	export const authStore: AuthStoreType = undefined as unknown as AuthStoreType;
	export let columns: EntityColumn<Record<string, unknown>>[] = [];
	export const entityName: string = 'elemento';
	export let entityNamePlural: string = 'elementos';
	export let actions: EntityAction<Record<string, unknown>>[] = [];
	export let showMobileView: boolean = true;
	export let showRowNumbers: boolean = false;
	export let stripedRows: boolean = true;
	export let hoverEffects: boolean = true;
	export let compactMode: boolean = false;
	export let theme: 'default' | 'modern' | 'minimal' = 'default';

	function formatValue(
		entity: Record<string, unknown>,
		column: EntityColumn<Record<string, unknown>>
	): string {
		if (column.formatter) {
			return column.formatter(entity[column.key], entity) || '-';
		}

		if (column.cell) {
			return column.cell(entity);
		}

		const value = entity[column.key];
		if (value === undefined || value === null) return '-';

		if (value instanceof Date) {
			return formatDate(value);
		}

		return String(value);
	}

	function shouldRenderAsHtml(column: EntityColumn<Record<string, unknown>>): boolean {
		return column.html === true && column.formatter !== undefined;
	}

	function formatDate(date: Date | string | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES', {
			year: 'numeric',
			month: 'short',
			day: 'numeric'
		});
	}

	function handleSort(field: string) {
		dispatch('changeSorting', field);
	}

	function triggerAction(
		action: EntityAction<Record<string, unknown>>,
		entity: Record<string, unknown>
	) {
		action.action(entity);
	}

	// Generate skeleton rows based on current page size
	$: skeletonCount = currentPagination?.size || 5;

	// Theme-based styling classes
	const themes = {
		default: {
			wrapper: 'overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg',
			header: 'bg-gradient-to-r from-gray-50 via-gray-100 to-gray-50',
			headerCell:
				'px-6 py-4 text-left text-xs font-semibold tracking-wider text-gray-700 uppercase border-b border-gray-200',
			row: (index: number, hover: boolean) => `
				transition-all duration-200 
				${hover ? 'hover:bg-gradient-to-r hover:from-blue-50 hover:to-indigo-50 hover:shadow-sm' : ''}
				${stripedRows && index % 2 === 1 ? 'bg-gray-50/50' : 'bg-white'}
			`,
			cell: 'px-6 py-4 whitespace-nowrap text-sm text-gray-900 border-b border-gray-100',
			actionCell: 'px-6 py-4 text-sm whitespace-nowrap text-gray-500 border-b border-gray-100',
			actionButton: (color: string, hoverColor: string) => `
				inline-flex items-center rounded-lg px-3 py-1.5 text-xs font-medium 
				transition-all duration-200 
				text-${color}-700 bg-${color}-50 
				hover:bg-${hoverColor}-100 hover:text-${hoverColor}-800 
				focus:ring-2 focus:outline-none focus:ring-${color}-500 focus:ring-offset-2
				shadow-sm hover:shadow-md
			`
		},
		modern: {
			wrapper: 'overflow-hidden rounded-2xl border-0 bg-white shadow-xl',
			header: 'bg-gradient-to-r from-slate-800 to-slate-900',
			headerCell: 'px-6 py-4 text-left text-xs font-semibold tracking-wider text-white uppercase',
			row: (index: number, hover: boolean) => `
				transition-all duration-300 
				${hover ? 'hover:bg-gradient-to-r hover:from-slate-50 hover:to-blue-50 hover:shadow-lg hover:scale-[1.01]' : ''}
				${stripedRows && index % 2 === 1 ? 'bg-slate-50/30' : 'bg-white'}
			`,
			cell: 'px-6 py-4 whitespace-nowrap text-sm text-slate-700',
			actionCell: 'px-6 py-4 text-sm whitespace-nowrap text-slate-500',
			actionButton: (color: string, hoverColor: string) => `
				inline-flex items-center rounded-xl px-4 py-2 text-xs font-semibold 
				transition-all duration-300 
				text-${color}-700 bg-${color}-50 
				hover:bg-${hoverColor}-100 hover:text-${hoverColor}-800 
				focus:ring-2 focus:outline-none focus:ring-${color}-500 focus:ring-offset-2
				shadow-md hover:shadow-lg hover:scale-105
			`
		},
		minimal: {
			wrapper: 'overflow-hidden border border-gray-100 bg-white',
			header: 'bg-gray-50',
			headerCell: 'px-4 py-3 text-left text-xs font-medium tracking-wide text-gray-600 uppercase',
			row: (index: number, hover: boolean) => `
				transition-colors duration-200 
				${hover ? 'hover:bg-gray-50' : ''}
				${stripedRows && index % 2 === 1 ? 'bg-gray-25' : 'bg-white'}
			`,
			cell: 'px-4 py-3 whitespace-nowrap text-sm text-gray-700',
			actionCell: 'px-4 py-3 text-sm whitespace-nowrap text-gray-500',
			actionButton: (color: string, hoverColor: string) => `
				inline-flex items-center rounded px-2 py-1 text-xs font-medium 
				transition-colors duration-200 
				text-${color}-600 bg-transparent 
				hover:bg-${hoverColor}-50 hover:text-${hoverColor}-700 
				focus:outline-none focus:ring-1 focus:ring-${color}-500
			`
		}
	};

	$: tableClasses = themes[theme];
	$: compactClasses = compactMode
		? {
				headerCell: tableClasses.headerCell.replace('px-6 py-4', 'px-4 py-3'),
				cell: tableClasses.cell.replace('px-6 py-4', 'px-4 py-3'),
				actionCell: tableClasses.actionCell.replace('px-6 py-4', 'px-4 py-3')
			}
		: tableClasses;
</script>

<div class={tableClasses.wrapper}>
	{#if loading && !paginatedData?.content}
		<!-- Enhanced initial loading state -->
		<div class="py-20 text-center">
			<div
				class="mx-auto h-16 w-16 animate-spin rounded-full border-4 border-blue-200 -blue-600 shadow-lg"
			></div>
			<p class="mt-6 text-xl font-semibold text-gray-700">Cargando {entityNamePlural}...</p>
			<p class="mt-2 text-sm text-gray-500">Por favor espera un momento</p>
		</div>
	{:else if !paginatedData?.content || paginatedData.content.length === 0}
		<!-- Enhanced empty state -->
		<div class="py-20 text-center">
			<div class="mx-auto h-32 w-32 text-gray-300">
				<svg fill="none" stroke="currentColor" viewBox="0 0 24 24" class="h-full w-full">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="1.5"
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
					/>
				</svg>
			</div>
			<h3 class="mt-6 text-xl font-semibold text-gray-900">No se encontraron {entityNamePlural}</h3>
			<p class="mx-auto mt-3 max-w-md text-sm text-gray-500">
				No hay {entityNamePlural} que coincidan con los filtros actuales.
			</p>
		</div>
	{:else}
		<!-- Enhanced Desktop Table -->
		<div class="hidden overflow-x-auto lg:block">
			<table class="min-w-full">
				<thead class={tableClasses.header}>
					<tr>
						{#if showRowNumbers}
							<th class="{compactClasses.headerCell} w-16"> # </th>
						{/if}
						{#each columns as column (column.key)}
							<th
								class="{compactClasses.headerCell} {column.class || ''}"
								style={column.width ? `width: ${column.width}` : ''}
							>
								{#if column.sortable}
									<button
										onclick={() => handleSort(column.key.toString())}
										class="flex items-center space-x-2 transition-all duration-200 hover:scale-105 hover:text-blue-600"
									>
										<span>{column.header}</span>
										{#if currentPagination.sortBy === column.key}
											<span class="font-bold text-blue-600">
												{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}
											</span>
										{:else}
											<span class="text-gray-400 opacity-50">↕</span>
										{/if}
									</button>
								{:else}
									{column.header}
								{/if}
							</th>
						{/each}
						{#if actions.length > 0}
							<th class={compactClasses.headerCell}> Acciones </th>
						{/if}
					</tr>
				</thead>
				<tbody>
					<!-- Enhanced skeleton rows during loading actions -->
					{#if loading && paginatedData?.content}
						{#each Array(skeletonCount), i (i)}
							<tr class="animate-pulse">
								{#if showRowNumbers}
									<td class={compactClasses.cell}>
										<div class="h-4 w-8 rounded bg-gray-200"></div>
									</td>
								{/if}
								{#each columns as column (column.key)}
									<td class={compactClasses.cell}>
										<div class="h-4 w-24 rounded bg-gray-200"></div>
									</td>
								{/each}
								{#if actions.length > 0}
									<td class={compactClasses.actionCell}>
										<div class="flex space-x-2">
											{#each actions as action (action.label)}
												<div class="h-8 w-20 rounded bg-gray-200"></div>
											{/each}
										</div>
									</td>
								{/if}
							</tr>
						{/each}
					{:else}
						{#each paginatedData.content as entity, index (entity.id)}
							<tr class={tableClasses.row(index, hoverEffects)}>
								{#if showRowNumbers}
									<td class="{compactClasses.cell} font-mono text-gray-500">
										{index + 1}
									</td>
								{/if}
								{#each columns as column (column.key)}
									<td class="{compactClasses.cell} {column.class || ''}">
										{#if shouldRenderAsHtml(column)}
											<div class="text-sm text-gray-900">
												<!-- eslint-disable-next-line svelte/no-at-html-tags -->
												{@html formatValue(entity, column)}
											</div>
										{:else}
											<div class="text-sm text-gray-900">
												{formatValue(entity, column)}
											</div>
										{/if}
									</td>
								{/each}
								{#if actions.length > 0}
									<td class={compactClasses.actionCell}>
										<div class="flex space-x-2">
											{#each actions as action (action.label)}
												{#if !action.condition || action.condition(entity)}
													{@const isDisabled =
														typeof action.disabled === 'function'
															? action.disabled(entity)
															: action.disabled}
													<button
														onclick={(e) => {
															e.preventDefault();
															e.currentTarget.blur();
															if (!isDisabled) {
																triggerAction(action, entity);
															}
														}}
														class={tableClasses.actionButton(
															typeof action.color === 'function'
																? action.color(entity)
																: action.color,
															typeof action.hoverColor === 'function'
																? action.hoverColor(entity)
																: action.hoverColor
														)}
														class:opacity-50={isDisabled}
														class:cursor-not-allowed={isDisabled}
														disabled={isDisabled}
														title={action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
													>
														{action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
													</button>
												{/if}
											{/each}
										</div>
									</td>
								{/if}
							</tr>
						{/each}
					{/if}
				</tbody>
			</table>
		</div>

		<!-- Enhanced Mobile View -->
		{#if showMobileView}
			<div class="lg:hidden">
				<!-- Enhanced skeleton cards during loading actions -->
				{#if loading && paginatedData?.content}
					{#each Array(skeletonCount), i (i)}
						<div class="animate-pulse border-b border-gray-200 bg-white p-6">
							<div class="mb-4 flex items-start justify-between">
								<div class="flex-1 space-y-3">
									<div class="h-6 w-40 rounded bg-gray-200"></div>
									<div class="h-4 w-32 rounded bg-gray-200"></div>
								</div>
								<div class="ml-4 h-8 w-24 rounded-full bg-gray-200"></div>
							</div>
							<div class="space-y-3">
								<div class="h-4 w-full rounded bg-gray-200"></div>
								<div class="h-4 w-3/4 rounded bg-gray-200"></div>
								<div class="h-4 w-1/2 rounded bg-gray-200"></div>
							</div>
							<div class="mt-6 flex flex-wrap gap-2">
								{#each actions as action (action.label)}
									<div class="h-8 w-20 rounded bg-gray-200"></div>
								{/each}
							</div>
						</div>
					{/each}
				{:else}
					{#each paginatedData.content as entity, index (entity.id)}
						<div
							class="border-b border-gray-100 p-6 transition-all duration-200 hover:bg-gray-50 {stripedRows &&
							index % 2 === 1
								? 'bg-gray-50/30'
								: 'bg-white'}"
						>
							<div class="mb-4">
								{#each columns as column (column.key)}
									{#if column.key === columns[0].key}
										<h3 class="mb-2 text-lg font-semibold text-gray-900">
											{formatValue(entity, column)}
										</h3>
									{/if}
								{/each}
							</div>
							<div class="space-y-3 text-sm text-gray-600">
								{#each columns as column, i (column.key)}
									{#if i !== 0}
										<div class="flex items-center justify-between py-1">
											<span class="font-medium text-gray-700">{column.header}:</span>
											<div class="max-w-xs text-right">
												{#if shouldRenderAsHtml(column)}
													<!-- eslint-disable-next-line svelte/no-at-html-tags -->
													{@html formatValue(entity, column)}
												{:else}
													<span class="text-gray-900">{formatValue(entity, column)}</span>
												{/if}
											</div>
										</div>
									{/if}
								{/each}
							</div>
							{#if actions.length > 0}
								<div class="mt-6 flex flex-wrap gap-2">
									{#each actions as action (action.label)}
										{#if !action.condition || action.condition(entity)}
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													triggerAction(action, entity);
												}}
												class="inline-flex items-center rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 text-{action.color}-700 bg-{action.color}-50 hover:bg-{action.hoverColor}-100 hover:text-{action.hoverColor}-800 focus:ring-2 focus:outline-none focus:ring-{action.color}-500 shadow-sm hover:shadow-md focus:ring-offset-2"
											>
												{action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
											</button>
										{/if}
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				{/if}
			</div>
		{/if}
	{/if}
</div>
