<script lang="ts">
	import { getStripe } from '$lib/stripe.js';

	// State
	let stripeLoaded = $state(false);
	let stripeError = $state('');

	// Test environment variable loading
	const envCheck = {
		stripeKey: import.meta.env.PUBLIC_STRIPE_PUBLISHABLE_KEY,
		hasKey: !!import.meta.env.PUBLIC_STRIPE_PUBLISHABLE_KEY,
		allEnvVars: Object.keys(import.meta.env).filter((key) => key.startsWith('PUBLIC_'))
	};

	// Load Stripe on component mount
	$effect(() => {
		loadStripe();
	});

	async function loadStripe() {
		try {
			const stripe = await getStripe();
			stripeLoaded = !!stripe;
		} catch (err) {
			console.error('Failed to load Stripe:', err);
			stripeError = err instanceof Error ? err.message : 'Unknown error';
		}
	}
</script>

<svelte:head>
	<title>Environment Test</title>
</svelte:head>

<div class="mx-auto max-w-4xl p-8">
	<h1 class="mb-8 text-3xl font-bold text-gray-900">Environment Variable Test</h1>

	<div class="mt-8 rounded-lg border border-gray-200 bg-gray-50 p-4">
		<h2 class="mb-4 text-xl font-semibold text-gray-900">Stripe Configuration Test</h2>

		<div class="mb-4 border-b border-gray-200 p-2">
			<strong>Stripe Key Present:</strong>
			<span class={envCheck.hasKey ? 'font-semibold text-green-600' : 'font-semibold text-red-600'}>
				{envCheck.hasKey ? '✅ Yes' : '❌ No'}
			</span>
		</div>

		<div class="mb-4 border-b border-gray-200 p-2">
			<strong>Stripe Key Length:</strong>
			<span>{envCheck.stripeKey?.length || 0} characters</span>
		</div>

		<div class="mb-4 border-b border-gray-200 p-2">
			<strong>Stripe Key Prefix:</strong>
			<span>{envCheck.stripeKey?.substring(0, 20) || 'N/A'}...</span>
		</div>

		<div class="mb-4 border-b border-gray-200 p-2">
			<strong>Stripe Object Loaded:</strong>
			<span class={stripeLoaded ? 'font-semibold text-green-600' : 'font-semibold text-red-600'}>
				{stripeLoaded ? '✅ Yes' : '❌ No'}
			</span>
		</div>

		{#if stripeError}
			<div class="mb-4 border-b border-gray-200 p-2">
				<strong>Stripe Error:</strong>
				<span class="font-semibold text-red-600">{stripeError}</span>
			</div>
		{/if}

		<div class="mb-4 p-2">
			<strong>All PUBLIC_ Environment Variables:</strong>
			<ul class="mt-2 ml-6">
				{#each envCheck.allEnvVars as envVar (envVar)}
					<li class="mb-1">{envVar}: {import.meta.env[envVar] ? '✅ Set' : '❌ Not Set'}</li>
				{/each}
			</ul>
		</div>
	</div>
</div>
