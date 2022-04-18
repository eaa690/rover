package org.eaa690.rover.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
     * Script output.
     */
    private String scriptOutput;

    /**
     * Command to be issued to rover, in Python.
     */
    private String command;

    public Rover(Long id, String name, String passcode) {
        this.id = id;
        this.name = name;
        this.passcode = passcode;
    }
}
