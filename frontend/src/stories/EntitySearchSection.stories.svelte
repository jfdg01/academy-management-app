<script module lang="ts">
	import { defineMeta } from '@storybook/addon-svelte-csf';
	import EntitySearchSection from '../lib/components/common/EntitySearchSection.svelte';
	import { fn, expect, userEvent, within } from 'storybook/test';
	import type { EntityFilters, PaginatedEntities } from '../lib/components/common/types';

	// Mock data for testing
	const mockPaginatedData: PaginatedEntities<Record<string, unknown>> = {
		content: [
			{ id: 1, nombre: 'Juan García', email: 'juan@email.com', dni: '12345678A' },
			{ id: 2, nombre: 'María López', email: 'maria@email.com', dni: '87654321B' },
			{ id: 3, nombre: 'Carlos Ruiz', email: 'carlos@email.com', dni: '11223344C' }
		],
		page: {
			totalElements: 3,
			totalPages: 1,
			size: 20,
			number: 0,
			first: true,
			last: true,
			hasNext: false,
			hasPrevious: false
		}
	};

	const { Story } = defineMeta({
		title: 'Components/EntitySearchSection',
		component: EntitySearchSection,
		tags: ['autodocs'],
		argTypes: {
			loading: { control: 'boolean' }
		}
	});
</script>

<!-- Default Story - Alumnos Search -->
<Story
	name="Alumnos Search"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		}
	}}
/>

<!-- Profesores Search -->
<Story
	name="Profesores Search"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: '',
			nombre: '',
			apellidos: '',
			email: '',
			usuario: '',
			dni: '',
			habilitado: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'profesores',
		entityType: 'profesores',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: María'
			},
			{
				key: 'apellidos',
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
				key: 'usuario',
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
			key: 'habilitado',
			label: 'Estado de la cuenta',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Habilitado' },
				{ value: 'false', label: 'Deshabilitado' }
			]
		}
	}}
/>

<!-- Clases Search -->
<Story
	name="Clases Search"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: '',
			titulo: '',
			descripcion: '',
			nivel: '',
			presencialidad: '',
			precioMinimo: '',
			precioMaximo: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'clases',
		entityType: 'clases',
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
				placeholder: 'Ej: Curso de matemáticas para nivel avanzado'
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
					{ value: 'ONLINE', label: 'Online' },
					{ value: 'HIBRIDA', label: 'Híbrida' }
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
	}}
/>

<!-- Advanced Search Mode -->
<Story
	name="Advanced Search Mode"
	args={{
		currentFilters: {
			searchMode: 'advanced',
			q: 'Juan',
			nombre: 'Juan',
			apellidos: 'García',
			dni: '12345678A',
			email: 'juan@email.com',
			matriculado: 'true'
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		}
	}}
/>

<!-- Loading State -->
<Story
	name="Loading State"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		},
		paginatedData: null,
		loading: true,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		}
	}}
/>

<!-- Empty Results -->
<Story
	name="Empty Results"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: 'NoExiste',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		},
		paginatedData: {
			content: [],
			page: {
				totalElements: 0,
				totalPages: 0,
				size: 20,
				number: 0,
				first: true,
				last: true,
				hasNext: false,
				hasPrevious: false
			}
		},
		loading: false,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		}
	}}
/>

<!-- Generic Search (Minimal Configuration) -->
<Story 
	name="Generic Search" 
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'elementos',
		entityType: 'generic',
		advancedFields: [],
		statusField: null
	}} 
/>

<!-- Functional Testing Story -->
<Story 
	name="Interactive Search Testing"
	args={{
		currentFilters: {
			searchMode: 'simple',
			q: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		},
		onSwitchSearchMode: fn(),
		onUpdateFilters: fn(),
		onClearFilters: fn(),
		onExportResults: fn()
	}}
	play={async ({ args, canvasElement }) => {
		const canvas = within(canvasElement);
		
		// Test 1: Search mode switching
		const advancedButton = canvas.getByText('⚙️ Búsqueda Avanzada');
		await userEvent.click(advancedButton);
		expect(args.onSwitchSearchMode).toHaveBeenCalledWith('advanced');
		
		// Test 2: Simple search input
		const searchInput = canvas.getByLabelText('Búsqueda General');
		await userEvent.type(searchInput, 'Juan García');
		
		// Test 3: Clear filters button (if visible)
		const clearButton = canvas.queryByText('Limpiar Filtros');
		if (clearButton) {
			await userEvent.click(clearButton);
			expect(args.onClearFilters).toHaveBeenCalled();
		}
		
		// Test 4: Export button (if visible)
		const exportButton = canvas.queryByText('Exportar');
		if (exportButton) {
			await userEvent.click(exportButton);
			expect(args.onExportResults).toHaveBeenCalled();
		}
	}}
/>

<!-- Advanced Search Interaction Test -->
<Story 
	name="Advanced Search Interactions"
	args={{
		currentFilters: {
			searchMode: 'advanced',
			q: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		},
		paginatedData: mockPaginatedData,
		loading: false,
		entityNamePlural: 'alumnos',
		entityType: 'alumnos',
		advancedFields: [
			{
				key: 'nombre',
				label: 'Nombre',
				type: 'text',
				placeholder: 'Ej: Juan'
			},
			{
				key: 'apellidos',
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
			key: 'matriculado',
			label: 'Estado de matrícula',
			options: [
				{ value: '', label: 'Todos' },
				{ value: 'true', label: 'Matriculado' },
				{ value: 'false', label: 'No matriculado' }
			]
		},
		onSwitchSearchMode: fn(),
		onUpdateFilters: fn(),
		onClearFilters: fn(),
		onExportResults: fn()
	}}
	play={async ({ args, canvasElement }) => {
		const canvas = within(canvasElement);
		
		// Test advanced search fields
		const nombreInput = canvas.getByLabelText('Nombre');
		await userEvent.type(nombreInput, 'Juan');
		
		const apellidosInput = canvas.getByLabelText('Apellidos');
		await userEvent.type(apellidosInput, 'García');
		
		const dniInput = canvas.getByLabelText('DNI');
		await userEvent.type(dniInput, '12345678A');
		
		const emailInput = canvas.getByLabelText('Email');
		await userEvent.type(emailInput, 'juan@email.com');
		
		// Test status field dropdown
		const statusSelect = canvas.getByLabelText('Estado de matrícula');
		await userEvent.selectOptions(statusSelect, 'true');
		
		// Switch back to simple mode
		const simpleButton = canvas.getByText('🔍 Búsqueda Simple');
		await userEvent.click(simpleButton);
		expect(args.onSwitchSearchMode).toHaveBeenCalledWith('simple');
	}}
/>
