package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;

import java.time.Clock;
import java.time.Year;

/**
 * Cadastra o veículo e o publica no serviço de venda. Se o serviço de venda falhar,
 * a transação é desfeita, para que os dois bancos não fiquem divergentes.
 */
public class CreateVehicleUseCase {

    private final VehicleRepository vehicles;
    private final SalesServiceGateway salesService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public CreateVehicleUseCase(VehicleRepository vehicles, SalesServiceGateway salesService,
                                UnitOfWork unitOfWork, Clock clock) {
        this.vehicles = vehicles;
        this.salesService = salesService;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    public Vehicle execute(VehicleData data) {
        Vehicle vehicle = Vehicle.register(data, Year.now(clock));
        return unitOfWork.execute(() -> {
            Vehicle saved = vehicles.save(vehicle);
            salesService.syncVehicle(saved);
            return saved;
        });
    }
}
