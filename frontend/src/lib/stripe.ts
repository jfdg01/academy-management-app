import { loadStripe, type Stripe } from '@stripe/stripe-js';
import { ErrorHandler } from '$lib/utils/errorHandler';

// Direct Stripe publishable key (will be obfuscated later)
const STRIPE_PUBLISHABLE_KEY =
	'pk_test_51S0UCkFCuMbaQ3eotp2Da6H8FP4dOgtEERfVZOkF5WqS7JvjWyu4FHrvFrhncOnLm40z0a2oVPKvizWOXQ31FaW100gZtDyUbf';

if (!STRIPE_PUBLISHABLE_KEY) {
	ErrorHandler.logError('Stripe publishable key not found.', 'stripe-config');
}

// Lazy load Stripe to prevent errors during module initialization
let stripeInstance: Stripe | null = null;

export async function getStripe(): Promise<Stripe> {
	try {
		if (!STRIPE_PUBLISHABLE_KEY) {
			throw new Error('Stripe publishable key not found.');
		}

		if (!stripeInstance) {
			const loadedStripe = await loadStripe(STRIPE_PUBLISHABLE_KEY);
			if (!loadedStripe) {
				throw new Error('Failed to load Stripe');
			}
			stripeInstance = loadedStripe;
		}

		return stripeInstance;
	} catch (error) {
		ErrorHandler.logError(error, 'getStripe');
		throw await ErrorHandler.parseError(error);
	}
}

// For backward compatibility, export a promise that resolves to the stripe instance
export const stripe = getStripe();
