import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
	sub: string;
	usuario?: string;
	id?: number;
	userId?: number;
	roles?: string[];
}

interface LoginResponse {
	token?: string;
	type?: string;
	id?: number;
	username?: string;
	email?: string;
	firstName?: string;
	lastName?: string;
	role?: string;
}

// State
let token = $state<string | null>(null);
let user = $state<DecodedToken | null>(null);
const isAuthenticated = $derived(!!token);
const isAdmin = $derived(user?.roles?.includes('ROLE_ADMIN') ?? false);
const isProfesor = $derived(user?.roles?.includes('ROLE_PROFESOR') ?? false);
const isAlumno = $derived(user?.roles?.includes('ROLE_ALUMNO') ?? false);

// Helper to decode and set user from token
function updateUserFromToken(jwt: string) {
	try {
		const decoded = jwtDecode<DecodedToken>(jwt);
		console.log('Decoded token:', decoded);
		user = decoded;
		token = jwt;

		// Handle userId field from JWT token (priority: userId > id)
		if (decoded.userId) {
			user.id = decoded.userId;
		} else if (decoded.id) {
			user.id = decoded.id;
		}

		if (browser) {
			localStorage.setItem('jwt_token', jwt);
			// Store the user ID in localStorage for persistence
			if (user.id) {
				localStorage.setItem('user_id', user.id.toString());
				console.log('Stored user ID in localStorage:', user.id);
			}
			// Restore the user ID from localStorage if available and not already set
			const storedUserId = localStorage.getItem('user_id');
			console.log('Stored user ID from localStorage:', storedUserId);
			if (storedUserId && !user.id) {
				user.id = parseInt(storedUserId);
				console.log('Restored user ID:', user.id);
			}
		}
	} catch (error) {
		console.error('Invalid token:', error);
		logout();
	}
}

// Actions
function login(jwtOrResponse: string | LoginResponse) {
	if (typeof jwtOrResponse === 'string') {
		// Handle JWT token
		updateUserFromToken(jwtOrResponse);
	} else {
		// Handle full login response
		const response = jwtOrResponse;
		const roleMapping = {
			ADMIN: 'ROLE_ADMIN',
			PROFESOR: 'ROLE_PROFESOR',
			ALUMNO: 'ROLE_ALUMNO'
		};

		// Validate required fields
		if (!response.token || !response.username || !response.role) {
			console.error('Invalid login response:', response);
			throw new Error('Invalid login response from server');
		}

		// Create a user object with the response data and proper role format
		user = {
			sub: response.username,
			usuario: response.username,
			id: response.id,
			roles: [roleMapping[response.role as keyof typeof roleMapping] || `ROLE_${response.role}`]
		};

		console.log('Created user object:', user);

		token = response.token;

		if (browser) {
			localStorage.setItem('jwt_token', token);
			// Store the user ID separately since it's not in the JWT
			if (response.id) {
				localStorage.setItem('user_id', response.id.toString());
				console.log('Stored user ID in localStorage:', response.id);
			}
		}
	}

	// Redirect based on role
	if (user?.roles?.includes('ROLE_ADMIN')) {
		goto('/alumnos');
	} else if (user?.roles?.includes('ROLE_PROFESOR')) {
		goto('/alumnos');
	} else if (user?.roles?.includes('ROLE_ALUMNO')) {
		goto('/alumnos/perfil');
	} else {
		goto('/');
	}
}

function logout() {
	token = null;
	user = null;
	if (browser) {
		localStorage.removeItem('jwt_token');
		localStorage.removeItem('user_id');
	}
	goto('/auth');
}

// Initialization (run once on client)
if (browser) {
	const storedToken = localStorage.getItem('jwt_token');
	if (storedToken) {
		updateUserFromToken(storedToken);
	}
}

// Export the reactive store
export const authStore = {
	get token() {
		return token;
	},
	get user() {
		return user;
	},
	get isAuthenticated() {
		return isAuthenticated;
	},
	get isAdmin() {
		return isAdmin;
	},
	get isProfesor() {
		return isProfesor;
	},
	get isAlumno() {
		return isAlumno;
	},
	login: login as (jwtOrResponse: string | LoginResponse) => void,
	logout
};
