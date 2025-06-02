const editBtn = document.getElementById("editBtn");
    const form = document.getElementById("profileForm");
    const inputs = form.querySelectorAll("input:not([readonly])");
    let editing = false;

    editBtn.addEventListener("click", () => {
        if (!editing) {
            inputs.forEach(input => {
                input.disabled = false;
                input.classList.add("editable");
            });
            editBtn.textContent = "Сохранить";
            editing = true;
        } else {
            form.submit();
        }
    });