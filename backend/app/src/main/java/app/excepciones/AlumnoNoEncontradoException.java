package app.excepciones;

import org.springframework.http.HttpStatus;

public class AlumnoNoEncontradoException extends ApiException {
    public AlumnoNoEncontradoException(String message) {
        super(message, HttpStatus.NOT_FOUND, "ALUMNO_NOT_FOUND", message);
    }

    public AlumnoNoEncontradoException(String alumnoId, String message) {
        super(String.format("Alumno no encontrado con ID: %s", alumnoId), 
              HttpStatus.NOT_FOUND, 
              "ALUMNO_NOT_FOUND", 
              message);
    }
}
