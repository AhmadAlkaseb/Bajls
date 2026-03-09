package persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import persistence.enums.EyeColorType;
import persistence.enums.GenderType;
import persistence.enums.SkinColorType;

@Entity
@Table(name = "characters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GameCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_profile"))
    @ToString.Exclude
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 30)
    private GenderType gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "skincolor", nullable = false, length = 30)
    private SkinColorType skincolor;

    @Enumerated(EnumType.STRING)
    @Column(name = "eyecolor", nullable = false, length = 30)
    private EyeColorType eyecolor;

    @Column(name = "height", nullable = false, length = 30)
    private String height;

    @Column(name = "weight", nullable = false, length = 30)
    private String weight;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "house_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_characters_house")
    )
    @ToString.Exclude
    private House house;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "garage_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_characters_garage")
    )
    @ToString.Exclude
    private Garage garage;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CharacterDrug> characterDrugs = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<CharacterQuest> characterQuests = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<GangAffiliation> gangAffiliations = new ArrayList<>();
}
