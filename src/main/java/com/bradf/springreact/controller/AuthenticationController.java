package com.bradf.springreact.controller;

import com.bradf.springreact.config.JwtConfig;
import com.bradf.springreact.exception.AppException;
import com.bradf.springreact.exception.BadRequestException;
import com.bradf.springreact.model.RefreshToken;
import com.bradf.springreact.model.Role;
import com.bradf.springreact.model.RoleName;
import com.bradf.springreact.model.User;
import com.bradf.springreact.payload.*;
import com.bradf.springreact.repository.RefreshTokenRepository;
import com.bradf.springreact.repository.RoleRepository;
import com.bradf.springreact.repository.UserRepository;
import com.bradf.springreact.security.JwtTokenProvider;
import com.bradf.springreact.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    private final JwtConfig jwtConfig;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal)authentication.getPrincipal();

        String jwt = tokenProvider.generateToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken();

        this.saveRefreshToken(userPrincipal, jwt, refreshToken);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    private void saveRefreshToken(UserPrincipal userPrincipal, String accessToken, String refreshToken) {
        RefreshToken refresh = new RefreshToken();
        refresh.setAccessToken(accessToken);
        refresh.setRefreshToken(refreshToken);
        refresh.setUser(this.userRepository.getOne(userPrincipal.getId()));

        //TODO: Use clock injected.
        Instant expirationDateTime = Instant.now().plus(this.jwtConfig.getRefreshExpiration());
        refresh.setExpirationDateTime(expirationDateTime);

        // TODO: Beef this up to look by device and other info potentially.
        // Does a refresh token already exist for this user?  If so delete it
        this.refreshTokenRepository.findByUserIdIs(userPrincipal.getId())
                .ifPresent(this.refreshTokenRepository::delete);

        this.refreshTokenRepository.save(refresh);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    // This mapping is important and is linked to the configuration property(application.jwt.refreshPath).  I tried to make this dynamic and failed
    @PostMapping("/refresh")
    @PreAuthorize("hasRole(@jwtConfig.refreshSystemRole)")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
        // Assume proper header since got here already
        String accessToken = authHeader.substring(7);

        Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository.findByAccessTokenIs(accessToken);
        if(optionalRefreshToken.isPresent()){
            RefreshToken existing = optionalRefreshToken.get();
            RefreshToken newRefresh = new RefreshToken();
            newRefresh.setExpirationDateTime(existing.getExpirationDateTime());
            //TODO: Do not use the existing user, lookup the user again, roles may have changed, could be inactive.
            newRefresh.setUser(existing.getUser());
            newRefresh.setRefreshToken(existing.getRefreshToken());
            newRefresh.setAccessToken(this.tokenProvider.generateToken(UserPrincipal.create(existing.getUser())));

            this.refreshTokenRepository.delete(existing);
            this.refreshTokenRepository.save(newRefresh);
            return ResponseEntity.ok(new JwtAuthenticationResponse(newRefresh.getAccessToken()));
        } else {
            throw new BadRequestException("Invalid Request Token");
        }
    }
}
