import type { PageLoad } from './$types';
import { authStore } from '$lib/stores/authStore.svelte';
import { redirect } from '@sveltejs/kit';

export const load: PageLoad = async () => {
	// Check authentication
	if (!authStore.isAuthenticated) {
		throw redirect(302, '/auth');
	}

	// Check permissions
	if (!authStore.isAdmin && !authStore.isProfesor) {
		throw redirect(302, '/');
	}

	return {
		// No complex data loading here - the component handles everything
	};
};
