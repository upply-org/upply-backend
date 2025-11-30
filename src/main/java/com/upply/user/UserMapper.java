package com.upply.user;

import com.upply.skill.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final SkillMapper skillMapper;
    public  UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .university(user.getUniversity())
                .skills(user.getUserSkills().stream()
                        .map(skillMapper::toSkillResponse)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
