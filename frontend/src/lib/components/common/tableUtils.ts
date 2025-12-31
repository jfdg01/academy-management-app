/**
 * Utility functions for creating beautiful table columns with consistent styling
 */

import type { EntityColumn } from './types';

/**
 * Creates a status badge with consistent styling
 */
export function createStatusBadge(
	value: unknown,
	entity: Record<string, unknown>,
	statusMap: Record<string, { label: string; color: string; bgColor: string }>
): string {
	const status = String(value).toLowerCase();
	const config = statusMap[status] || { label: String(value), color: 'gray', bgColor: 'gray' };

	return `<span class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold text-${config.color}-800 bg-${config.bgColor}-100">${config.label}</span>`;
}

/**
 * Creates an enabled/disabled status badge
 */
export function createEnabledBadge(value: unknown): string {
	return createStatusBadge(
		value,
		{},
		{
			true: { label: 'Habilitado', color: 'green', bgColor: 'green' },
			false: { label: 'Deshabilitado', color: 'red', bgColor: 'red' }
		}
	);
}

/**
 * Creates a matriculation status badge
 */
export function createMatriculationBadge(value: unknown): string {
	return createStatusBadge(
		value,
		{},
		{
			true: { label: 'Matriculado', color: 'green', bgColor: 'green' },
			false: { label: 'No Matriculado', color: 'yellow', bgColor: 'yellow' }
		}
	);
}

/**
 * Creates a price formatter with currency symbol
 */
export function createPriceFormatter(currency: string = '€'): (value: unknown) => string {
	return (value: unknown) => {
		if (value === null || value === undefined) return '-';
		const num = parseFloat(String(value));
		if (isNaN(num)) return '-';
		return `${num.toFixed(2)}${currency}`;
	};
}

/**
 * Creates a date formatter with consistent formatting
 */
export function createDateFormatter(
	options: Intl.DateTimeFormatOptions = {}
): (value: unknown) => string {
	const defaultOptions: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: 'short',
		day: 'numeric',
		...options
	};

	return (value: unknown) => {
		if (!value) return '-';
		try {
			return new Date(value as string | Date).toLocaleDateString('es-ES', defaultOptions);
		} catch {
			return '-';
		}
	};
}

/**
 * Creates a name formatter that combines first and last name
 */
export function createNameFormatter(
	firstNameKey: string,
	lastNameKey: string
): (value: unknown, entity: Record<string, unknown>) => string {
	return (value: unknown, entity: Record<string, unknown>) => {
		const firstName = entity[firstNameKey] || '';
		const lastName = entity[lastNameKey] || '';
		const fullName = `${firstName} ${lastName}`.trim();
		return fullName || '-';
	};
}

/**
 * Creates a truncated text formatter
 */
export function createTruncatedTextFormatter(maxLength: number = 50): (value: unknown) => string {
	return (value: unknown) => {
		if (!value) return '-';
		const text = String(value);
		if (text.length <= maxLength) return text;
		return `${text.substring(0, maxLength)}...`;
	};
}

/**
 * Creates a phone number formatter
 */
export function createPhoneFormatter(): (value: unknown) => string {
	return (value: unknown) => {
		if (!value) return '-';
		const phone = String(value).replace(/\D/g, '');
		if (phone.length === 0) return '-';

		// Format Spanish phone numbers
		if (phone.startsWith('34') && phone.length === 11) {
			return `+34 ${phone.substring(2, 5)} ${phone.substring(5, 8)} ${phone.substring(8)}`;
		}
		if (phone.length === 9) {
			return `${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6)}`;
		}

		return String(value);
	};
}

/**
 * Creates an email formatter with truncation
 */
export function createEmailFormatter(maxLength: number = 30): (value: unknown) => string {
	return (value: unknown) => {
		if (!value) return '-';
		const email = String(value);
		if (email.length <= maxLength) return email;

		const [localPart, domain] = email.split('@');
		if (!domain) return email;

		const maxLocalLength = Math.max(10, maxLength - domain.length - 4);
		const truncatedLocal =
			localPart.length > maxLocalLength
				? `${localPart.substring(0, maxLocalLength)}...`
				: localPart;

		return `${truncatedLocal}@${domain}`;
	};
}

/**
 * Creates a DNI formatter with proper formatting
 */
export function createDNIFormatter(): (value: unknown) => string {
	return (value: unknown) => {
		if (!value) return '-';
		const dni = String(value).toUpperCase();
		if (dni.length === 9) {
			return `${dni.substring(0, 8)}-${dni.substring(8)}`;
		}
		return dni;
	};
}

/**
 * Creates a level badge for classes
 */
export function createLevelBadge(value: unknown): string {
	const levelMap: Record<string, { label: string; color: string; bgColor: string }> = {
		PRINCIPIANTE: { label: 'Principiante', color: 'blue', bgColor: 'blue' },
		INTERMEDIO: { label: 'Intermedio', color: 'yellow', bgColor: 'yellow' },
		AVANZADO: { label: 'Avanzado', color: 'purple', bgColor: 'purple' }
	};

	return createStatusBadge(value, {}, levelMap);
}

/**
 * Creates a presenciality badge for classes
 */
export function createPresencialityBadge(value: unknown): string {
	const presencialityMap: Record<string, { label: string; color: string; bgColor: string }> = {
		PRESENCIAL: { label: 'Presencial', color: 'green', bgColor: 'green' },
		ONLINE: { label: 'Online', color: 'blue', bgColor: 'blue' }
	};

	return createStatusBadge(value, {}, presencialityMap);
}

/**
 * Common column configurations for different entity types
 */
export const commonColumns = {
	student: {
		id: { key: 'id', header: 'ID', sortable: true, width: '80px' },
		name: {
			key: 'firstName',
			header: 'Nombre',
			sortable: true,
			formatter: createNameFormatter('firstName', 'lastName')
		},
		email: { key: 'email', header: 'Email', sortable: true, formatter: createEmailFormatter() },
		dni: { key: 'dni', header: 'DNI', sortable: true, formatter: createDNIFormatter() },
		enrollment: {
			key: 'enrolled',
			header: 'Matriculación',
			sortable: true,
			html: true,
			formatter: createMatriculationBadge
		},
		enabled: {
			key: 'enabled',
			header: 'Estado',
			sortable: true,
			html: true,
			formatter: createEnabledBadge
		}
	},
	teacher: {
		id: { key: 'id', header: 'ID', sortable: true, width: '80px' },
		name: {
			key: 'firstName',
			header: 'Nombre',
			sortable: true,
			formatter: createNameFormatter('firstName', 'lastName')
		},
		email: { key: 'email', header: 'Email', sortable: true, formatter: createEmailFormatter() },
		dni: { key: 'dni', header: 'DNI', sortable: true, formatter: createDNIFormatter() },
		enabled: {
			key: 'enabled',
			header: 'Estado',
			sortable: true,
			html: true,
			formatter: createEnabledBadge
		},
		createdAt: {
			key: 'createdAt',
			header: 'Fecha de Creación',
			sortable: true,
			formatter: createDateFormatter()
		}
	},
	class: {
		id: { key: 'id', header: 'ID', sortable: true, width: '80px' },
		title: {
			key: 'titulo',
			header: 'Título',
			sortable: true,
			formatter: createTruncatedTextFormatter(50)
		},
		description: {
			key: 'descripcion',
			header: 'Descripción',
			sortable: true,
			formatter: createTruncatedTextFormatter(100)
		},
		level: {
			key: 'nivel',
			header: 'Nivel',
			sortable: true,
			html: true,
			formatter: createLevelBadge
		},
		presenciality: {
			key: 'presencialidad',
			header: 'Presencialidad',
			sortable: true,
			html: true,
			formatter: createPresencialityBadge
		},
		price: {
			key: 'precio',
			header: 'Precio',
			sortable: true,
			formatter: createPriceFormatter('€')
		}
	}
};

/**
 * Helper function to create a column with all properties
 */
export function createColumn<T = Record<string, unknown>>(
	config: Partial<EntityColumn<T>> & { key: string; header: string }
): EntityColumn<T> {
	return {
		sortable: false,
		...config
	} as EntityColumn<T>;
}

/**
 * Helper function to create multiple columns from a configuration object
 */
export function createColumns<T = Record<string, unknown>>(
	configs: Record<string, Partial<EntityColumn<T>> & { key: string; header: string }>
): EntityColumn<T>[] {
	return Object.values(configs).map(createColumn);
}
