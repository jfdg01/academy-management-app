<script lang="ts">
	import { goto } from '$app/navigation';

	export let title: string;
	export let subtitle: string;
	export let backUrl: string;
	export let backText: string = 'Volver';
	export let showBackButton: boolean = true;
	export let actions: Array<{
		label: string;
		onClick: () => void;
		variant?: 'primary' | 'secondary' | 'danger';
		icon?: string;
	}> = [];
</script>

<div class="mb-8 sm:mb-12">
	<div class="mb-6 flex flex-col gap-4 sm:mb-8 sm:flex-row sm:items-center sm:justify-between">
		<div>
			<h1
				class="montserrat-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-2xl font-bold text-transparent sm:text-3xl lg:text-4xl"
			>
				{title}
			</h1>
			<p class="mt-2 text-sm text-gray-600 sm:text-base">
				{subtitle}
			</p>
		</div>

		<div class="flex flex-col items-start gap-3 sm:flex-row sm:items-center">
			{#if showBackButton}
				<button
					onclick={() => goto(backUrl)}
					class="inline-flex items-center rounded-lg px-4 py-2 text-sm font-medium text-blue-600 transition-all duration-200 hover:bg-blue-50 hover:text-blue-700"
				>
					<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 19l-7-7 7-7"
						></path>
					</svg>
					{backText}
				</button>
			{/if}

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
	</div>
</div>
