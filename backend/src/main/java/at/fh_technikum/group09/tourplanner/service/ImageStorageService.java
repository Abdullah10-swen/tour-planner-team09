package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.service.exception.TourServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores and retrieves tour images on the local filesystem.
 * The database only keeps the relative path – no binary data in the DB.
 */
@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    private final Path uploadDir;

    public ImageStorageService(@Value("${tour.image.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Image upload directory: {}", this.uploadDir);
        } catch (IOException ex) {
            throw new TourServiceException("Could not create image upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Saves the uploaded file to the filesystem and returns the relative URL path
     * (e.g. {@code /api/images/tour-5-a1b2c3d4.jpg}) that is stored in the database.
     */
    public String store(long tourId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String filename = "tour-" + tourId + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path target = uploadDir.resolve(filename).normalize();

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored image: {}", target);
        } catch (IOException ex) {
            throw new TourServiceException("Failed to store image for tourId=" + tourId, ex);
        }
        return "/api/images/" + filename;
    }

    /** Returns the absolute path for a given filename so the controller can serve it. */
    public Path load(String filename) {
        return uploadDir.resolve(filename).normalize();
    }

    /** Deletes an existing image file silently (missing file is not an error). */
    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String filename = imageUrl.replaceFirst("^/api/images/", "");
        try {
            Path file = uploadDir.resolve(filename).normalize();
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("Deleted image: {}", file);
            }
        } catch (IOException ex) {
            log.warn("Could not delete image file '{}': {}", filename, ex.getMessage());
        }
    }
}
