<script lang="ts">
	import { authStore } from '$lib/stores/authStore.svelte';
	import { autenticacionApi } from '$lib/api';

	let isLogin = $state(true);

	// Form state
	let username = $state('');
	let password = $state('');
	let email = $state(''); // For registration
	let firstName = $state(''); // For registration
	let lastName = $state(''); // For registration
	let error = $state<string | null>(null);
	let loading = $state(false);

	async function handleSubmit() {
		loading = true;
		error = null;
		try {
			if (isLogin) {
				const response = await autenticacionApi.login({
					dTOPeticionLogin: { username, password }
				});
				authStore.login(response);
			} else {
				const response = await autenticacionApi.registro({
					dTOPeticionRegistro: { username, password, email, firstName, lastName }
				});
				authStore.login(response);
			}
		} catch (e: unknown) {
			const caughtError = e as { response?: { json(): Promise<unknown> }; message?: string };
			if (caughtError.response) {
				try {
					const errorData = (await caughtError.response.json()) as {
						fieldErrors?: Record<string, string[]>;
						message?: string;
					};
					if (errorData.fieldErrors) {
						error = Object.values(errorData.fieldErrors).join(', ');
					} else {
						error = errorData.message || 'An unknown error occurred.';
					}
				} catch {
					error = 'Failed to parse error response.';
				}
			} else {
				error = caughtError.message || 'An unknown network error occurred.';
			}
			console.error(e);
		} finally {
			loading = false;
		}
	}
</script>

<div>
	<h2 class="mb-6 text-center text-xl font-bold">
		{isLogin ? 'Iniciar sesión' : 'Crear una nueva cuenta'}
	</h2>
	<form
		class="space-y-6"
		onsubmit={(e) => {
			e.preventDefault();
			handleSubmit();
		}}
	>
		<div>
			<label for="username" class="block text-sm leading-6 font-medium text-gray-900"
				>Nombre de usuario</label
			>
			<div class="mt-2">
				<input
					bind:value={username}
					id="username"
					name="username"
					type="text"
					autocomplete="username"
					required
					class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-gray-300 ring-inset placeholder:text-gray-400 focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm sm:leading-6"
				/>
			</div>
		</div>

		{#if !isLogin}
			<div>
				<label for="firstName" class="block text-sm leading-6 font-medium text-gray-900"
					>Nombre</label
				>
				<div class="mt-2">
					<input
						bind:value={firstName}
						id="firstName"
						name="firstName"
						type="text"
						autocomplete="given-name"
						required
						class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-gray-300 ring-inset placeholder:text-gray-400 focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm sm:leading-6"
					/>
				</div>
			</div>
			<div>
				<label for="lastName" class="block text-sm leading-6 font-medium text-gray-900"
					>Apellidos</label
				>
				<div class="mt-2">
					<input
						bind:value={lastName}
						id="lastName"
						name="lastName"
						type="text"
						autocomplete="family-name"
						required
						class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-gray-300 ring-inset placeholder:text-gray-400 focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm sm:leading-6"
					/>
				</div>
			</div>
			<div>
				<label for="email" class="block text-sm leading-6 font-medium text-gray-900"
					>Correo electrónico</label
				>
				<div class="mt-2">
					<input
						bind:value={email}
						id="email"
						name="email"
						type="email"
						autocomplete="email"
						required
						class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-gray-300 ring-inset placeholder:text-gray-400 focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm sm:leading-6"
					/>
				</div>
			</div>
		{/if}

		<div>
			<div class="flex items-center justify-between">
				<label for="password" class="block text-sm leading-6 font-medium text-gray-900"
					>Contraseña</label
				>
			</div>
			<div class="mt-2">
				<input
					bind:value={password}
					id="password"
					name="password"
					type="password"
					autocomplete={isLogin ? 'current-password' : 'new-password'}
					required
					class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-gray-300 ring-inset placeholder:text-gray-400 focus:ring-2 focus:ring-indigo-600 focus:ring-inset sm:text-sm sm:leading-6"
				/>
			</div>
		</div>

		{#if error}
			<div class="text-sm text-red-600">
				{error}
			</div>
		{/if}

		<div>
			<button
				type="submit"
				disabled={loading}
				class="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm leading-6 font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:opacity-50"
			>
				{loading ? '...' : isLogin ? 'Iniciar sesión' : 'Crear cuenta'}
			</button>
		</div>
	</form>

	<p class="mt-10 text-center text-sm text-gray-500">
		{isLogin ? 'No eres miembro?' : 'Ya tienes una cuenta?'}
		<button
			onclick={() => (isLogin = !isLogin)}
			class="leading-6 font-semibold text-indigo-600 hover:text-indigo-500"
		>
			{isLogin ? 'Crear una cuenta' : 'Iniciar sesión'}
		</button>
	</p>
</div>
