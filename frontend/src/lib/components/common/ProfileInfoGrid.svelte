<script lang="ts">
	export let title: string = '';
	export let columns: 1 | 2 = 2;
	export let items: Array<{
		label: string;
		value: string | number | boolean;
		type?: 'text' | 'status' | 'badge' | 'date' | 'email' | 'phone';
		status?: 'success' | 'warning' | 'error' | 'info';
	}> = [];
</script>

{#if title}
	<h3 class="mb-4 text-sm font-medium tracking-wide text-gray-500 uppercase">
		{title}
	</h3>
{/if}

<div class="grid grid-cols-1 gap-6 {columns === 2 ? 'md:grid-cols-2' : ''}">
	{#each items as item, index (index)}
		<div>
			<dt class="text-sm font-medium text-gray-700">{item.label}</dt>
			<dd class="mt-1">
				{#if item.type === 'status' || item.type === 'badge'}
					<span
						class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {item.status ===
						'success'
							? 'bg-green-100 text-green-800'
							: item.status === 'warning'
								? 'bg-yellow-100 text-yellow-800'
								: item.status === 'error'
									? 'bg-red-100 text-red-800'
									: item.status === 'info'
										? 'bg-blue-100 text-blue-800'
										: typeof item.value === 'boolean'
											? item.value
												? 'bg-green-100 text-green-800'
												: 'bg-red-100 text-red-800'
											: 'bg-gray-100 text-gray-800'}"
					>
						{#if typeof item.value === 'boolean'}
							{item.value ? 'Activo' : 'Inactivo'}
						{:else}
							{item.value}
						{/if}
					</span>
				{:else if item.type === 'email'}
					<a
						href="mailto:{item.value}"
						class="text-blue-600 transition-colors duration-200 hover:text-blue-800"
					>
						{item.value}
					</a>
				{:else if item.type === 'phone'}
					<a
						href="tel:{item.value}"
						class="text-blue-600 transition-colors duration-200 hover:text-blue-800"
					>
						{item.value}
					</a>
				{:else if item.type === 'date'}
					<span class="text-gray-900">
						{typeof item.value === 'string' || typeof item.value === 'number'
							? new Date(item.value).toLocaleDateString('es-ES', {
									year: 'numeric',
									month: 'long',
									day: 'numeric'
								})
							: item.value}
					</span>
				{:else}
					<span class="text-gray-900">
						{item.value || 'No especificado'}
					</span>
				{/if}
			</dd>
		</div>
	{/each}
</div>
