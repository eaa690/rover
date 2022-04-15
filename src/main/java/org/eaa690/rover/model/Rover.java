package org.eaa690.rover.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
@Data
@NoArgsConstructor
public class Rover {

    /**
     * Rover ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * Rover name.
     *
     * Ex: black-rover, white-rover, orange-rover, etc
     */
    private String name;

    /**
     * Passcode of the rover.
     */
    private String passcode;

    /**
     * Command to be issued to rover, in Python.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private String command;

    private boolean active;

    public Rover(Long id, String name, String passcode, boolean active) {
        this.id = id;
        this.name = name;
        this.passcode = passcode;
        this.active = active;
    }
}
