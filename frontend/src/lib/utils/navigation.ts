import { goto } from '$app/navigation';

export class NavigationUtils {
	static goToEntity(entityType: string, id: number): void {
		goto(`/${entityType}/${id}`);
	}

	static goToEntityEdit(entityType: string, id: number): void {
		goto(`/${entityType}/${id}/editar`);
	}

	static goToPayment(classId: number, amount: number, description: string): void {
		const url = `/payment?classId=${classId}&amount=${amount}&description=${encodeURIComponent(description)}`;
		goto(url);
	}

	static goToPaymentSuccess(paymentId: number, classId?: number): void {
		let url = `/payment-success?payment_id=${paymentId}`;
		if (classId) {
			url += `&classId=${classId}`;
		}
		goto(url);
	}

	static goBack(fallbackUrl: string): void {
		if (typeof window !== 'undefined' && window.history.length > 1) {
			window.history.back();
		} else {
			goto(fallbackUrl);
		}
	}

	static goToEntityList(entityType: string): void {
		goto(`/${entityType}`);
	}

	static goToEntityCreate(entityType: string): void {
		goto(`/${entityType}/nuevo`);
	}

	static goToAuth(): void {
		goto('/auth');
	}

	static goToHome(): void {
		goto('/');
	}

	static goToProfile(): void {
		goto('/profile');
	}

	static goToDeliveryGrade(deliveryId: number): void {
		goto(`/entregas/${deliveryId}/calificar`);
	}

	static goToDeliveryView(deliveryId: number): void {
		goto(`/entregas/${deliveryId}`);
	}

	static goToExerciseView(exerciseId: number): void {
		goto(`/ejercicios/${exerciseId}`);
	}

	static goToClassView(classId: number): void {
		goto(`/clases/${classId}`);
	}

	static goToPaymentView(paymentId: number): void {
		goto(`/pagos/${paymentId}`);
	}

	static goToAdminPayments(): void {
		goto('/pagos/admin');
	}

	static goToMyPayments(): void {
		goto('/pagos/my-payments');
	}

	static navigateToEditExercise(exerciseId: number): void {
		goto(`/ejercicios/${exerciseId}/editar`);
	}

	static navigateToSubmitExercise(exerciseId: number): void {
		goto(`/ejercicios/${exerciseId}/entregar`);
	}

	static navigateToDelivery(deliveryId: number): void {
		goto(`/entregas/${deliveryId}`);
	}

	static navigateToGradeDelivery(deliveryId: number): void {
		goto(`/entregas/${deliveryId}/calificar`);
	}
}
