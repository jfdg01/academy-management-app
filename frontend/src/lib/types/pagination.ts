// Simplified pagination types using generated API types
import type { DTOMetadatosPaginacion, DTORespuestaPaginadaDTOAlumno } from '$lib/generated/api';

// Use generated types directly
export type PaginatedAlumnosResponse = DTORespuestaPaginadaDTOAlumno;
export type PageMetadata = DTOMetadatosPaginacion;
export type SortDirection = 'ASC' | 'DESC';

// Generic interface for paginated data
export interface PaginatedData<T> {
	content: T[];
	page: PageMetadata;
}

// Type for UI display helper
export type PageDisplayInfo = {
	currentPage: number;
	totalPages: number;
	startItem: number;
	endItem: number;
	totalItems: number;
	hasNext: boolean;
	hasPrevious: boolean;
	isFirstPage: boolean;
	isLastPage: boolean;
};

// Centralized parameter validation and creation
export function createValidPaginationParams(searchParams: URLSearchParams) {
	return {
		page: Math.max(0, parseInt(searchParams.get('page') || '0')),
		size: Math.min(100, Math.max(1, parseInt(searchParams.get('size') || '20'))),
		sortBy: searchParams.get('sortBy') || 'id',
		sortDirection: (searchParams.get('sortDirection') || 'ASC') as SortDirection,
		// Filters
		firstName: searchParams.get('firstName') || undefined,
		lastName: searchParams.get('lastName') || undefined,
		dni: searchParams.get('dni') || undefined,
		email: searchParams.get('email') || undefined,
		enrolled: searchParams.get('enrolled') ? searchParams.get('enrolled') === 'true' : undefined
	};
}

// Simple UI helper for display info
export function getPageDisplayInfo(metadata: DTOMetadatosPaginacion): PageDisplayInfo {
	const currentPage = (metadata.number ?? 0) + 1; // Convert to 1-indexed
	const size = metadata.size ?? 20;
	const totalElements = metadata.totalElements ?? 0;

	return {
		currentPage,
		totalPages: metadata.totalPages ?? 0,
		startItem: Math.min(totalElements, (metadata.number ?? 0) * size + 1),
		endItem: Math.min(totalElements, ((metadata.number ?? 0) + 1) * size),
		totalItems: totalElements,
		hasNext: metadata.hasNext ?? false,
		hasPrevious: metadata.hasPrevious ?? false,
		isFirstPage: metadata.first ?? true,
		isLastPage: metadata.last ?? true
	};
}
