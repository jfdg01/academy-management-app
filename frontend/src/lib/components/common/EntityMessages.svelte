<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	const {
		successMessage = null,
		error = null,
		autoHide = true,
		successDuration = 3000,
		errorDuration = 5000
	} = $props<{
		successMessage?: string | null;
		error?: string | null;
		autoHide?: boolean;
		successDuration?: number;
		errorDuration?: number;
	}>();

	// Auto hide messages after a delay if autoHide is enabled
	$effect(() => {
		if (autoHide && successMessage) {
			const timer = setTimeout(() => {
				clearSuccess();
			}, successDuration);

			return () => clearTimeout(timer);
		}
	});

	$effect(() => {
		if (autoHide && error) {
			const timer = setTimeout(() => {
				clearError();
			}, errorDuration);

			return () => clearTimeout(timer);
		}
	});

	function clearSuccess() {
		dispatch('clearSuccess');
	}

	function clearError() {
		dispatch('clearError');
	}
</script>

{#if successMessage || error}
	<div class="mb-6 space-y-2">
		{#if successMessage}
			<div
				class="relative flex items-center rounded-lg bg-green-50 p-4 text-green-800 shadow"
				role="alert"
			>
				<svg
					class="mr-2 h-5 w-5 flex-shrink-0"
					fill="currentColor"
					viewBox="0 0 20 20"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						fill-rule="evenodd"
						d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
						clip-rule="evenodd"
					></path>
				</svg>
				<p class="text-sm">{successMessage}</p>
				<button
					type="button"
					onclick={clearSuccess}
					class="ml-auto inline-flex h-5 w-5 items-center justify-center rounded-md text-green-700 hover:bg-green-100"
					aria-label="Cerrar"
				>
					<span class="sr-only">Cerrar</span>
					<svg
						class="h-4 w-4"
						fill="currentColor"
						viewBox="0 0 20 20"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							fill-rule="evenodd"
							d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
							clip-rule="evenodd"
						></path>
					</svg>
				</button>
			</div>
		{/if}

		{#if error}
			<div
				class="relative flex items-center rounded-lg bg-red-50 p-4 text-red-800 shadow"
				role="alert"
			>
				<svg
					class="mr-2 h-5 w-5 flex-shrink-0"
					fill="currentColor"
					viewBox="0 0 20 20"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						fill-rule="evenodd"
						d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
						clip-rule="evenodd"
					></path>
				</svg>
				<p class="text-sm">{error}</p>
				<button
					type="button"
					onclick={clearError}
					class="ml-auto inline-flex h-5 w-5 items-center justify-center rounded-md text-red-700 hover:bg-red-100"
					aria-label="Cerrar"
				>
					<span class="sr-only">Cerrar</span>
					<svg
						class="h-4 w-4"
						fill="currentColor"
						viewBox="0 0 20 20"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							fill-rule="evenodd"
							d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
							clip-rule="evenodd"
						></path>
					</svg>
				</button>
			</div>
		{/if}
	</div>
{/if}
