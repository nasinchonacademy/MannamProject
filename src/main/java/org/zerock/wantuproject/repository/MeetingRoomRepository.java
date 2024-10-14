package org.zerock.wantuproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.wantuproject.dto.MeetingRoomDto;
import org.zerock.wantuproject.entity.MeetingRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    // 좌표 기반으로 채팅방 목록을 페이징하여 조회
    Page<MeetingRoom> findByLatitudeAndLongitude(double latitude, double longitude, Pageable pageable);

    // 20km 이내의 채팅방 검색 (위도, 경도를 기준으로)
    @Query("SELECT m FROM MeetingRoom m WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(m.latitude)) * " +
            "cos(radians(m.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(m.latitude)))) <= :distance")
    Page<MeetingRoom> findByLocationWithin(double latitude, double longitude, double distance, Pageable pageable);

    @Query("SELECT m FROM MeetingRoom m WHERE " +
            "m.title LIKE %:title% AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(m.latitude)) * " +
            "cos(radians(m.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(m.latitude)))) <= :distance")
    Page<MeetingRoom> findByTitleContainingAndLocationWithin(@Param("title") String title,
                                                             @Param("latitude") double latitude,
                                                             @Param("longitude") double longitude,
                                                             @Param("distance") double distance,
                                                             Pageable pageable);

    @Query("SELECT m FROM MeetingRoom m WHERE :interest IN elements(m.interests) AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(m.latitude)) * " +
            "cos(radians(m.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(m.latitude)))) <= :distance")
    Page<MeetingRoom> findByInterestsContainingAndLocationWithin(@Param("interest") String interest,
                                                                 @Param("latitude") double latitude,
                                                                 @Param("longitude") double longitude,
                                                                 @Param("distance") double distance,
                                                                 Pageable pageable);


    // 제목으로 검색 (대소문자 구분 없이)
    Page<MeetingRoom> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 관심사로 검색 (대소문자 구분 없이)
    Page<MeetingRoom> findByInterestsContainingIgnoreCase(String interest, Pageable pageable);

    @Query("SELECT new org.zerock.wantuproject.dto.MeetingRoomDto(m.id, m.title, m.imagePath, m.interests, size(m.participants), m.likes, m.address, m.description) " +
            "FROM MeetingRoom m WHERE m.id = :roomId")
    MeetingRoomDto findMeetingRoomDtoById(@Param("roomId") Long roomId);

    @EntityGraph(attributePaths = {"participants", " user"})
    @Query("SELECT m FROM MeetingRoom m LEFT JOIN FETCH m.participants WHERE m.id = :roomId")
    Optional<MeetingRoom> findByIdWithParticipants(@Param("roomId") Long roomId);

    @Modifying
    @Transactional
    @Query("UPDATE MeetingRoom m SET m.likes = m.likes + 1 WHERE m.id = :roomId")
    void incrementLikes(Long roomId);

    @Modifying
    @Transactional
    @Query("UPDATE MeetingRoom m SET m.likes = m.likes - 1 WHERE m.id = :roomId")
    void decrementLikes(Long roomId);
}


