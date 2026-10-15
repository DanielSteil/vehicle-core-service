package br.com.fiap.soat.vehiclecore.infrastructure.config;

import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.application.usecase.CreateVehicleUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.GetVehicleUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.ListVehiclesUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.UpdateVehicleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Os casos de uso não conhecem o Spring; é aqui que eles são montados. */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    CreateVehicleUseCase createVehicleUseCase(VehicleRepository vehicles, SalesServiceGateway salesService,
                                              UnitOfWork unitOfWork, Clock clock) {
        return new CreateVehicleUseCase(vehicles, salesService, unitOfWork, clock);
    }

    @Bean
    UpdateVehicleUseCase updateVehicleUseCase(VehicleRepository vehicles, SalesServiceGateway salesService,
                                              UnitOfWork unitOfWork, Clock clock) {
        return new UpdateVehicleUseCase(vehicles, salesService, unitOfWork, clock);
    }

    @Bean
    GetVehicleUseCase getVehicleUseCase(VehicleRepository vehicles) {
        return new GetVehicleUseCase(vehicles);
    }

    @Bean
    ListVehiclesUseCase listVehiclesUseCase(VehicleRepository vehicles) {
        return new ListVehiclesUseCase(vehicles);
    }
}
