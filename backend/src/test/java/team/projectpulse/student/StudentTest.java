package team.projectpulse.student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BR-evaluation-active-evaluatee's week cutoff, as the two predicates over {@link Student#deactivationWeek()}.
 */
@DisplayName("Unit tests for a student's evaluation cutoff")
class StudentTest {

    private static Student enabled() {
        return new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
    }

    private static Student deactivatedAt(LocalDateTime at) {
        Student student = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", false, "student");
        student.setDeactivatedAt(at);
        return student;
    }

    @Test
    void testEnabledStudentIsEvaluableAndReportedForEveryWeek() {
        Student john = enabled();

        assertThat(john.deactivationWeek()).isEmpty();
        assertThat(john.isEvaluableFor("2023-W01")).isTrue();
        assertThat(john.isEvaluableFor("2099-W01")).isTrue();
        assertThat(john.isReportedFor("2023-W01")).isTrue();
        assertThat(john.isReportedFor("2099-W01")).isTrue();
    }

    @Test
    void testDeactivatedStudentUpToAndAfterHerDeactivationWeek() {
        // Sunday 2023-08-20 23:30, the dev clock, is the last moment of 2023-W33
        Student john = deactivatedAt(LocalDateTime.of(2023, 8, 20, 23, 30));

        assertThat(john.deactivationWeek()).contains("2023-W33");
        assertThat(john.isEvaluableFor("2023-W32")).isTrue();
        assertThat(john.isEvaluableFor("2023-W33")).isTrue();
        assertThat(john.isEvaluableFor("2023-W34")).isFalse();
        assertThat(john.isReportedFor("2023-W32")).isTrue();
        assertThat(john.isReportedFor("2023-W33")).isTrue();
        assertThat(john.isReportedFor("2023-W34")).isFalse();
    }

    @Test
    void testCutoffAcrossAnIsoYearBoundary() {
        // Saturday 2027-01-02 belongs to 2026-W53, not to any week of 2027
        Student john = deactivatedAt(LocalDateTime.of(2027, 1, 2, 12, 0));

        assertThat(john.deactivationWeek()).contains("2026-W53");
        assertThat(john.isEvaluableFor("2026-W52")).isTrue();
        assertThat(john.isEvaluableFor("2026-W53")).isTrue();
        assertThat(john.isEvaluableFor("2027-W01")).isFalse();
        assertThat(john.isReportedFor("2026-W53")).isTrue();
        assertThat(john.isReportedFor("2027-W01")).isFalse();
    }

    @Test
    void testDeactivatedStudentWithNoRecordedDate() {
        Student john = deactivatedAt(null);

        assertThat(john.deactivationWeek()).isEmpty();
        // No peer evaluation of her is accepted or edited for any week (fail closed), but the report keeps her for every week
        assertThat(john.isEvaluableFor("2023-W01")).isFalse();
        assertThat(john.isEvaluableFor("2099-W01")).isFalse();
        assertThat(john.isReportedFor("2023-W01")).isTrue();
        assertThat(john.isReportedFor("2099-W01")).isTrue();
    }

}
