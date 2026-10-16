package br.com.fiap.soat.vehiclecore.infrastructure.persistence;

import br.com.fiap.soat.vehiclecore.domain.model.Vehicle;
import br.com.fiap.soat.vehiclecore.domain.model.VehicleData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 60)
    private String brand;

    @Column(nullable = false, length = 60)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    private int year;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VehicleJpaEntity() {
    }

    VehicleJpaEntity(UUID id) {
        this.id = id;
    }

    void apply(Vehicle vehicle) {
        VehicleData data = vehicle.getData();
        this.brand = data.brand();
        this.model = data.model();
        this.year = data.year();
        this.color = data.color();
        this.price = data.price();
    }

    Vehicle toDomain() {
        return new Vehicle(id, new VehicleData(brand, model, year, color, price), version);
    }

    Long getVersion() {
        return version;
    }
}
