package program.utils;

public class NotValidArchiveNameException extends Exception {
    public NotValidArchiveNameException() {
        super("filename isn't valid");
    }
}
