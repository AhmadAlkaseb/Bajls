package app.auth;

import app.audit.AuditContext;
import app.dao.ProfileRepository;
import app.dto.LoginRequestDTO;
import app.dto.LoginResponseDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import persistence.entity.Profile;
import persistence.enums.ProfileRole;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthService {
    private static final String CURRENT_USER = "currentUser";
    private static final String BASIC_PREFIX = "Basic ";

    private final ProfileRepository profileRepository;

    public AuthService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void login(Context ctx) {
        LoginRequestDTO body = ctx.bodyAsClass(LoginRequestDTO.class);
        validateLoginRequest(body);

        LoginResponseDTO response = authenticate(body.getUsername(), body.getPassword());
        if (response == null) {
            throw new UnauthorizedResponse("Invalid credentials");
        }
        ctx.json(response);
    }

    public void register(Context ctx) {
        Profile profile = ctx.bodyAsClass(Profile.class);
        validateProfile(profile);
        assignDefaultRole(profile);

        Profile savedProfile = profileRepository.save(profile);
        ctx.status(201).json(new LoginResponseDTO(savedProfile.getId(), savedProfile.getUsername(), savedProfile.getRole()));
    }

    public void logout(Context ctx) {
        ctx.attribute(CURRENT_USER, null);
        ctx.json("Logged out. Remove the Basic Authorization header on the client.");
    }

    public void requireAuthenticated(Context ctx) {
        rememberAuthenticatedUser(ctx, getAuthenticatedUser(ctx));
    }

    public void requireRole(Context ctx, ProfileRole role) {
        LoginResponseDTO user = getAuthenticatedUser(ctx);
        if (user.getRole() != role) {
            throw new ForbiddenResponse("Forbidden");
        }
        rememberAuthenticatedUser(ctx, user);
    }

    public void requireProfileOwnerOrAdmin(Context ctx) {
        LoginResponseDTO user = getAuthenticatedUser(ctx);
        if (user.getRole() == ProfileRole.ADMIN) {
            rememberAuthenticatedUser(ctx, user);
            return;
        }

        Long profileId = parseProfileId(ctx);

        if (!user.getProfileId().equals(profileId)) {
            throw new ForbiddenResponse("Forbidden");
        }

        rememberAuthenticatedUser(ctx, user);
    }

    private LoginResponseDTO getAuthenticatedUser(Context ctx) {
        LoginResponseDTO user = ctx.attribute(CURRENT_USER);
        if (user != null) {
            return user;
        }

        String[] credentials = decodeBasicCredentials(ctx);
        LoginResponseDTO authenticatedUser = authenticate(credentials[0], credentials[1]);
        if (authenticatedUser == null) {
            throw new UnauthorizedResponse("Invalid credentials");
        }

        rememberAuthenticatedUser(ctx, authenticatedUser);
        return authenticatedUser;
    }

    private LoginResponseDTO authenticate(String username, String password) {
        return profileRepository.authenticate(username, password);
    }

    private void validateLoginRequest(LoginRequestDTO body) {
        if (body == null || isBlank(body.getUsername()) || isBlank(body.getPassword())) {
            throw new BadRequestResponse("Username and password are required");
        }
    }

    private void validateProfile(Profile profile) {
        if (profile == null) {
            throw new BadRequestResponse("Profile is required");
        }
        if (isBlank(profile.getFirstName()) || isBlank(profile.getLastName()) || isBlank(profile.getEmail())
                || isBlank(profile.getUsername()) || isBlank(profile.getPassword())) {
            throw new BadRequestResponse("Missing required profile fields");
        }
    }

    private void assignDefaultRole(Profile profile) {
        if (profile.getRole() == null) {
            profile.setRole(ProfileRole.USER);
        }
    }

    private void rememberAuthenticatedUser(Context ctx, LoginResponseDTO user) {
        AuditContext.setAuthenticatedUser(user);
        ctx.attribute(CURRENT_USER, user);
    }

    private Long parseProfileId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid id");
        }
    }

    private String[] decodeBasicCredentials(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            throw new UnauthorizedResponse("Missing Authorization header");
        }

        String credentials;
        try {
            byte[] decoded = Base64.getDecoder().decode(header.substring(BASIC_PREFIX.length()).trim());
            credentials = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedResponse("Invalid Authorization header");
        }

        int separatorIndex = credentials.indexOf(':');
        if (separatorIndex <= 0) {
            throw new UnauthorizedResponse("Invalid Authorization header");
        }

        return new String[] {
                credentials.substring(0, separatorIndex),
                credentials.substring(separatorIndex + 1)
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
