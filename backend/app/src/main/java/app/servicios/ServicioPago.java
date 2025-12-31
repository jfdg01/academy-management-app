package app.servicios;

import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTOPago;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Pago;
import app.entidades.Alumno;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
import app.excepciones.PaymentNotFoundException;
import app.excepciones.PaymentProcessingException;
import app.repositorios.RepositorioPago;
import app.repositorios.RepositorioAlumno;
import app.util.SecurityUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServicioPago {
    
    private final RepositorioPago repositorioPago;
    private final RepositorioAlumno repositorioAlumno;
    private final SecurityUtils securityUtils;
    private final ServicioClase servicioClase; // Add enrollment service dependency
    
    public DTOPago crearPago(DTOPeticionCrearPago peticion) {
        // Security check: Students can only create payments for themselves
        if (securityUtils.hasRole("ALUMNO")) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!peticion.alumnoId().equals(currentUserId.toString())) {
                throw new RuntimeException("No tienes permisos para crear pagos para otros alumnos");
            }
        }
        
        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToCents(peticion.importe()))
                .setCurrency(peticion.currency().toLowerCase()) // Stripe expects lowercase
                .setDescription(peticion.description())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Get the student entity
            Alumno alumno = repositorioAlumno.findById(Long.valueOf(peticion.alumnoId()))
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con ID: " + peticion.alumnoId()));
            
            // Create Pago entity with JPA relationships
            Pago pago = new Pago(peticion.importe(), EMetodoPago.STRIPE, alumno);
            pago.setStripePaymentIntentId(paymentIntent.getId());
            pago.setFechaExpiracion(LocalDateTime.now().plusHours(24));
            
            // Set classId if provided for enrollment payments
            if (peticion.classId() != null) {
                pago.setClassId(peticion.classId());
            }
            
            Pago savedPago = repositorioPago.save(pago);
            
            // Create DTO with client_secret
            DTOPago dtoPago = new DTOPago(
                savedPago.getId(),
                savedPago.getFechaPago(),
                savedPago.getImporte(),
                savedPago.getMetodoPago(),
                savedPago.getEstado(),
                savedPago.getAlumno() != null ? savedPago.getAlumno().getId().toString() : null,
                savedPago.getFacturaCreada(),
                savedPago.getItems(),
                savedPago.getStripePaymentIntentId(),
                savedPago.getStripeChargeId(),
                savedPago.getFailureReason(),
                paymentIntent.getClientSecret(), // Only in response, never stored
                savedPago.getClassId() // Include classId in response
            );
            
            return dtoPago;
            
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error creating payment", e.getMessage());
        }
    }
    
    public DTOPago obtenerPagoPorId(Long id) {
        // Use Entity Graph to load items for better performance
        Pago pago = repositorioPago.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("id", id));
        
        return new DTOPago(pago);
    }
    
    public DTORespuestaPaginada<DTOPago> obtenerPagos(Pageable pageable) {
        Page<Pago> pagoPage = repositorioPago.findAll(pageable);
        return DTORespuestaPaginada.fromPage(
            pagoPage.map(DTOPago::new),
            pageable.getSort().toString(),
            "DESC"
        );
    }
    
    public void procesarEventoStripe(String eventType, String paymentIntentId, String chargeId, String failureReason) {
        // Use basic findByStripePaymentIntentId since Entity Graph method doesn't exist
        Pago pago = repositorioPago.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new PaymentNotFoundException("stripePaymentIntentId", paymentIntentId));
        
        switch (eventType) {
            case "payment_intent.succeeded":
                pago.setEstado(EEstadoPago.EXITO);
                pago.setStripeChargeId(chargeId);
                
                // Handle class enrollment if this was a class payment
                handleClassEnrollmentIfApplicable(pago);
                break;
            case "payment_intent.payment_failed":
                pago.setEstado(EEstadoPago.ERROR);
                pago.setFailureReason(failureReason);
                break;
            case "payment_intent.processing":
                pago.setEstado(EEstadoPago.PROCESANDO);
                break;
        }
        
        repositorioPago.save(pago);
    }
    
    /**
     * Handle class enrollment if the payment was for a class enrollment
     */
    private void handleClassEnrollmentIfApplicable(Pago pago) {
        try {
            // Check if this payment was for a class enrollment
            if (pago.getClassId() != null) {
                // Enroll the student in the class
                Long studentId = pago.getAlumno().getId();
                servicioClase.inscribirAlumnoEnClase(new app.dtos.DTOPeticionEnrollment(studentId, pago.getClassId()));
                
                log.info("Student {} enrolled in class {} after successful payment {}", 
                    studentId, pago.getClassId(), pago.getId());
            }
        } catch (Exception e) {
            log.error("Error handling class enrollment for payment {}: {}", pago.getId(), e.getMessage());
            // Don't fail the payment processing if enrollment fails
        }
    }
    
    private Long convertToCents(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).longValue();
    }
    
    /**
     * Check if a payment was successful
     * Useful for demo purposes
     * @param paymentId ID of the payment to check
     * @return true if payment was successful, false otherwise
     */
    public boolean isPaymentSuccessful(Long paymentId) {
        // Use Entity Graph to load items for better performance
        Pago pago = repositorioPago.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("id", paymentId));
        return EEstadoPago.EXITO.equals(pago.getEstado());
    }
    
    /**
     * Get recent payments for demo purposes
     * @param limit maximum number of payments to return
     * @return List of recent payments
     */
    public List<DTOPago> getRecentPayments(int limit) {
        return repositorioPago.findAllOrderedByFechaDesc()
            .stream()
            .limit(limit)
            .map(DTOPago::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent payments for the authenticated student
     * @param limit maximum number of payments to return
     * @return List of recent payments for the current student
     */
    public List<DTOPago> getMyRecentPayments(int limit) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return repositorioPago.findByAlumnoIdOrderByFechaPagoDesc(currentUserId)
            .stream()
            .limit(limit)
            .map(DTOPago::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a payment is owned by a specific user
     * Used for security authorization in REST endpoints
     * @param paymentId ID of the payment to check
     * @param userId ID of the user to check ownership against
     * @return true if the payment belongs to the user, false otherwise
     */
    public boolean isPaymentOwnedByUser(Long paymentId, Long userId) {
        try {
            Pago pago = repositorioPago.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("id", paymentId));
            return pago.getAlumno() != null && pago.getAlumno().getId().equals(userId);
        } catch (PaymentNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Get paginated payments for a specific student
     * @param alumnoId ID of the student
     * @param pageable pagination parameters
     * @return Paginated response with student's payments
     */
    public DTORespuestaPaginada<DTOPago> obtenerPagosPorAlumno(String alumnoId, Pageable pageable) {
        Page<Pago> pagoPage = repositorioPago.findByAlumnoIdOrderByFechaPagoDesc(Long.parseLong(alumnoId), pageable);
        return DTORespuestaPaginada.fromPage(
            pagoPage.map(DTOPago::new),
            pageable.getSort().toString(),
            "DESC"
        );
    }
    
    /**
     * Get all payments with enhanced details for admin view
     * @param pageable pagination parameters
     * @return Paginated response with all payments
     */
    public DTORespuestaPaginada<DTOPago> obtenerTodosLosPagos(Pageable pageable) {
        Page<Pago> pagoPage = repositorioPago.findAll(pageable);
        return DTORespuestaPaginada.fromPage(
            pagoPage.map(DTOPago::new),
            pageable.getSort().toString(),
            "DESC"
        );
    }
}
