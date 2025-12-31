<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';

	let loading = true;
	let error: string | null = null;

	async function loadStats() {
		try {
			loading = true;
			error = null;
			// Statistics endpoint is not available in the current API
			// This could be implemented by fetching all materials and calculating stats locally
			// For now, we'll show a message that it's not available
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading material statistics';
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		loadStats();
	});
</script>

<svelte:head>
	<title>Material Statistics - Academia App</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6">
		<button
			on:click={() => goto('/materiales')}
			class="mb-4 inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
		>
			← Back to Materials
		</button>
		<h1 class="text-3xl font-bold text-gray-900">Material Statistics</h1>
		<p class="mt-2 text-gray-600">Overview of educational materials in the system.</p>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	{#if loading}
		<div class="py-12 text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
			></div>
			<p class="mt-4 text-gray-600">Loading statistics...</p>
		</div>
	{:else}
		<div class="rounded-lg border border-yellow-200 bg-yellow-50 p-6">
			<div class="flex">
				<div class="flex-shrink-0">
					<svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
						<path
							fill-rule="evenodd"
							d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
							clip-rule="evenodd"
						/>
					</svg>
				</div>
				<div class="ml-3">
					<h3 class="text-sm font-medium text-yellow-800">Statistics Not Available</h3>
					<div class="mt-2 text-sm text-yellow-700">
						<p>
							Material statistics are not currently available through the API. This feature could be
							implemented by fetching all materials and calculating statistics locally, or by adding
							a statistics endpoint to the backend API.
						</p>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>
