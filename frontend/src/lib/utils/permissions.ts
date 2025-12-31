import type { DTOAlumno, DTOProfesor } from '$lib/generated/api';

interface EntityWithStatus {
	estado?: string;
	id?: number;
	alumnoEntreganteId?: number;
}

export interface Action {
	id: string;
	label: string;
	icon?: string;
	color?: string;
	condition: (
		user: DTOAlumno | DTOProfesor | { roles?: string[] },
		entity?: EntityWithStatus
	) => boolean;
}

export class PermissionUtils {
	static canEditEntity(
		entityType: string,
		user: DTOAlumno | DTOProfesor | { roles?: string[] }
	): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return (
				user.roles.includes('ROLE_ADMIN') ||
				(entityType === 'profesores' && user.roles.includes('ROLE_PROFESOR'))
			);
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return (
				user.role.includes('ADMIN') ||
				(entityType === 'profesores' && user.role.includes('PROFESOR'))
			);
		}
		return false;
	}

	static canDeleteEntity(
		entityType: string,
		user: DTOAlumno | DTOProfesor | { roles?: string[] }
	): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN');
		}
		return false;
	}

	static canGradeDelivery(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static canEnrollInClass(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ALUMNO');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ALUMNO');
		}
		return false;
	}

	static canManagePayments(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN');
		}
		return false;
	}

	static canViewAllDeliveries(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static canViewAllStudents(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static canViewAllTeachers(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN');
		}
		return false;
	}

	static canCreateClasses(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static canCreateExercises(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static canCreateMaterials(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
		}
		return false;
	}

	static getVisibleActions(
		entityType: string,
		user: DTOAlumno | DTOProfesor | { roles?: string[] },
		entity?: EntityWithStatus
	): Action[] {
		const actions: Action[] = [
			{
				id: 'view',
				label: 'Ver',
				icon: '👁️',
				color: 'blue',
				condition: () => true
			}
		];

		if (this.canEditEntity(entityType, user)) {
			actions.push({
				id: 'edit',
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				condition: () => true
			});
		}

		if (this.canDeleteEntity(entityType, user)) {
			actions.push({
				id: 'delete',
				label: 'Eliminar',
				icon: '🗑️',
				color: 'red',
				condition: () => true
			});
		}

		// Special actions for deliveries
		if (entityType === 'entregas' && this.canGradeDelivery(user)) {
			actions.push({
				id: 'grade',
				label: 'Calificar',
				icon: '📝',
				color: 'purple',
				condition: (user, entity) => entity?.estado === 'ENTREGADO'
			});
		}

		return actions.filter((action) => action.condition(user, entity));
	}

	static canAccessRoute(
		route: string,
		user: DTOAlumno | DTOProfesor | { roles?: string[] }
	): boolean {
		const routePermissions: Record<string, string[]> = {
			'/admin': ['ADMIN'],
			'/profesores': ['ADMIN', 'PROFESOR'],
			'/alumnos': ['ADMIN', 'PROFESOR'],
			'/pagos/admin': ['ADMIN'],
			'/clases/nuevo': ['ADMIN', 'PROFESOR'],
			'/ejercicios/nuevo': ['ADMIN', 'PROFESOR'],
			'/materiales/nuevo': ['ADMIN', 'PROFESOR']
		};

		const requiredrole = routePermissions[route];
		if (!requiredrole) return true;

		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return requiredrole.some((role) => user.roles.includes(`ROLE_${role}`));
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return requiredrole.some((role) => user.role.includes(role));
		}
		return false;
	}

	static isAdmin(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ADMIN');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ADMIN');
		}
		return false;
	}

	static isProfessor(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_PROFESOR');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('PROFESOR');
		}
		return false;
	}

	static isStudent(user: DTOAlumno | DTOProfesor | { roles?: string[] }): boolean {
		// Handle auth store format (roles array)
		if ('roles' in user && user.roles) {
			return user.roles.includes('ROLE_ALUMNO');
		}
		// Handle DTO format (role string)
		if ('role' in user && user.role) {
			return user.role.includes('ALUMNO');
		}
		return false;
	}

	static canViewEntity(
		entityType: string,
		user: DTOAlumno | DTOProfesor | { roles?: string[] },
		entity?: EntityWithStatus
	): boolean {
		// Students can only view their own entities
		if (this.isStudent(user)) {
			if (entityType === 'alumnos' && entity?.id !== user.id) {
				return false;
			}
			if (entityType === 'entregas' && entity?.alumnoEntreganteId !== user.id) {
				return false;
			}
		}

		return true;
	}
}
