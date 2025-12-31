<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';

	onMount(() => {
		// Redirect to the unified class management route
		goto('/profesores/mis-clases');
	});
</script>

<svelte:head>
	<title>Redirigiendo... - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8 text-center">
	<div class="py-12">
		<div class="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		<p class="text-gray-600">Redirigiendo a la gestión unificada de clases...</p>
	</div>
</div>
