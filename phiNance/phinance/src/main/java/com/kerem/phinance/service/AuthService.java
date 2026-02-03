package com.kerem.phinance.service;

import com.kerem.phinance.dto.auth.*;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.model.Category;
import com.kerem.phinance.model.User;
import com.kerem.phinance.repository.CategoryRepository;
import com.kerem.phinance.repository.UserRepository;
import com.kerem.phinance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CategoryRepository categoryRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPreferredCurrency(request.getPreferredCurrency());

        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);

        // Email verification removed: mark as verified and save user
        user.setEmailVerified(true);
        userRepository.save(user);

        // Create default categories for new user
        createDefaultCategories(user.getId());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Reactivate user if account was deactivated
        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newAccessToken = jwtTokenProvider.generateToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public AuthResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPreferredCurrency(request.getPreferredCurrency());

        User saved = userRepository.save(user);

        // Build minimal AuthResponse with updated user DTO
        return buildAuthResponse(saved, null, null);
    }

    public void deleteAccount(String userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password is incorrect");
        }

        // Soft delete: mark user as inactive
        user.setActive(false);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .preferredCurrency(user.getPreferredCurrency())
                        .emailVerified(user.isEmailVerified())
                        .build())
                .build();
    }

    private void createDefaultCategories(String userId) {
        List<Category> defaultCategories = new ArrayList<>();

        // Salary (INCOME)
        Category salary = new Category();
        salary.setUserId(userId);
        salary.setName("Salary");
        salary.setType(Category.CategoryType.INCOME);
        salary.setColor("#6ee772"); // Emerald green
        defaultCategories.add(salary);

        // Rent (INCOME)
        Category rentIncome = new Category();
        rentIncome.setUserId(userId);
        rentIncome.setName("Rent");
        rentIncome.setType(Category.CategoryType.INCOME);
        rentIncome.setColor("#3B82F6"); // Blue
        defaultCategories.add(rentIncome);

        // Food (EXPENSE)
        Category food = new Category();
        food.setUserId(userId);
        food.setName("Food");
        food.setType(Category.CategoryType.EXPENSE);
        food.setColor("#f59e0b"); // Amber
        defaultCategories.add(food);

        // Transportation (EXPENSE)
        Category transportation = new Category();
        transportation.setUserId(userId);
        transportation.setName("Transportation");
        transportation.setType(Category.CategoryType.EXPENSE);
        transportation.setColor("#A78BFA"); // Purple
        defaultCategories.add(transportation);

        // Entertainment (EXPENSE)
        Category entertainment = new Category();
        entertainment.setUserId(userId);
        entertainment.setName("Entertainment");
        entertainment.setType(Category.CategoryType.EXPENSE);
        entertainment.setColor("#F472B6"); // Pink
        defaultCategories.add(entertainment);

        // Healthcare (EXPENSE)
        Category healthcare = new Category();
        healthcare.setUserId(userId);
        healthcare.setName("Healthcare");
        healthcare.setType(Category.CategoryType.EXPENSE);
        healthcare.setColor("#EF4444"); // Red
        defaultCategories.add(healthcare);

        // Rent (EXPENSE)
        Category rentExpense = new Category();
        rentExpense.setUserId(userId);
        rentExpense.setName("Rent");
        rentExpense.setType(Category.CategoryType.EXPENSE);
        rentExpense.setColor("#3B82F6"); // Blue
        defaultCategories.add(rentExpense);

        categoryRepository.saveAll(defaultCategories);
    }
}
