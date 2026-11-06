package team.projectpulse.student.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.student.dto.StudentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit tests for the student DTO's deactivation fields")
class StudentToStudentDtoConverterTest {

    private final StudentToStudentDtoConverter converter = new StudentToStudentDtoConverter();

    private Student john;


    @BeforeEach
    void setUp() {
        Section section = new Section();
        section.setSectionId(2);
        section.setSectionName("2023-2024");
        this.john = new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        this.john.setSection(section);
    }

    @Test
    void testEnabledStudent() {
        StudentDto dto = this.converter.convert(this.john);

        assertThat(dto.enabled()).isTrue();
        assertThat(dto.deactivatedAt()).isNull();
        assertThat(dto.deactivationWeek()).isNull();
    }

    @Test
    void testDeactivatedStudent() {
        this.john.setEnabled(false);
        this.john.setDeactivatedAt(LocalDateTime.of(2023, 8, 20, 23, 30));

        StudentDto dto = this.converter.convert(this.john);

        assertThat(dto.enabled()).isFalse();
        assertThat(dto.deactivatedAt()).isEqualTo(LocalDateTime.of(2023, 8, 20, 23, 30));
        assertThat(dto.deactivationWeek()).isEqualTo("2023-W33");
    }

    @Test
    void testStudentDeactivatedBeforeDatesWereRecorded() {
        this.john.setEnabled(false);

        StudentDto dto = this.converter.convert(this.john);

        assertThat(dto.enabled()).isFalse();
        assertThat(dto.deactivatedAt()).isNull();
        assertThat(dto.deactivationWeek()).isNull();
    }

}
