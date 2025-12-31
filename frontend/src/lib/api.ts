import {
	type Middleware,
	Configuration,
	StudentsApi,
	ProfessorsApi,
	ClassesApi,
	ClassManagementApi,
	AuthenticationApi,
	UserOperationsApi,
	MaterialsApi,
	PruebasApi,
	ExerciseDeliveriesApi,
	ExercisesApi,
	PaymentsApi
} from './generated/api';
import { authStore } from './stores/authStore.svelte';
import { getApiBasePath } from './config';

export * from './generated/api';

const authMiddleware: Middleware = {
	async pre({ init, url }) {
		const token = authStore.token;
		if (token) {
			// Ensure headers object exists
			if (!init.headers) {
				init.headers = {};
			}
			// Set the Authorization header
			(init.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
		}
		//The return type of pre is Promise<FetchParams | void>
		//The init property is part of FetchParams so we must return it inside an object
		return { url, init };
	}
};

const config = new Configuration({
	basePath: getApiBasePath(),
	middleware: [authMiddleware]
});

// Create API instances
export const alumnoApi = new StudentsApi(config);
export const profesorApi = new ProfessorsApi(config);
export const claseApi = new ClassesApi(config);
export const classManagementApi = new ClassManagementApi(config);
export const autenticacionApi = new AuthenticationApi(config);
export const userOperationsApi = new UserOperationsApi(config);
export const materialApi = new MaterialsApi(config);
export const pruebasApi = new PruebasApi(config);
export const entregaApi = new ExerciseDeliveriesApi(config);
export const ejercicioApi = new ExercisesApi(config);
export const pagoApi = new PaymentsApi(config);
