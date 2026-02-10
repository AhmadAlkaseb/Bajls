package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.WeightName;

@Entity
@Table(name = "weights")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Weight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private WeightName name;
}
