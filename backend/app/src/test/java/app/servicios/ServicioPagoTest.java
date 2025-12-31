package app.servicios;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioPagoTest {

    @Mock
    private RepositorioPago repositorioPago;
    
    @Mock
    private RepositorioAlumno repositorioAlumno;
    
    @Mock
    private SecurityUtils securityUtils;
    
    @InjectMocks
    private ServicioPago servicioPago;
    
    private DTOPeticionCrearPago peticionValida;
    private Pago pagoGuardado;
    private Alumno alumno;
    
    @BeforeEach
    void setUp() {
        // Create a mock Alumno
        alumno = new Alumno();
        alumno.setId(123L);
        alumno.setUsername("teststudent");
        alumno.setFirstName("Test");
        alumno.setLastName("Student");
        
        peticionValida = new DTOPeticionCrearPago(
            new BigDecimal("50.00"),
            "123",
            "Test payment",
            "EUR",
            null // TODO: classId - optional for enrollment payments
        );
        
        pagoGuardado = new Pago();
        pagoGuardado.setId(1L);
        pagoGuardado.setImporte(new BigDecimal("50.00"));
        pagoGuardado.setMetodoPago(EMetodoPago.STRIPE);
        pagoGuardado.setEstado(EEstadoPago.PENDIENTE);
        pagoGuardado.setAlumno(alumno);
        pagoGuardado.setStripePaymentIntentId("pi_test_123");
        pagoGuardado.setFechaPago(LocalDateTime.now());
    }
    
    @Test
    void crearPago_DeberiaCrearPagoExitoso() throws StripeException {
        // Given
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_123_secret_abc");
        when(repositorioAlumno.findById(123L)).thenReturn(Optional.of(alumno));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // Use MockedStatic to mock the static PaymentIntent.create method
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(mockPaymentIntent);
            
            // When
            DTOPago resultado = servicioPago.crearPago(peticionValida);
            
            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.importe()).isEqualTo(new BigDecimal("50.00"));
            assertThat(resultado.metodoPago()).isEqualTo(EMetodoPago.STRIPE);
            assertThat(resultado.estado()).isEqualTo(EEstadoPago.PENDIENTE);
            assertThat(resultado.clientSecret()).isEqualTo("pi_test_123_secret_abc");
            assertThat(resultado.stripePaymentIntentId()).isEqualTo("pi_test_123");
            
            verify(repositorioPago).save(argThat(pago -> 
                pago.getImporte().equals(new BigDecimal("50.00")) &&
                pago.getMetodoPago() == EMetodoPago.STRIPE &&
                pago.getEstado() == EEstadoPago.PENDIENTE &&
                pago.getAlumno().getId().equals(123L) &&
                pago.getStripePaymentIntentId().equals("pi_test_123")
            ));
        }
    }
    
    @Test
    void crearPago_DeberiaLanzarExcepcionCuandoStripeFalla() throws StripeException {
        // Given
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenThrow(new RuntimeException("Stripe API error"));
            
            // When & Then
            assertThatThrownBy(() -> servicioPago.crearPago(peticionValida))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stripe API error");
        }
    }
    
    @Test
    void crearPago_DeberiaConvertirImporteACentavos() throws StripeException {
        // Given
        DTOPeticionCrearPago peticionConDecimales = new DTOPeticionCrearPago(
            new BigDecimal("99.99"),
            "123",
            "Test payment with decimals",
            "EUR",
            null // TODO: classId - optional for enrollment payments
        );
        
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_123_secret_abc");
        when(repositorioAlumno.findById(123L)).thenReturn(Optional.of(alumno));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenAnswer(invocation -> {
                    PaymentIntentCreateParams params = invocation.getArgument(0);
                    // Verify that amount is converted to cents (9999 for 99.99)
                    assertThat(params.getAmount()).isEqualTo(9999L);
                    return mockPaymentIntent;
                });
            
            // When
            servicioPago.crearPago(peticionConDecimales);
            
            // Then - verification is done in the mock answer above
        }
    }
    
    @Test
    void crearPago_DeberiaUsarMonedaEnMinusculas() throws StripeException {
        // Given
        DTOPeticionCrearPago peticionConMonedaMayuscula = new DTOPeticionCrearPago(
            new BigDecimal("50.00"),
            "123",
            "Test payment",
            "EUR",
            null // TODO: classId - optional for enrollment payments
        );
        
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_123_secret_abc");
        when(repositorioAlumno.findById(123L)).thenReturn(Optional.of(alumno));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenAnswer(invocation -> {
                    PaymentIntentCreateParams params = invocation.getArgument(0);
                    // Verify that currency is lowercase
                    assertThat(params.getCurrency()).isEqualTo("eur");
                    return mockPaymentIntent;
                });
            
            // When
            servicioPago.crearPago(peticionConMonedaMayuscula);
            
            // Then - verification is done in the mock answer above
        }
    }
    
    @Test
    void obtenerPagoPorId_DeberiaRetornarPago() {
        // Given
        when(repositorioPago.findById(1L)).thenReturn(Optional.of(pagoGuardado));
        
        // When
        DTOPago resultado = servicioPago.obtenerPagoPorId(1L);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.importe()).isEqualTo(new BigDecimal("50.00"));
        assertThat(resultado.metodoPago()).isEqualTo(EMetodoPago.STRIPE);
        assertThat(resultado.estado()).isEqualTo(EEstadoPago.PENDIENTE);
    }
    
    @Test
    void obtenerPagoPorId_DeberiaLanzarExcepcionSiNoExiste() {
        // Given
        when(repositorioPago.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> servicioPago.obtenerPagoPorId(999L))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining("Pago no encontrado con id: 999");
    }
    
    @Test
    void obtenerPagos_DeberiaRetornarPaginacion() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pago> pagoPage = new PageImpl<>(List.of(pagoGuardado), pageable, 1);
        when(repositorioPago.findAll(pageable)).thenReturn(pagoPage);
        
        // When
        DTORespuestaPaginada<DTOPago> resultado = servicioPago.obtenerPagos(pageable);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.content().get(0).id()).isEqualTo(1L);
        assertThat(resultado.content().get(0).importe()).isEqualTo(new BigDecimal("50.00"));
    }
    
    @Test
    void procesarEventoStripe_DeberiaActualizarEstadoExitoso() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("payment_intent.succeeded", "pi_test_123", "ch_test_123", null);
        
        // Then
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.EXITO &&
            "ch_test_123".equals(pago.getStripeChargeId())
        ));
    }
    
    @Test
    void procesarEventoStripe_DeberiaActualizarEstadoError() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("payment_intent.payment_failed", "pi_test_123", null, "Card declined");
        
        // Then
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.ERROR &&
            "Card declined".equals(pago.getFailureReason())
        ));
    }
    
    @Test
    void procesarEventoStripe_DeberiaActualizarEstadoProcesando() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("payment_intent.processing", "pi_test_123", null, null);
        
        // Then
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.PROCESANDO
        ));
    }
    
    @Test
    void procesarEventoStripe_DeberiaLanzarExcepcionSiPagoNoExiste() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_inexistente"))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> servicioPago.procesarEventoStripe("payment_intent.succeeded", "pi_inexistente", "ch_test_123", null))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining("Pago no encontrado con stripePaymentIntentId: pi_inexistente");
    }
    
    @Test
    void procesarEventoStripe_DeberiaIgnorarEventosDesconocidos() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("unknown_event", "pi_test_123", null, null);
        
        // Then - should save without changing state
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.PENDIENTE // Original state unchanged
        ));
    }
    
    @Test
    void isPaymentSuccessful_DeberiaRetornarTrueParaPagoExitoso() {
        // Given
        Pago pagoExitoso = new Pago();
        pagoExitoso.setId(1L);
        pagoExitoso.setEstado(EEstadoPago.EXITO);
        
        when(repositorioPago.findById(1L)).thenReturn(Optional.of(pagoExitoso));
        
        // When
        boolean resultado = servicioPago.isPaymentSuccessful(1L);
        
        // Then
        assertThat(resultado).isTrue();
    }
    
    @Test
    void isPaymentSuccessful_DeberiaRetornarFalseParaPagoFallido() {
        // Given
        Pago pagoFallido = new Pago();
        pagoFallido.setId(2L);
        pagoFallido.setEstado(EEstadoPago.ERROR);
        
        when(repositorioPago.findById(2L)).thenReturn(Optional.of(pagoFallido));
        
        // When
        boolean resultado = servicioPago.isPaymentSuccessful(2L);
        
        // Then
        assertThat(resultado).isFalse();
    }
    
    @Test
    void isPaymentSuccessful_DeberiaRetornarFalseParaPagoPendiente() {
        // Given
        Pago pagoPendiente = new Pago();
        pagoPendiente.setId(3L);
        pagoPendiente.setEstado(EEstadoPago.PENDIENTE);
        
        when(repositorioPago.findById(3L)).thenReturn(Optional.of(pagoPendiente));
        
        // When
        boolean resultado = servicioPago.isPaymentSuccessful(3L);
        
        // Then
        assertThat(resultado).isFalse();
    }
    
    @Test
    void isPaymentSuccessful_DeberiaLanzarExcepcionSiPagoNoExiste() {
        // Given
        when(repositorioPago.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> servicioPago.isPaymentSuccessful(999L))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining("Pago no encontrado con id: 999");
    }
    
    @Test
    void getRecentPayments_DeberiaRetornarListaLimitada() {
        // Given
        Pago pago1 = new Pago();
        pago1.setId(1L);
        pago1.setImporte(new BigDecimal("50.00"));
        pago1.setEstado(EEstadoPago.EXITO);
        
        Pago pago2 = new Pago();
        pago2.setId(2L);
        pago2.setImporte(new BigDecimal("75.00"));
        pago2.setEstado(EEstadoPago.EXITO);
        
        List<Pago> pagos = List.of(pago1, pago2);
        when(repositorioPago.findAllOrderedByFechaDesc()).thenReturn(pagos);
        
        // When
        List<DTOPago> resultado = servicioPago.getRecentPayments(2);
        
        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(1).id()).isEqualTo(2L);
    }
    
    @Test
    void getRecentPayments_DeberiaRespetarLimite() {
        // Given
        Pago pago1 = new Pago();
        pago1.setId(1L);
        pago1.setEstado(EEstadoPago.EXITO);
        
        Pago pago2 = new Pago();
        pago2.setId(2L);
        pago2.setEstado(EEstadoPago.EXITO);
        
        Pago pago3 = new Pago();
        pago3.setId(3L);
        pago3.setEstado(EEstadoPago.EXITO);
        
        List<Pago> pagos = List.of(pago1, pago2, pago3);
        when(repositorioPago.findAllOrderedByFechaDesc()).thenReturn(pagos);
        
        // When
        List<DTOPago> resultado = servicioPago.getRecentPayments(2);
        
        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(1).id()).isEqualTo(2L);
    }
    
    @Test
    void getRecentPayments_DeberiaRetornarListaVaciaSiNoHayPagos() {
        // Given
        when(repositorioPago.findAllOrderedByFechaDesc()).thenReturn(List.of());
        
        // When
        List<DTOPago> resultado = servicioPago.getRecentPayments(5);
        
        // Then
        assertThat(resultado).isEmpty();
    }
}



