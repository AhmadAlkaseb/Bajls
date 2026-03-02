package app.dto;

public record ProfileDTO(
        int id,
        String firstName,
        String lastName,
        String email,
        String username,
        int roleId
) {
}
