package team.projectpulse.student;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.system.WeekKeys;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
public class Student extends PeerEvaluationUser {

    @ManyToOne(optional = false)
    private Section section;
    @ManyToOne
    private Team team;
    // When she was deactivated, in calendar time (the injected Clock), so the week it falls in lines up with the
    // course section's active weeks. Null while she is enabled, and for a student disabled before it was recorded.
    private LocalDateTime deactivatedAt;
    // The course admin or instructor who deactivated her. A course admin is an Instructor, so one type covers both.
    @ManyToOne
    private Instructor deactivatedBy;


    public Student() {
    }

    public Student(String username, String firstName, String lastName, String email, String password, boolean enabled, String roles) {
        super(username, firstName, lastName, email, password, enabled, roles);
    }

    /**
     * The week key of the week she was deactivated in, or empty when no deactivation is recorded: she is enabled, or
     * she was disabled before deactivations were recorded.
     */
    public Optional<String> deactivationWeek() {
        return Optional.ofNullable(this.deactivatedAt).map(at -> WeekKeys.of(at.toLocalDate()));
    }

    /**
     * Whether a teammate may submit or edit a peer evaluation of her for the given week (BR-evaluation-active-evaluatee):
     * always while she is enabled; once she is deactivated, only for a week in or before her deactivation week. When
     * that week is unknown, never, so a gap in the record fails closed.
     *
     * @throws IllegalArgumentException if she is deactivated and {@code week} is not a well-formed week key
     */
    public boolean isEvaluableFor(String week) {
        if (isEnabled()) {
            return true;
        }
        return deactivationWeek().map(cutoff -> isOnOrBefore(week, cutoff)).orElse(false);
    }

    /**
     * Whether the course section's peer evaluation report lists her for the given week (BR-evaluation-active-evaluatee):
     * always while she is enabled; once she is deactivated, only for a week in or before her deactivation week. When
     * that week is unknown, for every week, so that the team's record is kept rather than erased.
     *
     * @throws IllegalArgumentException if she is deactivated and {@code week} is not a well-formed week key
     */
    public boolean isReportedFor(String week) {
        if (isEnabled()) {
            return true;
        }
        return deactivationWeek().map(cutoff -> isOnOrBefore(week, cutoff)).orElse(true);
    }

    // Week keys are compared as dates, never as strings
    private static boolean isOnOrBefore(String week, String cutoff) {
        return !WeekKeys.monday(week).isAfter(WeekKeys.monday(cutoff));
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public Instructor getDeactivatedBy() {
        return deactivatedBy;
    }

    public void setDeactivatedBy(Instructor deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

}
