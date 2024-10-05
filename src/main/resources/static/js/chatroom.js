let roomId = document.getElementById('chatRoom').getAttribute('data-room-id');
let otherUserId = document.getElementById('chatRoom').getAttribute('data-other-user-id');
loggedInUserId = document.getElementById('chatRoom').getAttribute('data-user-id');
let stompClient = null;
let lastSenderId = null;

console.log("Room ID:", roomId, "Other User ID:", otherUserId, "Logged in User ID:", loggedInUserId);

function connect() {
    if (stompClient !== null) {
        stompClient.disconnect(); // 기존 WebSocket 연결을 끊음
    }

    console.log("roomId로 WebSocket 연결 시도:", roomId);
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 서버에 연결되었습니다, frame:', frame);

        // 새로운 채팅방에 대한 구독 설정
        stompClient.subscribe(`/topic/chat/${roomId}`, function (messageOutput) {
            console.log("받은 메시지:", messageOutput.body);
            const message = JSON.parse(messageOutput.body);
            showMessage(message);
        });
    });
}

function findAnotherUser() {
    // 서버로 새로운 상대 찾기 요청
    fetch(`/chat/findAnotherUser/${loggedInUserId}`, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('새로운 상대를 찾을 수 없습니다. 다시 시도해 주세요.');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.roomid) {
                // 새로운 채팅방 정보로 UI 및 WebSocket, 채팅 기록 업데이트
                updateChatRoom(data.roomid, data.otherUser, data.profileImageUrls);
            } else {
                console.error("새로운 상대를 찾을 수 없습니다.");
            }
        })
        .catch(error => {
            alert(error.message);  // 사용자에게 오류 메시지 표시
            console.error('Error finding another user:', error);
        });
}

function updateChatRoom(newRoomId, otherUser, profileImageUrls) {
    // 채팅방 ID 업데이트
    roomId = newRoomId; // 전역 변수 roomId 업데이트
    document.getElementById('chatRoom').setAttribute('data-room-id', roomId);

    // WebSocket 재연결
    connect();  // 새로운 roomId에 맞게 WebSocket 연결 재설정

    // 새로운 채팅방의 채팅 기록 불러오기
    loadChatHistory();

    // 상대방 프로필 업데이트
    updateChatRoomInfo(otherUser, roomId, profileImageUrls);
}


function updateChatRoomInfo(otherUser, roomId, profileImageUrls) {
    // 채팅방 ID 업데이트
    document.getElementById('chatRoom').setAttribute('data-room-id', roomId);

    // 상대방 이름 업데이트
    document.querySelector('.profile-info h3').textContent = otherUser.name;

    // 생일 및 주소 업데이트
    document.getElementById('user-info').setAttribute('data-birth-date', otherUser.birthDate);
    const birthDate = new Date(otherUser.birthDate);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    if (today.getMonth() < birthDate.getMonth() || (today.getMonth() === birthDate.getMonth() && today.getDate() < birthDate.getDate())) {
        age--;
    }
    document.getElementById('user-info').textContent = `${otherUser.address.split(' ')[0]} | ${age}세`;

    // 프로필 이미지 슬라이드 업데이트
    const profileImagesContainer = document.querySelector('.profile-images');
    profileImagesContainer.innerHTML = ''; // 기존 이미지 제거
    profileImageUrls.forEach((imageUrl, index) => {
        const imgElement = document.createElement('img');
        imgElement.src = `/files?fileName=${imageUrl}`;
        imgElement.alt = "상대방 프로필 사진";
        imgElement.classList.add('profile-pic');
        if (index === 0) imgElement.classList.add('active'); // 첫 번째 이미지는 활성화
        profileImagesContainer.appendChild(imgElement);
    });

    // 슬라이드 초기화
    currentIndex = 0;
    updateSlide();
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const messageText = messageInput.value.trim();

    console.log("메시지 전송, roomId:", roomId, "내용:", messageText);

    if (messageText && stompClient) {
        stompClient.send("/app/chat/" + roomId + "/sendMessage", {}, JSON.stringify({
            roomId: roomId,  // 서버에서 인증된 사용자 정보와 함께 메시지를 보냄
            content: messageText ,    // 메시지 내용만 전송
        }));
        messageInput.value = ''; // 입력창 비우기
    }
}

/// 수신된 메시지를 화면에 표시하는 함수
function showMessage(message) {
    console.log("Received message:", message);
    const chatMessages = document.getElementById('chatRoom');

    // 새로운 메시지 요소를 생성
    const messageElement = document.createElement('div');

    // 내 메시지인지 확인하여 클래스 적용
    if (message.sender === loggedInUserId) {
        messageElement.classList.add('message', 'user1'); // 내가 보낸 메시지 (오른쪽 정렬)
    } else {
        messageElement.classList.add('message', 'user2'); // 상대방 메시지 (왼쪽 정렬)
    }

    // 연속된 동일 발신자의 경우 이름 생략
    if (message.sender !== lastSenderId) {
        const senderName = message.senderName ? message.senderName : "알 수 없는 사용자";
        messageElement.innerHTML = `<span>${senderName}</span><p>${message.content}</p>`;
    } else {
        messageElement.innerHTML = `<p>${message.content}</p>`;
    }

    // 생성한 메시지 요소를 채팅창에 추가
    chatMessages.appendChild(messageElement);

    // 새로운 메시지가 추가될 때 스크롤을 최신 메시지로 이동
    chatMessages.scrollTop = chatMessages.scrollHeight;

    // 마지막 발신자 ID 업데이트
    lastSenderId = message.sender;
}



function loadChatHistory() {
    fetch(`/chat/messages/${roomId}`)  // 새로운 채팅방 ID로 메시지 조회 API 호출
        .then(response => response.json())
        .then(messages => {
            lastSenderId = null; // 채팅 기록 로드 전 발신자 초기화
            const chatMessages = document.getElementById('chatRoom');
            chatMessages.innerHTML = ''; // 기존 채팅 기록 삭제
            messages.forEach(showMessage);  // 불러온 메시지를 화면에 표시
        })
        .catch(error => console.error('Error loading chat history:', error));
}

document.addEventListener('DOMContentLoaded', function() {
    loadChatHistory();  // 이전 채팅 기록 불러오기
    connect();  // WebSocket 연결
});


let currentIndex = 0;
const profilePics = document.querySelectorAll('.profile-pic');

/// 이미지 슬라이드 업데이트 함수
function updateSlide() {
    const profilePics = document.querySelectorAll('.profile-pic');
    profilePics.forEach((pic, index) => {
        pic.classList.toggle('active', index === currentIndex);
    });
}

// 이전 이미지로 이동
function prevImage() {
    currentIndex = (currentIndex - 1 + profilePics.length) % profilePics.length;
    updateSlide();
}

// 다음 이미지로 이동
function nextImage() {
    currentIndex = (currentIndex + 1) % profilePics.length;
    updateSlide();
}

// 초기 슬라이드 설정
document.addEventListener('DOMContentLoaded', updateSlide);


document.addEventListener('DOMContentLoaded', function() {
    const userInfoElement = document.getElementById('user-info');
    const birthDate = new Date(userInfoElement.getAttribute('data-birth-date'));

    if (!isNaN(birthDate)) {
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();

        // 생일이 지났는지 확인하여 나이 조정
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }

        // 나이를 포함한 텍스트 설정
        const city = userInfoElement.textContent.split(" | ")[0];  // 도시 이름 추출
        userInfoElement.textContent = `${city} | ${age}세`;
    }
});