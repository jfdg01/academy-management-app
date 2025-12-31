// Common components for entity management
// ===================================

// Main data display component for entities with support for sorting, loading states, and actions
export { default as EntityDataTable } from './EntityDataTable.svelte';

// Enhanced data table component with beautiful styling and flexible themes
export { default as EnhancedDataTable } from './EnhancedDataTable.svelte';

// Search section with simple/advanced modes and export functionality
export { default as EntitySearchSection } from './EntitySearchSection.svelte';

// Pagination controls with page navigation, size selection, and sorting options
export { default as EntityPaginationControls } from './EntityPaginationControls.svelte';

// Confirmation modal for deleting entity records
export { default as EntityDeleteModal } from './EntityDeleteModal.svelte';

// Reusable message component for displaying success and error notifications
export { default as EntityMessages } from './EntityMessages.svelte';

// Error display component for showing detailed error information
export { default as ErrorDisplay } from './ErrorDisplay.svelte';

// Table utilities for creating beautiful columns and formatters
export * from './tableUtils';

// Re-export all types for easier imports
export * from './types';

// Profile Components
// =================

// Header component for profile pages with title, subtitle, and actions
export { default as ProfileHeader } from './ProfileHeader.svelte';

// Card component for profile sections with consistent styling
export { default as ProfileCard } from './ProfileCard.svelte';

// Information grid component for displaying profile data
export { default as ProfileInfoGrid } from './ProfileInfoGrid.svelte';

// Form component for profile editing with consistent styling
export { default as ProfileForm } from './ProfileForm.svelte';

// Input component for profile forms with validation
export { default as ProfileInput } from './ProfileInput.svelte';
