<script lang="ts">
	export let onSubmit: (e: Event) => void;
	export let onCancel: () => void;
	export let loading: boolean = false;
	export let valid: boolean = true;
	export let submitText: string = 'Guardar Cambios';
	export let cancelText: string = 'Cancelar';
	export let showCancel: boolean = true;
</script>

<form onsubmit={onSubmit} class="space-y-6">
	<slot />

	<!-- Form Actions -->
	<div
		class="flex flex-col justify-end space-y-3  border-gray-200 pt-6 sm:flex-row sm:space-y-0 sm:space-x-4"
	>
		{#if showCancel}
			<button
				type="button"
				onclick={onCancel}
				class="rounded-lg bg-gray-100 px-6 py-3 font-medium text-gray-700 transition-all duration-200 hover:bg-gray-200 hover:shadow-md"
				disabled={loading}
			>
				{cancelText}
			</button>
		{/if}

		<button
			type="submit"
			disabled={loading || !valid}
			class="transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg disabled:transform-none disabled:cursor-not-allowed disabled:opacity-50"
		>
			{#if loading}
				<div class="flex items-center">
					<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
					Guardando...
				</div>
			{:else}
				{submitText}
			{/if}
		</button>
	</div>
</form>
