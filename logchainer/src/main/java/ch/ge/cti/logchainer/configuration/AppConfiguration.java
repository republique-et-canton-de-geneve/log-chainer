package ch.ge.cti.logchainer.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:${application.properties}")
@ComponentScan("ch.ge.cti.logchainer")
public class AppConfiguration {

}
