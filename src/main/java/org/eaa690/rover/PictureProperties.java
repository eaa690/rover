package org.eaa690.rover;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("pictures")
@Getter
@Setter
public class PictureProperties {

    private String rootLocation;

}
