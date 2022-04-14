package org.eaa690.rover;

import org.eaa690.rover.model.Rover;
import org.eaa690.rover.model.RoverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Value("${welcome.message}")
    private String message;

    @Autowired
    private RoverRepository roverRepository;

    @GetMapping("/")
    public String main(final Model model) {
        model.addAttribute("message", message);
        model.addAttribute("rover", null);
        return "welcome";
    }

    @PostMapping("/rover")
    public String rover(@RequestBody final Rover rover, final Model model) {
        return "rover";
    }

    @PostMapping("/rover/authenticate")
    public String authRover(@RequestBody final Rover rover, final Model model) {
        roverRepository
                .findAll()
                .flatMap(rovers -> rovers
                    .stream()
                    .filter(r -> r.getPasscode().equals(rover.getPasscode()))
                    .findFirst())
                .ifPresent(value -> model.addAttribute("rover", value));
        return "rover";
    }


}
