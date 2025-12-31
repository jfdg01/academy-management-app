package app.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.excepciones.ValidationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling file uploads with validation and secure storage
 */
@Slf4j
@Component
public class FileUploadUtils {

    // Allowed file types for exercise submissions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "png");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "application/pdf",
        "image/png"
    );
    
    // File size limits (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    
    // Base upload directory
    private static final String UPLOAD_BASE_DIR = "uploads";
    private static final String EXERCISE_DELIVERIES_DIR = "exercise-deliveries";
    
    /**
     * Result object for file upload operations
     */
    public static class FileUploadResult {
        private final String uniqueFilename;
        private final String originalFilename;
        private final String relativePath;
        
        public FileUploadResult(String uniqueFilename, String originalFilename, String relativePath) {
            this.uniqueFilename = uniqueFilename;
            this.originalFilename = originalFilename;
            this.relativePath = relativePath;
        }
        
        public String getUniqueFilename() { return uniqueFilename; }
        public String getOriginalFilename() { return originalFilename; }
        public String getRelativePath() { return relativePath; }
    }

    /**
     * Validates and saves an uploaded file for exercise delivery
     * 
     * @param file The uploaded file
     * @param studentId The ID of the student uploading the file
     * @param exerciseId The ID of the exercise
     * @return FileUploadResult with filename information and storage path
     * @throws ValidationException if file validation fails
     */
    public FileUploadResult saveExerciseDeliveryFile(MultipartFile file, Long studentId, Long exerciseId) {
        validateFile(file);
        
        try {
            // Create directory structure: uploads/exercise-deliveries/studentId/exerciseId/
            String relativePath = createFileDirectory(studentId, exerciseId);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(originalFilename, fileExtension);
            
            // Full path for saving
            Path fullPath = Paths.get(UPLOAD_BASE_DIR, EXERCISE_DELIVERIES_DIR, 
                studentId.toString(), exerciseId.toString(), uniqueFilename);
            
            // Ensure parent directories exist
            Files.createDirectories(fullPath.getParent());
            
            // Save the file
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path for database storage
            String relativeFilePath = Paths.get(EXERCISE_DELIVERIES_DIR, 
                studentId.toString(), exerciseId.toString(), uniqueFilename).toString();
            
            log.info("File saved successfully: {} for student {} exercise {}", 
                relativeFilePath, studentId, exerciseId);
            
            return new FileUploadResult(uniqueFilename, originalFilename, relativeFilePath);
            
        } catch (IOException e) {
            log.error("Error saving file for student {} exercise {}: {}", 
                studentId, exerciseId, e.getMessage());
            throw new ValidationException("Error al guardar el archivo: " + e.getMessage());
        }
    }
    
    /**
     * Validates an uploaded file
     * 
     * @param file The file to validate
     * @throws ValidationException if validation fails
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("El archivo no puede estar vacío");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("El archivo es demasiado grande. Tamaño máximo: 10MB");
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ValidationException("El nombre del archivo no puede estar vacío");
        }
        
        String fileExtension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new ValidationException("Tipo de archivo no permitido. Solo se permiten: " + 
                String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("Tipo de contenido no permitido. Solo se permiten: " + 
                String.join(", ", ALLOWED_MIME_TYPES));
        }
        
        // Additional validation for PNG files (check magic bytes)
        if ("png".equalsIgnoreCase(fileExtension)) {
            validatePngFile(file);
        }
        
        // Additional validation for PDF files (check magic bytes)
        if ("pdf".equalsIgnoreCase(fileExtension)) {
            validatePdfFile(file);
        }
    }
    
    /**
     * Validates PNG file by checking magic bytes
     */
    private void validatePngFile(MultipartFile file) {
        try {
            byte[] header = new byte[8];
            int bytesRead = file.getInputStream().read(header);
            
            if (bytesRead < 8 || 
                header[0] != (byte) 0x89 || 
                header[1] != (byte) 0x50 || 
                header[2] != (byte) 0x4E || 
                header[3] != (byte) 0x47 || 
                header[4] != (byte) 0x0D || 
                header[5] != (byte) 0x0A || 
                header[6] != (byte) 0x1A || 
                header[7] != (byte) 0x0A) {
                throw new ValidationException("El archivo no es un PNG válido");
            }
        } catch (IOException e) {
            throw new ValidationException("Error al validar el archivo PNG");
        }
    }
    
    /**
     * Validates PDF file by checking magic bytes
     */
    private void validatePdfFile(MultipartFile file) {
        try {
            byte[] header = new byte[4];
            int bytesRead = file.getInputStream().read(header);
            
            if (bytesRead < 4 || 
                header[0] != (byte) 0x25 || 
                header[1] != (byte) 0x50 || 
                header[2] != (byte) 0x44 || 
                header[3] != (byte) 0x46) {
                throw new ValidationException("El archivo no es un PDF válido");
            }
        } catch (IOException e) {
            throw new ValidationException("Error al validar el archivo PDF");
        }
    }
    
    /**
     * Creates the directory structure for file storage
     */
    private String createFileDirectory(Long studentId, Long exerciseId) {
        return Paths.get(EXERCISE_DELIVERIES_DIR, studentId.toString(), exerciseId.toString()).toString();
    }
    
    /**
     * Generates a unique filename with timestamp, UUID, and original filename
     */
    private String generateUniqueFilename(String originalFilename, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // Clean the original filename to make it safe for filesystem
        String safeOriginalName = sanitizeFilename(originalFilename);
        
        return String.format("delivery_%s_%s_%s.%s", timestamp, uuid, safeOriginalName, extension);
    }
    
    /**
     * Sanitizes a filename to make it safe for filesystem storage
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "";
        
        // Remove extension for processing
        String nameWithoutExt = filename;
        if (filename.contains(".")) {
            nameWithoutExt = filename.substring(0, filename.lastIndexOf("."));
        }
        
        // Replace problematic characters and limit length
        String sanitized = nameWithoutExt
            .replaceAll("[^a-zA-Z0-9\\s\\-_]", "_")  // Replace special chars with underscore
            .replaceAll("\\s+", "_")                  // Replace spaces with underscore
            .replaceAll("_+", "_")                    // Replace multiple underscores with single
            .toLowerCase();                           // Convert to lowercase
        
        // Limit length to prevent extremely long filenames
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        return sanitized;
    }
    
    /**
     * Extracts file extension from filename
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Gets the full path for a stored file
     */
    public Path getFullFilePath(String relativePath) {
        return Paths.get(UPLOAD_BASE_DIR, relativePath);
    }
    
    /**
     * Checks if a file exists
     */
    public boolean fileExists(String relativePath) {
        Path fullPath = getFullFilePath(relativePath);
        return Files.exists(fullPath);
    }
    
    /**
     * Deletes a file
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path fullPath = getFullFilePath(relativePath);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            log.error("Error deleting file {}: {}", relativePath, e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets file size in bytes
     */
    public long getFileSize(String relativePath) {
        try {
            Path fullPath = getFullFilePath(relativePath);
            return Files.size(fullPath);
        } catch (IOException e) {
            log.error("Error getting file size for {}: {}", relativePath, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Formats file size for display
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Renames a file in the file system
     */
    public String renombrarArchivo(String rutaOriginal, String nuevoNombre) {
        try {
            Path rutaOriginalCompleta = getFullFilePath(rutaOriginal);
            Path directorio = rutaOriginalCompleta.getParent();
            Path nuevaRutaCompleta = directorio.resolve(nuevoNombre);
            
            // Check if new file already exists
            if (Files.exists(nuevaRutaCompleta)) {
                log.error("File already exists: {}", nuevaRutaCompleta);
                return null;
            }
            
            // Rename the file
            Files.move(rutaOriginalCompleta, nuevaRutaCompleta);
            
            // Return the new relative path
            String nuevaRutaRelativa = Paths.get(EXERCISE_DELIVERIES_DIR, 
                directorio.getName(directorio.getNameCount() - 2).toString(),
                directorio.getName(directorio.getNameCount() - 1).toString(),
                nuevoNombre).toString();
            
            log.info("File renamed successfully: {} -> {}", rutaOriginal, nuevaRutaRelativa);
            return nuevaRutaRelativa;
            
        } catch (IOException e) {
            log.error("Error renaming file {} to {}: {}", rutaOriginal, nuevoNombre, e.getMessage());
            return null;
        }
    }
    
    /**
     * Saves a file for exercise delivery (alias for saveExerciseDeliveryFile)
     */
    public FileUploadResult guardarArchivoEntrega(Long studentId, Long exerciseId, MultipartFile file) {
        return saveExerciseDeliveryFile(file, studentId, exerciseId);
    }
    

    
    /**
     * Checks if a file is an image
     */
    public boolean isImageFile(String filename) {
        String extension = getFileExtension(filename);
        return "png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension) || 
               "gif".equals(extension) || "bmp".equals(extension) || "webp".equals(extension);
    }
    
    /**
     * Checks if a file is a PDF
     */
    public boolean isPdfFile(String filename) {
        String extension = getFileExtension(filename);
        return "pdf".equals(extension);
    }
    
    /**
     * Gets a human-readable file type label
     */
    public String getFileTypeLabel(String filename) {
        String extension = getFileExtension(filename);
        return switch (extension) {
            case "pdf" -> "PDF Document";
            case "png" -> "PNG Image";
            case "jpg", "jpeg" -> "JPEG Image";
            case "gif" -> "GIF Image";
            case "bmp" -> "BMP Image";
            case "webp" -> "WebP Image";
            case "doc", "docx" -> "Word Document";
            case "xls", "xlsx" -> "Excel Spreadsheet";
            case "ppt", "pptx" -> "PowerPoint Presentation";
            case "txt" -> "Text File";
            case "zip", "rar", "7z" -> "Compressed Archive";
            default -> "Unknown File Type";
        };
    }
}
