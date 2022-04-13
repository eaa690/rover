package org.eaa690.rover.model;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface PictureRepository extends Repository<Picture, Long> {

    Optional<List<Picture>> findAll();

    Picture save(Picture picture);
}
