package com.authnexus.centralapplication.services;

import com.authnexus.centralapplication.domains.dto.UserDto;

public interface AuthService {

    UserDto registerUser(UserDto userDto);
    //todo:  login refresh token

    // refreshToken -> generate new access token using refresh token -> stores refresh token in DB

}
