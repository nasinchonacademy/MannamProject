package org.zerock.wantuproject.dto;

import org.zerock.wantuproject.entity.User;

public class UserProfileDto {
    private User user;
    private int likeCount;

    public UserProfileDto(User user, int likeCount) {
        this.user = user;
        this.likeCount = likeCount;
    }

    public User getUser() {
        return user;
    }

    public int getLikeCount() {
        return likeCount;
    }
}

