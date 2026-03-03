package app.auth;

import app.dao.ProfileDao;
import app.dto.LoginRequestDTO;
import app.dto.LoginResponseDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import persistence.enums.RoleName;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class AuthService {

    private static final String PRINCIPAL_ATTR = "authPrincipal";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(10);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{3,20}$");

    private final ProfileDao profileDao;
    private final ConcurrentMap<String, SessionState> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LoginAttemptState> loginAttempts = new ConcurrentHashMap<>();

    public AuthService(EntityManagerFactory entityManagerFactory) {
        this.profileDao = new ProfileDao(entityManagerFactory);
    }

    public void login(Context ctx) {
        cleanupExpiredSessions();

        LoginRequestDTO body = ctx.bodyAsClass(LoginRequestDTO.class);
        if (body == null || isBlank(body.username()) || isBlank(body.password())) {
            throw new BadRequestResponse("Username and password are required");
        }
        if (!USERNAME_PATTERN.matcher(body.username()).matches()) {
            throw new BadRequestResponse("Invalid username format");
        }
        if (body.password().length() > 128) {
            throw new BadRequestResponse("Invalid password format");
        }

        String attemptKey = getAttemptKey(ctx, body.username());
        if (isBlocked(attemptKey)) {
            throw new ForbiddenResponse("Too many login attempts. Try again later.");
        }

        AuthPrincipal principal = authenticate(body.username(), body.password())
                .orElseGet(() -> {
                    registerFailedAttempt(attemptKey);
                    throw new UnauthorizedResponse("Invalid credentials");
                });

        loginAttempts.remove(attemptKey);

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(SESSION_TTL);
        sessions.put(token, new SessionState(principal, expiresAt));

        ctx.json(new LoginResponseDTO(token, expiresAt.toString(), principal.profileId(), principal.username(), principal.roleName()));
    }

    public void logout(Context ctx) {
        cleanupExpiredSessions();

        String token = extractBearerToken(ctx)
                .orElseThrow(() -> new UnauthorizedResponse("Missing bearer token"));

        sessions.remove(token);
        ctx.status(204);
    }

    public void me(Context ctx) {
        cleanupExpiredSessions();

        AuthPrincipal principal = resolvePrincipal(ctx)
                .orElseThrow(() -> new UnauthorizedResponse("Unauthorized"));
        ctx.json(principal);
    }

    public void requireAuthenticated(Context ctx) {
        cleanupExpiredSessions();

        AuthPrincipal principal = resolvePrincipal(ctx)
                .orElseThrow(() -> new UnauthorizedResponse("Unauthorized"));
        ctx.attribute(PRINCIPAL_ATTR, principal);
    }

    public void requireAdmin(Context ctx) {
        AuthPrincipal principal = Optional.ofNullable(ctx.<AuthPrincipal>attribute(PRINCIPAL_ATTR))
                .or(() -> resolvePrincipal(ctx))
                .orElseThrow(() -> new UnauthorizedResponse("Unauthorized"));

        if (principal.roleName() != RoleName.ADMIN) {
            throw new ForbiddenResponse("Admin role required");
        }
        ctx.attribute(PRINCIPAL_ATTR, principal);
    }

    private Optional<AuthPrincipal> authenticate(String username, String password) {
        return profileDao.authenticate(username, password);
    }

    private Optional<AuthPrincipal> resolvePrincipal(Context ctx) {
        return extractBearerToken(ctx)
                .map(sessions::get)
                .filter(session -> session.expiresAt().isAfter(Instant.now()))
                .map(SessionState::principal);
    }

    private Optional<String> extractBearerToken(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null) {
            return Optional.empty();
        }
        if (!authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        loginAttempts.entrySet().removeIf(entry -> entry.getValue().windowStart().plus(LOGIN_WINDOW).isBefore(now));
    }

    private String getAttemptKey(Context ctx, String username) {
        String ip = Optional.ofNullable(ctx.ip()).orElse("unknown");
        return username + "|" + ip;
    }

    private boolean isBlocked(String attemptKey) {
        LoginAttemptState state = loginAttempts.get(attemptKey);
        if (state == null) {
            return false;
        }

        Instant now = Instant.now();
        if (state.windowStart().plus(LOGIN_WINDOW).isBefore(now)) {
            loginAttempts.remove(attemptKey);
            return false;
        }
        return state.attempts() >= MAX_LOGIN_ATTEMPTS;
    }

    private void registerFailedAttempt(String attemptKey) {
        Instant now = Instant.now();
        loginAttempts.compute(attemptKey, (k, current) -> {
            if (current == null || current.windowStart().plus(LOGIN_WINDOW).isBefore(now)) {
                return new LoginAttemptState(1, now);
            }
            return new LoginAttemptState(current.attempts() + 1, current.windowStart());
        });
    }

    private record SessionState(AuthPrincipal principal, Instant expiresAt) {
    }

    private record LoginAttemptState(int attempts, Instant windowStart) {
    }
}
