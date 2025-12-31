// For more info, see https://github.com/storybookjs/eslint-plugin-storybook#configuration-flat-config-format
import storybook from 'eslint-plugin-storybook';

import prettier from 'eslint-config-prettier';
import js from '@eslint/js';
import { includeIgnoreFile } from '@eslint/compat';
import svelte from 'eslint-plugin-svelte';
import globals from 'globals';
import { fileURLToPath } from 'node:url';
import ts from 'typescript-eslint';
import svelteConfig from './svelte.config.js';

const gitignorePath = fileURLToPath(new URL('./.gitignore', import.meta.url));

export default ts.config(
	includeIgnoreFile(gitignorePath),
	js.configs.recommended,
	...ts.configs.recommended,
	...svelte.configs.recommended,
	prettier,
	...svelte.configs.prettier,
	{
		languageOptions: {
			globals: { ...globals.browser, ...globals.node }
		},
		rules: { 'no-undef': 'off' }
	},
	{
		files: ['**/*.svelte', '**/*.svelte.ts', '**/*.svelte.js'],
		languageOptions: {
			parserOptions: {
				projectService: true,
				extraFileExtensions: ['.svelte'],
				parser: ts.parser,
				svelteConfig
			}
		}
	},
	storybook.configs['flat/recommended'],
	// Ignore auto-generated files
	{
		ignores: [
			'src/lib/generated/**/*',
			'src/lib/paraglide/**/*',
			'src/paraglide/**/*',
			'node_modules/**/*',
			'.svelte-kit/**/*',
			'build/**/*',
			'dist/**/*',
			'storybook-static/**/*',
			'coverage/**/*',
			'*.config.js',
			'*.config.ts',
			'vite.config.ts',
			'svelte.config.js',
			'playwright.config.ts',
			'vitest-setup-client.ts',
			'openapitools.json',
			'project.inlang/**/*',
			'messages/**/*',
			'package-lock.json',
			'pnpm-lock.yaml',
			'yarn.lock',
			'src/stories/**/*',
			'e2e/**/*',
			'**/*.test.ts',
			'**/*.spec.ts',
			'**/*.test.js',
			'**/*.spec.js',
			'**/*.test.svelte',
			'**/*.spec.svelte',
			'.storybook/**/*',
			'**/*.stories.svelte',
			'**/*.stories.ts',
			'**/*.stories.js',
			'**/*.stories.mdx',
			'**/*.test.tsx',
			'**/*.spec.tsx',
			'**/*.test.jsx',
			'**/*.spec.jsx',
			'docs/**/*',
			'static/**/*',
			'src/lib/assets/**/*',
		]
	}
);
