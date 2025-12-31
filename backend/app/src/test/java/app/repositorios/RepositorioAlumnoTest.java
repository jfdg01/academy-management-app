package app.repositorios;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import app.entidades.Alumno;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para RepositorioAlumno")
class RepositorioAlumnoTest {

    @Mock
    private RepositorioAlumno repositorioAlumno;

    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;

    @BeforeEach
    void setUp() {
        alumno1 = new Alumno("alumno1", "password1", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "123456789");
        alumno1.setId(1L);
        alumno1.setEnrollDate(LocalDateTime.now().minusDays(30));
        alumno1.setEnrolled(true);

        alumno2 = new Alumno("alumno2", "password2", "María", "García", "87654321Y", "maria@ejemplo.com", "987654321");
        alumno2.setId(2L);
        alumno2.setEnrollDate(LocalDateTime.now().minusDays(15));
        alumno2.setEnrolled(false);

        alumno3 = new Alumno("alumno3", "password3", "Carlos", "López", "11223344X", "carlos@ejemplo.com", "555666777");
        alumno3.setId(3L);
        alumno3.setEnrollDate(LocalDateTime.now());
        alumno3.setEnrolled(true);
    }

    @Test
    @DisplayName("findByUsername debe retornar alumno cuando existe")
    void testFindByUsernameExiste() {
        when(repositorioAlumno.findByUsername("alumno1")).thenReturn(Optional.of(alumno1));

        Optional<Alumno> resultado = repositorioAlumno.findByUsername("alumno1");

        assertTrue(resultado.isPresent());
        assertEquals(alumno1, resultado.get());
        verify(repositorioAlumno).findByUsername("alumno1");
    }

    @Test
    @DisplayName("findByUsername debe retornar empty cuando no existe")
    void testFindByUsernameNoExiste() {
        when(repositorioAlumno.findByUsername("inexistente")).thenReturn(Optional.empty());

        Optional<Alumno> resultado = repositorioAlumno.findByUsername("inexistente");

        assertFalse(resultado.isPresent());
        verify(repositorioAlumno).findByUsername("inexistente");
    }

    @Test
    @DisplayName("findByEmail debe retornar alumno cuando existe")
    void testFindByEmailExiste() {
        when(repositorioAlumno.findByEmail("juan@ejemplo.com")).thenReturn(Optional.of(alumno1));

        Optional<Alumno> resultado = repositorioAlumno.findByEmail("juan@ejemplo.com");

        assertTrue(resultado.isPresent());
        assertEquals(alumno1, resultado.get());
        verify(repositorioAlumno).findByEmail("juan@ejemplo.com");
    }

    @Test
    @DisplayName("findByDni debe retornar alumno cuando existe")
    void testFindByDniExiste() {
        when(repositorioAlumno.findByDni("12345678Z")).thenReturn(Optional.of(alumno1));

        Optional<Alumno> resultado = repositorioAlumno.findByDni("12345678Z");

        assertTrue(resultado.isPresent());
        assertEquals(alumno1, resultado.get());
        verify(repositorioAlumno).findByDni("12345678Z");
    }

    @Test
    @DisplayName("existsByUsername debe retornar true cuando existe")
    void testExistsByUsernameExiste() {
        when(repositorioAlumno.existsByUsername("alumno1")).thenReturn(true);

        boolean resultado = repositorioAlumno.existsByUsername("alumno1");

        assertTrue(resultado);
        verify(repositorioAlumno).existsByUsername("alumno1");
    }

    @Test
    @DisplayName("existsByEmail debe retornar true cuando existe")
    void testExistsByEmailExiste() {
        when(repositorioAlumno.existsByEmail("juan@ejemplo.com")).thenReturn(true);

        boolean resultado = repositorioAlumno.existsByEmail("juan@ejemplo.com");

        assertTrue(resultado);
        verify(repositorioAlumno).existsByEmail("juan@ejemplo.com");
    }

    @Test
    @DisplayName("existsByDni debe retornar true cuando existe")
    void testExistsByDniExiste() {
        when(repositorioAlumno.existsByDni("12345678Z")).thenReturn(true);

        boolean resultado = repositorioAlumno.existsByDni("12345678Z");

        assertTrue(resultado);
        verify(repositorioAlumno).existsByDni("12345678Z");
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe retornar lista de alumnos")
    void testFindByNombreContainingIgnoreCase() {
        List<Alumno> alumnos = Arrays.asList(alumno1, alumno2);
        when(repositorioAlumno.findByNombreContainingIgnoreCase("Juan")).thenReturn(alumnos);

        List<Alumno> resultado = repositorioAlumno.findByNombreContainingIgnoreCase("Juan");

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(alumno1));
        assertTrue(resultado.contains(alumno2));
        verify(repositorioAlumno).findByNombreContainingIgnoreCase("Juan");
    }

    @Test
    @DisplayName("findByApellidosContainingIgnoreCase debe retornar lista de alumnos")
    void testFindByApellidosContainingIgnoreCase() {
        List<Alumno> alumnos = Arrays.asList(alumno1);
        when(repositorioAlumno.findByApellidosContainingIgnoreCase("Pérez")).thenReturn(alumnos);

        List<Alumno> resultado = repositorioAlumno.findByApellidosContainingIgnoreCase("Pérez");

        assertEquals(1, resultado.size());
        assertEquals(alumno1, resultado.get(0));
        verify(repositorioAlumno).findByApellidosContainingIgnoreCase("Pérez");
    }

    @Test
    @DisplayName("countByMatriculado debe retornar conteo correcto")
    void testCountByMatriculado() {
        when(repositorioAlumno.countByMatriculado(true)).thenReturn(2L);
        when(repositorioAlumno.countByMatriculado(false)).thenReturn(1L);

        long matriculados = repositorioAlumno.countByMatriculado(true);
        long noMatriculados = repositorioAlumno.countByMatriculado(false);

        assertEquals(2L, matriculados);
        assertEquals(1L, noMatriculados);
        verify(repositorioAlumno).countByMatriculado(true);
        verify(repositorioAlumno).countByMatriculado(false);
    }

    @Test
    @DisplayName("findAllPaged debe retornar página de alumnos")
    void testFindAllPaged() {
        List<Alumno> alumnos = Arrays.asList(alumno1, alumno2);
        Pageable pageable = PageRequest.of(0, 2);
        Page<Alumno> page = new PageImpl<>(alumnos, pageable, 3);
        when(repositorioAlumno.findAllPaged(pageable)).thenReturn(page);

        Page<Alumno> resultado = repositorioAlumno.findAllPaged(pageable);

        assertEquals(2, resultado.getContent().size());
        assertEquals(3, resultado.getTotalElements());
        assertEquals(0, resultado.getNumber());
        assertEquals(2, resultado.getSize());
        verify(repositorioAlumno).findAllPaged(pageable);
    }

    @Test
    @DisplayName("findByFiltrosPaged debe retornar página de alumnos filtrados")
    void testFindByFiltrosPaged() {
        List<Alumno> alumnosFiltrados = Arrays.asList(alumno1);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Alumno> page = new PageImpl<>(alumnosFiltrados, pageable, 1);
        when(repositorioAlumno.findByFiltrosPaged("Juan", null, null, null, null, pageable))
                .thenReturn(page);

        Page<Alumno> resultado = repositorioAlumno.findByFiltrosPaged("Juan", null, null, null, null, pageable);

        assertEquals(1, resultado.getContent().size());
        assertEquals(1, resultado.getTotalElements());
        assertEquals(alumno1, resultado.getContent().get(0));
        verify(repositorioAlumno).findByFiltrosPaged("Juan", null, null, null, null, pageable);
    }

    @Test
    @DisplayName("findByMatriculadoPaged debe retornar página de alumnos matriculados")
    void testFindByMatriculadoPaged() {
        List<Alumno> alumnosMatriculados = Arrays.asList(alumno1, alumno3);
        Pageable pageable = PageRequest.of(0, 2);
        Page<Alumno> page = new PageImpl<>(alumnosMatriculados, pageable, 2);
        when(repositorioAlumno.findByMatriculadoPaged(true, pageable)).thenReturn(page);

        Page<Alumno> resultado = repositorioAlumno.findByMatriculadoPaged(true, pageable);

        assertEquals(2, resultado.getContent().size());
        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().stream().allMatch(Alumno::isEnrolled));
        verify(repositorioAlumno).findByMatriculadoPaged(true, pageable);
    }

    @Test
    @DisplayName("save debe retornar alumno guardado")
    void testSave() {
        when(repositorioAlumno.save(alumno1)).thenReturn(alumno1);

        Alumno resultado = repositorioAlumno.save(alumno1);

        assertEquals(alumno1, resultado);
        verify(repositorioAlumno).save(alumno1);
    }

    @Test
    @DisplayName("findById debe retornar alumno cuando existe")
    void testFindByIdExiste() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));

        Optional<Alumno> resultado = repositorioAlumno.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(alumno1, resultado.get());
        verify(repositorioAlumno).findById(1L);
    }

    @Test
    @DisplayName("count debe retornar número total de alumnos")
    void testCount() {
        when(repositorioAlumno.count()).thenReturn(3L);

        long resultado = repositorioAlumno.count();

        assertEquals(3L, resultado);
        verify(repositorioAlumno).count();
    }
}
