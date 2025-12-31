<script lang="ts">
	import { goto } from '$app/navigation';
	import { MaterialService } from '$lib/services/materialService';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';

	let name = '';
	let url = '';
	let loading = false;
	let error: string | null = null;

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!name.trim() || !url.trim()) {
			error = 'Please fill in all required fields';
			return;
		}

		try {
			loading = true;
			error = null;

			await MaterialService.createMaterial(name.trim(), url.trim());
			goto('/materiales');
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error creating material';
		} finally {
			loading = false;
		}
	}

	function handleCancel() {
		goto('/materiales');
	}
</script>

<svelte:head>
	<title>Add New Material - Academia App</title>
</svelte:head>

<div class="container mx-auto max-w-2xl px-4 py-8">
	<div class="mb-6">
		<h1 class="text-3xl font-bold text-gray-900">Add New Material</h1>
		<p class="mt-2 text-gray-600">Create a new educational material for your students.</p>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	<form on:submit={handleSubmit} class="space-y-6">
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
			<p class="mt-1 text-sm text-gray-500">Maximum 200 characters</p>
		</div>

		<div>
			<label for="url" class="mb-2 block text-sm font-medium text-gray-700"> Material URL * </label>
			<input
				type="url"
				id="url"
				bind:value={url}
				required
				maxlength="500"
				class="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-500 focus:outline-none"
				placeholder="https://example.com/document.pdf"
			/>
			<p class="mt-1 text-sm text-gray-500">
				Enter a valid URL to the material (maximum 500 characters)
			</p>
		</div>

		<div class="rounded-md border border-blue-200 bg-blue-50 p-4">
			<h3 class="mb-2 text-sm font-medium text-blue-800">Material Type Detection</h3>
			<p class="text-sm text-blue-700">
				The system will automatically detect the material type based on the file extension:
			</p>
			<ul class="mt-2 list-inside list-disc space-y-1 text-sm text-blue-700">
				<li><strong>Documents:</strong> .pdf, .doc, .docx, .txt, etc.</li>
				<li><strong>Images:</strong> .jpg, .jpeg, .png, .gif, .bmp, .webp, .svg</li>
				<li><strong>Videos:</strong> .mp4, .avi, .mov, .wmv, .flv, .webm, .mkv</li>
			</ul>
		</div>

		<div class="flex justify-end space-x-4 pt-6">
			<button
				type="button"
				on:click={handleCancel}
				class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
				disabled={loading}
			>
				Cancel
			</button>
			<button
				type="submit"
				disabled={loading}
				class="rounded-md border ransparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none disabled:cursor-not-allowed disabled:opacity-50"
			>
				{loading ? 'Creating...' : 'Create Material'}
			</button>
		</div>
	</form>
</div>
