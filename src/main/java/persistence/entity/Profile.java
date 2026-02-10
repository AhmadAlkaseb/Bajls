package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles",
        indexes = {
                @Index(name = "idx_profiles_email", columnList = "email"),
                @Index(name = "idx_profiles_username", columnList = "username")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 20)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 20)
    private String password;

    // Many profiles -> one role
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_profiles_role"))
    @ToString.Exclude
    private Role role;

    // One profile -> many characters
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<GameCharacter> characters = new ArrayList<>();
}
