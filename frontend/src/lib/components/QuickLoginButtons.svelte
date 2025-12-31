<script lang="ts">
	import { authStore } from '$lib/stores/authStore.svelte';
	import { goto } from '$app/navigation';
	import { autenticacionApi } from '$lib/api';

	let loginLoading = $state(false);
	let message = $state('');

	async function quickLogin(username: string, password: string, role: string, redirectTo?: string) {
		loginLoading = true;
		message = `Logging in as ${role}...`;

		try {
			// First logout current user
			authStore.logout();

			// Then login with new credentials
			const loginResponse = await autenticacionApi.login({
				dTOPeticionLogin: {
					username,
					password
				}
			});

			// Store the authentication data
			authStore.login(loginResponse.token);
			message = `✅ Successfully logged in as ${role}`;

			// Redirect if specified
			if (redirectTo) {
				setTimeout(() => {
					goto(redirectTo);
				}, 50);
			}
		} catch (error) {
			message = `❌ Login failed for ${role}: ${error}`;
			console.error('Login error:', error);
		} finally {
			loginLoading = false;
		}
	}

	async function loginAsAdmin() {
		await quickLogin('admin', 'admin', 'Admin', '/test');
	}

	async function loginAsStudent() {
		await quickLogin('estudiante', 'password', 'Student', '/test');
	}

	async function loginAsProfessor() {
		await quickLogin('profesor', 'password', 'Professor', '/test');
	}
</script>

<div class="rounded-lg border border-purple-200 bg-purple-50 p-4">
	<h3 class="mb-3 text-lg font-semibold text-purple-900">🚀 Quick Login (for Testing)</h3>

	<div class="mb-3 flex flex-wrap gap-3">
		<button
			onclick={loginAsAdmin}
			disabled={loginLoading}
			class="rounded-lg bg-red-600 px-4 py-2 font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍💼 Admin'}
		</button>

		<button
			onclick={loginAsStudent}
			disabled={loginLoading}
			class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍🎓 Student'}
		</button>

		<button
			onclick={loginAsProfessor}
			disabled={loginLoading}
			class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍🏫 Professor'}
		</button>
	</div>

	{#if message}
		<div class="rounded border bg-white p-2 text-sm text-purple-700">
			{message}
		</div>
	{/if}

	<p class="mt-2 text-xs text-purple-600">Auto-redirects to /test after login</p>
</div>
