package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.pagination.PageResult;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<UUID, Vehicle> store = new LinkedHashMap<>();

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        store.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public PageResult<Vehicle> findAll(PageQuery query) {
        List<Vehicle> all = List.copyOf(store.values());
        return new PageResult<>(all, query.page(), query.size(), all.size());
    }

    int count() {
        return store.size();
    }
}
