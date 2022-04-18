package org.eaa690.rover;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.rover.model.Rover;
import org.eaa690.rover.model.RoverRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class RoverApplication {

	@Resource
	private RoverRepository roverRepository;

	public static void main(String[] args) {
		SpringApplication.run(RoverApplication.class, args);
	}

	@PostConstruct
	public void setup() {
		roverRepository.save(new Rover(1L, "blue-rover", "5347"));
		roverRepository.save(new Rover(2L, "red-rover", "4324"));
		roverRepository.save(new Rover(3L, "white-rover", "7654"));
		roverRepository.save(new Rover(4L, "purple-rover", "4564"));
		roverRepository.save(new Rover(5L, "green-rover", "6578"));
		roverRepository.save(new Rover(6L, "orange-rover", "2312"));
		roverRepository.save(new Rover(7L, "black-rover", "9437"));
	}

	@Scheduled(fixedDelay = 30000)
	public void getScriptOutput() {
		final Optional<List<Rover>> roversOpt = roverRepository.findAll();
		if (roversOpt.isPresent()) {
			final List<Rover> rovers = roversOpt.get();
			rovers.forEach(rover -> {
				try {
					log.info("Retrieving script output from rover...");
					final Process process = Runtime.getRuntime().exec("ssh pi@" + rover.getName()
							+ " cat /home/pi/run.log");
					process.waitFor(10, TimeUnit.SECONDS);
					final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					final StringBuilder sb = new StringBuilder();
					String line;
					while ((line = inputReader.readLine()) != null) {
						log.info("Input stream received: {}", line);
						sb.append(line).append("\n");
					}
					final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					while ((line = errorReader.readLine()) != null) {
						log.info("Error stream received: {}", line);
						sb.append(line).append("\n");
					}
					inputReader.close();
					errorReader.close();
					log.info("receive complete");
					rover.setScriptOutput(sb.toString());
					roverRepository.save(rover);
				} catch (InterruptedException | IOException e) {
					log.error(e.getMessage(), e);
				}
			});
		}
	}
}
