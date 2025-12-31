<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ProfesorService } from '$lib/services/profesorService';

	let loading = $state(true);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(async () => {
		const userId = authStore.user?.id;

		if (!userId) {
			goto('/');
			return;
		}

		try {
			// Get professor data by ID using the service
			const profesor = await ProfesorService.getProfesorById(userId);

			// Redirect to the professor's profile page
			goto(`/profesores/${profesor.id}`);
		} catch (error) {
			console.error('Error finding professor profile:', error);
			goto('/');
		}
	});
</script>

{#if loading}
	<div class="container mx-auto px-4 py-8 text-center">
		<div class="mx-auto h-12 w-12 animate-spin rounded-full border-b-2 border-blue-500"></div>
		<p class="mt-4 text-gray-600">Cargando tu perfil...</p>
	</div>
{/if}
