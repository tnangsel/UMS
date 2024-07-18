package com.tenzin.utility;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
	
	List<String> openApiEndPoints = List.of("api/v1/auth/**");
	
	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndPoints.stream()
											.noneMatch(uri -> request.getURI().getPath().contains(uri));

}
			