package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.pagination.PageResult;
import br.com.fiap.soat.vehiclecore.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;

/** Listagem administrativa do cadastro. As listagens públicas ficam no serviço de venda. */
public class ListVehiclesUseCase {

    private final VehicleRepository vehicles;

    public ListVehiclesUseCase(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    public PageResult<Vehicle> execute(PageQuery query) {
        return vehicles.findAll(query);
    }
}
