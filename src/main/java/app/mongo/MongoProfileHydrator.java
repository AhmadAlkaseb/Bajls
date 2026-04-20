package app.mongo;

import persistence.entity.CharacterDrug;
import persistence.entity.CharacterQuest;
import persistence.entity.GangAffiliation;
import persistence.entity.GameCharacter;
import persistence.entity.Garage;
import persistence.entity.Profile;
import persistence.entity.Vehicle;

import java.util.ArrayList;

final class MongoProfileHydrator {
    private MongoProfileHydrator() {
    }

    static void hydrateProfile(Profile profile) {
        if (profile == null) {
            return;
        }

        if (profile.getCharacters() == null) {
            profile.setCharacters(new ArrayList<>());
        }

        for (GameCharacter character : profile.getCharacters()) {
            hydrateCharacter(profile, character);
        }
    }

    static void hydrateCharacter(Profile profile, GameCharacter character) {
        if (character == null) {
            return;
        }

        character.setProfile(profile);
        if (character.getCharacterDrugs() == null) {
            character.setCharacterDrugs(new ArrayList<>());
        }
        if (character.getCharacterQuests() == null) {
            character.setCharacterQuests(new ArrayList<>());
        }
        if (character.getGangAffiliations() == null) {
            character.setGangAffiliations(new ArrayList<>());
        }

        hydrateHouse(character);
        hydrateGarage(character);
        hydrateRelations(character);
    }

    private static void hydrateHouse(GameCharacter character) {
        if (character.getHouse() != null) {
            character.getHouse().setCharacter(character);
        }
    }

    private static void hydrateGarage(GameCharacter character) {
        if (character.getGarage() == null) {
            return;
        }

        Garage garage = character.getGarage();
        garage.setCharacter(character);
        if (garage.getVehicles() == null) {
            garage.setVehicles(new ArrayList<>());
        }
        for (Vehicle vehicle : garage.getVehicles()) {
            vehicle.setGarage(garage);
        }
    }

    private static void hydrateRelations(GameCharacter character) {
        for (CharacterDrug characterDrug : character.getCharacterDrugs()) {
            characterDrug.setCharacter(character);
            if (characterDrug.getDrug() != null && characterDrug.getDrug().getCharacterDrugs() == null) {
                characterDrug.getDrug().setCharacterDrugs(new ArrayList<>());
            }
        }

        for (CharacterQuest characterQuest : character.getCharacterQuests()) {
            characterQuest.setCharacter(character);
            if (characterQuest.getQuest() != null && characterQuest.getQuest().getCharacterQuests() == null) {
                characterQuest.getQuest().setCharacterQuests(new ArrayList<>());
            }
        }

        for (GangAffiliation gangAffiliation : character.getGangAffiliations()) {
            gangAffiliation.setCharacter(character);
            if (gangAffiliation.getGang() != null && gangAffiliation.getGang().getAffiliations() == null) {
                gangAffiliation.getGang().setAffiliations(new ArrayList<>());
            }
        }
    }
}
