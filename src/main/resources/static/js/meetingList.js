// 전역 변수로 선언된 지도 객체
var map;

document.addEventListener("DOMContentLoaded", function() {
    // 서버에서 유저의 주소 좌표를 받아 초기화
    $.ajax({
        url: '/getUserLocation', // 유저 위치 정보를 가져오는 엔드포인트
        type: 'GET',
        success: function (data) {
            const { latitude, longitude } = data;

            // 지도 초기화
            var mapContainer = document.getElementById('map'),
                mapOption = {
                    center: new kakao.maps.LatLng(latitude, longitude), // 유저 주소 기반 좌표로 초기화
                    level: 3 // 확대 수준
                };
            map = new kakao.maps.Map(mapContainer, mapOption); // 전역 변수 map을 초기화

            // 서버에서 마커 정보를 불러와 지도에 마커 생성
            loadMarkers();

            // 지도 이동 시 "재검색" 버튼 표시
            kakao.maps.event.addListener(map, 'dragend', function() {
                var reSearchButton = document.getElementById("reSearchButton");
                if (reSearchButton) {
                    reSearchButton.style.display = "block";  // 재검색 버튼 표시
                }
            });
        },
        error: function (error) {
            console.error('유저 위치를 가져오는 데 실패했습니다:', error);
        }
    });
});

// 마커를 지도에 표시하는 함수
function loadMarkers() {
    $.ajax({
        url: '/getAllMeetingRooms', // 모든 채팅방 데이터를 가져오는 엔드포인트
        type: 'GET',
        success: function (data) {
            data.forEach(function (meetingRoom) {
                var markerPosition = new kakao.maps.LatLng(meetingRoom.latitude, meetingRoom.longitude);

                var marker = new kakao.maps.Marker({
                    map: map, // 지도에 마커 표시
                    position: markerPosition,
                    title: meetingRoom.title
                });

                // 마커 클릭 이벤트 등록
                kakao.maps.event.addListener(marker, 'click', function() {
                    // 마커 클릭 시 AJAX로 해당 좌표에 있는 채팅방 목록 불러오기
                    loadMeetingRoomsByMarker(meetingRoom.latitude, meetingRoom.longitude);
                });
            });
        },
        error: function (error) {
            console.error('채팅방 데이터 불러오기 실패:', error);
        }
    });
}

// 마커 클릭 시 채팅방 목록 불러오기
function loadMeetingRoomsByMarker(latitude, longitude) {
    $.ajax({
        url: '/getMeetingRoomsByLocation',
        type: 'GET',
        data: { latitude: latitude, longitude: longitude },
        success: function (data) {
            let chatRoomListHtml = '';

            data.forEach(function (meetingRoom,index) {
                chatRoomListHtml += `
                    <div class="meeting-room">
                        <div class="list" id="list-img">                
                            <img src="/files?fileName=${meetingRoom.imagePath}" alt="Room Image" width="100">
                        </div> 
                        <div class="list" id="list-title-interests">   
                             <h3 class="room-title"><a href="/meetingRoomDetail/${meetingRoom.id}">${meetingRoom.title}</a></h3>                                                   
                            <p>관심사: ${meetingRoom.interests.join(' ')}</p>
                         </div>  
                         <div class="list" id="list-info">  
                            <p>참여 인원: ${meetingRoom.participantsCount}</p>                      
                            <p>좋아요 수: ${meetingRoom.likes}</p>
                         </div> 
                    </div>
                   `;

                // 마지막 항목에는 <hr> 태그 추가하지 않음
                if (index < data.length - 1) {
                    chatRoomListHtml += '<hr>';
                }
            });

            $('#chatRoomList').html(chatRoomListHtml);
        },
        error: function (error) {
            console.error('채팅방 목록 불러오기 실패:', error);
        }
    });
}

function searchMeetingRooms(page = 0) {
    let query = document.getElementById("searchQuery").value.trim();
    const searchCategory = document.querySelector('input[name="searchCategory"]:checked').value;
    const center = map.getCenter();
    const latitude = center.getLat();
    const longitude = center.getLng();
    let title = "";
    let interest = "";

    // 검색어 초기화 또는 카테고리에 따른 처리
    if (!query) {
        query = "";
    } else if (searchCategory === "interests" && !query.startsWith("#")) {
        query = "#" + query;
    }

    // 카테고리에 따른 검색어 설정
    if (searchCategory === "title") {
        title = query;
    } else if (searchCategory === "interests") {
        interest = query;
    }

    let params = {
        title: title || null,
        interest: interest || null,
        latitude: latitude,
        longitude: longitude,
        page: page
    };

    // Ajax 요청을 통해 JSON 데이터를 가져옴
    $.ajax({
        url: '/searchMeetingRooms',
        type: 'GET',
        data: params,
        success: function (response) {
            // 응답에서 회의방 데이터와 페이징 정보 추출
            let meetingRooms = response.meetingRooms;
            let totalPages = response.totalPages;
            let currentPage = response.currentPage;

            let chatRoomListHtml = '';

            // 만약 회의방이 없을 경우 처리
            if (meetingRooms.length === 0) {
                $('#chatRoomList').html('<p>No meeting rooms found.</p>');
                return;
            }

            // 각 채팅방 데이터를 처리하여 HTML로 생성
            meetingRooms.forEach(function (meetingRoom, index) {
                chatRoomListHtml += `
                        <div class="meeting-room">
                            <div id="list-left">
                                <div class="list" id="list-img">                
                                    <img src="/files?fileName=${meetingRoom.imagePath}" alt="Room Image" width="88">
                                </div> 
                                <div class="list" id="list-title-interests">   
                                    <h3 class="room-title"><a href="/meetingRoomDetail/${meetingRoom.id}">${meetingRoom.title}</a></h3>                                                   
                                    <p>${meetingRoom.interests.join(' ')}</p>
                                </div>
                            </div>  
                            <div class="list" id="list-info">  
                                <p>참여 인원: ${meetingRoom.participantsCount}</p>                      
                                <p>좋아요 수: ${meetingRoom.likes}</p>
                            </div> 
                        </div>
                    `;

                // 마지막 항목에는 <hr> 태그 추가하지 않음
                if (index < meetingRooms.length - 1) {
                    chatRoomListHtml += '<hr>';
                }
            });


            // 생성된 HTML을 chatRoomList에 삽입
            $('#chatRoomList').html(chatRoomListHtml);

            // 페이징 버튼 생성 및 표시
            let paginationHtml = generatePaginationHtml(currentPage, totalPages);
            $('#pagination').html(paginationHtml);
        },
        error: function (error) {
            console.error('Error fetching meeting rooms:', error);
        }
    });
}

function generatePaginationHtml(currentPage, totalPages) {
    let paginationHtml = '<div class="pagination-container">';

    // 이전 버튼
    if (currentPage > 0) {
        paginationHtml += `<button class="pagination-btn" onclick="searchMeetingRooms(${currentPage - 1})">이전</button>`;
    }

    // 페이지 번호
    for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
            paginationHtml += `<button class="pagination-btn active" onclick="searchMeetingRooms(${i})">${i + 1}</button>`;
        } else {
            paginationHtml += `<button class="pagination-btn" onclick="searchMeetingRooms(${i})">${i + 1}</button>`;
        }
    }

    // 다음 버튼
    if (currentPage < totalPages - 1) {
        paginationHtml += `<button class="pagination-btn" onclick="searchMeetingRooms(${currentPage + 1})">다음</button>`;
    }

    paginationHtml += '</div>';
    return paginationHtml;
}

// "재검색" 버튼 클릭 시 현재 위치 기준으로 채팅방 다시 검색
function reSearchMeetingRooms() {
    if (typeof map !== 'undefined') {
        const center = map.getCenter();  // 현재 지도 중심 좌표
        const latitude = center.getLat();
        const longitude = center.getLng();

        $.ajax({
            url: '/getNearbyMeetingRooms',
            type: 'GET',
            data: {
                latitude: latitude,
                longitude: longitude
            },
            success: function (data) {
                $('#chatRoomList').html(data);  // 새로운 결과로 채팅방 리스트 업데이트
            },
            error: function (error) {
                console.error('채팅방 검색 실패:', error);
            }
        });

        // 재검색 버튼 숨기기
        document.getElementById("reSearchButton").style.display = "none";
    } else {
        console.error('지도가 아직 초기화되지 않았습니다.');
    }
    }