package br.com.fiap.soat.vehiclecore.application.port;

import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.pagination.PageResult;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Optional<Vehicle> findById(UUID id);

    /** Persiste imediatamente; falha se outra transação alterou o veículo desde a leitura (lock otimista). */
    Vehicle save(Vehicle vehicle);

    /** Cadastros mais recentes primeiro. */
    PageResult<Vehicle> findAll(PageQuery query);
}
