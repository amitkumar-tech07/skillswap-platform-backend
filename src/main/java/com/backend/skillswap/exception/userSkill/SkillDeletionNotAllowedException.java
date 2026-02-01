package com.backend.skillswap.exception.userSkill;

public class SkillDeletionNotAllowedException extends RuntimeException {
    public SkillDeletionNotAllowedException(String message) {
        super(message);
    }
}
