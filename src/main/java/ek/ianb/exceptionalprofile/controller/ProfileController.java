package ek.ianb.exceptionalprofile.controller;

import ek.ianb.exceptionalprofile.exception.DuplicateProfileException;
import ek.ianb.exceptionalprofile.exception.InvalidProfileException;
import ek.ianb.exceptionalprofile.model.Profile;
import ek.ianb.exceptionalprofile.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/exprofiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("profiles", profileService.findAll());
        return "profiles/profile-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("formTitle", "Create Profile");
        model.addAttribute("formAction", "/exprofiles");
        model.addAttribute("submitLabel", "Create");
        return "profiles/profile-form";
    }

    @PostMapping
    public String create(@ModelAttribute Profile profile, Model model) {
        try {
            profileService.create(profile);
            return "redirect:/exprofiles";
        } catch (InvalidProfileException | DuplicateProfileException ex) {
            model.addAttribute("profile", profile);
            model.addAttribute("formTitle", "Create Profile");
            model.addAttribute("formAction", "/exprofiles");
            model.addAttribute("submitLabel", "Create");
            model.addAttribute("errorMessage", ex.getMessage());
            return "profiles/profile-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable int id, Model model) {
        Profile profile = profileService.findById(id);
        model.addAttribute("profile", profile);
        model.addAttribute("formTitle", "Edit Profile");
        model.addAttribute("formAction", "/exprofiles/" + id);
        model.addAttribute("submitLabel", "Update");
        return "profiles/profile-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable int id, @ModelAttribute Profile profile, Model model) {
        try {
            profileService.update(id, profile);
            return "redirect:/exprofiles";
        } catch (InvalidProfileException | DuplicateProfileException ex) {
            profile.setId(id);
            model.addAttribute("profile", profile);
            model.addAttribute("formTitle", "Edit Profile");
            model.addAttribute("formAction", "/exprofiles/" + id);
            model.addAttribute("submitLabel", "Update");
            model.addAttribute("errorMessage", ex.getMessage());
            return "profiles/profile-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        profileService.deleteById(id);
        return "redirect:/exprofiles";
    }
}