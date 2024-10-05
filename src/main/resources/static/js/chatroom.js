let roomId = document.getElementById('chatRoom').getAttribute('data-room-id');
let otherUserId = document.getElementById('chatRoom').getAttribute('data-other-user-id');
loggedInUserId = document.getElementById('chatRoom').getAttribute('data-user-id');

let lastSenderId = null;

console.log("Room ID:", roomId, "Other User ID:", otherUserId, "Logged in User ID:", loggedInUserId);

function connect() {
    console.log("roomId로 WebSocket 연결 시도:", roomId);
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 서버에 연결되었습니다, frame:', frame);

        stompClient.subscribe(`/topic/chat/${roomId}`, function (messageOutput) {
            console.log("받은 메시지:", messageOutput.body);
            const message = JSON.parse(messageOutput.body);
            showMessage(message);
        });
    });
}

// 새로운 상대를 찾고, 기존 채팅방 메시지를 삭제한 후 새로운 채팅방으로 리다이렉트하는 함수
function findAnotherUser() {
    const loggedInUserId = document.getElementById('chatRoom').getAttribute('data-user-id'); // 현재 로그인된 사용자 ID

    // 서버로 새로운 상대 찾기 요청
    fetch(`/chat/findAnotherUser/${loggedInUserId}`, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            if (data && data.roomId) {
                // 새로운 채팅방으로 리다이렉트
                window.location.href = `/chat/room/${data.roomId}`;
            } else {
                console.error("새로운 상대를 찾을 수 없습니다.");
            }
        })
        .catch(error => console.error('Error finding another user:', error));
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
    fetch(`/chat/messages/${roomId}`)  // 메시지 조회 API 호출
        .then(response => response.json())
        .then(messages => {
            lastSenderId = null; // 채팅 기록 로드 전 발신자 초기화
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

// 이미지 슬라이드 업데이트 함수
function updateSlide() {
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