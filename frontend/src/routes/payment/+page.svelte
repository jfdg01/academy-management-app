<script lang="ts">
	import PaymentForm from '$lib/components/PaymentForm.svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { authStore } from '$lib/stores/authStore.svelte';

	// Get payment details from URL parameters
	const urlParams = $derived(new URLSearchParams($page.url.search));
	const amountParam = $derived(urlParams.get('amount'));
	const descriptionParam = $derived(urlParams.get('description'));
	const classIdParam = $derived(urlParams.get('classId'));

	// Payment configuration
	const amount = $derived(amountParam ? parseFloat(amountParam) : 99.99);
	const description = $derived(
		descriptionParam ? decodeURIComponent(descriptionParam) : 'Advanced Mathematics Course'
	);
	const classId = $derived(classIdParam ? parseInt(classIdParam) : null);

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	function handleError(error: Error) {
		console.error('Payment failed:', error);
		// Show error toast or handle error
	}
</script>

<svelte:head>
	<title>Payment - {description}</title>
</svelte:head>

<div class="mx-auto my-12 max-w-2xl px-5">
	<h1 class="mb-8 text-center text-4xl font-semibold text-gray-900">Completa tu pago</h1>

	<PaymentForm
		{amount}
		{description}
		{classId}
		studentId={authStore.user?.id?.toString() || ''}
		onError={handleError}
	/>
</div>
