import type { PageMetadata } from '$lib/types/pagination';

/**
 * Represents the base entity that all other entities should extend
 */
export interface BaseEntity {
	id?: string | number;
	[key: string]: unknown;
}

/**
 * Type that extends BaseEntity with index signature for compatibility
 */
export type EntityWithIndex<T> = T & { [key: string]: unknown };

/**
 * Configuration for entity columns in a data table
 */
export interface EntityColumn<T = Record<string, unknown>> {
	key: keyof T | string;
	header: string;
	sortable?: boolean;
	formatter?: (value: unknown, entity: T) => string | null;
	html?: boolean; // Indicates if the formatter returns HTML that should be rendered
	cell?: (entity: T) => string; // For custom cell rendering
	width?: string;
	class?: string;
}

/**
 * Standard filter interface used across all entity search components
 */
export interface EntityFilters {
	searchMode: 'simple' | 'advanced' | string;
	q?: string; // General search parameter
	[key: string]: unknown;
}

/**
 * Standard pagination interface used across all entity components
 */
export interface EntityPagination {
	page: number;
	size: number;
	sortBy: string;
	sortDirection: 'ASC' | 'DESC';
}

/**
 * Common action configuration for entity tables
 */
export interface EntityAction<T = Record<string, unknown>> {
	label: string;
	dynamicLabel?: (entity: T) => string;
	icon?: string;
	color: string | ((entity: T) => string);
	hoverColor: string | ((entity: T) => string);
	condition?: (entity: T) => boolean;
	disabled?: boolean | ((entity: T) => boolean);
	action: (entity: T) => void;
}

/**
 * Generic entity collection with pagination metadata
 */
export interface PaginatedEntities<T = Record<string, unknown>> {
	content?: T[];
	page?: PageMetadata;
}

/**
 * Authentication store type with common properties
 */
export interface AuthStoreType {
	isAdmin: boolean;
	isAuthenticated: boolean;
	isProfesor?: boolean;
	[key: string]: unknown;
}
