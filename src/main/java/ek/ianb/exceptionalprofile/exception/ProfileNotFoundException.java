package ek.ianb.exceptionalprofile.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(int id) {
        super("Profile with id " + id + " was not found.");
    }
}