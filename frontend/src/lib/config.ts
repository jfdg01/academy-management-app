// API Configuration
//
// This file centralizes all API configuration to make it easier to manage
// across the application. To change the API base path, simply update the
// basePath value below.
//
// For production environments, you can uncomment the environment variable
// usage in getApiBasePath() function and set VITE_API_BASE_URL in your
// environment variables.

export const API_CONFIG = {
	basePath: 'http://localhost:8080'
} as const;

// Environment-based configuration
export const getApiBasePath = (): string => {
	// Use environment variable if available, otherwise fall back to default
	return import.meta.env?.PUBLIC_BACKEND_URL || API_CONFIG.basePath;
};
