package com.upply.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor

@RestController
@RequestMapping("/user")
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Return authenticated user data"
    )
    public ResponseEntity<UserResponse> getUser(){
        return ResponseEntity.ok(userService.getUser());
    }

    @PutMapping
    @Operation(
            summary = "Update user data",
            description = "Updates an existing user's information in the system",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserRequest userRequest){
        userService.updateUser(userRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
