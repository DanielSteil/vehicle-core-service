package br.com.fiap.soat.vehiclecore.infrastructure.persistence;

import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.pagination.PageResult;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class VehiclePersistenceAdapter implements VehicleRepository {

    private static final Sort NEWEST_FIRST = Sort.by("createdAt").descending().and(Sort.by("id"));

    private final VehicleJpaRepository jpa;

    public VehiclePersistenceAdapter(VehicleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpa.findById(id).map(VehicleJpaEntity::toDomain);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleJpaEntity entity = vehicle.getVersion() == null
                ? new VehicleJpaEntity(vehicle.getId())
                : jpa.findById(vehicle.getId())
                        .filter(found -> Objects.equals(found.getVersion(), vehicle.getVersion()))
                        .orElseThrow(() -> new ObjectOptimisticLockingFailureException(
                                VehicleJpaEntity.class, vehicle.getId()));
        entity.apply(vehicle);
        // flush antes de chamar o serviço de venda: erros de banco aparecem antes da chamada HTTP
        return jpa.saveAndFlush(entity).toDomain();
    }

    @Override
    public PageResult<Vehicle> findAll(PageQuery query) {
        Page<VehicleJpaEntity> page = jpa.findAll(PageRequest.of(query.page(), query.size(), NEWEST_FIRST));
        return new PageResult<>(page.getContent(), query.page(), query.size(), page.getTotalElements())
                .map(VehicleJpaEntity::toDomain);
    }
}
