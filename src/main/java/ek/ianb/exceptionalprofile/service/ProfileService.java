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

        try {
            Profile profile = profileRepository.findById(id);
            if (profile == null) {
                throw new ProfileNotFoundException(id);
            }
            return profile;
        } catch (ProfileNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to retrieve profile.", ex);
        }
    }

    public Profile create(Profile profile) {
        validateForCreateOrUpdate(profile);

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
        validateForCreateOrUpdate(profile);

        try {
            if (!profileRepository.existsById(id)) {
                throw new ProfileNotFoundException(id);
            }

            profile.setId(id);
            profileRepository.update(profile);
        } catch (ProfileNotFoundException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateProfileException("Name or email already exists.");
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to update profile.", ex);
        }
    }

    public void deleteById(int id) {
        validateId(id);

        try {
            if (!profileRepository.existsById(id)) {
                throw new ProfileNotFoundException(id);
            }
            profileRepository.deleteById(id);
        } catch (ProfileNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Failed to delete profile.", ex);
        }
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new InvalidProfileException("Id must be a positive integer.");
        }
    }

    private void validateForCreateOrUpdate(Profile profile) {
        if (profile == null) {
            throw new InvalidProfileException("Profile is required.");
        }

        String name = profile.getName();
        String email = profile.getEmail();

        if (name == null || name.isBlank()) {
            throw new InvalidProfileException("Name is required.");
        }

        if (name.length() > 100) {
            throw new InvalidProfileException("Name must be at most 100 characters.");
        }

        if (email == null || email.isBlank()) {
            throw new InvalidProfileException("Email is required.");
        }

        if (email.length() > 100) {
            throw new InvalidProfileException("Email must be at most 100 characters.");
        }

        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new InvalidProfileException("Email format is invalid.");
        }
    }
}
