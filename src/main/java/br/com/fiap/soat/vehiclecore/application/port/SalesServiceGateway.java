package br.com.fiap.soat.vehiclecore.application.port;

import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;

/** Porta de saída para o serviço de venda, que mantém sua própria cópia dos veículos. */
public interface SalesServiceGateway {

    /**
     * Envia os dados atuais do veículo ao serviço de venda. A operação é idempotente.
     *
     * @throws br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotEditableException
     *         se o veículo estiver reservado ou vendido
     * @throws br.com.fiap.soat.vehiclecore.application.exception.SalesServiceUnavailableException
     *         se o serviço de venda não responder
     * @throws br.com.fiap.soat.vehiclecore.application.exception.SalesServiceIntegrationException
     *         se o serviço de venda recusar a chamada por erro de integração
     */
    void syncVehicle(Vehicle vehicle);
}
