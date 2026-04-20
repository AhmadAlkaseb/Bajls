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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class EntityCopies {
    private EntityCopies() {
    }

    public static List<Profile> profiles(List<Profile> source) {
        return source.stream().map(profile -> Profile.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .username(profile.getUsername())
                .password(profile.getPassword())
                .role(profile.getRole())
                .characters(new ArrayList<>())
                .build()).toList();
    }

    public static List<Drug> drugs(List<Drug> source) {
        return source.stream().map(drug -> Drug.builder()
                .id(drug.getId())
                .name(drug.getName())
                .type(drug.getType())
                .characterDrugs(new ArrayList<>())
                .build()).toList();
    }

    public static List<Quest> quests(List<Quest> source) {
        return source.stream().map(quest -> Quest.builder()
                .id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .reward(quest.getReward() == null ? BigDecimal.ZERO : quest.getReward())
                .characterQuests(new ArrayList<>())
                .build()).toList();
    }

    public static List<Gang> gangs(List<Gang> source) {
        return source.stream().map(gang -> Gang.builder()
                .id(gang.getId())
                .name(gang.getName())
                .type(gang.getType())
                .affiliations(new ArrayList<>())
                .build()).toList();
    }

    public static List<GameCharacter> characters(List<GameCharacter> source) {
        return source.stream().map(character -> GameCharacter.builder()
                .id(character.getId())
                .name(character.getName())
                .balance(character.getBalance() == null ? BigDecimal.ZERO : character.getBalance())
                .profile(profileReference(character.getProfile()))
                .gender(character.getGender())
                .skincolor(character.getSkincolor())
                .eyecolor(character.getEyecolor())
                .height(character.getHeight())
                .weight(character.getWeight())
                .house(houseReference(character.getHouse()))
                .garage(garageReference(character.getGarage()))
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build()).toList();
    }

    public static List<Vehicle> vehicles(List<Vehicle> source) {
        return source.stream().map(vehicle -> Vehicle.builder()
                .id(vehicle.getId())
                .garage(garageReference(vehicle.getGarage()))
                .model(vehicle.getModel())
                .type(vehicle.getType())
                .plateNumber(vehicle.getPlateNumber())
                .build()).toList();
    }

    public static List<CharacterDrug> characterDrugs(List<CharacterDrug> source) {
        return source.stream().map(row -> CharacterDrug.builder()
                .id(row.getId())
                .character(characterReference(row.getCharacter()))
                .drug(drugReference(row.getDrug()))
                .quantity(row.getQuantity())
                .build()).toList();
    }

    public static List<CharacterQuest> characterQuests(List<CharacterQuest> source) {
        return source.stream().map(row -> CharacterQuest.builder()
                .id(row.getId())
                .character(characterReference(row.getCharacter()))
                .quest(questReference(row.getQuest()))
                .status(row.getStatus())
                .acceptedAt(row.getAcceptedAt())
                .build()).toList();
    }

    public static List<GangAffiliation> gangAffiliations(List<GangAffiliation> source) {
        return source.stream().map(row -> GangAffiliation.builder()
                .id(row.getId())
                .character(characterReference(row.getCharacter()))
                .gang(gangReference(row.getGang()))
                .joinDate(row.getJoinDate())
                .build()).toList();
    }

    public static House houseWithCharacter(House house, Long characterId) {
        return House.builder()
                .id(house.getId())
                .amountRooms(house.getAmountRooms())
                .amountBathrooms(house.getAmountBathrooms())
                .character(characterReference(characterId))
                .build();
    }

    public static Garage garageWithCharacter(Garage garage, Long characterId) {
        return Garage.builder()
                .id(garage.getId())
                .capacity(garage.getCapacity())
                .character(characterReference(characterId))
                .vehicles(new ArrayList<>())
                .build();
    }

    private static Profile profileReference(Profile profile) {
        return Profile.builder().id(profile.getId()).characters(new ArrayList<>()).build();
    }

    private static House houseReference(House house) {
        return House.builder().id(house.getId())
                .amountRooms(house.getAmountRooms())
                .amountBathrooms(house.getAmountBathrooms())
                .build();
    }

    private static Garage garageReference(Garage garage) {
        return Garage.builder().id(garage.getId())
                .capacity(garage.getCapacity())
                .vehicles(new ArrayList<>())
                .build();
    }

    private static Drug drugReference(Drug drug) {
        return Drug.builder().id(drug.getId())
                .name(drug.getName())
                .type(drug.getType())
                .characterDrugs(new ArrayList<>())
                .build();
    }

    private static Quest questReference(Quest quest) {
        return Quest.builder().id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .reward(quest.getReward())
                .characterQuests(new ArrayList<>())
                .build();
    }

    private static Gang gangReference(Gang gang) {
        return Gang.builder().id(gang.getId())
                .name(gang.getName())
                .type(gang.getType())
                .affiliations(new ArrayList<>())
                .build();
    }

    private static GameCharacter characterReference(GameCharacter character) {
        return characterReference(character.getId());
    }

    private static GameCharacter characterReference(Long characterId) {
        return GameCharacter.builder().id(characterId)
                .characterDrugs(new ArrayList<>())
                .characterQuests(new ArrayList<>())
                .gangAffiliations(new ArrayList<>())
                .build();
    }
}
