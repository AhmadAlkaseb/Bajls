package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.HeightName;

@Entity
@Table(name = "heights")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Height {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private HeightName name;
}
