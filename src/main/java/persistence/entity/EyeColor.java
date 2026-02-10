package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.EyeColorName;

@Entity
@Table(name = "eyecolors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EyeColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private EyeColorName name;
}
