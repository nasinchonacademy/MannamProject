package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.dto.ChatMessageDTO;
import org.zerock.wantuproject.entity.ProfileImage;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.ChatMessageService;
import org.zerock.wantuproject.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController  // RestController로 JSON 반환
@RequiredArgsConstructor
public class ChatController {

    private final UserService userService;
    private final ChatMessageService chatMessageService;

    // 특정 채팅방의 메시지 목록을 조회하는 API
    @GetMapping("/chat/messages/{roomId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable Long roomId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByRoomId(roomId);
        System.out.println("조회된 메시지: " + messages);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/chat/{roomId}/{otherUserId}")
    public String getChatRoom(@PathVariable Long roomId, @PathVariable Long otherUserId, Model model) {
        // 로그 객체 생성
        Logger logger = LoggerFactory.getLogger(getClass());

        // 상대방 사용자 정보 조회
        User otherUser = userService.findById(otherUserId);
        if (otherUser == null) {
            logger.warn("사용자를 찾을 수 없습니다: " + otherUserId);
            // 기본적으로 다른 사용자가 없을 때 처리할 내용 추가
            // 예를 들어, 빈 객체를 반환하거나, 오류 페이지로 이동하는 로직 추가
            return "error/user-not-found";  // 사용자를 찾지 못한 경우, 에러 페이지로 리다이렉트할 수 있습니다.
        }

        // User 엔티티를 DTO로 변환
        AddUserRequest otherUserDto = userService.convertToDto(otherUser);

        // 프로필 이미지 경로 추가
        List<String> profileImageUrls = otherUser.getProfileImages() != null
                ? otherUser.getProfileImages().stream()
                .map(ProfileImage::getImageUrl)
                .collect(Collectors.toList())
                : Collections.emptyList();

        System.out.println("------------------------------------------------------------------------------"+roomId);

        model.addAttribute("otherUser", otherUserDto);
        model.addAttribute("profileImageUrls", profileImageUrls); // 이미지 경로 추가
        model.addAttribute("roomId", roomId);  // 채팅방 ID 전달
        model.addAttribute("otherUserId", otherUserId);

        return "chat/chatroom";
    }
}