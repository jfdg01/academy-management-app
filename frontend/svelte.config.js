import { mdsvex } from 'mdsvex';
import adapter from '@sveltejs/adapter-auto';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

const config = {
	preprocess: [vitePreprocess(), mdsvex()],
	kit: {
		adapter: adapter(),
		alias: {
			$lib: './src/lib',
			'@/api': './src/lib/api.ts',
			$paraglide: './src/lib/paraglide'
		}
	},
	extensions: ['.svelte', '.svx']
};

export default config;
