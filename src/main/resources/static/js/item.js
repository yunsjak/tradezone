/**
 * 
 */

// static/js/item.js
document.addEventListener("DOMContentLoaded", function () {
    const priceInput = document.getElementById("price");

    priceInput.addEventListener("input", function (e) {
        let value = e.target.value.replace(/[^0-9]/g, "");
        if (value) {
            e.target.value = Number(value).toLocaleString("ko-KR");
        } else {
            e.target.value = "";
        }
    });

    priceInput.addEventListener("focus", function (e) {
        e.target.value = e.target.value.replace(/[^0-9]/g, "");
    });

    priceInput.addEventListener("blur", function (e) {
        const value = e.target.value.replace(/[^0-9]/g, "");
        if (value) {
            e.target.value = Number(value).toLocaleString("ko-KR");
        }
    });

    // 1차 카테고리 변경 시 2차 카테고리 동적 변경
    const parentSelect = document.getElementById("parentCategoryId");
    const childSelect = document.getElementById("childCategoryId");

    parentSelect.addEventListener("change", function () {
        const parentId = this.value;

        // 자식 카테고리 초기화
        childSelect.innerHTML = "";

        // AJAX로 자식 카테고리 가져오기
        fetch(`/categories/children?parentId=${parentId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("네트워크 응답에 문제가 있습니다.");
                }
                return response.json();
            })
            .then(children => {
                children.forEach(child => {
                    const option = document.createElement("option");
                    option.value = child.id;
                    option.textContent = child.name;
                    childSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error("자식 카테고리 불러오기 오류:", error);
            });
    });
});