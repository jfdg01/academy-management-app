<script lang="ts">
	export let title: string;
	export let subtitle: string = '';
	export let showHeader: boolean = true;
	export let actions: Array<{
		label: string;
		onClick: () => void;
		variant?: 'primary' | 'secondary' | 'danger';
		icon?: string;
	}> = [];
	export let loading: boolean = false;
	export let empty: boolean = false;
	export let emptyIcon: string = '📄';
	export let emptyTitle: string = 'No hay datos disponibles';
	export let emptyDescription: string = 'No se encontraron elementos para mostrar.';
	export let emptyAction: {
		label: string;
		onClick: () => void;
	} | null = null;
</script>

<div
	class="rounded-2xl border border-gray-200/50 bg-white/80 p-6 shadow-lg backdrop-blur-xl transition-all duration-300 hover:shadow-2xl sm:p-8"
>
	{#if showHeader}
		<div class="mb-6 flex items-center justify-between">
			<div>
				<h2 class="montserrat-semibold text-xl font-semibold text-gray-900">
					{title}
				</h2>
				{#if subtitle}
					<p class="mt-1 text-sm font-medium text-gray-600">
						{subtitle}
					</p>
				{/if}
			</div>

			{#if actions.length > 0}
				<div class="flex gap-3">
					{#each actions as action, index (index)}
						<button
							onclick={action.onClick}
							class="inline-flex transform items-center rounded-lg px-4 py-2 text-sm font-medium transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md {action.variant ===
							'primary'
								? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white hover:from-blue-700 hover:to-indigo-700'
								: action.variant === 'danger'
									? 'bg-gradient-to-r from-red-600 to-red-700 text-white hover:from-red-700 hover:to-red-800'
									: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
						>
							{#if action.icon}
								<span class="mr-2">{action.icon}</span>
							{/if}
							{action.label}
						</button>
					{/each}
				</div>
			{/if}
		</div>
	{/if}

	{#if loading}
		<div class="py-8 text-center">
			<div
				class="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-200 -blue-600"
			></div>
			<p class="mt-4 text-lg font-medium text-gray-600">Cargando...</p>
		</div>
	{:else if empty}
		<div class="rounded-xl border border-gray-200 bg-gray-50 p-8 text-center">
			<div class="mb-4 text-6xl">{emptyIcon}</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">{emptyTitle}</h3>
			<p class="mb-4 text-gray-600">{emptyDescription}</p>
			{#if emptyAction}
				<button
					onclick={emptyAction.onClick}
					class="inline-block transform rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-3 font-semibold text-white transition-all duration-200 hover:-translate-y-0.5 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg"
				>
					{emptyAction.label}
				</button>
			{/if}
		</div>
	{:else}
		<slot />
	{/if}
</div>
