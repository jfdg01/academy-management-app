<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	// Get props with defaults
	const props = $props();

	// Local variables
	let pageDisplayInfo = $derived(props.pageDisplayInfo ?? null);
	let currentPagination = $derived(props.currentPagination);
	let sortFields = $derived(props.sortFields ?? []);
	let pageSizeOptions = $derived(props.pageSizeOptions ?? [10, 20, 50, 100]);
	let showSortAndSize = $derived(props.showSortAndSize ?? true);
	let justifyContent = $derived(props.justifyContent ?? 'between');

	// Use a reactive statement to update selectedPage when pageDisplayInfo changes
	let selectedPage = $derived(pageDisplayInfo?.currentPage || 1);

	function goToPage(page: number) {
		dispatch('goToPage', page);
	}

	function goToPrevPage() {
		if (pageDisplayInfo?.hasPrevious) {
			goToPage(pageDisplayInfo.currentPage - 1);
		}
	}

	function goToNextPage() {
		if (pageDisplayInfo?.hasNext) {
			goToPage(pageDisplayInfo.currentPage + 1);
		}
	}

	function goToFirstPage() {
		goToPage(1);
	}

	function goToLastPage() {
		if (pageDisplayInfo) {
			goToPage(pageDisplayInfo.totalPages);
		}
	}

	function handlePageInputChange(e: Event) {
		const target = e.target as HTMLInputElement;
		const newPage = parseInt(target.value, 10);
		if (!isNaN(newPage) && pageDisplayInfo) {
			const clampedPage = Math.min(Math.max(1, newPage), pageDisplayInfo.totalPages);
			selectedPage = clampedPage;
		}
	}

	function handlePageInputKeyDown(e: KeyboardEvent) {
		if (e.key === 'Enter') {
			e.preventDefault();
			if (selectedPage && pageDisplayInfo) {
				const clampedPage = Math.min(Math.max(1, selectedPage), pageDisplayInfo.totalPages);
				goToPage(clampedPage);
			}
		}
	}

	function changePageSize(e: Event) {
		const select = e.target as HTMLSelectElement;
		const newSize = parseInt(select.value, 10);
		dispatch('changePageSize', newSize);
	}

	function changeSorting(e: Event) {
		const select = e.target as HTMLSelectElement;
		const value = select.value;
		dispatch('changeSorting', value);
	}
</script>

{#if pageDisplayInfo}
	<div
		class="flex flex-col gap-4 text-sm text-gray-600 sm:flex-row {justifyContent === 'between'
			? 'items-center justify-between'
			: 'justify-center'}"
	>
		<!-- Left side: Records info -->
		<div class="hidden whitespace-nowrap sm:block">
			{#if pageDisplayInfo.totalItems > 0}
				Mostrando <span class="font-semibold">{pageDisplayInfo.startItem}</span> a
				<span class="font-semibold">{pageDisplayInfo.endItem}</span> de
				<span class="font-semibold">{pageDisplayInfo.totalItems}</span> resultados
			{:else}
				No hay resultados
			{/if}
		</div>

		<!-- Right side: Pagination controls -->
		<div class="flex flex-wrap items-center gap-3">
			{#if showSortAndSize && sortFields.length > 0}
				<div class="flex items-center gap-2">
					<label for="sortBy" class="whitespace-nowrap">Ordenar por:</label>
					<select
						id="sortBy"
						class="w-auto rounded-md border border-gray-300 p-1"
						value={currentPagination.sortBy}
						onchange={changeSorting}
					>
						{#each sortFields as field (field.value)}
							<option value={field.value}>{field.label}</option>
						{/each}
					</select>
					<button
						class="rounded-full bg-gray-100 p-1"
						onclick={() => {
							dispatch('changeSorting', {
								value: currentPagination.sortBy,
								direction: currentPagination.sortDirection === 'ASC' ? 'DESC' : 'ASC'
							});
						}}
						title={currentPagination.sortDirection === 'ASC'
							? 'Orden ascendente'
							: 'Orden descendente'}
					>
						{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}
					</button>
				</div>
			{/if}

			{#if showSortAndSize && pageSizeOptions.length > 0}
				<div class="flex items-center gap-2">
					<label for="pageSize" class="whitespace-nowrap">Mostrar:</label>
					<select
						id="pageSize"
						class="w-auto rounded-md border border-gray-300 p-1"
						value={currentPagination.size}
						onchange={changePageSize}
					>
						{#each pageSizeOptions as size (size)}
							<option value={size}>{size}</option>
						{/each}
					</select>
				</div>
			{/if}

			{#if pageDisplayInfo.totalPages > 1}
				<div class="flex items-center gap-1">
					<!-- First page button -->
					<button
						class="rounded-md border border-gray-300 p-1 {pageDisplayInfo.hasPrevious
							? 'hover:bg-gray-100'
							: 'cursor-default opacity-50'}"
						disabled={!pageDisplayInfo.hasPrevious}
						onclick={goToFirstPage}
						title="Primera página"
					>
						««
					</button>

					<!-- Previous page button -->
					<button
						class="rounded-md border border-gray-300 p-1 {pageDisplayInfo.hasPrevious
							? 'hover:bg-gray-100'
							: 'cursor-default opacity-50'}"
						disabled={!pageDisplayInfo.hasPrevious}
						onclick={goToPrevPage}
						title="Página anterior"
					>
						«
					</button>

					<!-- Page number input -->
					<div class="flex items-center gap-1">
						<input
							type="text"
							inputmode="numeric"
							pattern="[0-9]*"
							class="w-10 rounded-md border border-gray-300 p-1 text-center"
							value={selectedPage}
							onblur={handlePageInputChange}
							onkeydown={handlePageInputKeyDown}
						/>
						de {pageDisplayInfo.totalPages}
					</div>

					<!-- Next page button -->
					<button
						class="rounded-md border border-gray-300 p-1 {pageDisplayInfo.hasNext
							? 'hover:bg-gray-100'
							: 'cursor-default opacity-50'}"
						disabled={!pageDisplayInfo.hasNext}
						onclick={goToNextPage}
						title="Página siguiente"
					>
						»
					</button>

					<!-- Last page button -->
					<button
						class="rounded-md border border-gray-300 p-1 {pageDisplayInfo.hasNext
							? 'hover:bg-gray-100'
							: 'cursor-default opacity-50'}"
						disabled={!pageDisplayInfo.hasNext}
						onclick={goToLastPage}
						title="Última página"
					>
						»»
					</button>
				</div>
			{/if}
		</div>
	</div>
{/if}
