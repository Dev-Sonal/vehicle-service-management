package com.tch.vehicle.controller;

import com.tch.vehicle.dto.SignupRequest;
import com.tch.vehicle.entity.User;
import com.tch.vehicle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("welcome", true);
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("user") SignupRequest signupRequest,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // Check if passwords match
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        // Check if username exists
        if (userService.existsByUsername(signupRequest.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
        }

        // Check if email exists
        if (userService.existsByEmail(signupRequest.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }

        // Validate full name
        if (signupRequest.getFullName() == null || signupRequest.getFullName().trim().isEmpty()) {
            result.rejectValue("fullName", "error.user", "Full name is required");
        }

        if (result.hasErrors()) {
            return "signup";
        }

        try {
            User user = new User();
            user.setUsername(signupRequest.getUsername());
            user.setPassword(signupRequest.getPassword());
            
            // Handle full name splitting
            String fullName = signupRequest.getFullName().trim();
            String[] nameParts = fullName.split("\\s+", 2);
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            user.setFullName(fullName); // Set the full name directly
            
            user.setEmail(signupRequest.getEmail());
            user.setEnabled(true);

            Set<String> roles = new HashSet<>();
            roles.add(signupRequest.getRole());
            user.setRoles(roles);

            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during registration. Please try again.");
            return "redirect:/signup";
        }
    }
} 