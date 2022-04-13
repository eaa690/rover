package org.eaa690.rover.model;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface RoverRepository extends Repository<Rover, Long> {

    Optional<List<Rover>> findAll();

    Rover save(Rover rover);
}
