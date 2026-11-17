package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.Basket;
import com.bookworm.model.GiftPoints;
import com.bookworm.model.User;
import com.bookworm.repository.BasketRepository;
import com.bookworm.repository.GiftPointsRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BasketRepository basketRepository;
    private final GiftPointsRepository giftPointsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       BasketRepository basketRepository,
                       GiftPointsRepository giftPointsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.basketRepository = basketRepository;
        this.giftPointsRepository = giftPointsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("User with email " + req.email() + " already exists.");
        }
        User user = new User(req.name(), req.email(), passwordEncoder.encode(req.password()), req.phone());
        user = userRepository.save(user);

        // Initialize empty basket
        Basket basket = new Basket(user);
        basketRepository.save(basket);

        // Initialize 100 sign-up bonus gift points
        GiftPoints giftPoints = new GiftPoints(user, 100);
        giftPointsRepository.save(giftPoints);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
        return new AuthResponse(token, "Bearer", jwtUtils.getExpirationMs() / 1000, profile);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
        return new AuthResponse(token, "Bearer", jwtUtils.getExpirationMs() / 1000, profile);
    }
}
