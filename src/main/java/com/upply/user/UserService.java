package com.upply.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUser(){
        return userRepository.getCurrentUser()
                .map(userMapper::toUserResponse)
                .orElseThrow(()-> new EntityNotFoundException());
    }

    public User updateUser(UserRequest userRequest){
        User user = userRepository.getCurrentUser()
                .orElseThrow(()-> new EntityNotFoundException());

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setUniversity(userRequest.university());

        return userRepository.save(user);

    }
}
