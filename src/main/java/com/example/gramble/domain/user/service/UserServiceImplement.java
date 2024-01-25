package com.example.gramble.domain.user.service;

import com.example.gramble.domain.user.entity.UserEntity;
import com.example.gramble.domain.user.repository.UserRepository;
import com.example.gramble.domain.user.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto register(UserDto.Registration registration) {
        userRepository.findByUsernameOrEmail(
                registration.getUsername(),
                registration.getEmail()
        ).stream().findAny().ifPresent(userEntity -> {
            throw new UserAlreadyExistsException();
        });

        UserEntity userEntity = UserEntity.builder().username(registration.getUsername())
                .email(registration.getEmail())
                .password(passwordEncoder.encode(registration.getPassword())).bio("").image("").build();
        userRepository.save(userEntity);
        return convertEntityToDto(userEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto login(UserDto.Login login) {
        UserEntity userEntity = userRepository.findByEmail(login.getEmail())
                .filter(userEntity1 -> passwordEncoder.matches(login.getPassword(), userEntity1.getPassword()))
                .orElseThrow(() -> new UserNotFoundException(Error));
        return convertEntityToDto(userEntity);
    }

    @Override
    public UserDto update(UserDto.Update update, UserDto.Update authUserDetails) {
        UserEntity userEntity = userRepository.findById(authUserDetails.getId())
                .orElseThrow(() -> new UserNotFoundException(Error));

        if (update.getUsername() != null) {
            userRepository.findByUsername(update.getUsername())
                    .filter(found -> !found.getId().equals(userEntity.getId()))
                    .ifPresent(found -> {
                        throw new UserAlreadyExistsException();
                    });
            userEntity.setUsername(update.getUsername());
        }

        if (update.getEmail() != null) {
            userRepository.findByEmail(update.getEmail())
                    .filter(found -> !found.getId().equals(userEntity.getId()))
                    .ifPresent(found -> {
                        throw new UserAlreadyExistsException();
                    });
            userEntity.setEmail(update.getEmail());
        }

        if (update.getPassword() != null) {
            userEntity.setPassword(passwordEncoder.encode(update.getPassword()));
        }

        if (update.getBio() != null) {
            userEntity.setBio(update.getBio());
        }

        if (update.getImage() != null) {
            userEntity.setImage(update.getImage());
        }

        userRepository.save(userEntity);
        return convertEntityToDto(userEntity);
    }
}
