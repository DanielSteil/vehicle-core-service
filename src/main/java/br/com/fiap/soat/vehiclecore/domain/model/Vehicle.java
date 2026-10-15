package br.com.fiap.soat.vehiclecore.domain.model;

import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

/** Veículo cadastrado pela revenda. Este serviço é a fonte da verdade dos dados cadastrais. */
public class Vehicle {

    /** Ano do primeiro automóvel (Benz Patent-Motorwagen). */
    public static final int FIRST_CAR_YEAR = 1886;

    private final UUID id;
    private VehicleData data;
    private final Long version;

    public Vehicle(UUID id, VehicleData data, Long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.data = Objects.requireNonNull(data, "data");
        this.version = version;
    }

    public static Vehicle register(VehicleData data, Year currentYear) {
        validateYear(data, currentYear);
        return new Vehicle(UUID.randomUUID(), data, null);
    }

    public void update(VehicleData newData, Year currentYear) {
        validateYear(newData, currentYear);
        this.data = newData;
    }

    /** Aceita o ano-modelo seguinte, comum no mercado brasileiro. */
    private static void validateYear(VehicleData data, Year currentYear) {
        int maxYear = currentYear.getValue() + 1;
        if (data.year() < FIRST_CAR_YEAR || data.year() > maxYear) {
            throw new BusinessRuleException(
                    "O ano do veículo deve estar entre " + FIRST_CAR_YEAR + " e " + maxYear);
        }
    }

    public UUID getId() {
        return id;
    }

    public VehicleData getData() {
        return data;
    }

    public Long getVersion() {
        return version;
    }
}
