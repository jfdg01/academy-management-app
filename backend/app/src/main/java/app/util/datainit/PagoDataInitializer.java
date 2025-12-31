
package app.util.datainit;

import app.entidades.Pago;
import app.entidades.ItemPago;
import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;
import app.repositorios.RepositorioPago;
import app.repositorios.RepositorioAlumno;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import app.entidades.Alumno;

@Component
@Profile("!test")
public class PagoDataInitializer extends BaseDataInitializer {
    private static final int NUM_PAYMENTS = 30;
    private final List<Pago> createdPayments = new ArrayList<>();

    @Override
    public void initialize() {
        RepositorioPago repositorioPago = context.getBean(RepositorioPago.class);
        RepositorioAlumno repositorioAlumno = context.getBean(RepositorioAlumno.class);

        // Check if payments already exist to avoid duplicates
        if (repositorioPago.count() > 0) {
            System.out.println("ℹ Payments already exist, skipping payment creation");
            return;
        }

        // Get all student IDs to assign payments to
        List<Long> studentIds = repositorioAlumno.findAll().stream()
                .map(alumno -> alumno.getId())
                .toList();

        if (studentIds.isEmpty()) {
            System.out.println("⚠ No students found, skipping payment creation");
            return;
        }

        // Create various types of payments
        createSuccessfulPayments(repositorioPago, studentIds, repositorioAlumno);
        createFailedPayments(repositorioPago, studentIds, repositorioAlumno);
        createRefundedPayments(repositorioPago, studentIds, repositorioAlumno);
        createManualPayments(repositorioPago, studentIds, repositorioAlumno);

        System.out.println("Payments created: " + createdPayments.size());
    }

    private void createSuccessfulPayments(RepositorioPago repositorioPago, List<Long> studentIds, RepositorioAlumno repositorioAlumno) {
        // Create successful payments with different payment methods
        EMetodoPago[] successfulMethods = {EMetodoPago.STRIPE, EMetodoPago.DEBITO, EMetodoPago.CREDITO, EMetodoPago.TRANSFERENCIA};
        
        for (int i = 0; i < 15; i++) {
            try {
                Long studentId = studentIds.get(random.nextInt(studentIds.size()));
                EMetodoPago method = successfulMethods[random.nextInt(successfulMethods.length)];
                
                Pago pago = createPayment(
                    generateRandomAmount(50.0, 500.0),
                    method,
                    EEstadoPago.EXITO,
                    studentId,
                    generateRandomDate(-30, 0), // Last 30 days
                    repositorioAlumno
                );
                
                // Add items to the payment
                addRandomItems(pago);
                
                Pago savedPago = repositorioPago.save(pago);
                createdPayments.add(savedPago);
            } catch (Exception e) {
                System.err.println("✗ Error creating successful payment: " + e.getMessage());
            }
        }
    }

    private void createFailedPayments(RepositorioPago repositorioPago, List<Long> studentIds, RepositorioAlumno repositorioAlumno) {
        // Create failed payments
        for (int i = 0; i < 5; i++) {
            try {
                Long studentId = studentIds.get(random.nextInt(studentIds.size()));
                
                Pago pago = createPayment(
                    generateRandomAmount(25.0, 200.0),
                    EMetodoPago.STRIPE,
                    EEstadoPago.ERROR,
                    studentId,
                    generateRandomDate(-15, 0), // Last 15 days
                    repositorioAlumno
                );
                
                // Add failure reason
                pago.setFailureReason(generateRandomFailureReason());
                
                Pago savedPago = repositorioPago.save(pago);
                createdPayments.add(savedPago);
            } catch (Exception e) {
                System.err.println("✗ Error creating failed payment: " + e.getMessage());
            }
        }
    }

    private void createRefundedPayments(RepositorioPago repositorioPago, List<Long> studentIds, RepositorioAlumno repositorioAlumno) {
        // Create refunded payments
        for (int i = 0; i < 3; i++) {
            try {
                Long studentId = studentIds.get(random.nextInt(studentIds.size()));
                
                Pago pago = createPayment(
                    generateRandomAmount(100.0, 300.0),
                    EMetodoPago.STRIPE,
                    EEstadoPago.REEMBOLSADO,
                    studentId,
                    generateRandomDate(-60, -30), // 30-60 days ago
                    repositorioAlumno
                );
                
                // Add items to the payment
                addRandomItems(pago);
                
                Pago savedPago = repositorioPago.save(pago);
                createdPayments.add(savedPago);
            } catch (Exception e) {
                System.err.println("✗ Error creating refunded payment: " + e.getMessage());
            }
        }
    }

    private void createManualPayments(RepositorioPago repositorioPago, List<Long> studentIds, RepositorioAlumno repositorioAlumno) {
        // Create manual cash payments
        for (int i = 0; i < 7; i++) {
            try {
                Long studentId = studentIds.get(random.nextInt(studentIds.size()));
                
                Pago pago = createPayment(
                    generateRandomAmount(20.0, 150.0),
                    EMetodoPago.EFECTIVO,
                    EEstadoPago.EXITO,
                    studentId,
                    generateRandomDate(-45, 0), // Last 45 days
                    repositorioAlumno
                );
                
                // Add items to the payment
                addRandomItems(pago);
                
                // Mark some as invoiced
                if (random.nextBoolean()) {
                    pago.setFacturaCreada(true);
                }
                
                Pago savedPago = repositorioPago.save(pago);
                createdPayments.add(savedPago);
            } catch (Exception e) {
                System.err.println("✗ Error creating manual payment: " + e.getMessage());
            }
        }
    }

    private Pago createPayment(BigDecimal amount, EMetodoPago method, EEstadoPago status, 
                              Long studentId, LocalDateTime date, RepositorioAlumno repositorioAlumno) {
        // Get the student entity
        Alumno alumno = repositorioAlumno.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Alumno no encontrado con ID: " + studentId));
        
        // Create payment with JPA relationship
        Pago pago = new Pago(amount, method, alumno);
        pago.setEstado(status);
        pago.setFechaPago(date);
        pago.setFacturaCreada(false);
        
        // Add Stripe fields for non-cash payments
        if (method != EMetodoPago.EFECTIVO) {
            pago.setStripePaymentIntentId("pi_" + generateRandomString(24));
            if (status == EEstadoPago.EXITO) {
                pago.setStripeChargeId("ch_" + generateRandomString(24));
            }
        }
        
        return pago;
    }

    private void addRandomItems(Pago pago) {
        String[] concepts = {
            "Matrícula anual", "Clase individual", "Taller de pintura", "Curso intensivo",
            "Material de arte", "Sesión de fotografía", "Clase de dibujo", "Workshop creativo",
            "Seminario avanzado", "Clase de escultura", "Curso online", "Sesión privada"
        };
        
        int numItems = random.nextInt(3) + 1; // 1-3 items
        
        for (int i = 0; i < numItems; i++) {
            String concept = concepts[random.nextInt(concepts.length)];
            BigDecimal unitPrice = generateRandomAmount(10.0, 100.0);
            int quantity = random.nextInt(3) + 1; // 1-3 quantity
            
            ItemPago item = new ItemPago(
                "item_" + pago.getId() + "_" + i,
                concept,
                unitPrice,
                quantity
            );
            
            pago.agregarItem(item);
        }
    }

    private BigDecimal generateRandomAmount(double min, double max) {
        double amount = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(Math.round(amount * 100.0) / 100.0);
    }

    private LocalDateTime generateRandomDate(int daysFrom, int daysTo) {
        int days = daysFrom + random.nextInt(daysTo - daysFrom + 1);
        return LocalDateTime.now().plusDays(days);
    }

    private String generateRandomFailureReason() {
        String[] reasons = {
            "Tarjeta rechazada por el banco",
            "Fondos insuficientes",
            "Tarjeta expirada",
            "Error de procesamiento",
            "Tarjeta bloqueada"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public List<Pago> getCreatedPayments() {
        return createdPayments;
    }
}