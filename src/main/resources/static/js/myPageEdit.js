// 주소 검색 API 호출 함수
function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            document.getElementById('address').value = data.address;
        }
    }).open();
}

document.addEventListener('DOMContentLoaded', function () {
    let photoCount = document.querySelectorAll('.photo-box').length;

    // 초기 인덱스 설정
    document.querySelectorAll('.photo-box').forEach((box, index) => {
        box.setAttribute('data-index', index);
        const img = box.querySelector('img');
        if (img) img.id = `preview${index}`;
        const input = box.querySelector('input[type="file"]');
        if (input) {
            input.id = `photoInput${index}`;
            input.setAttribute('onchange', `previewImage(event, ${index})`);
        }
        const addButton = box.querySelector('.add-photo-button');
        if (addButton) addButton.setAttribute('data-index', index);
        const deleteButton = box.querySelector('.delete-photo-button');
        if (deleteButton) deleteButton.setAttribute('data-index', index);
    });

    function previewImage(event, index) {
        const reader = new FileReader();
        reader.onload = function () {
            const preview = document.getElementById('preview' + index);
            if (preview) {
                preview.src = reader.result;
                preview.style.display = 'block';
            }
        };
        reader.readAsDataURL(event.target.files[0]);
    }

    function addNewPhotoBox() {
        if (photoCount >= 6) {
            alert("최대 6장까지 추가할 수 있습니다.");
            return;
        }

        const photoSection = document.getElementById('photo-section');
        const newPhotoBox = document.createElement('div');
        newPhotoBox.classList.add('photo-box');
        newPhotoBox.setAttribute('data-index', photoCount);
        newPhotoBox.innerHTML = `
        <img id="preview${photoCount}" src="#" alt="프로필 사진" style="display:none;">
        <input type="file" id="photoInput${photoCount}" name="profileImages" class="hidden-input" accept="image/*" onchange="previewImage(event, ${photoCount})">
        <button type="button" class="add-photo-button" data-index="${photoCount}">+</button>
        <button type="button" class="delete-photo-button" data-index="${photoCount}">삭제</button>
    `;
        photoSection.appendChild(newPhotoBox);
        photoCount++;

        bindEvents(); // 새로 추가된 요소에 이벤트 핸들러 연결
    }

    function deletePhotoBox(index) {
        const photoBox = document.querySelector(`.photo-box[data-index="${index}"]`);
        if (photoBox) {
            photoBox.remove();
            photoCount--;

            // 삭제 후 남은 박스들의 인덱스를 다시 설정
            const updatedBoxes = document.querySelectorAll('#photo-section .photo-box');
            updatedBoxes.forEach((box, idx) => {
                box.setAttribute('data-index', idx);
                box.querySelector('img').id = `preview${idx}`;
                box.querySelector('input[type="file"]').id = `photoInput${idx}`;
                box.querySelector('input[type="file"]').setAttribute('onchange', `previewImage(event, ${idx})`);
                box.querySelector('.add-photo-button').setAttribute('data-index', idx);
                box.querySelector('.delete-photo-button').setAttribute('data-index', idx);
            });

            bindEvents(); // 이벤트 재연결
        } else {
            console.error(`삭제할 수 있는 사진 박스를 찾을 수 없습니다: 인덱스 ${index}`);
        }
    }

    function bindEvents() {
        document.querySelectorAll('.add-photo-button').forEach(button => {
            button.onclick = function () {
                const index = this.getAttribute('data-index');
                document.getElementById('photoInput' + index).click();
            };
        });

        document.querySelectorAll('.delete-photo-button').forEach(button => {
            button.onclick = function () {
                const index = parseInt(this.getAttribute('data-index'));
                if (!isNaN(index)) {
                    deletePhotoBox(index);
                } else {
                    console.error(`유효하지 않은 인덱스: ${index}`);
                }
            };
        });

        document.querySelectorAll('input[type="file"]').forEach(input => {
            const index = input.id.replace('photoInput', '');
            input.onchange = function (event) {
                previewImage(event, index);
            };
        });
    }

    // 초기 이벤트 핸들러 설정
    bindEvents();

    // 사진 추가 버튼 이벤트
    document.getElementById('addPhotoButton').onclick = addNewPhotoBox;
});



let selectedInterests = [];

function prepareInterestsForSubmit() {
    const interestsInput = document.getElementById('interestsInput');
    interestsInput.value = selectedInterests.join(',');
    return true;
}

function toggleInterestSelection(button) {
    const interest = button.getAttribute("data-interest");

    if (selectedInterests.includes(interest)) {
        selectedInterests = selectedInterests.filter(i => i !== interest);
        button.classList.remove("selected");
    } else if (selectedInterests.length < 8) {
        selectedInterests.push(interest);
        button.classList.add("selected");
    } else {
        alert("최대 8개까지만 선택할 수 있습니다.");
    }
}

function saveSelectedInterests() {
    updateSelectedInterests();
    closeModal();
}

function updateSelectedInterests() {
    const selectedContainer = document.getElementById("selectedInterests");
    selectedContainer.innerHTML = "";
    selectedInterests.forEach(interest => {
        const button = document.createElement("button");
        button.className = "selected-interest-button";
        button.textContent = interest;
        selectedContainer.appendChild(button);
    });
}

function resetInterests() {
    selectedInterests = [];
    updateSelectedInterests();
    document.querySelectorAll(".interest-button").forEach(button => {
        button.classList.remove("selected");
    });
}

function openModal() {
    document.getElementById("interestModal").style.display = "block";
}

function closeModal() {
    document.getElementById("interestModal").style.display = "none";
}
