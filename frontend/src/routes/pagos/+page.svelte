<script lang="ts">
	import PaymentHistory from '$lib/components/PaymentHistory.svelte';
	import PaymentForm from '$lib/components/PaymentForm.svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State management
	let showPaymentForm = $state(false);
	let selectedAmount = $state(0);
	let selectedDescription = $state('');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	function handlePaymentError(error: Error) {
		console.error('Payment failed:', error);
		// Show error notification and close the form
		closePaymentForm();
	}

	function openPaymentForm(amount: number, description: string) {
		selectedAmount = amount;
		selectedDescription = description;
		showPaymentForm = true;
	}

	function closePaymentForm() {
		showPaymentForm = false;
		selectedAmount = 0;
		selectedDescription = '';
	}
</script>

<svelte:head>
	<title>Payment Management</title>
</svelte:head>

<div class="mx-auto max-w-6xl p-8">
	<div class="mb-8 flex items-center justify-between">
		<h1 class="m-0 text-4xl font-semibold text-gray-900">Payment Management</h1>
		<div class="flex items-center gap-4">
			{#if authStore.user?.roles?.includes('ROLE_ADMIN')}
				<button
					class="cursor-pointer rounded-md border-none bg-green-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-green-700"
					onclick={() => goto('/pagos/admin')}
				>
					Admin View
				</button>
			{/if}
			{#if authStore.user?.roles?.includes('ROLE_ALUMNO')}
				<button
					class="cursor-pointer rounded-md border-none bg-gray-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-gray-700"
					onclick={() => goto('/pagos/my-payments')}
				>
					View My Payments
				</button>
			{/if}
			<button
				class="cursor-pointer rounded-md border-none bg-blue-600 px-6 py-3 font-medium text-white transition-colors duration-200 hover:bg-blue-700"
				onclick={() => openPaymentForm(50, 'Course Payment')}
			>
				New Payment
			</button>
		</div>
	</div>

	{#if showPaymentForm}
		<div
			class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black"
			onclick={closePaymentForm}
			onkeydown={(e) => e.key === 'Escape' && closePaymentForm()}
			role="dialog"
			aria-modal="true"
			tabindex="-1"
		>
			<div
				class="max-h-screen w-11/12 max-w-lg overflow-y-auto rounded-lg bg-white p-8"
				onclick={(e) => e.stopPropagation()}
				onkeydown={(e) => e.key === 'Escape' && closePaymentForm()}
				role="dialog"
				tabindex="-1"
			>
				<div class="mb-6 flex items-center justify-between">
					<h2 class="m-0 text-2xl font-semibold text-gray-900">Create New Payment</h2>
					<button
						class="flex h-8 w-8 cursor-pointer items-center justify-center rounded border-none bg-transparent p-0 text-2xl text-gray-500 hover:bg-gray-100 hover:text-gray-700"
						onclick={closePaymentForm}
					>
						&times;
					</button>
				</div>
				<PaymentForm
					amount={selectedAmount}
					description={selectedDescription}
					studentId={authStore.user?.id?.toString() || ''}
					onError={handlePaymentError}
				/>
			</div>
		</div>
	{/if}

	<div class="grid gap-8">
		<div class="quick-payments">
			<h2 class="mb-4 text-2xl font-semibold text-gray-900">Quick Payments</h2>
			<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
				<button
					class="cursor-pointer rounded-lg border border-gray-200 bg-white p-6 text-center transition-all duration-200 hover:border-blue-600 hover:shadow-md"
					onclick={() => openPaymentForm(25, 'Basic Course')}
				>
					<h3 class="m-0 mb-2 text-lg font-semibold text-gray-900">Basic Course</h3>
					<p class="m-0 text-xl font-bold text-blue-600">€25.00</p>
				</button>
				<button
					class="cursor-pointer rounded-lg border border-gray-200 bg-white p-6 text-center transition-all duration-200 hover:border-blue-600 hover:shadow-md"
					onclick={() => openPaymentForm(50, 'Advanced Course')}
				>
					<h3 class="m-0 mb-2 text-lg font-semibold text-gray-900">Advanced Course</h3>
					<p class="m-0 text-xl font-bold text-blue-600">€50.00</p>
				</button>
				<button
					class="cursor-pointer rounded-lg border border-gray-200 bg-white p-6 text-center transition-all duration-200 hover:border-blue-600 hover:shadow-md"
					onclick={() => openPaymentForm(100, 'Premium Course')}
				>
					<h3 class="m-0 mb-2 text-lg font-semibold text-gray-900">Premium Course</h3>
					<p class="m-0 text-xl font-bold text-blue-600">€100.00</p>
				</button>
			</div>
		</div>

		<div class="payment-history-section">
			<h2 class="mb-4 text-2xl font-semibold text-gray-900">Recent Payments</h2>
			<PaymentHistory />
		</div>
	</div>
</div>
