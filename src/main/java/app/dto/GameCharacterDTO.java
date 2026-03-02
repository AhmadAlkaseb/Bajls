package app.dto;
public record GameCharacterDTO(
        int id,
        String name,
        float balance,
        int profileId,
        int genderId,
        int skinColorId,
        int eyeColorId,
        int heightId,
        int weightId,
        int houseId
) {
}
