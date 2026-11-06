package team.projectpulse.student;

import org.junit.jupiter.api.AfterEach;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.system.exception.StudentAlreadyDeactivatedException;
import team.projectpulse.user.MyUserPrincipal;
import team.projectpulse.user.userinvitation.UserInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    StudentRepository studentRepository;
    @Mock
    SectionRepository sectionRepository;
    @Mock
    UserInvitationService userInvitationService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserUtils userUtils;
    @Mock
    Clock clock;

    @InjectMocks
    private StudentService studentService;

    List<Student> students;

    @BeforeEach
    void setUp() {
        // Create students
        Student john = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        Student eric = new Student("eric", "Eric", "Hudson", "e.hudson@abc.edu", "123456", true, "student");
        Student jerry = new Student("jerry", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");
        Student woody = new Student("woody", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");
        Student amanda = new Student("amanda", "Amanda", "Wagner", "a.wagner@abc.edu", "123456", true, "student");
        Student cora = new Student("cora", "Cora", "Manning", "c.manning@abc.edu", "123456", true, "student");
        Student agustin = new Student("agustin", "Agustin", "Freeman", "a.freeman@abc.edu", "123456", true, "student");
        Student mavis = new Student("mavis", "Mavis", "Huber", "m.huber@abc.edu", "123456", true, "student");
        Student mary = new Student("mary", "Mary", "Vargas", "m.vargas@abc.edu", "123456", true, "student");
        Student rosendo = new Student("rosendo", "Rosendo", "Maxwell", "r.maxwell@abc.edu", "123456", true, "student");

        this.students = List.of(john, eric, jerry, woody, amanda, cora, agustin, mavis, mary, rosendo);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Clear the security context after each test to avoid interference between tests
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("firstName", "john");
        PageImpl expectedPage = new PageImpl(List.of(this.students.get(0)));

        given(this.userUtils.getUserSectionId()).willReturn(2);
        given(this.studentRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Student> result = this.studentService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void testFindStudentById() {
        // Given
        Student john = this.students.get(0);
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john));

        // When
        Student result = this.studentService.findStudentById(1);

        // Then
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void testSaveStudent() {
        // Given
        Student jan = new Student("jan", "Jan", "Mckinney", "j.mckinney@abc.edu", "Abc123456", true, "student");
        doNothing().when(this.userInvitationService).validateUserInvitation(anyString(), anyString(), anyInt(), anyInt(), anyString());
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(new Section()));
        given(this.passwordEncoder.encode(anyString())).willReturn("Encoded Password");
        given(this.studentRepository.save(jan)).willReturn(jan);

        // When
        Student result = this.studentService.saveStudent(jan, 1, 1, "token", "student");

        // Then
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Mckinney");
        assertThat(result.getEmail()).isEqualTo("j.mckinney@abc.edu");
        assertThat(result.getRoles()).isEqualTo("student");
        assertThat(result.getSection()).isNotNull();
        assertThat(result.isEnabled()).isEqualTo(true);
    }

    @Test
    void testStudentUpdateOwnInfo() {
        // Given
        Student update = new Student("john", "John (updated)", "Smith (updated)", "j.smith@abc.edu (updated)", "", false, "admin student");
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        given(this.studentRepository.save(any(Student.class))).willReturn(this.students.get(0));

        Instructor instructor = new Instructor();
        instructor.setRoles("student");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Student result = this.studentService.updateStudent(1, update);

        // Then
        assertThat(result.getUsername()).isEqualTo(update.getUsername());
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getLastName()).isEqualTo(update.getLastName());
        assertThat(result.getEmail()).isEqualTo(update.getEmail());
        assertThat(result.getRoles()).isNotEqualTo(update.getRoles());
        assertThat(result.isEnabled()).isNotEqualTo(update.isEnabled());
    }

    @Test
    void testInstructorUpdatesStudent() {
        // Given
        Student update = new Student("john", "John (updated)", "Smith (updated)", "j.smith@abc.edu (updated)", "", false, "admin student");
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        given(this.studentRepository.save(any(Student.class))).willReturn(this.students.get(0));

        Instructor instructor = new Instructor();
        instructor.setRoles("instructor");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Student result = this.studentService.updateStudent(1, update);

        // Then
        assertThat(result.getUsername()).isEqualTo(update.getUsername());
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getLastName()).isEqualTo(update.getLastName());
        assertThat(result.getEmail()).isEqualTo(update.getEmail());
//        assertThat(result.getRoles()).isEqualTo(update.getRoles());
        // Not even an instructor changes the enabled status here: that is deactivateStudent's job (UC-STU-deactivate-student)
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void testDeactivateStudentSuccess() {
        // Given
        Student john = this.students.get(0);
        Instructor bill = new Instructor("b.gates@abc.edu", "Bill", "Gates", "b.gates@abc.edu", "123456", true, "instructor");
        Clock fixedClock = Clock.fixed(LocalDateTime.of(2023, 8, 20, 23, 30).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());
        Student stored = new Student(john.getUsername(), john.getFirstName(), john.getLastName(), john.getEmail(), john.getPassword(), false, john.getRoles());
        stored.setDeactivatedAt(LocalDateTime.of(2023, 8, 20, 23, 30));
        stored.setDeactivatedBy(bill);
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john), Optional.of(stored));
        given(this.userUtils.getInstructor()).willReturn(bill);
        given(this.studentRepository.deactivateIfEnabled(1, LocalDateTime.of(2023, 8, 20, 23, 30), bill)).willReturn(1);

        // When
        Student result = this.studentService.deactivateStudent(1);

        // Then: deactivated through the conditional update, and the stored row is returned
        verify(this.studentRepository).deactivateIfEnabled(1, LocalDateTime.of(2023, 8, 20, 23, 30), bill);
        verify(this.studentRepository, never()).save(any(Student.class));
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getDeactivatedAt()).isEqualTo(LocalDateTime.of(2023, 8, 20, 23, 30));
        assertThat(result.getDeactivatedBy()).isSameAs(bill);
        assertThat(result.deactivationWeek()).contains("2023-W33");
    }

    @Test
    void testDeactivateStudentLosesConcurrentDeactivation() {
        // Given: John reads as enabled, but another caller deactivates him before this conditional update runs
        Student john = this.students.get(0);
        Instructor bill = new Instructor("b.gates@abc.edu", "Bill", "Gates", "b.gates@abc.edu", "123456", true, "instructor");
        Clock fixedClock = Clock.fixed(LocalDateTime.of(2023, 8, 20, 23, 30).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john));
        given(this.userUtils.getInstructor()).willReturn(bill);
        given(this.studentRepository.deactivateIfEnabled(1, LocalDateTime.of(2023, 8, 20, 23, 30), bill)).willReturn(0);

        // When
        Throwable thrown = catchThrowable(() -> this.studentService.deactivateStudent(1));

        // Then: refused as 3a (409), with nothing written over the first deactivation
        assertThat(thrown).isInstanceOf(StudentAlreadyDeactivatedException.class)
                .hasMessage("The student is already deactivated.");
        verify(this.studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testDeactivateAlreadyDeactivatedStudent() {
        // Given: John was deactivated before, by someone else, at another time
        Student john = this.students.get(0);
        Instructor earlier = new Instructor();
        LocalDateTime earlierAt = LocalDateTime.of(2023, 8, 1, 9, 0);
        john.setEnabled(false);
        john.setDeactivatedAt(earlierAt);
        john.setDeactivatedBy(earlier);
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john));

        // When
        Throwable thrown = catchThrowable(() -> this.studentService.deactivateStudent(1));

        // Then: refused (3a), and the original record is untouched
        assertThat(thrown).isInstanceOf(StudentAlreadyDeactivatedException.class)
                .hasMessage("The student is already deactivated.");
        assertThat(john.isEnabled()).isFalse();
        assertThat(john.getDeactivatedAt()).isEqualTo(earlierAt);
        assertThat(john.getDeactivatedBy()).isSameAs(earlier);
        verify(this.studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testDeactivateUnknownStudent() {
        // Given
        given(this.studentRepository.findById(999)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> this.studentService.deactivateStudent(999));

        // Then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class);
        verify(this.studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testDeleteStudent() {
        // Given
        given(this.studentRepository.findById(1)).willReturn(Optional.of(this.students.get(0)));
        doNothing().when(this.studentRepository).deleteById(1);

        // When
        this.studentService.deleteStudent(1);

        // Then
        verify(this.studentRepository, times(1)).deleteById(1);
    }

}