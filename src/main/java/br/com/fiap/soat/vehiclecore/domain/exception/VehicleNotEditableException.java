package br.com.fiap.soat.vehiclecore.domain.exception;

import java.util.UUID;

/** O serviço de venda informou que o veículo está reservado ou já foi vendido. */
public class VehicleNotEditableException extends ConflictException {

    public VehicleNotEditableException(UUID vehicleId) {
        super("Veículo " + vehicleId + " não pode ser editado: está reservado para uma compra ou já foi vendido");
    }
}
