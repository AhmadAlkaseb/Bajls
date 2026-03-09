package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.VehicleType;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vehicles_garage"))
    @ToString.Exclude
    private Garage garage;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private VehicleType type;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;
}
