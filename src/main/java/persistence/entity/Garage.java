package persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "garages")
@JsonIgnoreProperties({"character", "vehicles"})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @OneToOne(mappedBy = "garage", fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private GameCharacter character;

    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Vehicle> vehicles = new ArrayList<>();
}
