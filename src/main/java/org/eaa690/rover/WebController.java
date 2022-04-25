package org.eaa690.rover;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.rover.model.Rover;
import org.eaa690.rover.model.RoverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class WebController {

    @Value("${welcome.message}")
    private String message;

    @Autowired
    private RoverRepository roverRepository;

    @GetMapping("/")
    public String main(final Model model) {
        log.info("GET / called");
        model.addAttribute("message", message);
        model.addAttribute("rover", new Rover());
        roverRepository.findAll().ifPresent(rovers -> model.addAttribute("rovers", rovers));
        log.info("Returning \"welcome\"");
        return "welcome";
    }

    @GetMapping("/rover/{id}")
    public String getRover(@PathVariable("id") final Long id,
                           final Model model) {
        log.debug("GET /rover/" + id);
        final Optional<List<Rover>> roversOpt = roverRepository.findAll();
        if (roversOpt.isPresent()) {
            final List<Rover> rovers = roversOpt.get();
            final Optional<Rover> roverOpt = rovers.stream().filter(r -> r.getId() == id).findFirst();
            roverOpt.ifPresent(rover -> model.addAttribute("rover", rover));
        }
        return "output";
    }

    @PostMapping("/rover")
    public String rover(@ModelAttribute("rover") final Rover rover, final Model model) {
        log.info("POST /rover called with rover: {}", rover);
        StringBuilder sb = new StringBuilder();
        sb.append("import rover\n");
        sb.append("try:\n");
        sb.append("  rover.init(0)\n");
        final String[] commands = rover.getCommand().split("\n");
        for (String command : commands) {
            sb.append("  ");
            sb.append(command);
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("except Exception as e:\n");
        sb.append("  print (e)\n");
        final String filename = rover.getName() + "-command.py";
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            log.info("Writing {} to {}", sb, filename);
            out.println(sb);
            out.flush();
            formatScript(rover);
            sendScriptToRover(rover);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        model.addAttribute("rover", rover);
        log.info("Returning \"rover\"");
        return "rover";
    }

    @PostMapping("/rover/authenticate")
    public String authRover(@ModelAttribute("rover") final Rover rover, final Model model) {
        log.info("POST /rover/authenticate called with rover: {}", rover);
        final Optional<Rover> roverOpt = roverRepository
                .findAll()
                .flatMap(rovers -> rovers
                        .stream()
                        .filter(r -> r.getPasscode().equals(rover.getPasscode()))
                        .findFirst());
        if (roverOpt.isPresent()) {
            final Rover r = roverOpt.get();
            model.addAttribute("rover", r);
            log.info("Returning \"{}\" with rover set to {}", "rover", r);
            return "rover";
        }
        model.addAttribute("rover", new Rover());
        log.info("No rover found, returning \"{}\" with rover set to {}",
                "welcome", model.getAttribute("rover"));
        return "welcome";
    }

    private void sendScriptToRover(final Rover rover) {
        try {
            log.info("Sending script to rover...");
            final Process process = Runtime.getRuntime().exec("scp -p " + rover.getName() +
                    "-command.py pi@" + rover.getName() + ":/home/pi/new");
            process.waitFor(10, TimeUnit.SECONDS);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                log.info("Input stream received: {}", line);
            }
            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                log.info("Error stream received: {}", line);
            }
            inputReader.close();
            errorReader.close();
            log.info("send complete");
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void formatScript(final Rover rover) {
        try {
            log.info("formatting script...");
            final Process process = Runtime.getRuntime().exec("black " + rover.getName() + "-command.py");
            process.waitFor(5, TimeUnit.SECONDS);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                log.info("Input stream received: {}", line);
            }
            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                log.info("Error stream received: {}", line);
            }
            inputReader.close();
            errorReader.close();
            log.info("format complete");
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
