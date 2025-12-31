export interface SearchFieldConfig {
	key: string;
	label: string;
	type: 'text' | 'email' | 'number' | 'tel' | 'date' | 'select';
	placeholder?: string;
	options?: Array<{ value: string | number | boolean; label: string }>;
}

export interface EntitySearchConfig {
	entityType: 'alumnos' | 'profesores' | 'clases' | 'materiales' | 'generic';
	entityNamePlural: string;
	advancedFields: SearchFieldConfig[];
	statusField?: {
		key: string;
		label: string;
		options: Array<{ value: string | number | boolean; label: string }>;
	};
}

export const searchConfigs: Record<string, EntitySearchConfig> = {
	alumnos: {
		entityType: 'alumnos',
		entityNamePlural: 'alumnos',
		advancedFields: [
			{
				key: 'firstName',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'lastName',
				label: 'Apellidos',
				type: 'text',
				placeholder: 'Ej: García López'
			},
			{
				key: 'dni',
				label: 'DNI',
				type: 'text',
				placeholder: 'Ej: 12345678A'
			},
			{
				key: 'email',
				label: 'Email',
				type: 'email',
				placeholder: 'Ej: juan@email.com'
			}
		],
		statusField: {
			key: 'enrolled',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		}
	},
	materiales: {
		entityType: 'materiales',
		entityNamePlural: 'materiales',
		advancedFields: [
			{
				key: 'name',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Documento de matemáticas'
			},
			{
				key: 'url',
				label: 'URL',
				type: 'text',
				placeholder: 'Ej: https://example.com/document.pdf'
			},
			{
				key: 'type',
				label: 'Tipo',
				type: 'select',
				options: [
					{ value: '', label: 'Todos' },
					{ value: 'DOCUMENT', label: 'Documento' },
					{ value: 'IMAGE', label: 'Imagen' },
					{ value: 'VIDEO', label: 'Video' }
				]
			}
		]
	},
	profesores: {
		entityType: 'profesores',
		entityNamePlural: 'profesores',
		advancedFields: [
			{
				key: 'firstName',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: María'
			},
			{
				key: 'lastName',
				label: 'Apellidos',
				type: 'text',
				placeholder: 'Ej: Rodríguez Sánchez'
			},
			{
				key: 'email',
				label: 'Email',
				type: 'email',
				placeholder: 'Ej: maria@email.com'
			},
			{
				key: 'username',
				label: 'Usuario',
				type: 'text',
				placeholder: 'Ej: mrodriguez'
			},
			{
				key: 'dni',
				label: 'DNI',
				type: 'text',
				placeholder: 'Ej: 87654321B'
			}
		],
		statusField: {
			key: 'enabled',
			label: 'Estado de la cuenta',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Habilitado' },
				{ value: 'false', label: 'Deshabilitado' }
			]
		}
	},
	clases: {
		entityType: 'clases',
		entityNamePlural: 'clases',
		advancedFields: [
			{
				key: 'titulo',
				label: 'Título',
				type: 'text',
				placeholder: 'Ej: Matemáticas Avanzadas'
			},
			{
				key: 'descripcion',
				label: 'Descripción',
				type: 'text',
				placeholder: 'Ej: Curso de matemáticas...'
			},
			{
				key: 'nivel',
				label: 'Nivel',
				type: 'select',
				options: [
					{ value: '', label: 'Todos' },
					{ value: 'PRINCIPIANTE', label: 'Principiante' },
					{ value: 'INTERMEDIO', label: 'Intermedio' },
					{ value: 'AVANZADO', label: 'Avanzado' }
				]
			},
			{
				key: 'presencialidad',
				label: 'Presencialidad',
				type: 'select',
				options: [
					{ value: '', label: 'Todas' },
					{ value: 'PRESENCIAL', label: 'Presencial' },
					{ value: 'ONLINE', label: 'Online' }
				]
			},
			{
				key: 'precioMinimo',
				label: 'Precio mínimo',
				type: 'number',
				placeholder: 'Ej: 50'
			},
			{
				key: 'precioMaximo',
				label: 'Precio máximo',
				type: 'number',
				placeholder: 'Ej: 200'
			}
		]
	}
};

export function getSearchConfig(entityType: string): EntitySearchConfig {
	return (
		searchConfigs[entityType] || {
			entityType: 'generic',
			entityNamePlural: 'elementos',
			advancedFields: [],
			statusField: undefined
		}
	);
}
