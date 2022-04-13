package org.eaa690.rover;

import lombok.Getter;
import lombok.Setter;
import org.eaa690.rover.model.Rover;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("rovers")
@Getter
@Setter
public class RoverProperties {

    private List<Rover> rovers = new ArrayList<>();

}
