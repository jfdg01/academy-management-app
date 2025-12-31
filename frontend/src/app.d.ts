// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}

	// Environment variable types
	interface ImportMetaEnv {
		readonly PUBLIC_STRIPE_PUBLISHABLE_KEY: string;
		readonly PUBLIC_BACKEND_URL: string;
		readonly STRIPE_SECRET_KEY?: string;
		readonly NODE_ENV: 'development' | 'production' | 'test';
		readonly VITE_APP_TITLE?: string;
	}

	interface ImportMeta {
		readonly env: ImportMetaEnv;
	}
}

export {};
