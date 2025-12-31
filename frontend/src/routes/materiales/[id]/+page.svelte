<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { MaterialService } from '$lib/services/materialService';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import type { DTOMaterial } from '$lib/generated/api';

	let material: DTOMaterial | null = null;
	let loading = true;
	let error: string | null = null;
	let editing = false;
	let name = '';
	let url = '';

	$: materialId = $page.params.id;

	async function loadMaterial() {
		try {
			loading = true;
			error = null;
			material = await MaterialService.getMaterialById(materialId);
			name = material.name || '';
			url = material.url || '';
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading material';
		} finally {
			loading = false;
		}
	}

	async function handleUpdate(event: Event) {
		event.preventDefault();

		if (!name.trim() || !url.trim()) {
			error = 'Please fill in all required fields';
			return;
		}

		try {
			loading = true;
			error = null;

			material = await MaterialService.updateMaterial(
				parseInt(materialId),
				name.trim(),
				url.trim()
			);
			editing = false;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error updating material';
		} finally {
			loading = false;
		}
	}

	async function handleDelete() {
		if (!confirm('Are you sure you want to delete this material? This action cannot be undone.')) {
			return;
		}

		try {
			loading = true;
			error = null;

			await MaterialService.deleteMaterial(parseInt(materialId));
			goto('/materiales');
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error deleting material';
			loading = false;
		}
	}

	function startEditing() {
		editing = true;
		name = material?.name || '';
		url = material?.url || '';
	}

	function cancelEditing() {
		editing = false;
		name = material?.name || '';
		url = material?.url || '';
	}

	function getMaterialIcon(material: DTOMaterial): string {
		return MaterialService.getMaterialIcon(material);
	}

	function getMaterialTypeLabel(material: DTOMaterial): string {
		return MaterialService.getMaterialTypeLabel(material);
	}

	function getFileExtension(url: string): string | null {
		const extension = url.split('.').pop()?.toLowerCase();
		return extension || null;
	}

	function getMaterialType(url: string): 'DOCUMENT' | 'IMAGE' | 'VIDEO' {
		return MaterialService.getMaterialType(url);
	}

	function isVideo(url: string): boolean {
		return getMaterialType(url) === 'VIDEO';
	}

	function isImage(url: string): boolean {
		return getMaterialType(url) === 'IMAGE';
	}

	function isDocument(url: string): boolean {
		return getMaterialType(url) === 'DOCUMENT';
	}

	onMount(() => {
		loadMaterial();
	});
</script>

<svelte:head>
	<title>Material Details - Academia App</title>
</svelte:head>

<div class="container mx-auto max-w-4xl px-4 py-8">
	<div class="mb-6">
		<button
			on:click={() => goto('/materiales')}
			class="mb-4 inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
		>
			← Back to Materials
		</button>
		<h1 class="text-3xl font-bold text-gray-900">Material Details</h1>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	{#if loading && !material}
		<div class="py-12 text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
			></div>
			<p class="mt-4 text-gray-600">Loading material...</p>
		</div>
	{:else if material}
		<div class="overflow-hidden rounded-lg bg-white shadow">
			<!-- Header -->
			<div class="border-b border-gray-200 px-6 py-4">
				<div class="flex items-center justify-between">
					<div class="flex items-center space-x-3">
						<span class="text-3xl">{getMaterialIcon(material)}</span>
						<div>
							<h2 class="text-xl font-semibold text-gray-900">{material.name}</h2>
							<p class="text-sm text-gray-500">{getMaterialTypeLabel(material)}</p>
						</div>
					</div>
					<div class="flex space-x-2">
						{#if !editing}
							<button
								on:click={startEditing}
								class="rounded-md bg-blue-50 px-3 py-1.5 text-sm font-medium text-blue-700 hover:bg-blue-100 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							>
								Edit
							</button>
						{/if}
						<button
							on:click={handleDelete}
							disabled={loading}
							class="rounded-md bg-red-50 px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-100 focus:ring-2 focus:ring-red-500 focus:outline-none disabled:opacity-50"
						>
							Delete
						</button>
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 py-6">
				{#if editing}
					<form on:submit={handleUpdate} class="space-y-6">
						<div>
							<label for="name" class="mb-2 block text-sm font-medium text-gray-700">
								Material Name *
							</label>
							<input
								type="text"
								id="name"
								bind:value={name}
								required
								maxlength="200"
								class="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500 focus:outline-none"
								placeholder="Enter material name"
							/>
						</div>

						<div>
							<label for="url" class="mb-2 block text-sm font-medium text-gray-700">
								Material URL *
							</label>
							<input
								type="url"
								id="url"
								bind:value={url}
								required
								maxlength="500"
								class="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500 focus:outline-none"
								placeholder="https://example.com/document.pdf"
							/>
						</div>

						<div class="flex justify-end space-x-3">
							<button
								type="button"
								on:click={cancelEditing}
								class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:outline-none"
								disabled={loading}
							>
								Cancel
							</button>
							<button
								type="submit"
								disabled={loading}
								class="rounded-md border ransparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none disabled:opacity-50"
							>
								{loading ? 'Updating...' : 'Update Material'}
							</button>
						</div>
					</form>
				{:else}
					<div class="space-y-6">
						<div>
							<h3 class="mb-2 text-lg font-medium text-gray-900">Material Information</h3>
							<dl class="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
								<div>
									<dt class="text-sm font-medium text-gray-500">Name</dt>
									<dd class="mt-1 text-sm text-gray-900">{material.name}</dd>
								</div>
								<div>
									<dt class="text-sm font-medium text-gray-500">Type</dt>
									<dd class="mt-1 text-sm text-gray-900">{getMaterialTypeLabel(material)}</dd>
								</div>
								<div class="sm:col-span-2">
									<dt class="text-sm font-medium text-gray-500">URL</dt>
									<dd class="mt-1 text-sm text-gray-900">
										<a
											href={material.url}
											target="_blank"
											rel="noopener noreferrer"
											class="break-all text-blue-600 hover:text-blue-800"
										>
											{material.url}
										</a>
									</dd>
								</div>
								{#if material.url && getFileExtension(material.url)}
									<div>
										<dt class="text-sm font-medium text-gray-500">File Extension</dt>
										<dd class="mt-1 text-sm text-gray-900">{getFileExtension(material.url)}</dd>
									</div>
								{/if}
								{#if material.url}
									<div>
										<dt class="text-sm font-medium text-gray-500">Material Type</dt>
										<dd class="mt-1 text-sm text-gray-900">{getMaterialTypeLabel(material)}</dd>
									</div>
								{/if}
							</dl>
						</div>

						<div class="rounded-lg bg-gray-50 p-4">
							<h3 class="mb-2 text-sm font-medium text-gray-900">Type Detection</h3>
							<div class="flex items-center space-x-2 text-sm text-gray-600">
								<span>Video: {material.url && isVideo(material.url) ? '✅' : '❌'}</span>
								<span>Image: {material.url && isImage(material.url) ? '✅' : '❌'}</span>
								<span>Document: {material.url && isDocument(material.url) ? '✅' : '❌'}</span>
							</div>
						</div>
					</div>
				{/if}
			</div>
		</div>
	{:else}
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📄</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Material Not Found</h3>
			<p class="text-gray-600">
				The material you're looking for doesn't exist or has been removed.
			</p>
			<button
				on:click={() => goto('/materiales')}
				class="mt-4 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
			>
				Back to Materials
			</button>
		</div>
	{/if}
</div>
