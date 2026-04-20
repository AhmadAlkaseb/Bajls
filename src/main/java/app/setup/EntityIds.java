package app.setup;

import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.Drug;
import persistence.entity.GameCharacter;
import persistence.entity.Gang;
import persistence.entity.GangAffiliation;
import persistence.entity.Garage;
import persistence.entity.House;
import persistence.entity.Profile;
import persistence.entity.Quest;
import persistence.entity.Vehicle;

public final class EntityIds {
    private EntityIds() {
    }

    public static Long get(Object entity) {
        if (entity instanceof Profile profile) return profile.getId();
        if (entity instanceof GameCharacter character) return character.getId();
        if (entity instanceof House house) return house.getId();
        if (entity instanceof Garage garage) return garage.getId();
        if (entity instanceof Vehicle vehicle) return vehicle.getId();
        if (entity instanceof Drug drug) return drug.getId();
        if (entity instanceof CharacterDrug characterDrug) return characterDrug.getId();
        if (entity instanceof Quest quest) return quest.getId();
        if (entity instanceof CharacterQuest characterQuest) return characterQuest.getId();
        if (entity instanceof Gang gang) return gang.getId();
        if (entity instanceof GangAffiliation affiliation) return affiliation.getId();
        return null;
    }
}
