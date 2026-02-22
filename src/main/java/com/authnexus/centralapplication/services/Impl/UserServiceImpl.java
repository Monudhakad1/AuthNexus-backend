package com.authnexus.centralapplication.services.Impl;

import com.authnexus.centralapplication.Helper.UserHelper;
import com.authnexus.centralapplication.Mapper.UserMapper;
import com.authnexus.centralapplication.domains.dto.UserDto;
import com.authnexus.centralapplication.domains.entities.Provider;
import com.authnexus.centralapplication.domains.entities.User;
import com.authnexus.centralapplication.exception.ResourceNotFoundException;
import com.authnexus.centralapplication.repository.UserRepository;
import com.authnexus.centralapplication.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        User newUser = userMapper.toUser(userDto);
        newUser.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        User savedUser = userRepository.save(newUser);

        //todo: role assign auth

        return userMapper.toUserDto(savedUser);

    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uuid = UUID.fromString(userId);
        User existingUser = userRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        // Update fields
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getImageUrl() != null) {
            existingUser.setImageUrl(userDto.getImageUrl());
        }
        if (userDto.getProvider() != null) {
            existingUser.setProvider(userDto.getProvider());
        }
        //todo : password encryption
        if (userDto.getPassword() != null) {
            existingUser.setPassword(userDto.getPassword());
        }
        existingUser.setEnable(userDto.isEnable());
        existingUser.setUpdatedAt(Instant.now());
        return userMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        if (!userRepository.existsById(uid)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(uid);

    }

    @Override
    public UserDto getUserById(String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toUserDto(user);

    }

    @Override
    public Iterable<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDto).toList();
    }
}
