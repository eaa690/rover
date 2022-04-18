package org.eaa690.rover;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
public class PicturesController {

    /**
     * Root file storage location.
     */
    private final Path rootLocation;

    /**
     * Constructor.
     *
     * @param props PictureProperties
     */
    @Autowired
    public PicturesController(final PictureProperties props) {
        rootLocation = Paths.get(props.getRootLocation());
    }

    @GetMapping("/pictures/{roverId}/latest")
    public ResponseEntity<Resource> getLatestPicture(@PathVariable("roverId") Long roverId) {
        log.info("GET /pictures/{}/latest called", roverId);
        final Resource file = getLatestImageAsResource(roverId);
        if (file != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Gets the latest image for a rover.
     *
     * @param roverId ID
     * @return image resource
     */
    public Resource getLatestImageAsResource(final Long roverId) {
        final Path teamLocation = rootLocation.resolve(roverId.toString());
        final TreeMap<Long, Resource> resourceTree = new TreeMap<>();
        loadAll(roverId).forEach(file -> {
            try {
                final Resource resource = new UrlResource(teamLocation.resolve(file.getFileName()).toUri());
                if (resource.exists() && resource.isFile()) {
                    log.info("Adding resource with lastModified: {} to TreeMap", resource.lastModified());
                    resourceTree.put(resource.lastModified(), resource);
                } else {
                    log.info("No file found for: {}", resource);
                }
            } catch (IOException e) {
                log.error("Could not read file: " + file);
            }
        });
        final Map.Entry<Long, Resource> entry = resourceTree.lastEntry();
        if (entry != null) {
            final Resource resource = entry.getValue();
            if (resource.exists() || resource.isReadable()) {
                log.info("Returning resource: {}", resource.getFilename());
                return resource;
            }
            log.error("Could not read file: " + resource);
        }
        log.warn("[getLatestImageAsResource] Returning null");
        return null;
    }

    /**
     * Loads all images for a rover.
     *
     * @param roverId ID
     * @return list of image paths
     */
    public Stream<Path> loadAll(final Long roverId) {
        final Path teamLocation = rootLocation.resolve(roverId.toString());
        log.info("Loading all files at path {}", teamLocation);
        try {
            return Files.walk(teamLocation, 1)
                    .filter(path -> !path.equals(teamLocation))
                    .map(teamLocation::relativize)
                    .peek(path -> log.info("Path found: {}", path));
        } catch (IOException e) {
            log.error("Failed to read stored files", e);
        }
        log.info("[loadAll] Returning null");
        return null;
    }

    @PostMapping("/pictures/{roverId}")
    void addPicture(@PathVariable("roverId") final Long roverId,
                    @RequestParam("file") final MultipartFile file,
                    final RedirectAttributes redirectAttributes) {
        log.info("POST /pictures/{} called", roverId);
        final Path teamLocation = rootLocation.resolve(roverId.toString());
        try {
            if (file.isEmpty()) {
                log.error("Failed to store empty file.");
            }
            final Path destinationFile = teamLocation
                    .resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize()
                    .toAbsolutePath();
            if (!destinationFile.getParent().equals(teamLocation.toAbsolutePath())) {
                // This is a security check
                log.error("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store file.", e);
        }
    }
}
