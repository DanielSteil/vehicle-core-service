package br.com.fiap.soat.vehiclecore.domain.exception;

import java.util.UUID;

public class VehicleNotFoundException extends NotFoundException {

    public VehicleNotFoundException(UUID vehicleId) {
        super("Veículo " + vehicleId + " não encontrado");
    }
}
