package team.projectpulse.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import team.projectpulse.student.Student;
import team.projectpulse.user.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * FR-SEC-active-account at the token: UC-STU-deactivate-student 6a's edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for the per-request account check")
class ActiveAccountJwtValidatorTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ActiveAccountJwtValidator activeAccountJwtValidator;


    private static Jwt jwtWithUserId(Object userId) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("j.smith@abc.edu")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        if (userId != null) {
            builder.claim("userId", userId);
        }
        return builder.build();
    }

    private static Student student(boolean enabled) {
        return new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", enabled, "student");
    }

    private static void assertInvalidToken(OAuth2TokenValidatorResult result) {
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getErrorCode()).isEqualTo(OAuth2ErrorCodes.INVALID_TOKEN);
            assertThat(error.getDescription()).isEqualTo("The account is deactivated.");
        });
    }

    @Test
    void testEnabledAccountPasses() {
        given(this.userRepository.findById(4)).willReturn(Optional.of(student(true)));

        OAuth2TokenValidatorResult result = this.activeAccountJwtValidator.validate(jwtWithUserId(4L));

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void testDisabledAccountFails() {
        given(this.userRepository.findById(4)).willReturn(Optional.of(student(false)));

        assertInvalidToken(this.activeAccountJwtValidator.validate(jwtWithUserId(4L)));
    }

    @Test
    void testUnknownAccountFails() {
        given(this.userRepository.findById(999)).willReturn(Optional.empty());

        assertInvalidToken(this.activeAccountJwtValidator.validate(jwtWithUserId(999L)));
    }

    @Test
    void testMissingUserIdClaimFails() {
        assertInvalidToken(this.activeAccountJwtValidator.validate(jwtWithUserId(null)));
        verifyNoInteractions(this.userRepository);
    }

    @Test
    void testNonIntegerUserIdClaimFails() {
        assertInvalidToken(this.activeAccountJwtValidator.validate(jwtWithUserId("not-a-number")));
        assertInvalidToken(this.activeAccountJwtValidator.validate(jwtWithUserId(Long.MAX_VALUE)));
        verifyNoInteractions(this.userRepository);
    }

}
