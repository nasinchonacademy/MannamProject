//
// //
// // // 채팅방 좋아요 기능
// // function likeMeetingRoom(meetingRoomId) {
// //     $.ajax({
// //         url: '/likeMeetingRoom',
// //         type: 'POST',
// //         data: { roomId: meetingRoomId },
// //         success: function(response) {
// //             alert('채팅방을 좋아요했습니다!');
// //             // 좋아요 수 업데이트 등 추가적인 처리
// //         },
// //         error: function(error) {
// //             console.error('좋아요 처리 중 오류 발생:', error);
// //         }
// //     });
// // }
//
// function likeMeetingRoom(meetingRoomId) {
//     // 좋아요 버튼을 비활성화하여 중복 클릭 방지
//     const likeButton = document.getElementById("likeButton");
//     likeButton.disabled = true;
//
//     $.ajax({
//         url: '/likeMeetingRoom',
//         type: 'POST',
//         data: { roomId: meetingRoomId },
//         success: function(response) {
//             console.log("서버 응답:", response); // 서버 응답 데이터 확인
//
//             // 응답 데이터가 예상한 형태인지 확인합니다.
//             if (response && typeof response.likesCount === 'number' && typeof response.liked === 'boolean') {
//                 // 좋아요 상태 및 좋아요 수 업데이트
//                 const isLiked = response.liked;
//                 const likesCount = response.likesCount;
//
//                 // 좋아요 수 업데이트
//                 const likesCountElement = document.getElementById("likesCount");
//                 if (likesCountElement) {
//                     likesCountElement.textContent = likesCount;
//                 } else {
//                     console.error("likesCount 요소를 찾을 수 없습니다.");
//                 }
//
//                 // 좋아요 버튼 텍스트 변경
//                 if (isLiked) {
//                     likeButton.textContent = '좋아요 취소';
//                 } else {
//                     likeButton.textContent = '좋아요';
//                 }
//             } else {
//                 console.error('올바르지 않은 응답 형식:', response);
//             }
//         },
//         error: function(error) {
//             console.error('좋아요 처리 중 오류 발생:', error);
//         },
//         complete: function() {
//             // 요청 완료 후 좋아요 버튼을 다시 활성화
//             likeButton.disabled = false;
//         }
//     });
// }
//
