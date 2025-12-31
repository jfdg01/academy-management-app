<script lang="ts">
	// Props using Svelte 5 syntax
	const {
		currentPage = 0,
		totalPages = 0,
		onPageChange = () => {}
	} = $props<{
		currentPage?: number;
		totalPages?: number;
		onPageChange?: (page: number) => void;
	}>();

	function handlePageClick(page: number) {
		if (page >= 0 && page < totalPages && page !== currentPage) {
			onPageChange(page);
		}
	}

	function handlePrevious() {
		if (currentPage > 0) {
			onPageChange(currentPage - 1);
		}
	}

	function handleNext() {
		if (currentPage < totalPages - 1) {
			onPageChange(currentPage + 1);
		}
	}

	// Simple pagination - show current page and a few around it
	function getVisiblePages() {
		const pages: number[] = [];
		const maxVisible = 5;

		if (totalPages <= maxVisible) {
			for (let i = 0; i < totalPages; i++) {
				pages.push(i);
			}
		} else {
			let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
			let end = Math.min(totalPages - 1, start + maxVisible - 1);

			if (end === totalPages - 1) {
				start = Math.max(0, end - maxVisible + 1);
			}

			for (let i = start; i <= end; i++) {
				pages.push(i);
			}
		}

		return pages;
	}
</script>

{#if totalPages > 1}
	<nav class="flex items-center justify-center space-x-1">
		<!-- Previous button -->
		<button
			class="flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-500 transition-colors duration-200 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
			disabled={currentPage === 0}
			onclick={handlePrevious}
		>
			<svg class="mr-1 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
				<path
					fill-rule="evenodd"
					d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
					clip-rule="evenodd"
				/>
			</svg>
			Previous
		</button>

		<!-- Page numbers -->
		{#each getVisiblePages() as page (page)}
			<button
				class="rounded-md border px-3 py-2 text-sm font-medium transition-colors duration-200 {page ===
				currentPage
					? 'border-blue-500 bg-blue-500 text-white'
					: 'border-gray-300 bg-white text-gray-500 hover:bg-gray-50'}"
				onclick={() => handlePageClick(page)}
			>
				{page + 1}
			</button>
		{/each}

		<!-- Next button -->
		<button
			class="flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-500 transition-colors duration-200 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
			disabled={currentPage === totalPages - 1}
			onclick={handleNext}
		>
			Next
			<svg class="ml-1 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
				<path
					fill-rule="evenodd"
					d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
					clip-rule="evenodd"
				/>
			</svg>
		</button>
	</nav>
{/if}
