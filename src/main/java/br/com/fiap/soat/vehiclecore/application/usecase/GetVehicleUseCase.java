package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;

import java.util.UUID;

public class GetVehicleUseCase {

    private final VehicleRepository vehicles;

    public GetVehicleUseCase(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    public Vehicle execute(UUID vehicleId) {
        return vehicles.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }
}
