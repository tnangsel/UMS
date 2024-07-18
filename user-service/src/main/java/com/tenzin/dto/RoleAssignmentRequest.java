package com.tenzin.dto;

import java.util.Set;

import com.tenzin.datatypes.Permission;
import com.tenzin.datatypes.RoleType;

import lombok.Data;

@Data
public class RoleAssignmentRequest {
	private RoleType role;
    private Set<Permission> permissions;
}
