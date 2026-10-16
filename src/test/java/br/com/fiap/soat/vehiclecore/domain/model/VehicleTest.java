package br.com.fiap.soat.vehiclecore.domain.model;

import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    private static final Year CURRENT_YEAR = Year.of(2026);

    private static VehicleData data(int year, String price) {
        return new VehicleData("Hyundai", "HB20", year, "Branco", new BigDecimal(price));
    }

    @Test
    void registersWithNewIdentity() {
        Vehicle first = Vehicle.register(data(2024, "79990.00"), CURRENT_YEAR);
        Vehicle second = Vehicle.register(data(2024, "79990.00"), CURRENT_YEAR);

        assertThat(first.getId()).isNotNull().isNotEqualTo(second.getId());
        assertThat(first.getVersion()).isNull();
        assertThat(first.getData().model()).isEqualTo("HB20");
    }

    @Test
    void acceptsNextYearModel() {
        assertThat(Vehicle.register(data(2027, "1.00"), CURRENT_YEAR).getData().year()).isEqualTo(2027);
    }

    @Test
    void rejectsYearOutOfRange() {
        assertThatThrownBy(() -> Vehicle.register(data(2028, "1.00"), CURRENT_YEAR))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O ano do veículo deve estar entre 1886 e 2027");
        assertThatThrownBy(() -> Vehicle.register(data(1885, "1.00"), CURRENT_YEAR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updatesData() {
        Vehicle vehicle = Vehicle.register(data(2024, "79990.00"), CURRENT_YEAR);

        vehicle.update(data(2025, "74990.00"), CURRENT_YEAR);

        assertThat(vehicle.getData().year()).isEqualTo(2025);
        assertThat(vehicle.getData().price()).isEqualByComparingTo("74990.00");
    }

    @Test
    void updateValidatesYearToo() {
        Vehicle vehicle = Vehicle.register(data(2024, "79990.00"), CURRENT_YEAR);

        assertThatThrownBy(() -> vehicle.update(data(3000, "1.00"), CURRENT_YEAR))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(vehicle.getData().year()).isEqualTo(2024);
    }
}
