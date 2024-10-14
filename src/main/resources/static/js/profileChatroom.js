let roomId = null; // 전역 변수로 roomId 설정
let stompClient = null;
let currentIndex = 0;
let selectedUserId = null; // 전역 변수로 선택된 상대의 ID를 저장


// WebSocket 연결 함수
function connect() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            console.log('기존 WebSocket 연결을 끊었습니다.');
        });
    }

    if (!roomId) {
        console.error('roomId가 설정되지 않았습니다.');
        return;
    }

    const socket = new SockJS('/ws'); // WebSocket 연결 생성
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 연결됨: ' + frame);

        // 채팅방에 대한 WebSocket 구독 설정
        stompClient.subscribe(`/topic/chat/${roomId}`, function (message) {
            const parsedMessage = JSON.parse(message.body);
            console.log('수신된 메시지:', parsedMessage);
            showMessage(parsedMessage);
        });

        // 기존 채팅방의 이전 메시지를 가져오는 AJAX 요청 추가
        loadChatHistory();
    });
}

// 채팅 기록 불러오기 함수
function loadChatHistory() {
    if (!roomId) {
        console.error('채팅방 ID가 설정되지 않았습니다.');
        return;
    }

    fetch(`/chat/messages/${roomId}`)
        .then(response => response.json())
        .then(messages => {
            const chatMessages = document.getElementById('chatMessages');
            chatMessages.innerHTML = ''; // 기존 채팅 기록 삭제

            messages.forEach(showMessage);
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
        });
}

// 채팅방 정보를 업데이트하는 함수 (프로필 선택 시)
function updateChatRoom(newRoomId, otherUser, profileImageUrls) {
    const chatRoomElement = document.getElementById('chat-room');
    const findAnotherButton = document.getElementById('find-another'); // '다른 상대 찾기' 버튼

    if (chatRoomElement) {
        chatRoomElement.style.display = 'block';  // 채팅방 요소를 표시
        findAnotherButton.style.display = 'block';  // 다른 상대 찾기 버튼도 표시
        chatRoomElement.setAttribute('data-room-id', newRoomId);  // 데이터 속성 업데이트
    } else {
        console.error("'chat-room' 요소를 찾을 수 없습니다.");
        return;
    }

    roomId = newRoomId; // 전역 변수 roomId 업데이트

    // WebSocket 재연결
    connect();  // 새로운 roomId에 맞게 WebSocket 연결 재설정

    // 새로운 채팅방의 채팅 기록 불러오기
    loadChatHistory();

    // 상대방 프로필 업데이트
    updateProfileCard(otherUser, profileImageUrls); // 상대방 정보와 프로필 이미지들 전달
}

// 프로필 카드 업데이트 (유저 정보와 이미지 포함)
function updateProfileCard(otherUser, profileImageUrls) {
    console.log("프로필 카드 업데이트 시작");

    // 사용자 이름 업데이트
    const userNameElement = document.querySelector('.profile-info h3');
    console.log('userNameElement:', userNameElement); // 콘솔에서 요소를 확인

    if (userNameElement) {
        userNameElement.textContent = otherUser.name || "이름 없음";
    } else {
        console.error("userNameElement가 존재하지 않습니다.");
    }

    // 생일 및 주소 업데이트
    const userInfoElement = document.querySelector('.profile-info p[data-birth-date]');
    console.log('userInfoElement:', userInfoElement); // 콘솔에서 요소를 확인
    if (userInfoElement) {
        const birthDate = new Date(otherUser.birthDate);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        if (today.getMonth() < birthDate.getMonth() || (today.getMonth() === birthDate.getMonth() && today.getDate() < birthDate.getDate())) {
            age--;
        }

        const city = otherUser.address.split(' ')[0];  // 주소에서 도시명 추출
        userInfoElement.textContent = `${city} | ${age}세`;  // 도시명과 나이 표시
    }

    // 프로필 이미지 업데이트
    const profileImagesContainer = document.querySelector('.profile-images');
    if (profileImagesContainer && Array.isArray(profileImageUrls)) {
        profileImagesContainer.innerHTML = '';  // 기존 이미지를 모두 삭제

        profileImageUrls.forEach((imageUrl, index) => {
            const imgElement = document.createElement('img');
            imgElement.src = `/files?fileName=${imageUrl}`;
            imgElement.alt = "상대방 프로필 사진";
            imgElement.classList.add('profile-pic');
            if (index === 0) imgElement.classList.add('active');  // 첫 번째 이미지는 활성화
            profileImagesContainer.appendChild(imgElement);
        });

        // 슬라이드 초기화
        currentIndex = 0;
        updateSlide();
    }
}

// 새로운 사용자 목록을 받아 프로필 카드를 업데이트하는 함수
function updateProfileCards(users) {
    const profileList = document.getElementById('profile-list');
    profileList.innerHTML = '';  // 기존 프로필 카드 삭제

    users.forEach(user => {
        const uniqueId = `slider-${user.id}`; // 각 프로필의 슬라이더에 고유 ID 설정
        const cardHtml = `
            <div class="profile-card" data-user-id="${user.id}">
                <!-- 프로필 슬라이더 -->
                <div class="profile-slider" id="${uniqueId}">
                    <button class="slide-btn prev-btn" onclick="prevImage('${uniqueId}')">&#10094;</button>
                    <div class="profile-images">
                        ${user.profileImage.map((image, index) => `
                            <img src="/files?fileName=${image}" alt="상대방 프로필 사진" class="profile-pic ${index === 0 ? 'active' : ''}">
                        `).join('')}
                    </div>
                    <button class="slide-btn next-btn" onclick="nextImage('${uniqueId}')">&#10095;</button>
                </div>
                <div class="profile-info">
                    <h3>${user.name}</h3>
                    <p data-birth-date="${user.birthDate}">
                        ${user.address.split(' ')[0]} | ${calculateAge(user.birthDate)}세
                    </p>
                    <p class="likecount">좋아요 수: <span>${user.likeCount}</span></p>
                    <button class="select-profile-btn" data-user-id="${user.id}" onclick="selectProfile(this)">선택</button>
                </div>
            </div>
        `;
        profileList.innerHTML += cardHtml;
    });

    document.getElementById('find-another').style.display = 'block';  // 버튼 항상 표시
}

// 나이 계산 함수
function calculateAge(birthDate) {
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
    }
    return age;
}

// 다른 사람 찾기 버튼을 눌렀을 때
function findAnotherUser() {
    const loggedInUserUid = document.getElementById('loggedInUserUid').value;

    if (!loggedInUserUid) {
        alert("사용자 Uid를 찾을 수 없습니다.");
        return;
    }

    // 기존 선택된 사용자의 ID 목록을 가져옴
    const selectedUserIds = Array.from(document.querySelectorAll('.profile-card'))
        .map(card => card.getAttribute('data-user-id'));

    fetch(`/chat/findNewUser/${loggedInUserUid}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            selectedUserIds: selectedUserIds
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`서버 오류 발생: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data && data.users) {
                console.log("새로운 사용자 목록:", data.users);
                updateProfileCards(data.users);  // 프로필 카드 업데이트

                // 채팅방 숨기기 - chat-room 요소를 확인한 후
                const chatRoomElement = document.getElementById('chat-room');
                if (chatRoomElement) {
                    chatRoomElement.style.display = 'none';
                } else {
                    console.warn("'chat-room' 요소를 찾을 수 없습니다.");
                }
            } else {
                console.error("새로운 사용자를 찾을 수 없습니다.");
            }
        })
        .catch(error => {
            alert("오류가 발생했습니다. 다시 시도해 주세요.");
            console.error('Error finding another user:', error);
        });
}


// 각 슬라이드의 현재 인덱스를 저장하는 객체
const slideIndices = {};

// 슬라이드 업데이트 함수
function updateSlide(uniqueId) {
    const profilePics = document.querySelectorAll(`#${uniqueId} .profile-pic`);
    const currentIndex = slideIndices[uniqueId] || 0;
    profilePics.forEach((pic, index) => {
        pic.classList.toggle('active', index === currentIndex);
    });
}

// 이전 이미지로 이동
function prevImage(uniqueId) {
    slideIndices[uniqueId] = (slideIndices[uniqueId] || 0) - 1;
    if (slideIndices[uniqueId] < 0) {
        slideIndices[uniqueId] = document.querySelectorAll(`#${uniqueId} .profile-pic`).length - 1;
    }
    updateSlide(uniqueId);
}

// 다음 이미지로 이동
function nextImage(uniqueId) {
    slideIndices[uniqueId] = (slideIndices[uniqueId] || 0) + 1;
    if (slideIndices[uniqueId] >= document.querySelectorAll(`#${uniqueId} .profile-pic`).length) {
        slideIndices[uniqueId] = 0;
    }
    updateSlide(uniqueId);
}


// "좋아요 누르고 채팅방 저장하기" 기능
function likeAndSaveChatRoom() {
    const loggedInUserUid = document.getElementById('loggedInUserUid').value;
    const selectedUserIds = Array.from(document.querySelectorAll('.profile-card'))
        .map(card => parseInt(card.getAttribute('data-user-id'), 10));



    fetch(`/chat/likeAndChatSave/${roomId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            likerUid: loggedInUserUid,   // 현재 로그인된 사용자의 uid
            likedUserId: selectedUserId,  // 상대방의 사용자 id
            selectedUserIds:selectedUserIds
        })
    })
        .then(response => {
            if (!response.ok) {
                console.log('Selected User IDs:', selectedUserIds);
                throw new Error('좋아요 및 채팅방 저장에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.users) {
                console.log("새로운 사용자 목록:", data.users);
                updateProfileCards(data.users);  // 프로필 카드 업데이트

                // 채팅방 숨기기 - chat-room 요소를 확인한 후
                const chatRoomElement = document.getElementById('chat-room');
                if (chatRoomElement) {
                    chatRoomElement.style.display = 'none';
                } else {
                    console.warn("'chat-room' 요소를 찾을 수 없습니다.");
                }
            } else {
                console.error("새로운 사용자를 찾을 수 없습니다.");
            }
        })
        .catch(error => {
            alert(error.message);
            console.error('Error liking and saving chat room:', error);
        });
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const messageContent = messageInput.value.trim();

    const loggedInUserId = document.getElementById('loggedInUserId').value;
    const loggedInUserName = document.getElementById('loggedInUserName').value;

    console.log('보낼 메시지 내용:', messageContent);
    console.log('보낼 사용자 ID:', loggedInUserId);
    console.log('보낼 사용자 이름:', loggedInUserName);

    if (messageContent === '') {
        return;
    }

    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket이 아직 연결되지 않았습니다.');
        alert('메시지를 전송할 수 없습니다. 채팅방에 연결되지 않았습니다.');
        return;
    }

    // WebSocket을 통해 메시지 전송
    stompClient.send(`/app/chat/${roomId}/sendMessage`, {}, JSON.stringify({
        content: messageContent,
        roomId: roomId,
        senderId: loggedInUserId,  // 로그인한 사용자 ID
        senderName: loggedInUserName // 로그인한 사용자 이름
    }));

    messageInput.value = ''; // 메시지 입력창 초기화
}

// 채팅방 선택 시 프로필 연결 함수
function selectProfile(button) {
   selectedUserId = $(button).data('user-id');
    console.log('Selected User ID:', selectedUserId);

    if (!selectedUserId) {
        alert('선택된 사용자가 없습니다.');
        return;
    }

    document.querySelectorAll('.profile-card').forEach(card => {
        if (card.getAttribute('data-user-id') != selectedUserId) {
            card.style.display = 'none';
        }
    });

    document.getElementById('chat-room').style.display = 'block';

    // 선택한 프로필과 채팅방을 연동
    $.ajax({
        url: '/chat/startChatWithSelectedUser',
        type: 'POST',
        data: { selectedUserId: selectedUserId },
        success: function (data) {
            const chatMessages = document.getElementById('chatMessages');
            chatMessages.innerHTML = ''; // 기존 메시지 제거

            if (data.roomid) {
                // 기존 채팅방이 있을 경우 확인창 띄우기
                if (data.existingRoom) {
                    if (confirm("이미 채팅을 한 회원입니다. 기존 채팅방을 불러올까요?")) {
                        console.log('기존 채팅방을 불러옵니다.');
                        roomId = data.roomid; // roomId 전역 변수에 roomid 값 설정
                        connect();  // WebSocket 연결 시도
                    } else {
                        return;
                    }
                } else {
                    roomId = data.roomid; // roomId 전역 변수에 roomid 값 설정
                    connect();  // WebSocket 연결 시도
                }
            } else {
                console.error('roomid가 서버 응답에 포함되지 않았습니다.');
                alert('채팅방을 생성할 수 없습니다.');
            }
        },
        error: function (error) {
            alert('채팅방을 불러오는 중 오류가 발생했습니다.');
            console.error('채팅방 불러오기 오류:', error);
        }
    });
}

// 메시지를 화면에 표시하는 함수
function showMessage(message) {
    const chatMessages = document.getElementById('chatMessages');
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');

    // 본인이 보낸 메시지인지 확인 (로그인된 사용자 ID와 메시지의 senderId 비교)
    const loggedInUserId = document.getElementById('loggedInUserId').value; // 현재 로그인된 사용자 ID
    if (message.senderId === parseInt(loggedInUserId)) {
        messageElement.classList.add('sent');  // 본인 메시지는 오른쪽에 배치
    } else {
        messageElement.classList.add('received');  // 상대방 메시지는 왼쪽에 배치

        // 상대방 이름 표시
        const senderName = document.createElement('div');
        senderName.classList.add('sender-name');
        senderName.innerText = message.senderName; // 메시지에서 senderName 사용
        messageElement.appendChild(senderName);
    }

    // 메시지 내용 추가
    const messageContent = document.createElement('p');
    messageContent.innerText = message.content;
    messageElement.appendChild(messageContent);

    chatMessages.appendChild(messageElement);

    // 채팅 창 스크롤을 항상 최신 메시지로 이동
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 프로필 카드 슬라이더 초기화
document.addEventListener('DOMContentLoaded', function () {
    const profileCards = document.querySelectorAll('.profile-card');
    const findAnotherButton = document.getElementById('find-another');

    // "다른 상대 찾기" 버튼을 항상 보이도록 설정
    if (findAnotherButton) {
        findAnotherButton.style.display = 'block'; // 항상 표시
    }

    // 각 카드별로 슬라이더 초기화
    profileCards.forEach((card) => {
        const profilePics = card.querySelectorAll('.profile-pic');
        let currentIndex = 0;

        // 슬라이드 초기화
        updateSlide(card, currentIndex, profilePics);

        // 나이 계산 및 도시명 업데이트
        const userInfoElement = card.querySelector('.profile-info p[data-birth-date]');
        const birthDateStr = userInfoElement.getAttribute('data-birth-date');
        const birthDate = new Date(birthDateStr);

        if (!isNaN(birthDate)) {
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const monthDiff = today.getMonth() - birthDate.getMonth();

            // 생일이 지났는지 확인하여 나이 조정
            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }

            // 도시명 추출
            const city = userInfoElement.textContent.split(' | ')[0];  // 기존 텍스트에서 도시명 추출
            userInfoElement.textContent = `${city} | ${age}세`;  // 도시명과 나이 표시
        }

        // 이전 이미지로 이동
        card.querySelector('.prev-btn').addEventListener('click', function () {
            if (currentIndex > 0) {
                currentIndex--;
            } else {
                currentIndex = profilePics.length - 1;
            }
            updateSlide(card, currentIndex, profilePics);
        });

        // 다음 이미지로 이동
        card.querySelector('.next-btn').addEventListener('click', function () {
            if (currentIndex < profilePics.length - 1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
            updateSlide(card, currentIndex, profilePics);
        });

        // 슬라이드 업데이트 함수
        function updateSlide(card, currentIndex, profilePics) {
            profilePics.forEach((pic, index) => {
                pic.classList.toggle('active', index === currentIndex);
            });

            // 현재 위치에 맞게 슬라이더 이동
            const slider = card.querySelector('.profile-images');
            const picWidth = profilePics[0].clientWidth; // 각 사진의 너비
            const offset = -currentIndex * picWidth; // 이동할 거리 계산
            slider.style.transform = `translateX(${offset}px)`;
        }
    });
});

