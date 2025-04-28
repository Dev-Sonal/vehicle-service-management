package com.tch.vehicle.controller;

import com.tch.vehicle.dto.UserResponse;
import com.tch.vehicle.dto.UserUpdateRequest;
import com.tch.vehicle.entity.User;
import com.tch.vehicle.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(String.join(", ", user.getRoles()))
                .active(user.isEnabled())
                .lastLoginDate(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
        
        model.addAttribute("user", userResponse);
        if (!model.containsAttribute("updateRequest")) {
            model.addAttribute("updateRequest", new UserUpdateRequest());
        }
        return "users/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("updateRequest") UserUpdateRequest updateRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateRequest", bindingResult);
            redirectAttributes.addFlashAttribute("updateRequest", updateRequest);
            return "redirect:/profile";
        }

        try {
            userService.updateProfile(userDetails.getUsername(), updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }
} 