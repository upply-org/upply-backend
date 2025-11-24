package com.upply.user;

import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User toUser(UserRequest userRequest){
        return User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .university(userRequest.university())
                .build();
    }

    public  UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .university(user.getUniversity())
                .build();
    }
}
