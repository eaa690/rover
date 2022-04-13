package org.eaa690.rover;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.rover.model.Picture;
import org.eaa690.rover.model.PictureRepository;
import org.eaa690.rover.model.Rover;
import org.eaa690.rover.model.RoverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class RoverController {

    /**
     * Root file storage location.
     */
    private final Path rootLocation;

    @Autowired
    private RoverRepository roverRepository;

    @Autowired
    private PictureRepository pictureRepository;

    /**
     * Constructor.
     *
     * @param props PictureProperties
     */
    @Autowired
    public RoverController(final PictureProperties props) {
        rootLocation = Paths.get(props.getRootLocation());
    }

    @GetMapping("/rovers/{id}")
    public Rover getRovers(@PathVariable("id") Long id) {
        return roverRepository
                .findAll()
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(rover -> rover.getId() == id)
                .findAny()
                .get();
    }

    @GetMapping("/pictures/{roverId}")
    public List<Picture> getPictures(@PathVariable("roverId") Long roverId) {
        return pictureRepository
                .findAll()
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(picture -> Objects.equals(picture.getRoverId(), roverId))
                .collect(Collectors.toList());
    }

    @GetMapping("/pictures/{roverId}/latest")
    public ResponseEntity<Resource> getLatestPicture(@PathVariable("roverId") Long roverId) {
        final Optional<Picture> pictureOpt = pictureRepository
                .findAll()
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(pic -> Objects.equals(pic.getRoverId(), roverId))
                .sorted(Comparator.comparing(Picture::getTimestamp).reversed())
                .limit(1)
                .findFirst();
        try {
            if (pictureOpt.isPresent()) {
                final Path teamLocation = rootLocation.resolve(roverId.toString());
                final Picture picture = pictureOpt.get();
                final Resource file = new UrlResource(teamLocation.resolve(picture.getFileName()).toUri());
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/pictures/{roverId}")
    void addPicture(@PathVariable("roverId") final Long roverId,
                    @RequestParam("file") final MultipartFile file,
                    final RedirectAttributes redirectAttributes) {
        final Picture picture = new Picture();
        picture.setRoverId(roverId);
        picture.setTimestamp(ZonedDateTime.now());

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
        pictureRepository.save(picture);
    }
}
