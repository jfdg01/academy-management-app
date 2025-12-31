<script lang="ts">
	import { page } from '$app/state';
</script>

<div class="flex min-h-[70vh] flex-col items-center justify-center py-20">
	<div class="montserrat-bold mb-4 text-[120px] text-gray-800">{page.status}</div>
	<div class="montserrat-semibold mb-8 text-4xl">
		{#if page.status === 404}
			Página no encontrada
		{:else}
			Ha ocurrido un error
		{/if}
	</div>
	<p class="lato-regular mb-10 max-w-lg text-center text-xl text-gray-600">
		{#if page.status === 404}
			Lo sentimos, la página que estás buscando no existe o ha sido movida.
		{:else}
			Lo sentimos, ha ocurrido un error. Por favor, inténtalo de nuevo más tarde.
		{/if}
	</p>
	<a
		href="/"
		class="lato-regular bg-black px-8 py-3 text-white transition-colors duration-300 hover:bg-pink-700"
	>
		Volver al inicio
	</a>
</div>
