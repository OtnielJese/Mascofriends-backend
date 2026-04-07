package com.vetivet.service;

import com.vetivet.dto.*;
import com.vetivet.model.Role;
import com.vetivet.model.Role.ERole;
import com.vetivet.model.User;
import com.vetivet.repository.*;
import com.vetivet.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtTokenProvider.generateToken(auth);

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow();

        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        var rtEntity = refreshTokenService.validateRefreshToken(refreshToken);
        User user = rtEntity.getUser();

        String newAccessToken = jwtTokenProvider.generateTokenFromUser(user);

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }

    @Transactional
    public void registerUser(String username, String email, String password,
                             String firstName, String lastName, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                    .orElseThrow(() -> new RuntimeException("Rol cliente no encontrado"));
            roles.add(clientRole);
        } else {
            for (String rn : roleNames) {
                ERole erole = ERole.valueOf(rn);
                Role r = roleRepository.findByName(erole)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rn));
                roles.add(r);
            }
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .active(true)
                .build();

        userRepository.save(user);
    }
}
