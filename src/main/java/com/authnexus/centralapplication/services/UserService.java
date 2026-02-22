package com.authnexus.centralapplication.services;

import com.authnexus.centralapplication.domains.dto.UserDto;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto , String userId);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    // get all users
    Iterable<UserDto> getAllUsers();



}
