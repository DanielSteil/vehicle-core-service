package br.com.fiap.soat.vehiclecore.application.usecase;

import br.com.fiap.soat.vehiclecore.application.exception.SalesServiceUnavailableException;
import br.com.fiap.soat.vehiclecore.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclecore.application.port.SalesServiceGateway;
import br.com.fiap.soat.vehiclecore.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclecore.domain.exception.BusinessRuleException;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotEditableException;
import br.com.fiap.soat.vehiclecore.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class VehicleUseCasesTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneId.of("UTC"));

    private final InMemoryVehicleRepository vehicles = spy(new InMemoryVehicleRepository());
    private final SalesServiceGateway salesService = mock(SalesServiceGateway.class);
    private final UnitOfWork unitOfWork = new UnitOfWork() {
        @Override
        public <T> T execute(Supplier<T> work) {
            return work.get();
        }
    };

    private final CreateVehicleUseCase create = new CreateVehicleUseCase(vehicles, salesService, unitOfWork, CLOCK);
    private final UpdateVehicleUseCase update = new UpdateVehicleUseCase(vehicles, salesService, unitOfWork, CLOCK);
    private final GetVehicleUseCase get = new GetVehicleUseCase(vehicles);
    private final ListVehiclesUseCase list = new ListVehiclesUseCase(vehicles);

    private static VehicleData data(String price) {
        return new VehicleData("Nissan", "Kicks", 2025, "Vermelho", new BigDecimal(price));
    }

    @Test
    void createSavesAndThenPublishesToTheSalesService() {
        Vehicle created = create.execute(data("115000.00"));

        InOrder order = inOrder(vehicles, salesService);
        order.verify(vehicles).save(any());
        order.verify(salesService).syncVehicle(created);
        assertThat(get.execute(created.getId())).isSameAs(created);
    }

    @Test
    void createValidatesTheYearAgainstTheClock() {
        VehicleData tooNew = new VehicleData("Nissan", "Kicks", 2028, "Vermelho", BigDecimal.TEN);

        assertThatThrownBy(() -> create.execute(tooNew)).isInstanceOf(BusinessRuleException.class);
        verify(salesService, never()).syncVehicle(any());
    }

    @Test
    void createPropagatesSalesServiceFailureSoTheTransactionRollsBack() {
        doThrow(new SalesServiceUnavailableException("fora do ar", null)).when(salesService).syncVehicle(any());

        assertThatThrownBy(() -> create.execute(data("115000.00")))
                .isInstanceOf(SalesServiceUnavailableException.class);
    }

    @Test
    void updateChangesDataAndPublishesIt() {
        Vehicle created = create.execute(data("115000.00"));
        clearInvocations(salesService);

        Vehicle updated = update.execute(created.getId(), data("109000.00"));

        assertThat(updated.getData().price()).isEqualByComparingTo("109000.00");
        verify(salesService).syncVehicle(updated);
    }

    @Test
    void updateOfUnknownVehicleFails() {
        assertThatThrownBy(() -> update.execute(UUID.randomUUID(), data("1.00")))
                .isInstanceOf(VehicleNotFoundException.class);
        verify(salesService, never()).syncVehicle(any());
    }

    @Test
    void updateIsRefusedWhenTheSalesServiceSaysTheVehicleIsTaken() {
        Vehicle created = create.execute(data("115000.00"));
        doThrow(new VehicleNotEditableException(created.getId())).when(salesService).syncVehicle(any());

        assertThatThrownBy(() -> update.execute(created.getId(), data("1.00")))
                .isInstanceOf(VehicleNotEditableException.class)
                .hasMessageContaining("reservado");
    }

    @Test
    void getOfUnknownVehicleFails() {
        assertThatThrownBy(() -> get.execute(UUID.randomUUID())).isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void listReturnsTheRegisteredVehicles() {
        create.execute(data("1.00"));
        create.execute(data("2.00"));

        assertThat(list.execute(new PageQuery(0, 10)).totalElements()).isEqualTo(2);
    }
}
