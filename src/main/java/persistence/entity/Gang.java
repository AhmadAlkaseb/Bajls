package persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import persistence.enums.GangType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gangs")
@JsonIgnoreProperties({"affiliations"})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Gang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private GangType type;

    @OneToMany(mappedBy = "gang", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<GangAffiliation> affiliations = new ArrayList<>();
}
