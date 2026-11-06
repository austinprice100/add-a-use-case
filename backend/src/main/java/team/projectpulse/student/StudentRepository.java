package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.projectpulse.instructor.Instructor;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    // Find all students in a section
    List<Student> findBySectionSectionId(Integer sectionId);

    List<Student> findByTeamTeamId(Integer teamId);

    /**
     * Deactivates the student only while she is still enabled, as one conditional update, so that of two concurrent
     * deactivations only the first records when and by whom (UC-STU-deactivate-student 3a). The persistence context
     * is flushed before and cleared after, so a later load of her reads the stored row.
     *
     * @return 1 if she was enabled and is now deactivated, 0 if she was already deactivated (or does not exist)
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Student s set s.enabled = false, s.deactivatedAt = :deactivatedAt, s.deactivatedBy = :deactivatedBy "
            + "where s.id = :studentId and s.enabled = true")
    int deactivateIfEnabled(@Param("studentId") Integer studentId,
                            @Param("deactivatedAt") LocalDateTime deactivatedAt,
                            @Param("deactivatedBy") Instructor deactivatedBy);
}
