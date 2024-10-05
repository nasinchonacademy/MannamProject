function showLoginForm() {
    const loginForm = document.getElementById('loginForm');
    loginForm.classList.remove('hidden'); // 폼을 보이게 하기
    loginForm.classList.add('show');      // 애니메이션 효과 추가
}

// 팝업 열기
function showRecoveryPopup() {
    document.getElementById('popupRecoveryForm').classList.remove('hidden');
    document.getElementById('popupBackground').classList.remove('hidden');
}

// 팝업 닫기
function closeRecoveryPopup() {
    document.getElementById('popupRecoveryForm').classList.add('hidden');
    document.getElementById('popupBackground').classList.add('hidden');
    document.getElementById('codeVerification').classList.add('hidden'); // 인증 코드 입력 폼 숨김
    document.getElementById('passwordReset').classList.add('hidden'); // 비밀번호 재설정 폼 숨김
}

// 인증 코드 전송
function sendRecoveryCode() {
    const email = document.getElementById('email').value;

    fetch(`/send-recovery-code?email=${email}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('인증 코드가 전송되었습니다. 이메일을 확인하세요.');
                document.getElementById('codeVerification').classList.remove('hidden');
            } else {
                alert('인증 코드 전송에 실패했습니다. 다시 시도해주세요.');
            }
        });
}

// 인증 코드 확인
function verifyRecoveryCode() {
    const email = document.getElementById('email').value;
    const code = document.getElementById('code').value;

    fetch(`/verify-recovery-code?email=${email}&code=${code}`)
        .then(response => response.json())
        .then(data => {
            if (data.verified) {
                alert('인증 성공!');
                document.getElementById('recoveryOptions').classList.remove('hidden'); // 아이디 찾기 및 비밀번호 재설정 옵션 표시
                document.getElementById('passwordReset').classList.remove('hidden'); // 비밀번호 재설정 폼 표시
            } else {
                alert('인증 코드가 일치하지 않습니다.');
            }
        });
}

// 비밀번호 재설정
function resetPassword() {
    const email = document.getElementById('email').value;
    const newPassword = document.getElementById('newPassword').value;

    fetch(`/recover-id-password`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: email,
            password: newPassword
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.message) {
                alert(data.message);

                // 비밀번호 재설정 성공 시 팝업창 닫기
                if (data.message === "비밀번호가 성공적으로 재설정되었습니다.") {
                    closeRecoveryPopup(); // 팝업창 닫기
                }
            }
        });
}

// 아이디 찾기
function findUsername() {
    const name = document.getElementById('name').value;
    const email = document.getElementById('useremail').value;

    fetch(`/find-username`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name: name, email: email })
    })
        .then(response => response.json())
        .then(data => {
            if (data.uid) {
                document.getElementById('useridDisplay').textContent = data.uid;
                document.getElementById('usernameResult').classList.remove('hidden');
            } else {
                alert('이름과 이메일이 일치하는 사용자를 찾을 수 없습니다.');
            }
        });
}

// 아이디 찾기 팝업 열기
function showFindUsernamePopup() {
    document.getElementById('popupFindUsername').classList.remove('hidden');
    document.getElementById('popupBackground').classList.remove('hidden');
}

// 아이디 찾기 팝업 닫기
function closeFindUsernamePopup() {
    document.getElementById('popupFindUsername').classList.add('hidden');
    document.getElementById('popupBackground').classList.add('hidden');
}