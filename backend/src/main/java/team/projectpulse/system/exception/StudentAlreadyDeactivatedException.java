package team.projectpulse.system.exception;

public class StudentAlreadyDeactivatedException extends RuntimeException {

    public StudentAlreadyDeactivatedException() {
        super("The student is already deactivated.");
    }

}
