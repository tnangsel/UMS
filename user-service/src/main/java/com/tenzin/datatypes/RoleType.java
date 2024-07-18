package com.tenzin.datatypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum RoleType {

	ADMIN(Set.of(
            Permission.ADMIN_READ,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_DELETE,
            Permission.ADMIN_CREATE,
            Permission.MANAGER_READ,
            Permission.MANAGER_UPDATE,
            Permission.MANAGER_DELETE,
            Permission.MANAGER_CREATE,
            Permission.USER_CREATE,
			Permission.USER_READ,
			Permission.USER_UPDATE,
			Permission.USER_DELETE
    )),
    MANAGER(Set.of(
            Permission.MANAGER_READ,
            Permission.MANAGER_UPDATE,
            Permission.MANAGER_DELETE,
            Permission.MANAGER_CREATE,
            Permission.USER_CREATE,
			Permission.USER_READ,
			Permission.USER_UPDATE,
			Permission.USER_DELETE
    )),
    USER(Set.of(
			Permission.USER_CREATE,
			Permission.USER_READ,
			Permission.USER_UPDATE,
			Permission.USER_DELETE
	));

	@Getter
	private final Set<Permission> permissions;
	
	public List<SimpleGrantedAuthority> getAuthorities() {
		var authorities = getPermissions()
	            .stream()
	            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
	            .collect(Collectors.toList());
	    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
	    return authorities;
	}
}
