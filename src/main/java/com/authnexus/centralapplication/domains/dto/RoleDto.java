package com.authnexus.centralapplication.domains.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    private UUID id ;
    private String name; // ADMIN USER GUEST
}
