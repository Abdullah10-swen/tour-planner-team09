package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.service.ImageStorageService;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@CrossOrigin
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageStorageService imageStorageService;
    private final TourService tourService;

    public ImageController(ImageStorageService imageStorageService, TourService tourService) {
        this.imageStorageService = imageStorageService;
        this.tourService = tourService;
    }

    /**
     * Accepts an image upload for a tour, saves it on the filesystem,
     * and updates the tour's imageUrl in the database.
     */
    @PostMapping("/api/tours/{id}/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable long id,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String oldUrl = tourService.getTourById(id).getImageUrl();
        if (oldUrl != null) {
            imageStorageService.deleteByUrl(oldUrl);
        }

        String imageUrl = imageStorageService.store(id, file);
        tourService.updateImageUrl(id, imageUrl);
        log.info("Image uploaded for tourId={}: {}", id, imageUrl);

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    /** Serves a stored image file by filename. */
    @GetMapping("/api/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Path filePath = imageStorageService.load(filename);

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            return ResponseEntity.notFound().build();
        }

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
