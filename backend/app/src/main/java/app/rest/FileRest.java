package app.rest;

import app.dtos.DTOEntregaEjercicio;
import app.entidades.EntregaEjercicio;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioEntregaEjercicio;
import app.servicios.ServicioEntregaEjercicio;
import app.util.FileUploadUtils;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * REST controller for file viewing and download operations
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "File Operations", description = "API for file viewing and download operations")
@Slf4j
public class FileRest extends BaseRestController {

    private final FileUploadUtils fileUploadUtils;
    private final ServicioEntregaEjercicio servicioEntregaEjercicio;
    private final RepositorioEntregaEjercicio repositorioEntregaEjercicio;
    private final SecurityUtils securityUtils;

    /**
     * View a file from a delivery (for inline viewing in browser)
     */
    @GetMapping("/view")
    @Operation(
        summary = "View file from delivery",
        description = "Serves a file for inline viewing in the browser. Supports PNG images and PDF documents."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File served successfully for viewing"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file path or delivery ID"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this file"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "File or delivery not found"
        )
    })
    public ResponseEntity<Resource> viewFile(
            @Parameter(description = "File path relative to uploads directory", required = true)
            @RequestParam("path") @NotBlank(message = "File path is required") String filePath,
            @Parameter(description = "Delivery ID for access control", required = true)
            @RequestParam("deliveryId") Long deliveryId) {
        
        try {
            // Validate and get delivery for access control
            DTOEntregaEjercicio delivery = servicioEntregaEjercicio.obtenerEntregaPorId(deliveryId);
            
            // Check if the file belongs to this delivery
            if (!delivery.archivosEntregados().contains(filePath)) {
                log.warn("File {} does not belong to delivery {}", filePath, deliveryId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get the file resource
            Path fullPath = fileUploadUtils.getFullFilePath(filePath);
            Resource resource = new UrlResource(fullPath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type based on file extension
            String contentType = determineContentType(filePath);
            
            // Set appropriate headers for inline viewing with iframe support
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + 
                URLEncoder.encode(getFileName(filePath), StandardCharsets.UTF_8) + "\"");
            
            // Add headers for iframe compatibility
            headers.set("X-Frame-Options", "SAMEORIGIN");
            headers.set("X-Content-Type-Options", "nosniff");
            
            log.info("Serving file for viewing: {} from delivery {}", filePath, deliveryId);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (EntidadNoEncontradaException e) {
            log.warn("Delivery not found: {}", deliveryId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error serving file {} from delivery {}: {}", filePath, deliveryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a file from a delivery
     */
    @GetMapping("/download")
    @Operation(
        summary = "Download file from delivery",
        description = "Serves a file for download. Works with all file types."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File served successfully for download"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file path or delivery ID"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to download this file"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "File or delivery not found"
        )
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File path relative to uploads directory", required = true)
            @RequestParam("path") @NotBlank(message = "File path is required") String filePath,
            @Parameter(description = "Delivery ID for access control", required = true)
            @RequestParam("deliveryId") Long deliveryId) {
        
        try {
            // Validate and get delivery for access control
            DTOEntregaEjercicio delivery = servicioEntregaEjercicio.obtenerEntregaPorId(deliveryId);
            
            // Check if the file belongs to this delivery
            if (!delivery.archivosEntregados().contains(filePath)) {
                log.warn("File {} does not belong to delivery {}", filePath, deliveryId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get the file resource
            Path fullPath = fileUploadUtils.getFullFilePath(filePath);
            Resource resource = new UrlResource(fullPath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type based on file extension
            String contentType = determineContentType(filePath);
            
            // Set appropriate headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                URLEncoder.encode(getFileName(filePath), StandardCharsets.UTF_8) + "\"");
            
            log.info("Serving file for download: {} from delivery {}", filePath, deliveryId);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (EntidadNoEncontradaException e) {
            log.warn("Delivery not found: {}", deliveryId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error serving file {} from delivery {}: {}", filePath, deliveryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get file information for a delivery
     */
    @GetMapping("/info/{deliveryId}")
    @Operation(
        summary = "Get file information for a delivery",
        description = "Returns information about all files in a delivery including file types and sizes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File information retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this delivery"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        )
    })
    public ResponseEntity<List<FileInfo>> getFileInfo(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable Long deliveryId) {
        
        try {
            // Get delivery with access control
            DTOEntregaEjercicio delivery = servicioEntregaEjercicio.obtenerEntregaPorId(deliveryId);
            
            // Build file information for each file
            List<FileInfo> fileInfos = delivery.archivosEntregados().stream()
                .map(filePath -> {
                    String fileName = getFileName(filePath);
                    String originalFileName = extractOriginalFileName(fileName);
                    String extension = getFileExtension(fileName);
                    String contentType = determineContentType(filePath);
                    long fileSize = fileUploadUtils.getFileSize(filePath);
                    String formattedSize = fileUploadUtils.formatFileSize(fileSize);
                    
                    return new FileInfo(
                        fileName,
                        originalFileName,
                        filePath,
                        extension,
                        contentType,
                        fileSize,
                        formattedSize,
                        isImageFile(extension),
                        isPdfFile(extension)
                    );
                })
                .toList();
            
            return ResponseEntity.ok(fileInfos);
            
        } catch (EntidadNoEncontradaException e) {
            log.warn("Delivery not found: {}", deliveryId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting file info for delivery {}: {}", deliveryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determines the content type based on file extension
     */
    private String determineContentType(String filePath) {
        String extension = getFileExtension(getFileName(filePath)).toLowerCase();
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    /**
     * Extracts the file name from a file path
     */
    private String getFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Extracts the file extension from a file name
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Extracts the original file name from a generated file name.
     * Expected format: delivery_YYYYMMDD_HHMMSS_uuid_originalname.extension
     */
    private String extractOriginalFileName(String fileName) {
        if (fileName == null || !fileName.startsWith("delivery_")) {
            return fileName; // Return as-is if not in expected format
        }
        
        try {
            // Split by underscore to get parts
            String[] parts = fileName.split("_");
            
            // Expected format: delivery_YYYYMMDD_HHMMSS_uuid_originalname.extension
            if (parts.length >= 4) {
                // Get the part after the UUID (parts[3]) and before the extension
                String partWithExtension = parts[3];
                
                // If there are more parts, concatenate them (in case original filename had underscores)
                if (parts.length > 4) {
                    StringBuilder originalName = new StringBuilder(parts[3]);
                    for (int i = 4; i < parts.length - 1; i++) {
                        originalName.append("_").append(parts[i]);
                    }
                    partWithExtension = originalName.toString();
                }
                
                // Remove extension if present
                if (partWithExtension.contains(".")) {
                    return partWithExtension.substring(0, partWithExtension.lastIndexOf("."));
                }
                
                return partWithExtension;
            }
        } catch (Exception e) {
            // If parsing fails, return the original filename
            log.warn("Failed to parse original filename from: {}", fileName);
        }
        
        return fileName; // Fallback to original filename
    }

    /**
     * Checks if a file is an image based on its extension
     */
    private boolean isImageFile(String extension) {
        String ext = extension.toLowerCase();
        return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif");
    }

    /**
     * Checks if a file is a PDF based on its extension
     */
    private boolean isPdfFile(String extension) {
        return extension.toLowerCase().equals("pdf");
    }

    /**
     * DTO for file information
     */
    public record FileInfo(
        String fileName,
        String originalFileName,
        String filePath,
        String extension,
        String contentType,
        long fileSize,
        String formattedSize,
        boolean isImage,
        boolean isPdf
    ) {}
}
