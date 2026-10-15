package br.com.fiap.soat.vehiclecore.infrastructure.web;

import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.usecase.CreateVehicleUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.GetVehicleUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.ListVehiclesUseCase;
import br.com.fiap.soat.vehiclecore.application.usecase.UpdateVehicleUseCase;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.infrastructure.web.dto.PageResponse;
import br.com.fiap.soat.vehiclecore.infrastructure.web.dto.VehicleRequest;
import br.com.fiap.soat.vehiclecore.infrastructure.web.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Cadastro de veículos")
public class VehicleController {

    private final CreateVehicleUseCase createVehicle;
    private final UpdateVehicleUseCase updateVehicle;
    private final GetVehicleUseCase getVehicle;
    private final ListVehiclesUseCase listVehicles;

    public VehicleController(CreateVehicleUseCase createVehicle, UpdateVehicleUseCase updateVehicle,
                             GetVehicleUseCase getVehicle, ListVehiclesUseCase listVehicles) {
        this.createVehicle = createVehicle;
        this.updateVehicle = updateVehicle;
        this.getVehicle = getVehicle;
        this.listVehicles = listVehicles;
    }

    @PostMapping
    @Operation(summary = "Cadastra um veículo para venda e o publica no serviço de venda")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        Vehicle vehicle = createVehicle.execute(request.toData());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(vehicle.getId()).toUri();
        return ResponseEntity.created(location).body(VehicleResponse.from(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita os dados do veículo (409 se ele já estiver reservado ou vendido)")
    public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody VehicleRequest request) {
        return VehicleResponse.from(updateVehicle.execute(id, request.toData()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta o cadastro de um veículo")
    public VehicleResponse get(@PathVariable UUID id) {
        return VehicleResponse.from(getVehicle.execute(id));
    }

    @GetMapping
    @Operation(summary = "Lista o cadastro, do mais recente para o mais antigo")
    public PageResponse<VehicleResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
        return PageResponse.from(listVehicles.execute(new PageQuery(page, size)), VehicleResponse::from);
    }
}
