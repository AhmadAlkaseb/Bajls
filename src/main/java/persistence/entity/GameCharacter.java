package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GameCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private float balance = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_profile"))
    @ToString.Exclude
    private Profile profile;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_gender"))
    @ToString.Exclude
    private Gender gender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "skincolor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_skincolor"))
    @ToString.Exclude
    private SkinColor skinColor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "eyecolor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_eyecolor"))
    @ToString.Exclude
    private EyeColor eyeColor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "height_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_height"))
    @ToString.Exclude
    private Height height;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "weight_id", nullable = false, foreignKey = @ForeignKey(name = "fk_characters_weight"))
    @ToString.Exclude
    private Weight weight;

    @OneToOne(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private House house;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<GangAffiliation> gangAffiliations = new ArrayList<>();
}
