/**
 * 
 */
// static/js/mypage.js
document.addEventListener("DOMContentLoaded", () => {
  const tabs = document.querySelectorAll(".tab-buttons button");
  tabs.forEach(btn =>
    btn.addEventListener("click", () => {
      tabs.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
    })
  );
});


function showTab(tab) {
  const tabs = document.querySelectorAll(".tab-button");
  tabs.forEach(btn => btn.classList.remove("active"));

  document.getElementById('itemList').style.display = 'none';
  document.getElementById('likesList').style.display = 'none';
  document.getElementById('reviewList').style.display = 'none';

  if (tab === 'item') {
    document.getElementById('itemList').style.display = 'block';
    tabs[0].classList.add("active");
  } else if (tab === 'likes') {
    document.getElementById('likesList').style.display = 'block';
    tabs[1].classList.add("active");
  } else if (tab === 'review') {
    document.getElementById('reviewList').style.display = 'block';
    tabs[2].classList.add("active");
  }
}

//document.getElementById('checkDuplicateBtn').addEventListener('click', function () {
//  const username = document.getElementById('usernameInput').value;
//
//  if (!username) {
//    alert("닉네임을 입력해주세요.");
//    return;
//  }
//
//  fetch(`/api/check-username?username=${encodeURIComponent(username)}`)
//    .then(res => res.json())
//    .then(data => {
//      if (data.exists) {
//        alert("이미 사용 중인 닉네임입니다.");
//      } else {
//        alert("사용 가능한 닉네임입니다.");
//      }
//    })
//    .catch(err => {
//      console.error("중복 확인 에러:", err);
//      alert("오류가 발생했습니다.");
//    });
//});