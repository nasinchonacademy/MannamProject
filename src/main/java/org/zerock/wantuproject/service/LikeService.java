package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.wantuproject.controller.MeetingRoomController;
import org.zerock.wantuproject.entity.MeetingRoomLike;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.LikeRepository;
import org.zerock.wantuproject.repository.MeetingRoomRepository;
import org.zerock.wantuproject.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MeetingRoomController.LikeResponse toggleLike(Long roomId, Authentication authentication) {
        // 로그인한 사용자 가져오기
        String username = authentication.getName();
        User user = userRepository.findByUid(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        MeetingRoom meetingRoom = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임방이 존재하지 않습니다."));

        Optional<MeetingRoomLike> likeOptional = likeRepository.findByUserAndMeetingRoom(user, meetingRoom);
        boolean isLiked;

        if (likeOptional.isPresent()) {
            // 이미 좋아요를 눌렀다면 좋아요를 취소
            likeRepository.delete(likeOptional.get());
            meetingRoom.setLikes(meetingRoom.getLikes() - 1);
            isLiked = false;
        } else {
            // 좋아요를 누르지 않았다면 추가
            MeetingRoomLike like = MeetingRoomLike.builder()
                    .user(user)
                    .meetingRoom(meetingRoom)
                    .build();
            likeRepository.save(like);
            meetingRoom.setLikes(meetingRoom.getLikes() + 1);
            isLiked = true;
        }

        int updatedLikesCount = meetingRoom.getLikes();
        return new MeetingRoomController.LikeResponse(isLiked, updatedLikesCount);
    }

    public int getLikesCount(Long roomId) {
        return meetingRoomRepository.findById(roomId)
                .map(MeetingRoom::getLikes)
                .orElse(0);
    }
}
