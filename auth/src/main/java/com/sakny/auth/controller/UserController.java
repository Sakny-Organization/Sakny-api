package com.sakny.auth.controller;

import com.sakny.auth.service.UserService;
import com.sakny.common.dto.ApiResponse;
import com.sakny.common.dto.LocationRequest;
import com.sakny.common.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@Valid @RequestBody LocationRequest request) {
        userService.updateUserLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
