package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.student.StudentSecurityService;

import java.util.function.Supplier;

/**
 * Guards the routes a course admin may use on any student of her course, even one in a course section she is not
 * assigned to: viewing the student and deactivating her (BR-student-lifecycle). Composed with
 * {@link StudentInstructorAuthorizationManager} or {@link UserOwnershipAuthorizationManager} through
 * {@code AuthorizationManagers.anyOf}, never used alone.
 *
 * <p>Reads {@code studentId} and delegates to {@link StudentSecurityService#isCurrentUserAdminOfStudentCourse}.
 */
@Component
public class StudentCourseOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final StudentSecurityService studentSecurityService;


    public StudentCourseOwnershipAuthorizationManager(StudentSecurityService studentSecurityService) {
        this.studentSecurityService = studentSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer studentId = PathVariables.readId(context, "studentId");
        if (studentId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.studentSecurityService.isCurrentUserAdminOfStudentCourse(studentId));
    }

}
