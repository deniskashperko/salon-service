document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('registerEmployeeForm');

    if (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();

            const formData = new FormData(form);
            const data = {
                name: formData.get('name'),
                surname: formData.get('surname'),
                login: formData.get('login'),
                password: formData.get('password'),
                phoneNum: formData.get('phoneNum'),
                procedureIds: [],
                preferredMethod: formData.get('preferredMethod'),
                email: formData.get('email')?.trim(),
                telegramUsername: formData.get('telegramUsername')?.trim()
            };

            formData.getAll('procedureIds').forEach(function (value) {
                data.procedureIds.push(parseInt(value));
            });

            const queryParams = new URLSearchParams();
            queryParams.append('procedureIds', data.procedureIds.join(','));
            queryParams.append('preferredMethod', data.preferredMethod || '');

            // Приоритет email'у: если он указан, не отправляем telegramUsername
            if (data.email) {
                queryParams.append('email', data.email);
            } else if (data.telegramUsername) {
                queryParams.append('telegramUsername', data.telegramUsername);
            }

            fetch('/register-employee?' + queryParams.toString(), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: data.name,
                    surname: data.surname,
                    login: data.login,
                    password: data.password,
                    phoneNum: data.phoneNum
                }),
            })
                .then(response => {
                    if (response.ok) {
                        alert("Сотрудник зарегистрирован успешно!");
                        location.reload();
                    } else {
                        alert("Произошла ошибка при регистрации сотрудника.");
                    }
                })
                .catch(error => console.error("Ошибка при отправке данных:", error));
        });
    }
});



$(document).ready(function () {
    // Обработчик клика по кнопке удаления сотрудника
    $('.deleteEmployeeBtn').click(function () {
        const employeeId = $(this).data('employee-id');  // Получаем ID сотрудника

        // Подтверждение перед удалением
        if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
            // Отправляем DELETE запрос на сервер
            $.ajax({
                url: '/delete-employee/' + employeeId,
                type: 'DELETE',
                success: function () {
                    alert('Сотрудник успешно удален');
                    location.reload();  // Перезагружаем страницу для обновления списка
                },
                error: function () {
                    alert('Ошибка при удалении сотрудника');
                }
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".edit-btn").forEach(button => {
    button.addEventListener("click", function () {
      const row = button.closest("tr");
      const userId = row.dataset.userId;

      const nameCell = row.querySelector(".name-cell");
      const loginCell = row.querySelector(".login-cell");
      const phoneCell = row.querySelector(".phone-cell");

      // Если в режиме редактирования — сохраняем
      if (button.textContent.trim() === "Сохранить") {
        const newName = nameCell.querySelector(".name-input").value.trim();
        const newSurname = nameCell.querySelector(".surname-input").value.trim();
        const newLogin = loginCell.querySelector("input").value.trim();
        const newPhone = phoneCell.querySelector("input").value.trim();

        // Проверка обязательных полей
        if (!newName || !newSurname || !newLogin || !newPhone) {
          alert("Все поля обязательны для заполнения.");
          return; // Прерываем выполнение, если хоть одно поле не заполнено
        }

        // Проверка номера телефона (10 цифр)
        const phoneRegex = /^\d{10}$/;
        if (!phoneRegex.test(newPhone)) {
          alert("Номер телефона должен содержать ровно 10 цифр.");
          return; // Не отправляем запрос, если номер некорректен
        }

        fetch('/admin/update-client', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            id: userId,
            name: newName,
            surname: newSurname,
            login: newLogin,
            phoneNum: newPhone
          })
        })
        .then(response => {
          if (!response.ok) throw new Error("Ошибка при обновлении");
          return response.json();
        })
        .then(data => {
          // Обновляем отображение
          nameCell.innerHTML = `<span>${data.name} ${data.surname}</span>`; // Имя и фамилия теперь вместе
          loginCell.innerHTML = `<span>${data.login}</span>`;
          phoneCell.innerHTML = `<span>${data.phoneNum}</span>`;
          button.textContent = "Редактировать";
        })
        .catch(err => {
          alert("Не удалось обновить клиента: " + err.message);
        });
      } else {
        // Включаем режим редактирования
        const fullName = nameCell.textContent.trim();
        const nameParts = fullName.split(' ');
        const currentName = nameParts[0] || '';  // Имя - первое слово
        const currentSurname = nameParts.slice(1).join(' ') || '';  // Фамилия - все остальные слова

        const currentLogin = loginCell.textContent.trim();
        const currentPhone = phoneCell.textContent.trim();

        // Обновляем ячейки для редактирования
        nameCell.innerHTML = `
          <input type="text" class="form-control form-control-sm name-input" value="${currentName}">
          <input type="text" class="form-control form-control-sm surname-input" value="${currentSurname}">
        `;
        loginCell.innerHTML = `<input type="text" class="form-control form-control-sm" value="${currentLogin}">`;
        phoneCell.innerHTML = `<input type="text" class="form-control form-control-sm" value="${currentPhone}">`;

        button.textContent = "Сохранить";
      }
    });
  });
});

$(document).ready(function () {
    // Обработчик клика по кнопке удаления клиента
    $('.deleteClientBtn').click(function () {
        const clientId = $(this).closest('tr').data('user-id');  // Получаем ID клиента через родительский элемент tr

        // Подтверждение перед удалением
        if (confirm('Вы уверены, что хотите удалить этого клиента?')) {
            // Отправляем DELETE запрос на сервер
            $.ajax({
                url: '/delete-client/' + clientId,
                type: 'DELETE',
                success: function () {
                    alert('Клиент успешно удалён');
                    location.reload();  // Перезагружаем страницу для обновления списка
                },
                error: function () {
                    alert('Ошибка при удалении клиента');
                }
            });
        }
    });
});

$(document).ready(function () {
    // Обработчик клика по кнопке удаления услуги
    $('.deleteProcedureEmployeeBtn').click(function () {
        const procedureId = $(this).closest('tr').data('procedure-id');  // Получаем ID

        // Подтверждение перед удалением
        if (confirm('Вы уверены, что хотите удалить эту услугу?')) {
            // Отправляем DELETE запрос на сервер
            $.ajax({
                url: '/delete-procedure/' + procedureId,
                type: 'DELETE',
                success: function () {
                    alert('Услуга успешно удалена');
                    location.reload();  // Перезагружаем страницу для обновления списка
                },
                error: function () {
                    alert('Ошибка при удалении услуги');
                }
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const addProcedureForm = document.getElementById("addProcedureForm");
    if (addProcedureForm) { // Проверяем, существует ли форма
        addProcedureForm.addEventListener("submit", function (e) {
            e.preventDefault(); // Отменяем стандартную отправку формы

            const formData = new FormData(addProcedureForm);
            const name = formData.get("name");
            const price = formData.get("price");
            const exTimeStr = formData.get("exTime");  // Получаем время как строку HH:mm

            // Преобразуем время в минуты
            const [hours, minutes] = exTimeStr.split(":").map(Number);
            const exTimeMinutes = hours * 60 + minutes;

            fetch("/add-procedure", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    name: name,
                    price: price,
                    exTimeMinutes: exTimeMinutes // Отправляем как количество минут
                })
            })
            .then(response => {
                if (response.redirected) {
                    window.location.href = response.url; // Перенаправление на /lkemployee/procedures
                } else {
                    return response.text();
                }
            })
            .catch(error => {
                console.error("Ошибка при добавлении услуги:", error);
                showToast("Ошибка при добавлении услуги", "error");
            });
        });
    }

    function showToast(message, type) {
        const toast = document.createElement("div");
        toast.className = `toast ${type === "success" ? "toast-success" : "toast-error"}`;
        toast.innerHTML = `<div class="toast-body"><i class="bi ${type === "success" ? "bi-check-circle" : "bi-x-circle"}"></i> ${message}</div>`;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
});

document.addEventListener("DOMContentLoaded", function () {
    // Обработчик для кнопки "Редактировать"
    const editButtons = document.querySelectorAll(".edit-procedure-btn");

    editButtons.forEach(button => {
        button.addEventListener("click", function() {
            const row = this.closest('tr'); // Находим строку
            const id = row.querySelector('td').textContent.trim(); // Получаем ID услуги
            const nameCell = row.querySelector('.name-cell');
            const priceCell = row.querySelector('.price-cell');
            const exTimeCell = row.querySelector('.ex-time-cell');

            // Проверяем, был ли уже изменен элемент (если это уже инпуты)
            if (this.classList.contains("save-procedure-btn")) {
                // Если это кнопка "Сохранить", значит поля уже заменены, и мы собираем данные для обновления
                const updatedName = nameCell.querySelector('input').value.trim();
                const updatedPrice = priceCell.querySelector('input').value.trim();
                const updatedExTime = exTimeCell.querySelector('input').value.trim();

                // Отправка обновленных данных на сервер
                fetch(`/admin/update-procedure`, {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        id: id,
                        name: updatedName,
                        price: updatedPrice,
                        exTime: updatedExTime
                    })
                })
                .then(response => response.json())
                .then(data => {
                    // Обновляем отображение данных на странице
                    nameCell.innerHTML = `<span>${data.name}</span>`;
                    priceCell.innerHTML = `<span>${data.price}</span>`;
                    exTimeCell.innerHTML = `<span>${data.exTime}</span>`;

                    // Меняем кнопку обратно на "Редактировать"
                    button.textContent = 'Редактировать';
                    button.classList.remove('save-procedure-btn');
                    button.classList.add('edit-procedure-btn');

                    alert('Услуга обновлена!');
                })
                .catch(error => {
                    console.error("Ошибка при обновлении услуги:", error);
                    alert('Ошибка при обновлении услуги');
                });

            } else {
                // Если это кнопка "Редактировать", то заменяем текст на input для редактирования
                const name = nameCell.querySelector('span').textContent.trim();
                const price = priceCell.querySelector('span').textContent.trim();
                const exTime = exTimeCell.querySelector('span').textContent.trim();

                nameCell.innerHTML = `<input type="text" value="${name}" class="form-control" />`;
                priceCell.innerHTML = `<input type="number" value="${price}" class="form-control" />`;
                exTimeCell.innerHTML = `<input type="text" value="${exTime}" class="form-control" />`;

                // Меняем текст на кнопке на "Сохранить"
                button.textContent = 'Сохранить';
                button.classList.remove('edit-procedure-btn');
                button.classList.add('save-procedure-btn');
            }
        });
    });
});
