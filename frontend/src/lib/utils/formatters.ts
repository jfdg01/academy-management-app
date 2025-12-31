export interface DateFormatOptions {
	readonly includeTime?: boolean;
	readonly includeSeconds?: boolean;
	readonly format?: 'short' | 'long' | 'full';
}

export interface StatusConfig {
	readonly delivery: Readonly<Record<string, { readonly text: string; readonly color: string }>>;
	readonly exercise: Readonly<Record<string, { readonly text: string; readonly color: string }>>;
	readonly payment: Readonly<Record<string, { readonly text: string; readonly color: string }>>;
}

export class FormatterUtils {
	private static readonly STATUS_CONFIG: StatusConfig = {
		delivery: {
			PENDIENTE: { text: 'Pendiente', color: 'bg-yellow-100 text-yellow-800' },
			ENTREGADO: { text: 'Entregado', color: 'bg-blue-100 text-blue-800' },
			CALIFICADO: { text: 'Calificado', color: 'bg-green-100 text-green-800' }
		} as const,
		exercise: {
			ACTIVE: { text: 'Activo', color: 'bg-green-100 text-green-800' },
			EXPIRED: { text: 'Vencido', color: 'bg-red-100 text-red-800' },
			FUTURE: { text: 'Futuro', color: 'bg-blue-100 text-blue-800' },
			WITH_DELIVERIES: { text: 'Con entregas', color: 'bg-purple-100 text-purple-800' },
			WITHOUT_DELIVERIES: { text: 'Sin entregas', color: 'bg-yellow-100 text-yellow-800' }
		} as const,
		payment: {
			EXITO: { text: 'Success', color: 'bg-green-100 text-green-800' },
			PENDIENTE: { text: 'Pending', color: 'bg-yellow-100 text-yellow-800' },
			PROCESANDO: { text: 'Processing', color: 'bg-blue-100 text-blue-800' },
			ERROR: { text: 'Failed', color: 'bg-red-100 text-red-800' },
			REEMBOLSADO: { text: 'Refunded', color: 'bg-purple-100 text-purple-800' }
		} as const
	};

	static formatDate(
		date: Date | string | undefined | null,
		options: Readonly<DateFormatOptions> = {}
	): string {
		if (!date) return 'N/A';

		const dateObj = new Date(date);
		if (isNaN(dateObj.getTime())) return 'N/A';

		const { includeTime = false, includeSeconds = false, format = 'short' } = options;

		const dateOptions: Intl.DateTimeFormatOptions = {
			year: 'numeric',
			month: format === 'short' ? 'short' : 'long',
			day: 'numeric'
		};

		if (includeTime) {
			dateOptions.hour = '2-digit';
			dateOptions.minute = '2-digit';
			if (includeSeconds) {
				dateOptions.second = '2-digit';
			}
		}

		return dateObj.toLocaleDateString('es-ES', dateOptions);
	}

	static formatStatus(status: string | undefined | null, type: keyof StatusConfig): string {
		if (!status) return 'N/A';
		return this.STATUS_CONFIG[type][status]?.text ?? status;
	}

	static getStatusColor(status: string | undefined | null, type: keyof StatusConfig): string {
		if (!status) return 'bg-gray-100 text-gray-800';
		return this.STATUS_CONFIG[type][status]?.color ?? 'bg-gray-100 text-gray-800';
	}

	static formatGrade(nota: number | undefined | null): string {
		if (nota === undefined || nota === null || isNaN(nota)) return 'N/A';
		return nota.toFixed(1);
	}

	static getGradeColor(nota: number | undefined | null): string {
		if (nota === undefined || nota === null || isNaN(nota)) return 'text-gray-500';
		if (nota >= 9) return 'text-green-600 font-bold';
		if (nota >= 7) return 'text-blue-600 font-semibold';
		if (nota >= 5) return 'text-yellow-600 font-semibold';
		return 'text-red-600 font-semibold';
	}

	static formatPrice(precio: number | undefined | null): string {
		if (precio === undefined || precio === null || isNaN(precio)) return 'N/A';
		return `€${precio.toFixed(2)}`;
	}

	static formatAmount(amount: number | undefined | null): string {
		if (amount === undefined || amount === null || isNaN(amount)) return '€0.00';
		return new Intl.NumberFormat('es-ES', {
			style: 'currency',
			currency: 'EUR'
		}).format(amount);
	}

	static getNivelColor(nivel: string | undefined | null): string {
		if (!nivel) return 'bg-gray-100 text-gray-800';

		const nivelColors: Readonly<Record<string, string>> = {
			PRINCIPIANTE: 'bg-green-100 text-green-800',
			INTERMEDIO: 'bg-blue-100 text-blue-800',
			AVANZADO: 'bg-purple-100 text-purple-800',
			EXPERTO: 'bg-red-100 text-red-800'
		} as const;

		return nivelColors[nivel.toUpperCase()] ?? 'bg-gray-100 text-gray-800';
	}

	static formatNivel(nivel: string | undefined | null): string {
		if (!nivel) return 'N/A';

		const nivelNames: Readonly<Record<string, string>> = {
			PRINCIPIANTE: 'Principiante',
			INTERMEDIO: 'Intermedio',
			AVANZADO: 'Avanzado',
			EXPERTO: 'Experto'
		} as const;

		return nivelNames[nivel.toUpperCase()] ?? nivel;
	}

	static formatFileSize(bytes: number | undefined | null): string {
		if (bytes === undefined || bytes === null || isNaN(bytes) || bytes < 0) return 'N/A';

		const units = ['B', 'KB', 'MB', 'GB'] as const;
		let size = bytes;
		let unitIndex = 0;

		while (size >= 1024 && unitIndex < units.length - 1) {
			size /= 1024;
			unitIndex++;
		}

		return `${size.toFixed(1)} ${units[unitIndex]}`;
	}

	static formatDuration(minutes: number | undefined | null): string {
		if (minutes === undefined || minutes === null || isNaN(minutes) || minutes < 0) return 'N/A';

		const hours = Math.floor(minutes / 60);
		const remainingMinutes = minutes % 60;

		if (hours === 0) {
			return `${remainingMinutes} min`;
		}

		return remainingMinutes === 0 ? `${hours}h` : `${hours}h ${remainingMinutes}min`;
	}

	static getPresencialidadColor(presencialidad: string | undefined | null): string {
		if (!presencialidad) return 'bg-gray-100 text-gray-800';

		const presencialidadColors: Readonly<Record<string, string>> = {
			ONLINE: 'bg-blue-100 text-blue-800',
			PRESENCIAL: 'bg-purple-100 text-purple-800'
		} as const;

		return presencialidadColors[presencialidad.toUpperCase()] ?? 'bg-gray-100 text-gray-800';
	}

	static getPresencialidadText(presencialidad: string | undefined | null): string {
		if (!presencialidad) return 'N/A';

		const presencialidadNames: Readonly<Record<string, string>> = {
			ONLINE: 'Online',
			PRESENCIAL: 'Presencial'
		} as const;

		return presencialidadNames[presencialidad.toUpperCase()] ?? presencialidad;
	}

	static getExerciseStatusColor(status: string | undefined | null): string {
		if (!status) return 'bg-gray-100 text-gray-800';
		return this.getStatusColor(status, 'exercise');
	}

	static getExerciseStatusText(status: string | undefined | null): string {
		if (!status) return 'N/A';
		return this.formatStatus(status, 'exercise');
	}
}
