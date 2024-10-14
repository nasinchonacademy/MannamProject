
const geocoder = new kakao.maps.services.Geocoder();

let selectedInterests = [];

// 관심사 추가
document.getElementById('addInterestBtn').addEventListener('click', function() {
    const interest = prompt('관심사를 입력하세요');
    if (interest && !selectedInterests.includes(interest)) {
        selectedInterests.push(interest);

        // 쉼표로 구분된 문자열로 숨겨진 필드에 저장
        document.getElementById('interests').value = selectedInterests.join(', ');

        // 관심사 버튼을 추가
        const interestButton = document.createElement('button');
        interestButton.textContent = `#${interest}`;
        interestButton.classList.add('interest-button');
        interestButton.type = 'button';

        // 버튼 클릭 시 삭제 기능 추가
        interestButton.addEventListener('click', function() {
            const index = selectedInterests.indexOf(interest);
            if (index > -1) {
                selectedInterests.splice(index, 1); // 배열에서 관심사 제거
                document.getElementById('interests').value = selectedInterests.join(', '); // 숨겨진 필드 업데이트
                interestButton.remove(); // 버튼 제거
                alert('선택된 관심사: ' + selectedInterests.join(', '));
            }
        });

        // 관심사 버튼 리스트에 추가
        const interestList = document.getElementById('interestList');
        interestList.insertBefore(interestButton, document.getElementById('addInterestBtn'));

        alert('선택된 관심사: ' + selectedInterests.join(', '));
    } else if (selectedInterests.includes(interest)) {
        alert('이미 추가된 관심사입니다.');
    }
});

// 카카오 주소 검색 API 사용
document.getElementById('setLocationBtn').addEventListener('click', function() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 검색 결과에서 주소를 가져와서 주소 필드에 넣기
            document.getElementById('address').value = data.address;

            // 주소를 좌표로 변환
            geocoder.addressSearch(data.address, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    const latitude = result[0].y;
                    const longitude = result[0].x;

                    // 변환된 좌표를 숨겨진 필드에 저장
                    document.getElementById('latitude').value = latitude;
                    document.getElementById('longitude').value = longitude;

                } else {
                    alert('좌표를 찾을 수 없습니다.');
                }
            });
        }
    }).open();
});


// 폼 제출 시 서버로 데이터 전송
document.getElementById('createMeetingForm').addEventListener('submit', function(event) {
    if (!document.getElementById('latitude').value || !document.getElementById('longitude').value) {
        event.preventDefault();
        alert('주소를 설정해주세요.');
    }
});

// 이미지 미리보기 및 파일명 표시
function previewImage() {
    const fileInput = document.getElementById('image');
    const file = fileInput.files[0];
    const preview = document.getElementById('imagePreview');
    const uploadButton = document.getElementById('imageUploadButton');

    if (file) {
        const reader = new FileReader();

        // 파일이 로드되었을 때 실행되는 함수
        reader.onload = function(e) {
            preview.src = e.target.result;  // 미리보기 이미지 경로 설정
            preview.style.display = "block";  // 미리보기 이미지 표시
        };

        reader.readAsDataURL(file);  // 파일을 읽어 Data URL 형식으로 변환

        // 파일 이름으로 버튼 텍스트 변경 및 너비 동적 설정
        uploadButton.textContent = file.name;

        // 기본적으로 파일명 길이에 따라 동적으로 버튼 너비 조정 (최대 300px까지만)
        let newWidth = Math.min((file.name.length + 1) * 10, 300); // 길이에 따라 width 설정
        uploadButton.style.width = newWidth + "px";
    } else {
        preview.style.display = "none";  // 파일이 없을 때 미리보기 숨김
        uploadButton.textContent = "+ 모임 이미지 설정";  // 기본 텍스트로 복원
        uploadButton.style.width = "150px";  // 기본 너비로 복원
    }
}