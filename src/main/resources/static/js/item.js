document.addEventListener("DOMContentLoaded", function () {
    // 💰 가격 입력 포맷팅
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

    // 📂 카테고리 동적 변경
    const parentSelect = document.getElementById("parentCategoryId");
    const childSelect = document.getElementById("childCategoryId");
    const selectedChildId = childSelect.getAttribute("data-selected-id");

    function loadChildren(parentId, selectedId) {
        childSelect.innerHTML = "";

        fetch(`/categories/children?parentId=${parentId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("자식 카테고리 요청 실패");
                }
                return response.json();
            })
            .then(children => {
                children.forEach(child => {
                    const option = document.createElement("option");
                    option.value = child.id;
                    option.textContent = child.name;
                    if (selectedId && child.id == selectedId) {
                        option.selected = true;
                    }
                    childSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error("자식 카테고리 불러오기 오류:", error);
            });
    }

    // 초기 로딩 시 자식 카테고리 불러오기
    if (parentSelect && parentSelect.value) {
        loadChildren(parentSelect.value, selectedChildId);
    }

    // 부모 카테고리 변경 시 자식 카테고리 갱신
    parentSelect.addEventListener("change", function () {
        loadChildren(this.value, null);
    });
});
