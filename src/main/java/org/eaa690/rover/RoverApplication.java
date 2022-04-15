package org.eaa690.rover;

import org.eaa690.rover.model.Rover;
import org.eaa690.rover.model.RoverRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@SpringBootApplication
public class RoverApplication {

	@Resource
	private RoverRepository roverRepository;

	public static void main(String[] args) {
		SpringApplication.run(RoverApplication.class, args);
	}

	@PostConstruct
	public void setup() {
		roverRepository.save(new Rover(1L, "blue-rover", "1234"));
		roverRepository.save(new Rover(2L, "red-rover", "4324"));
		roverRepository.save(new Rover(3L, "white-rover", "7654"));
		roverRepository.save(new Rover(4L, "purple-rover", "4564"));
		roverRepository.save(new Rover(5L, "green-rover", "6578"));
		roverRepository.save(new Rover(6L, "orange-rover", "2312"));
		roverRepository.save(new Rover(7L, "black-rover", "9437"));
	}
}
