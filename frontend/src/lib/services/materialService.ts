import { materialApi } from '$lib/api';
import type { DTOMaterial, DTORespuestaPaginada } from '$lib/generated/api';
import type { MaterialStats } from '$lib/generated/api/models/MaterialStats';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class MaterialService {
	/**
	 * Get paginated materials with optional filters
	 */
	static async getMaterials(
		params: {
			q?: string;
			name?: string;
			url?: string;
			type?: string;
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: string;
		} = {}
	): Promise<DTORespuestaPaginada> {
		try {
			return await materialApi.obtenerMateriales(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getMaterials');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a specific material by ID
	 */
	static async getMaterialById(id: string): Promise<DTOMaterial> {
		try {
			const response = await materialApi.obtenerMaterialPorId({ id: parseInt(id) });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getMaterialById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get material statistics
	 * Note: This is a placeholder implementation since the API doesn't have a statistics endpoint
	 * In a real implementation, this would call the backend statistics endpoint
	 */
	static async getMaterialStats(): Promise<MaterialStats> {
		try {
			// Since there's no statistics endpoint in the API, we'll return placeholder data
			// In a real implementation, this would call materialApi.obtenerEstadisticas()
			return {
				totalMaterials: 0,
				totalDocuments: 0,
				totalImages: 0,
				totalVideos: 0
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getMaterialStats');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get material type from URL
	 */
	static getMaterialType(url: string): 'DOCUMENT' | 'IMAGE' | 'VIDEO' {
		const extension = url.split('.').pop()?.toLowerCase();

		if (!extension) return 'DOCUMENT';

		const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
		const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv'];

		if (imageExtensions.includes(extension)) {
			return 'IMAGE';
		} else if (videoExtensions.includes(extension)) {
			return 'VIDEO';
		} else {
			return 'DOCUMENT';
		}
	}

	/**
	 * Get material icon based on type
	 */
	static getMaterialIcon(material: DTOMaterial): string {
		// Use URL analysis instead of non-existent properties
		const type = MaterialService.getMaterialType(material.url || '');
		switch (type) {
			case 'VIDEO':
				return '🎥';
			case 'IMAGE':
				return '🖼️';
			default:
				return '📄';
		}
	}

	/**
	 * Get material type label
	 */
	static getMaterialTypeLabel(material: DTOMaterial): string {
		// Use URL analysis instead of non-existent properties
		const type = MaterialService.getMaterialType(material.url || '');
		switch (type) {
			case 'VIDEO':
				return 'Video';
			case 'IMAGE':
				return 'Image';
			default:
				return 'Document';
		}
	}

	/**
	 * Create a new material (independent of classes)
	 */
	static async createMaterial(name: string, url: string): Promise<DTOMaterial> {
		try {
			return await materialApi.crearMaterial({ name, url });
		} catch (error) {
			ErrorHandler.logError(error, 'createMaterial');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Update an existing material
	 */
	static async updateMaterial(id: number, name: string, url: string): Promise<DTOMaterial> {
		try {
			return await materialApi.actualizarMaterial({ id, name, url });
		} catch (error) {
			ErrorHandler.logError(error, 'updateMaterial');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a material
	 */
	static async deleteMaterial(id: number): Promise<void> {
		try {
			await materialApi.eliminarMaterial({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'deleteMaterial');
			throw await ErrorHandler.parseError(error);
		}
	}
}
