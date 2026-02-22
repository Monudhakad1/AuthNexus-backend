package com.authnexus.centralapplication.Mapper;

import com.authnexus.centralapplication.domains.dto.UserDto;
import com.authnexus.centralapplication.domains.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" ,unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);
}
