package com.upply.user;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String university;
}
