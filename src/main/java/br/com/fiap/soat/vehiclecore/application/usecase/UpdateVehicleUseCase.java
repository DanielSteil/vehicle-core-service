package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;

import java.time.Clock;
import java.time.Year;
import java.util.UUID;

/**
 * Edita o veículo e repassa a alteração ao serviço de venda. É o serviço de venda quem
 * sabe se o veículo já foi reservado ou vendido; nesse caso ele recusa e a edição é desfeita.
 */
public class UpdateVehicleUseCase {

    private final VehicleRepository vehicles;
    private final SalesServiceGateway salesService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public UpdateVehicleUseCase(VehicleRepository vehicles, SalesServiceGateway salesService,
                                UnitOfWork unitOfWork, Clock clock) {
        this.vehicles = vehicles;
        this.salesService = salesService;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    public Vehicle execute(UUID vehicleId, VehicleData data) {
        return unitOfWork.execute(() -> {
            Vehicle vehicle = vehicles.findById(vehicleId)
                    .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
            vehicle.update(data, Year.now(clock));
            Vehicle saved = vehicles.save(vehicle);
            salesService.syncVehicle(saved);
            return saved;
        });
    }
}
