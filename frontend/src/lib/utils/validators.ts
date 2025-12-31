import type { ValidationResult } from '$lib/types/common';
import { VALIDATION_CONSTANTS } from '$lib/types/common';

export interface ValidationSchema {
	[key: string]: {
		required?: boolean;
		minLength?: number;
		maxLength?: number;
		pattern?: RegExp;
		custom?: (value: unknown) => ValidationResult;
	};
}

export class ValidationUtils {
	static validateName(name: string): ValidationResult {
		if (!name || name.trim().length === 0) {
			return { isValid: false, message: 'Este campo es obligatorio' };
		}
		if (name.length > VALIDATION_CONSTANTS.MAX_NAME_LENGTH) {
			return {
				isValid: false,
				message: `Máximo ${VALIDATION_CONSTANTS.MAX_NAME_LENGTH} caracteres`
			};
		}
		const nameRegex = /^[a-zA-ZáéíóúñÁÉÍÓÚÑ\s]+$/;
		if (!nameRegex.test(name)) {
			return { isValid: false, message: 'Solo se permiten letras, acentos y espacios' };
		}
		return { isValid: true, message: '✓ Válido' };
	}

	static validateDNI(dni: string): ValidationResult {
		if (!dni || dni.trim().length === 0) {
			return { isValid: false, message: 'El DNI es obligatorio' };
		}

		const dniRegex = /^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$/i;
		if (!dniRegex.test(dni)) {
			return { isValid: false, message: 'Formato: 8 números + 1 letra (ej: 12345678Z)' };
		}

		// Calcular letra correcta
		const numbers = dni.substring(0, 8);
		const letter = dni.substring(8, 9).toUpperCase();
		const correctLetters = 'TRWAGMYFPDXBNJZSQVHLCKE';
		const correctLetter = correctLetters[parseInt(numbers) % 23];

		if (letter !== correctLetter) {
			return { isValid: false, message: `La letra debería ser ${correctLetter}` };
		}

		return { isValid: true, message: '✓ DNI válido' };
	}

	static validateEmail(email: string): ValidationResult {
		if (!email || email.trim().length === 0) {
			return { isValid: false, message: 'El email es obligatorio' };
		}

		if (email.length > VALIDATION_CONSTANTS.MAX_EMAIL_LENGTH) {
			return {
				isValid: false,
				message: `Máximo ${VALIDATION_CONSTANTS.MAX_EMAIL_LENGTH} caracteres`
			};
		}

		const [localPart] = email.split('@');
		if (localPart && localPart.length > VALIDATION_CONSTANTS.MAX_LOCAL_EMAIL_LENGTH) {
			return {
				isValid: false,
				message: `La parte local no puede superar ${VALIDATION_CONSTANTS.MAX_LOCAL_EMAIL_LENGTH} caracteres`
			};
		}

		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			return { isValid: false, message: 'Formato de email inválido' };
		}

		if (email.includes('..')) {
			return { isValid: false, message: 'No se permiten puntos consecutivos' };
		}

		return { isValid: true, message: '✓ Email válido' };
	}

	static validatePhoneNumber(phone: string): ValidationResult {
		if (!phone || phone.trim().length === 0) {
			return { isValid: true, message: 'Campo opcional' };
		}

		const allowedCharsRegex = /^[0-9+\-\s().]+$/;
		if (!allowedCharsRegex.test(phone)) {
			return { isValid: false, message: 'Solo números, espacios, guiones, puntos, paréntesis y +' };
		}

		const digits = phone.replace(/[^0-9]/g, '');

		if (digits.length < VALIDATION_CONSTANTS.MIN_PHONE_DIGITS) {
			return { isValid: false, message: `Mínimo ${VALIDATION_CONSTANTS.MIN_PHONE_DIGITS} dígitos` };
		}

		if (digits.length > VALIDATION_CONSTANTS.MAX_PHONE_DIGITS) {
			return { isValid: false, message: `Máximo ${VALIDATION_CONSTANTS.MAX_PHONE_DIGITS} dígitos` };
		}

		return { isValid: true, message: '✓ Teléfono válido' };
	}

	static validateUsername(username: string): ValidationResult {
		if (!username || username.trim().length === 0) {
			return { isValid: false, message: 'El usuario es obligatorio' };
		}
		if (username.length < VALIDATION_CONSTANTS.MIN_USERNAME_LENGTH) {
			return {
				isValid: false,
				message: `Mínimo ${VALIDATION_CONSTANTS.MIN_USERNAME_LENGTH} caracteres`
			};
		}
		if (username.length > VALIDATION_CONSTANTS.MAX_USERNAME_LENGTH) {
			return {
				isValid: false,
				message: `Máximo ${VALIDATION_CONSTANTS.MAX_USERNAME_LENGTH} caracteres`
			};
		}
		return { isValid: true, message: '✓ Usuario válido' };
	}

	static validatePassword(password: string): ValidationResult {
		if (!password || password.trim().length === 0) {
			return { isValid: false, message: 'La contraseña es obligatoria' };
		}
		if (password.length < VALIDATION_CONSTANTS.MIN_PASSWORD_LENGTH) {
			return {
				isValid: false,
				message: `Mínimo ${VALIDATION_CONSTANTS.MIN_PASSWORD_LENGTH} caracteres`
			};
		}
		return { isValid: true, message: '✓ Contraseña válida' };
	}

	static validateFormData(
		data: Record<string, unknown>,
		schema: ValidationSchema
	): ValidationResult[] {
		const results: ValidationResult[] = [];

		for (const [field, rules] of Object.entries(schema)) {
			const value = data[field];

			if (rules.required && (!value || (typeof value === 'string' && value.trim().length === 0))) {
				results.push({ isValid: false, message: `${field} es obligatorio` });
				continue;
			}

			if (value && typeof value === 'string' && rules.minLength && value.length < rules.minLength) {
				results.push({
					isValid: false,
					message: `${field} debe tener al menos ${rules.minLength} caracteres`
				});
			}

			if (value && typeof value === 'string' && rules.maxLength && value.length > rules.maxLength) {
				results.push({
					isValid: false,
					message: `${field} no puede superar ${rules.maxLength} caracteres`
				});
			}

			if (value && typeof value === 'string' && rules.pattern && !rules.pattern.test(value)) {
				results.push({ isValid: false, message: `${field} tiene un formato inválido` });
			}

			if (value && rules.custom) {
				const customResult = rules.custom(value);
				if (!customResult.isValid) {
					results.push(customResult);
				}
			}
		}

		return results;
	}

	static validateGrade(grade: number): ValidationResult {
		if (grade === undefined || grade === null) {
			return { isValid: false, message: 'La nota es obligatoria' };
		}
		if (isNaN(grade)) {
			return { isValid: false, message: 'La nota debe ser un número válido' };
		}
		if (grade < VALIDATION_CONSTANTS.MIN_GRADE || grade > VALIDATION_CONSTANTS.MAX_GRADE) {
			return {
				isValid: false,
				message: `La nota debe estar entre ${VALIDATION_CONSTANTS.MIN_GRADE} y ${VALIDATION_CONSTANTS.MAX_GRADE}`
			};
		}
		return { isValid: true, message: '✓ Nota válida' };
	}

	static validatePrice(price: number): ValidationResult {
		if (price === undefined || price === null) {
			return { isValid: false, message: 'El precio es obligatorio' };
		}
		if (isNaN(price)) {
			return { isValid: false, message: 'El precio debe ser un número válido' };
		}
		if (price < VALIDATION_CONSTANTS.MIN_PRICE) {
			return { isValid: false, message: 'El precio no puede ser negativo' };
		}
		return { isValid: true, message: '✓ Precio válido' };
	}
}
