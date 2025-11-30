package com.upply.user;

import com.upply.skill.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserMapper userMapper;
    private final SkillMapper skillMapper;

    public UserResponse getUser() {
        return userRepository.getCurrentUser()
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new EntityNotFoundException());
    }

    public void updateUser(UserRequest userRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setUniversity(userRequest.university());

        userRepository.save(user);
    }

    @Transactional
    public void addSkillToUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill Not found"));

        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void addSkillByName(SkillRequest skillRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        String normalizedName =
                ((skillRequest.getSkillName() == null) ? null : skillRequest.getSkillName().toLowerCase().replaceAll("\\s+", ""));
        Skill skill = skillRepository.findSkillByName(normalizedName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillRequest.getSkillName());
                    newSkill.setCategory(skillRequest.getSkillCategory());
                    return skillRepository.save(newSkill);
                });
        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void removeSkillFromUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill Not found"));

        user.getUserSkills().remove(skill);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<SkillResponse> getUserSkills(){
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        return user.getUserSkills().stream()
                .map(skillMapper::toSkillResponse)
                .collect(Collectors.toSet());
    }


}
