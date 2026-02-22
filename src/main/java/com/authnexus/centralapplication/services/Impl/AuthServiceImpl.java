package com.authnexus.centralapplication.services.Impl;

import com.authnexus.centralapplication.domains.dto.UserDto;
import com.authnexus.centralapplication.services.AuthService;
import com.authnexus.centralapplication.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        //todo: (verify email and password strength)
        // todo : role assignation
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        UserDto userDto1= userService.createUser(userDto);
        return userDto1;

    }
}
