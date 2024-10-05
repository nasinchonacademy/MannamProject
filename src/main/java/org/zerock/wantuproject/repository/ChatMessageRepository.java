package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.ChatMessage;
import org.zerock.wantuproject.entity.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

//    // 특정 채팅방의 메시지를 시간 순으로 가져오기
//    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // roomId 타입을 String에서 Long으로 변경
    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomRoomid(Long roomId);
}
