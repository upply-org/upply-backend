package com.upply.common;

public class NormalizeSkillName {

    public static String normalizeSkill(String skillName) {
        if (skillName == null) {
            return null;
        }

        return skillName.toLowerCase()
                .replaceAll("[\\s.\\-_/]", "");
    }
}
