package app.migration;

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

import java.util.List;

public record MigrationSnapshot(
        List<Profile> profiles,
        List<Drug> drugs,
        List<Quest> quests,
        List<Gang> gangs,
        List<House> houses,
        List<Garage> garages,
        List<GameCharacter> characters,
        List<Vehicle> vehicles,
        List<CharacterDrug> characterDrugs,
        List<CharacterQuest> characterQuests,
        List<GangAffiliation> gangAffiliations
) {
}
