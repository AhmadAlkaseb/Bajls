package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.GenderName;

@Entity
@Table(name = "genders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Gender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private GenderName name;
}
