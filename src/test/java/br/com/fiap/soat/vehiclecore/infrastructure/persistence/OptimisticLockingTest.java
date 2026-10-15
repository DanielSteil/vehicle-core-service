package br.com.fiap.soat.vehiclecore.infrastructure.persistence;

import br.com.fiap.soat.vehiclecore.IntegrationTest;
import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Duas edições simultâneas a partir da mesma versão: a segunda é recusada em vez de sobrescrever a primeira. */
class OptimisticLockingTest extends IntegrationTest {

    @Autowired
    private VehicleRepository vehicles;

    @Autowired
    private UnitOfWork unitOfWork;

    private static VehicleData data(String price) {
        return new VehicleData("Peugeot", "208", 2024, "Azul", new BigDecimal(price));
    }

    @Test
    void savingAStaleVehicleFails() {
        Vehicle created = unitOfWork.execute(() -> vehicles.save(Vehicle.register(data("90000.00"), Year.of(2026))));
        Vehicle first = vehicles.findById(created.getId()).orElseThrow();
        Vehicle stale = vehicles.findById(created.getId()).orElseThrow();

        first.update(data("88000.00"), Year.of(2026));
        Vehicle saved = unitOfWork.execute(() -> vehicles.save(first));
        stale.update(data("1.00"), Year.of(2026));

        assertThat(saved.getVersion()).isGreaterThan(stale.getVersion());
        assertThatThrownBy(() -> unitOfWork.execute(() -> vehicles.save(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(vehicles.findById(created.getId()).orElseThrow().getData().price())
                .isEqualByComparingTo("88000.00");
    }
}
