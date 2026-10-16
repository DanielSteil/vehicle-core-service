package br.com.fiap.soat.vehiclecore.infrastructure.web.dto;

import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleResponse(UUID id, String brand, String model, int year, String color, BigDecimal price) {

    public static VehicleResponse from(Vehicle vehicle) {
        VehicleData data = vehicle.getData();
        return new VehicleResponse(vehicle.getId(), data.brand(), data.model(), data.year(), data.color(),
                data.price());
    }
}
