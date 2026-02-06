package com.sobolev.travel.service;

import com.sobolev.travel.dto.user.UserDto;
import com.sobolev.travel.dto.user.UserProfileDto;
import com.sobolev.travel.entity.User;
import com.sobolev.travel.entity.UserProfile;
import com.sobolev.travel.exception.ConflictException;
import com.sobolev.travel.exception.ResourceNotFoundException;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servizio che gestisce le operazioni legate agli utenti (ricerca profilo e update del profilo).
 *
 * Contiene logica di business per validare l'unicità di username/email in modo case-insensitive
 * e per aggiornare il profilo utente (UserProfile) creando l'entity se non presente.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper mapper;

    public UserService(UserRepository userRepository, EntityMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * Restituisce i dati dell'utente corrente come DTO.
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapper.toUserDto(user);
    }

    /**
     * Aggiorna il profilo dell'utente.
     *
     * Logica importante:
     * - Verifica che username/email non siano già in uso da un altro utente in modo case-insensitive.
     * - Crea il `UserProfile` se non esiste.
     */
    @Transactional
    public UserDto updateProfile(Integer userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Update username if provided and different (case-insensitive comparison)
        if (profileDto.username() != null && !profileDto.username().equalsIgnoreCase(user.getUsername())) {
            // Check if username already exists for another user (case-insensitive)
            Optional<User> existingUser = userRepository.findByUsernameIgnoreCase(profileDto.username());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new ConflictException("Username already exists");
            }
            user.setUsername(profileDto.username());
        }

        // Update email if provided and different (case-insensitive comparison)
        if (profileDto.email() != null && !profileDto.email().equalsIgnoreCase(user.getEmail())) {
            // Check if email already exists for another user (case-insensitive)
            Optional<User> existingUser = userRepository.findByEmailIgnoreCase(profileDto.email());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(profileDto.email());
        }

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (profileDto.firstName() != null) {
            profile.setFirstName(profileDto.firstName());
        }
        if (profileDto.lastName() != null) {
            profile.setLastName(profileDto.lastName());
        }
        if (profileDto.preferredUnits() != null) {
            profile.setPreferredUnits(profileDto.preferredUnits());
        }

        user = userRepository.save(user);
        return mapper.toUserDto(user);
    }
}
