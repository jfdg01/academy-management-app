import { writable } from 'svelte/store';
import type { DTORespuestaPaginada } from '$lib/generated/api';

export interface PaginationState {
	currentPage: number;
	pageSize: number;
	totalElements: number;
	totalPages: number;
	sortBy: string;
	sortDirection: 'asc' | 'desc';
}

export function createPaginationStore(initialState: Partial<PaginationState> = {}) {
	const defaultState: PaginationState = {
		currentPage: 0,
		pageSize: 20,
		totalElements: 0,
		totalPages: 0,
		sortBy: 'id',
		sortDirection: 'desc',
		...initialState
	};

	const { subscribe, set, update } = writable<PaginationState>(defaultState);

	return {
		subscribe,
		setPage: (page: number) => update((state) => ({ ...state, currentPage: page })),
		setPageSize: (size: number) =>
			update((state) => ({ ...state, pageSize: size, currentPage: 0 })),
		setSort: (sortBy: string, sortDirection: 'asc' | 'desc') =>
			update((state) => ({ ...state, sortBy, sortDirection, currentPage: 0 })),
		updateFromResponse: (response: DTORespuestaPaginada) =>
			update((state) => ({
				...state,
				totalElements: response.totalElements || 0,
				totalPages: response.totalPages || 0,
				currentPage: response.page || 0
			})),
		reset: () => set(defaultState),
		nextPage: () =>
			update((state) => {
				if (state.currentPage < state.totalPages - 1) {
					return { ...state, currentPage: state.currentPage + 1 };
				}
				return state;
			}),
		previousPage: () =>
			update((state) => {
				if (state.currentPage > 0) {
					return { ...state, currentPage: state.currentPage - 1 };
				}
				return state;
			}),
		goToFirstPage: () => update((state) => ({ ...state, currentPage: 0 })),
		goToLastPage: () =>
			update((state) => ({ ...state, currentPage: Math.max(0, state.totalPages - 1) })),
		getPaginationParams: () => {
			let currentState: PaginationState;
			subscribe((state) => {
				currentState = state;
			})();
			return {
				page: currentState!.currentPage,
				size: currentState!.pageSize,
				sortBy: currentState!.sortBy,
				sortDirection: currentState!.sortDirection
			};
		}
	};
}

// Default pagination store instance
export const paginationStore = createPaginationStore();

// Specialized pagination stores for different entities
export const alumnosPaginationStore = createPaginationStore({ sortBy: 'firstName' });
export const profesoresPaginationStore = createPaginationStore({ sortBy: 'firstName' });
export const clasesPaginationStore = createPaginationStore({ sortBy: 'nombre' });
export const pagosPaginationStore = createPaginationStore({ sortBy: 'fechaPago' });
export const entregasPaginationStore = createPaginationStore({ sortBy: 'fechaEntrega' });
export const ejerciciosPaginationStore = createPaginationStore({ sortBy: 'titulo' });
