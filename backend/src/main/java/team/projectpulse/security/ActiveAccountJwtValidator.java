package team.projectpulse.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

/**
 * Refuses a bearer token whose account is no longer active (FR-SEC-active-account).
 *
 * <p>Login already refuses a disabled account, but a token issued before the account was deactivated stays
 * otherwise valid until it expires. This validator runs on every bearer request, after the signature and expiry
 * checks, and loads the account the token's {@code userId} claim names. The token fails validation when the claim
 * is missing or not an integer, when no such account exists, or when the account is disabled. A failed validation
 * surfaces as the standard {@code 401 invalid_token}, which the SPA turns into a return to the login page.
 *
 * <p>The lookup is one primary-key read per request and is deliberately not cached: a cache would reopen the window
 * between deactivation and cut-off that this check exists to close.
 */
@Component
public class ActiveAccountJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ACCOUNT_DEACTIVATED = new OAuth2Error(
            OAuth2ErrorCodes.INVALID_TOKEN, "The account is deactivated.", null);

    private final UserRepository userRepository;


    public ActiveAccountJwtValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Integer userId = readUserId(jwt.getClaim("userId"));
        if (userId == null) {
            return OAuth2TokenValidatorResult.failure(ACCOUNT_DEACTIVATED);
        }
        boolean active = this.userRepository.findById(userId)
                .map(PeerEvaluationUser::isEnabled)
                .orElse(false);
        return active ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(ACCOUNT_DEACTIVATED);
    }

    private static Integer readUserId(Object claim) {
        if (claim instanceof Integer i) return i;
        if (claim instanceof Long l) return l.intValue() == l ? l.intValue() : null;
        if (claim instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

}
