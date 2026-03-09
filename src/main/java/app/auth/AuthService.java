package app.auth;

import app.dao.ProfileDao;
import app.dto.LoginRequestDTO;
import app.dto.LoginResponseDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import persistence.entity.Profile;
import persistence.enums.ProfileRole;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthService {
    private static final String CURRENT_USER = "currentUser";

    private final ProfileDao profileDao;

    public AuthService(EntityManagerFactory entityManagerFactory) {
        this.profileDao = new ProfileDao(entityManagerFactory);
    }

    public void login(Context ctx) {
        LoginRequestDTO body = ctx.bodyAsClass(LoginRequestDTO.class);
        if (body == null || isBlank(body.getUsername()) || isBlank(body.getPassword())) {
            throw new BadRequestResponse("Username and password are required");
        }
        LoginResponseDTO response = profileDao.authenticate(body.getUsername(), body.getPassword());
        if (response == null) {
            throw new UnauthorizedResponse("Invalid credentials");
        }
        ctx.json(response);
    }

    public void register(Context ctx) {
        Profile profile = ctx.bodyAsClass(Profile.class);
        if (profile == null) {
            throw new BadRequestResponse("Profile is required");
        }
        if (isBlank(profile.getFirstName()) || isBlank(profile.getLastName()) || isBlank(profile.getEmail())
                || isBlank(profile.getUsername()) || isBlank(profile.getPassword())) {
            throw new BadRequestResponse("Missing required profile fields");
        }
        if (profile.getRole() == null) {
            profile.setRole(ProfileRole.USER);
        }

        Profile savedProfile = profileDao.save(profile);
        ctx.status(201).json(new LoginResponseDTO(savedProfile.getId(), savedProfile.getUsername(), savedProfile.getRole()));
    }

    public void logout(Context ctx) {
        ctx.attribute(CURRENT_USER, null);
        ctx.json("Logged out. Remove the Basic Authorization header on the client.");
    }

    public void requireAuthenticated(Context ctx) {
        LoginResponseDTO user = getAuthenticatedUser(ctx);
        ctx.attribute(CURRENT_USER, user);
    }

    public void requireRole(Context ctx, ProfileRole role) {
        LoginResponseDTO user = getAuthenticatedUser(ctx);
        if (user.getRole() != role) {
            throw new ForbiddenResponse("Forbidden");
        }
        ctx.attribute(CURRENT_USER, user);
    }

    public void requireProfileOwnerOrAdmin(Context ctx) {
        LoginResponseDTO user = getAuthenticatedUser(ctx);
        if (user.getRole() == ProfileRole.ADMIN) {
            ctx.attribute(CURRENT_USER, user);
            return;
        }

        Long profileId;
        try {
            profileId = Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid id");
        }

        if (!user.getProfileId().equals(profileId)) {
            throw new ForbiddenResponse("Forbidden");
        }

        ctx.attribute(CURRENT_USER, user);
    }

    private LoginResponseDTO getAuthenticatedUser(Context ctx) {
        LoginResponseDTO user = ctx.attribute(CURRENT_USER);
        if (user != null) {
            return user;
        }

        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            throw new UnauthorizedResponse("Missing Authorization header");
        }

        String credentials;
        try {
            byte[] decoded = Base64.getDecoder().decode(header.substring("Basic ".length()).trim());
            credentials = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedResponse("Invalid Authorization header");
        }

        int separatorIndex = credentials.indexOf(':');
        if (separatorIndex <= 0) {
            throw new UnauthorizedResponse("Invalid Authorization header");
        }

        String username = credentials.substring(0, separatorIndex);
        String password = credentials.substring(separatorIndex + 1);
        LoginResponseDTO authenticatedUser = profileDao.authenticate(username, password);
        if (authenticatedUser == null) {
            throw new UnauthorizedResponse("Invalid credentials");
        }

        ctx.attribute(CURRENT_USER, authenticatedUser);
        return authenticatedUser;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
