package app.repositorios;

import app.entidades.Pago;
import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pago
 * Proporciona operaciones de acceso a datos para pagos
 */
@Repository
public interface RepositorioPago extends JpaRepository<Pago, Long> {
    
    /**
     * Busca pagos por metodo de pago
     * @param metodoPago metodo de pago
     * @return Lista de pagos
     */
    @Query("SELECT p FROM Pago p WHERE p.metodoPago = :metodoPago")
    List<Pago> findByMetodoPago(@Param("metodoPago") EMetodoPago metodoPago);
    
    /**
     * Busca pagos por estado
     * @param estado Estado del pago
     * @return Lista de pagos
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = :estado")
    List<Pago> findByEstado(@Param("estado") EEstadoPago estado);
    
    /**
     * Busca pagos por rango de fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de pagos
     */
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    List<Pago> findByFechaPagoBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca pagos por rango de importe
     * @param importeMin Importe mínimo
     * @param importeMax Importe máximo
     * @return Lista de pagos
     */
    @Query("SELECT p FROM Pago p WHERE p.importe BETWEEN :importeMin AND :importeMax")
    List<Pago> findByImporteBetween(@Param("importeMin") BigDecimal importeMin, @Param("importeMax") BigDecimal importeMax);
    
    /**
     * Busca pagos que tienen factura creada
     * @param facturaCreada true para buscar con factura, false para sin factura
     * @return Lista de pagos
     */
    @Query("SELECT p FROM Pago p WHERE p.facturaCreada = :facturaCreada")
    List<Pago> findByFacturaCreada(@Param("facturaCreada") Boolean facturaCreada);
    
    /**
     * Busca un pago por su Stripe Payment Intent ID
     * @param stripePaymentIntentId ID del payment intent de Stripe
     * @return Optional del pago encontrado
     */
    @Query("SELECT p FROM Pago p WHERE p.stripePaymentIntentId = :stripePaymentIntentId")
    Optional<Pago> findByStripePaymentIntentId(@Param("stripePaymentIntentId") String stripePaymentIntentId);
    
    /**
     * Obtiene todos los pagos ordenados por ID
     * @return Lista de pagos ordenada
     */
    @Query("SELECT p FROM Pago p ORDER BY p.id")
    List<Pago> findAllOrderedById();
    
    /**
     * Obtiene todos los pagos ordenados por importe descendente
     * @return Lista de pagos ordenada por importe
     */
    @Query("SELECT p FROM Pago p ORDER BY p.importe DESC")
    List<Pago> findAllOrderedByImporteDesc();
    
    /**
     * Busca pagos exitosos de un alumno
     * @param alumnoId ID del alumno
     * @return Lista de pagos exitosos
     */
    @Query("SELECT p FROM Pago p WHERE p.alumno.id = :alumnoId AND p.estado = 'EXITO'")
    List<Pago> findPagosExitososByAlumnoId(@Param("alumnoId") Long alumnoId);
    
    /**
     * Busca pagos que pueden ser facturados (exitosos y sin factura)
     * @return Lista de pagos facturables
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'EXITO' AND p.facturaCreada = false")
    List<Pago> findPagosFacturables();
    
    /**
     * Busca pagos manuales (efectivo) de un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de pagos manuales
     */
    @Query("SELECT p FROM Pago p WHERE p.metodoPago = 'EFECTIVO' AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    List<Pago> findPagosManualesByPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                         @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Calcula el total de ingresos de un alumno
     * @param alumnoId ID del alumno
     * @return Suma total de pagos exitosos
     */
    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM Pago p WHERE p.alumno.id = :alumnoId AND p.estado = 'EXITO'")
    BigDecimal calcularTotalIngresosAlumno(@Param("alumnoId") Long alumnoId);
    
    /**
     * Obtiene pagos paginados con items cargados usando EntityGraph
     * @param pageable configuración de paginación
     * @return Página de pagos con items cargados
     */
    @EntityGraph(value = "Pago.withItems")
    Page<Pago> findAll(Pageable pageable);
    
    /**
     * Busca un pago por ID con items cargados usando EntityGraph
     * @param id ID del pago
     * @return Optional del pago con items cargados
     */
    @EntityGraph(value = "Pago.withItems")
    Optional<Pago> findById(Long id);
    
    /**
     * Busca pagos por alumno con items cargados usando EntityGraph
     * @param alumnoId ID del alumno
     * @return Lista de pagos con items cargados
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.alumno.id = :alumnoId")
    List<Pago> findByAlumnoId(@Param("alumnoId") Long alumnoId);
    
    /**
     * Obtiene todos los pagos ordenados por fecha descendente con items cargados
     * @return Lista de pagos ordenada por fecha con items cargados
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p ORDER BY p.fechaPago DESC")
    List<Pago> findAllOrderedByFechaDesc();
    
    /**
     * Busca pagos de un alumno ordenados por fecha de pago descendente
     * @param alumnoId ID del alumno
     * @return Lista de pagos del alumno ordenada por fecha descendente
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.alumno.id = :alumnoId ORDER BY p.fechaPago DESC")
    List<Pago> findByAlumnoIdOrderByFechaPagoDesc(@Param("alumnoId") Long alumnoId);
    
    /**
     * Busca pagos de un alumno ordenados por fecha de pago descendente con paginación
     * @param alumnoId ID del alumno
     * @param pageable Parámetros de paginación
     * @return Página de pagos del alumno ordenada por fecha descendente
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.alumno.id = :alumnoId ORDER BY p.fechaPago DESC")
    Page<Pago> findByAlumnoIdOrderByFechaPagoDesc(@Param("alumnoId") Long alumnoId, Pageable pageable);

    /**
     * Busca pagos por clase con items cargados usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de pagos con items cargados
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.clase.id = :claseId")
    List<Pago> findByClaseId(@Param("claseId") Long claseId);

    /**
     * Busca pagos por clase ordenados por fecha de pago descendente
     * @param claseId ID de la clase
     * @return Lista de pagos de la clase ordenada por fecha descendente
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.clase.id = :claseId ORDER BY p.fechaPago DESC")
    List<Pago> findByClaseIdOrderByFechaPagoDesc(@Param("claseId") Long claseId);

    /**
     * Busca pagos por clase ordenados por fecha de pago descendente con paginación
     * @param claseId ID de la clase
     * @param pageable Parámetros de paginación
     * @return Página de pagos de la clase ordenada por fecha descendente
     */
    @EntityGraph(value = "Pago.withItems")
    @Query("SELECT p FROM Pago p WHERE p.clase.id = :claseId ORDER BY p.fechaPago DESC")
    Page<Pago> findByClaseIdOrderByFechaPagoDesc(@Param("claseId") Long claseId, Pageable pageable);

    /**
     * Busca un pago por ID con todas sus relaciones cargadas
     * @param pagoId ID del pago
     * @return Optional<Pago> con relaciones cargadas
     */
    @EntityGraph(value = "Pago.withItemsAndAlumno")
    @Query("SELECT p FROM Pago p WHERE p.id = :pagoId")
    Optional<Pago> findByIdWithRelationships(@Param("pagoId") Long pagoId);

    /**
     * Calcula el total de ingresos de una clase
     * @param claseId ID de la clase
     * @return Suma total de pagos exitosos
     */
    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM Pago p WHERE p.clase.id = :claseId AND p.estado = 'EXITO'")
    BigDecimal calcularTotalIngresosClase(@Param("claseId") Long claseId);

    /**
     * Cuenta pagos por alumno
     * @param alumnoId ID del alumno
     * @return Número de pagos
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.alumno.id = :alumnoId")
    Long countByAlumnoId(@Param("alumnoId") Long alumnoId);

    /**
     * Cuenta pagos por clase
     * @param claseId ID de la clase
     * @return Número de pagos
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.clase.id = :claseId")
    Long countByClaseId(@Param("claseId") Long claseId);
}
