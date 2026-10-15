package br.com.fiap.soat.vehiclecore.domain.model;

import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/** Dados de cadastro de um veículo à venda. */
public record VehicleData(String brand, String model, int year, String color, BigDecimal price) {

    public VehicleData {
        brand = requireText(brand, "marca");
        model = requireText(model, "modelo");
        color = requireText(color, "cor");
        if (price == null || price.signum() <= 0) {
            throw new BusinessRuleException("O preço do veículo deve ser maior que zero");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("O campo " + field + " é obrigatório");
        }
        return value.strip();
    }
}
