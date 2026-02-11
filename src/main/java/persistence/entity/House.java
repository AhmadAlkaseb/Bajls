package persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "houses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "amount_rooms", nullable = false)
    private int amountRooms;

    @Column(name = "amount_bathrooms", nullable = false)
    private int amountBathrooms;

    @OneToOne(mappedBy = "house", fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private GameCharacter character;
}
