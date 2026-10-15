package br.com.fiap.soat.vehiclecore.infrastructure.web.dto;

import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleRequest(
        @Schema(example = "Toyota") @NotBlank @Size(max = 60) String brand,
        @Schema(example = "Corolla XEi") @NotBlank @Size(max = 60) String model,
        @Schema(example = "2022") @NotNull @Min(Vehicle.FIRST_CAR_YEAR) Integer year,
        @Schema(example = "Prata") @NotBlank @Size(max = 30) String color,
        @Schema(example = "119900.00") @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price) {

    public VehicleData toData() {
        return new VehicleData(brand, model, year, color, price);
    }
}
