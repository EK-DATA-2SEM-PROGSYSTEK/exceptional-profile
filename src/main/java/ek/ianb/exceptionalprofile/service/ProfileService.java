package ek.ianb.exceptionalprofile.service;


import ek.ianb.exceptionalprofile.exception.DatabaseOperationException;
import ek.ianb.exceptionalprofile.exception.DuplicateProfileException;
import ek.ianb.exceptionalprofile.exception.InvalidProfileException;
import ek.ianb.exceptionalprofile.exception.ProfileNotFoundException;
import ek.ianb.exceptionalprofile.model.Profile;
import ek.ianb.exceptionalprofile.repository.ProfileRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> findAll() {
        try {
            return profileRepository.findAll();
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to retrieve profiles.", ex);
        }
    }

    public Profile findById(int id) {
        validateId(id);

        Profile profile;
        try {
            profile = profileRepository.findById(id);
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to retrieve profile.", ex);
        }
        if (profile == null) {
            throw new ProfileNotFoundException(id);
        }
        return profile;
    }

    public Profile create(Profile profile) {
        validateProfile(profile);
        try {
            return profileRepository.insert(profile);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProfileException("Name or email already exists.");
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to create profile.", ex);
        }
    }

    public void update(int id, Profile profile) {
        validateId(id);
        validateProfile(profile);
        profile.setId(id);
        try {
            boolean updated = profileRepository.update(profile);
            if (!updated) {
                throw new ProfileNotFoundException(id);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProfileException("Name or email already exists.");
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update profile.", ex);
        }
    }

    public void deleteById(int id) {
        validateId(id);
        try {
            boolean deleted = profileRepository.deleteById(id);
            if (!deleted) {
                throw new ProfileNotFoundException(id);
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete profile.", ex);
        }
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new InvalidProfileException("Id must be a positive integer.");
        }
    }

    private void validateProfile(Profile profile) {
        if (profile == null) {
            throw new InvalidProfileException("Profile is required.");
        }

        String name = profile.getName();
        if (name == null || name.isBlank()) {
            throw new InvalidProfileException("Name is required.");
        }
        if (name.length() > 100) {
            throw new InvalidProfileException("Name must be at most 100 characters.");
        }

        validateEmail(profile.getEmail());
    }

    private void validateEmail(String email){
        if (email == null || email.isBlank()) {
            throw new InvalidProfileException("Email is required.");
        }
        if (email.length() > 100) {
            throw new InvalidProfileException("Email must be at most 100 characters.");
        }
        final Pattern EMAIL_PATTERN =
                Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidProfileException("Email format is invalid.");
        }
    }
}
