package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import persistence.enums.SkinColorName;

@Entity
@Table(name = "skincolors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SkinColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private SkinColorName name;
}
