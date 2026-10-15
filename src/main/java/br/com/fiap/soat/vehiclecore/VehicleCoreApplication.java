package br.com.fiap.soat.vehiclecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VehicleCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleCoreApplication.class, args);
    }
}
