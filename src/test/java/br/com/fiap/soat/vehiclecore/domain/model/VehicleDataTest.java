package br.com.fiap.soat.vehiclecore.domain.model;

import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleDataTest {

    @Test
    void trimsTextFields() {
        VehicleData data = new VehicleData("  Fiat ", " Argo", 2024, "Cinza  ", BigDecimal.ONE);

        assertThat(data.brand()).isEqualTo("Fiat");
        assertThat(data.model()).isEqualTo("Argo");
        assertThat(data.color()).isEqualTo("Cinza");
    }

    @Test
    void requiresBrandModelAndColor() {
        assertThatThrownBy(() -> new VehicleData(null, "Argo", 2024, "Cinza", BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("marca");
        assertThatThrownBy(() -> new VehicleData("Fiat", " ", 2024, "Cinza", BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("modelo");
        assertThatThrownBy(() -> new VehicleData("Fiat", "Argo", 2024, "", BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("cor");
    }

    @Test
    void requiresPositivePrice() {
        assertThatThrownBy(() -> new VehicleData("Fiat", "Argo", 2024, "Cinza", null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new VehicleData("Fiat", "Argo", 2024, "Cinza", new BigDecimal("-1")))
                .isInstanceOf(BusinessRuleException.class);
    }
}
