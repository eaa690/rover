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
import java.util.List;
import java.util.Optional;

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
		roverRepository.save(new Rover(1L, "blue-rover", "5347", true));
		roverRepository.save(new Rover(2L, "red-rover", "4324", false));
		roverRepository.save(new Rover(3L, "white-rover", "7654", false));
		roverRepository.save(new Rover(4L, "purple-rover", "4564", false));
		roverRepository.save(new Rover(5L, "green-rover", "6578", false));
		roverRepository.save(new Rover(6L, "orange-rover", "2312", false));
		roverRepository.save(new Rover(7L, "black-rover", "9437", false));
	}

	@Scheduled(cron = "0 * * ? * *")
	public void rotateActiveRover() {
		int roverCount;
		long activeRoverId = 1L;
		final Optional<List<Rover>> roversOpt = roverRepository.findAll();
		if (roversOpt.isPresent()) {
			final List<Rover> rovers = roversOpt.get();
			roverCount = rovers.size();
			for (Rover r : rovers) {
				log.info("Checking for active rover: {}", r);
				if (r.isActive()) {
					activeRoverId = r.getId();
				}
			}
			log.info("Prior active rover ID: {}", activeRoverId);
			activeRoverId++;
			log.info("Incremented active rover ID: {}; rover count: {}", activeRoverId, roverCount);
			if (activeRoverId > roverCount) {
				log.info("Resetting active roverId to 1");
				activeRoverId = 1L;
			}
			log.info("Setting all rovers to inactive");
			rovers.forEach(r -> {
				r.setActive(false);
				roverRepository.save(r);
			});
			log.info("Setting rover ID: {} to active", activeRoverId);
			for (Rover r : rovers) {
				if (r.getId() == activeRoverId) {
					r.setActive(true);
					log.info("Saving new active rover: {}", r);
					roverRepository.save(r);
				}
			}
		}
	}

}
