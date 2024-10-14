const chatContainer = document.getElementById('chat-container');
const roomId = chatContainer.getAttribute('data-room-id');
const username = chatContainer.getAttribute('data-username');
const usersname = chatContainer.getAttribute('data-usersname');

let stompClient = null;

// WebSocket 연결 함수
function connect() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // 기존 메시지 내역을 로드
        loadMessageHistory();

        // 특정 채팅방 구독 (WebSocket을 통해 실시간 메시지를 받음)
        stompClient.subscribe('/topic/meeting/' + roomId, function (message) {
            showMessage(JSON.parse(message.body));
        });
    });
}

// 메시지 내역을 서버에서 로드
function loadMessageHistory() {
    fetch(`/api/meetingRoom/${roomId}/messages`)
        .then(response => response.json())
        .then(data => {
            if (Array.isArray(data)) {
                data.forEach(message => showMessage(message));
            }
        })
        .catch(error => console.error('Error loading message history:', error));
}

// 메시지를 서버로 전송
function sendMessage() {
    let messageContent = document.querySelector('#messageInput').value.trim();
    if (messageContent && stompClient) {
        let chatMessage = {
            sender: username,
            content: messageContent,
            roomId: roomId,
            type: 'message'
        };

        stompClient.send("/app/meeting/" + roomId + "/sendMessage", {}, JSON.stringify(chatMessage));
        document.querySelector('#messageInput').value = '';
    }
}

// 메시지를 화면에 표시
function showMessage(message) {
    const chatBox = document.querySelector('#chat-box');

    let messageElement = document.createElement('div');
    messageElement.classList.add('message');

    if (message.sender === username) {
        // 본인의 메시지인 경우 오른쪽에 표시
        messageElement.classList.add('my-message');
    } else {
        // 상대방의 메시지인 경우 왼쪽에 표시 및 프로필 사진 추가
        messageElement.classList.add('other-message');

        // sender(uid) 값을 전달해서 프로필 사진 가져오기
        getUserProfilePicture(message.sender)
            .then(profilePictureUrl => {
                // 프로필 사진과 이름을 담을 컨테이너 생성
                let profileContainer = document.createElement('div');
                profileContainer.classList.add('profile-container');

                let profilePicElement = document.createElement('img');
                profilePicElement.src = profilePictureUrl; // 서버에서 가져온 프로필 사진 경로 설정
                profilePicElement.classList.add('profile-pic');
                profilePicElement.width = 40; // 프로필 사진 크기
                profilePicElement.height = 40;
                profilePicElement.addEventListener('click', function () {
                    openModal(message.sender); // 프로필 사진 클릭 시 모달 열기
                });

                // 사용자 이름 추가
                let usersnameElement = document.createElement('div');
                usersnameElement.classList.add('usersname');
                usersnameElement.textContent = message.usersname; // 사용자 이름으로 설정

                // 프로필 사진과 이름을 프로필 컨테이너에 추가
                profileContainer.appendChild(profilePicElement);
                profileContainer.appendChild(usersnameElement);

                // 프로필 컨테이너를 메시지 상자에 추가
                messageElement.appendChild(profileContainer);
            })
            .catch(error => console.error('Error loading profile picture:', error));
    }

    let contentElement = document.createElement('div');
    contentElement.classList.add('message-content');
    contentElement.textContent = message.content;

    messageElement.appendChild(contentElement); // 메시지 내용 추가
    chatBox.appendChild(messageElement);
    chatBox.scrollTop = chatBox.scrollHeight; // 스크롤을 마지막 메시지로 이동
}

// 프로필 사진을 서버에서 가져오는 함수
function getUserProfilePicture(userUid) {
    const url = `/api/user/${userUid}/profile-picture`;

    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Profile picture not found');
            }
            return response.url;  // 서버에서 반환된 이미지 URL을 사용
        })
        .catch(error => {
            console.warn(`Profile picture not found for user ${userUid}. Using default profile picture.`);
            return '/path/to/default-profile-picture.jpg';  // 기본 프로필 사진 경로 설정
        });
}

// 사용자 정보 모달 열기
function openModal(senderUid) {
    const modal = document.getElementById('userModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');

    modalTitle.textContent = senderUid + "님의 정보";
    modalBody.textContent = senderUid + "의 상세 정보가 여기에 표시됩니다.";  // 실제 사용자 정보 표시 로직으로 대체 가능

    modal.style.display = "flex";  // 모달을 화면에 표시
}

// 모달 닫기
function closeModal() {
    const modal = document.getElementById('userModal');
    modal.style.display = "none";  // 모달 숨기기
}

// WebSocket 연결 시도
connect();
