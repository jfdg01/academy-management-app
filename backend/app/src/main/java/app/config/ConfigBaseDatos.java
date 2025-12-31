package app.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuración para la base de datos
 * Inicializa las extensiones y funciones necesarias para las búsquedas accent-insensitive
 */
@Configuration
@RequiredArgsConstructor
public class ConfigBaseDatos {

    private final DataSource dataSource;

    @PostConstruct
    public void initializeDatabase() {
        try {
            // Intentar crear la extensión unaccent
            try (var connection = dataSource.getConnection()) {
                try (var statement = connection.createStatement()) {
                    // Crear extensión unaccent si no existe
                    statement.execute("CREATE EXTENSION IF NOT EXISTS unaccent");
                    // System.out.println("✅ Extensión unaccent verificada/creada");
                    
                    // Crear función de normalización
                    String createFunction = """
                        CREATE OR REPLACE FUNCTION normalize_text(input_text TEXT)
                        RETURNS TEXT AS $$
                        BEGIN
                            IF input_text IS NULL THEN
                                RETURN NULL;
                            END IF;
                            
                            RETURN TRIM(LOWER(unaccent(input_text)));
                        END;
                        $$ LANGUAGE plpgsql IMMUTABLE;
                        """;
                    
                    statement.execute(createFunction);
                    // System.out.println("✅ Función normalize_text creada correctamente");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo inicializar unaccent: " + e.getMessage());
            System.out.println("🔄 Intentando crear función de normalización alternativa...");
            
            // Fallback sin unaccent
            try (var connection = dataSource.getConnection()) {
                try (var statement = connection.createStatement()) {
                    String createFallbackFunction = """
                        CREATE OR REPLACE FUNCTION normalize_text(input_text TEXT)
                        RETURNS TEXT AS $$
                        BEGIN
                            IF input_text IS NULL THEN
                                RETURN NULL;
                            END IF;
                            
                            -- Función básica de normalización sin unaccent
                            RETURN TRIM(LOWER(
                                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                REPLACE(REPLACE(REPLACE(REPLACE(
                                    input_text,
                                    'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'),
                                    'Á', 'a'), 'É', 'e'), 'Í', 'i'), 'Ó', 'o'), 'Ú', 'u'),
                                    'ñ', 'n'), 'Ñ', 'n'), 'ü', 'u'), 'Ü', 'u')
                            ));
                        END;
                        $$ LANGUAGE plpgsql IMMUTABLE;
                        """;
                    
                    statement.execute(createFallbackFunction);
                    System.out.println("✅ Función normalize_text alternativa creada correctamente");
                }
            } catch (Exception fallbackException) {
                System.out.println("❌ Error crítico: No se pudo crear ninguna función de normalización");
                System.out.println("Las búsquedas accent-insensitive no funcionarán correctamente");
            }
        }
    }
}
