package br.com.fiap.soat.vehiclecore.infrastructure.client;

import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;

import java.math.BigDecimal;

/** Contrato do PUT /internal/vehicles/{id} do serviço de venda. */
public record SyncVehicleRequest(String brand, String model, int year, String color, BigDecimal price) {

    static SyncVehicleRequest from(VehicleData data) {
        return new SyncVehicleRequest(data.brand(), data.model(), data.year(), data.color(), data.price());
    }
}
