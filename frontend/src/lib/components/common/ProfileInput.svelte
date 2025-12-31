<script lang="ts">
	export let id: string;
	export let label: string;
	export let value: string = '';
	export let type: 'text' | 'email' | 'tel' | 'password' | 'url' = 'text';
	export let placeholder: string = '';
	export let required: boolean = false;
	export let disabled: boolean = false;
	export let maxlength: number | undefined = undefined;
	export let validation: {
		isValid: boolean;
		message: string;
	} | null = null;
	export let helpText: string = '';
	export let showValidation: boolean = true;
</script>

<div>
	<label for={id} class="mb-2 block text-sm font-medium text-gray-700">
		{label}
		{#if required}<span class="text-red-500">*</span>{/if}
	</label>

	<div class="relative">
		<input
			{id}
			{type}
			{placeholder}
			{required}
			{disabled}
			{maxlength}
			bind:value
			class="w-full rounded-lg border border-gray-200 bg-white px-4 py-3 pr-10 transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500 {validation &&
			showValidation
				? validation.isValid
					? 'border-green-500 bg-green-50'
					: 'border-red-500 bg-red-50'
				: 'border-gray-300'} {disabled ? 'cursor-not-allowed opacity-50' : ''}"
		/>

		{#if validation && showValidation && value}
			<div class="absolute inset-y-0 right-0 flex items-center pr-3">
				{#if validation.isValid}
					<span class="text-green-500">✓</span>
				{:else}
					<span class="text-red-500">✗</span>
				{/if}
			</div>
		{/if}
	</div>

	{#if validation && showValidation && value}
		<p class="mt-1 text-xs {validation.isValid ? 'text-green-600' : 'text-red-600'}">
			{validation.message}
		</p>
	{/if}

	{#if helpText}
		<p class="mt-1 text-xs text-gray-500">
			{helpText}
		</p>
	{/if}
</div>
