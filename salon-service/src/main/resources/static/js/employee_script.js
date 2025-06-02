// Обработчик смены пароля
$(document).ready(function() {
    $("form[action='/change-password']").submit(function(event) {
        event.preventDefault(); // предотвращаем стандартную отправку формы

        var oldPassword = $("#oldPassword").val();
        var newPassword = $("#newPassword").val();
        var confirmPassword = $("#confirmPassword").val();

        // Проверка на совпадение паролей
        if (newPassword !== confirmPassword) {
            alert("Новый пароль и его повтор не совпадают!");  // Показываем alert в случае ошибки
            return;
        }

        // Отправляем форму через AJAX
        $.ajax({
            url: '/change-password',
            type: 'POST',
            data: {
                oldPassword: oldPassword,
                newPassword: newPassword,
                confirmPassword: confirmPassword
            },
            success: function(response) {
                if (response.success) {
                    alert("Пароль успешно изменён!"); // Показываем alert о успешной смене пароля

                    // Закрываем окно изменения пароля
                    $('#changePasswordModal').modal('hide');  // Закрываем модальное окно

                    // Очищаем поля формы
                    $("#oldPassword").val('');
                    $("#newPassword").val('');
                    $("#confirmPassword").val('');

                } else {
                    // Если возникла ошибка, показываем alert с ошибкой
                    alert("Ошибка смены пароля!");
                }
            },
            error: function() {
                // Если произошла ошибка при запросе, показываем alert
                alert("Неверный прошлый пароль!");
            }
        });
    });
});

// Функция для добавления услуги мастеру
document.addEventListener("DOMContentLoaded", function() {
    const saveBtn = document.getElementById("saveProcedureBtn");
    const cancelBtn = document.getElementById("cancelProcedureBtn");
    const addProcedureBlock = document.getElementById("addProcedureBlock");
    const procedureSelect = document.getElementById("procedureSelect");

    // Проверка на существование cancelBtn
    if (cancelBtn) {
        // Отмена добавления — просто очищаем выбор
        cancelBtn.addEventListener("click", function() {
            procedureSelect.selectedIndex = 0; // Очищаем селектор
        });
    }

    // Проверка на существование saveBtn
    if (saveBtn) {
        // Сохранение новой услуги
        saveBtn.addEventListener("click", function() {
            const selectedProcedureId = procedureSelect.value;

            if (!selectedProcedureId) {
                alert("Пожалуйста, выберите услугу.");
                return;
            }

            fetch('/add-procedure-for-master', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    procedureId: selectedProcedureId
                })
            })
            .then(response => {
                if (response.ok) {
                    location.reload(); // Перезагрузка страницы для обновления списка
                } else {
                    alert("Ошибка при добавлении услуги.");
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert("Ошибка при добавлении услуги.");
            });
        });
    }
});

// Функция для удаления услуги у мастера
document.addEventListener("DOMContentLoaded", function () {
    const deleteButtons = document.querySelectorAll('.deleteProcedureBtn');

    // Проверка на существование кнопок удаления
    if (deleteButtons.length > 0) {
        deleteButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                const procedureId = button.getAttribute('data-procedure-id');

                if (confirm("Вы уверены, что хотите удалить эту услугу?")) {
                    fetch('/delete-procedure-for-master?procedureId=' + procedureId, {
                        method: 'POST'
                    })
                    .then(response => {
                        if (response.ok) {
                            button.closest('tr').remove();
                            alert("Услуга успешно удалена из списка оказываемых услуг.");
                            location.reload(); // перезагрузка страницы
                        } else {
                            alert("Ошибка при удалении услуги.");
                        }
                    })
                    .catch(error => {
                        console.error('Ошибка:', error);
                        alert("Ошибка при удалении услуги.");
                    });
                }
            });
        });
    }
});

// Функция для фильтрации клиентской базы
document.addEventListener('DOMContentLoaded', function () {
    const nameInput = document.getElementById('nameSearch');
    const phoneInput = document.getElementById('phoneSearch');
    const table = document.getElementById('clientsTable');

    if (!table) return; // Если таблицы нет — выходим из функции

    const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

    function filterTable() {
        const nameFilter = nameInput?.value.toLowerCase() || "";
        const phoneFilter = phoneInput?.value.toLowerCase() || "";

        Array.from(rows).forEach(row => {
            const cells = row.getElementsByTagName('td');
            if (cells.length === 0) return; // пропускаем сообщение "Нет клиентов"

            const fullName = cells[0].textContent.toLowerCase();
            const phone = cells[cells.length - 2]?.textContent.toLowerCase() || ""; // Учитываем, что у мастера номер телефона в 2-ом или 3-ем столбце, а у админа - в 3-ем или 4-ом

            const matchesName = fullName.includes(nameFilter);
            const matchesPhone = phone.includes(phoneFilter);

            row.style.display = (matchesName || nameFilter === "") && (matchesPhone || phoneFilter === "") ? "" : "none";
        });
    }

    nameInput?.addEventListener('input', filterTable);
    phoneInput?.addEventListener('input', filterTable);
});

// Функция для отображения записей клиентов внутри базы
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll('.view-records-btn').forEach(button => {
    button.addEventListener('click', () => {
      const userId = button.getAttribute('data-user-id');
      const table = document.getElementById('recordsTable');
      const tableBody = document.getElementById('recordsTableBody');
      const noRecordsMessage = document.getElementById('noRecordsMessage');

      // Показываем таблицу и скрываем сообщение
      table.style.display = 'table';
      noRecordsMessage.style.display = 'none';
      tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Загрузка...</td></tr>`;

      fetch(`/get-client-records/${userId}`)
        .then(response => {
          if (!response.ok) {
            throw new Error('Ошибка сервера');
          }
          return response.json();
        })
        .then(data => {
          if (!data || data.length === 0) {
            // Скрываем таблицу и показываем сообщение
            table.style.display = 'none';
            noRecordsMessage.style.display = 'block';
            noRecordsMessage.innerText = 'Нет записей';
          } else {
            tableBody.innerHTML = '';

            const statusMap = {
              AWAIT: 'Ожидает',
              DONE: 'Выполнена',
              CANCELLED: 'Отменена'
            };

            data.forEach(record => {
              const dateObj = new Date(record.date);
              const formattedDate = dateObj.toLocaleDateString('ru-RU');

              // Преобразуем record.time в формат ЧЧ:ММ
              const timeObj = new Date(`1970-01-01T${record.time}`);
              const formattedTime = timeObj.toLocaleTimeString('ru-RU', {
                hour: '2-digit',
                minute: '2-digit'
              });

              const row = `<tr>
                <td>${formattedDate}</td>
                <td>${formattedTime}</td>
                <td>${record.procedure?.name || '-'}</td>
                <td>${record.master ? `${record.master.name} ${record.master.surname}` : 'Имя Фамилия'}</td>
                <td>${statusMap[record.status] || record.status}</td>
                <td>${record.feedback || '-'}</td>
              </tr>`;
              tableBody.insertAdjacentHTML('beforeend', row);
            });
          }
        })
        .catch(() => {
          table.style.display = 'none';
          noRecordsMessage.style.display = 'block';
          noRecordsMessage.innerText = 'Нет записей';
          noRecordsMessage.classList.add('text-danger');
        });
    });
  });
});

// Функция для отображения дней графика
// Загружаем дни графика по ID мастера
function loadScheduleDates(masterId) {
    if (!masterId) return;

    fetch(`/get-schedule-dates?masterId=${masterId}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`Ошибка при загрузке дат: ${response.status} ${response.statusText} - ${text}`);
                });
            }
            return response.json();
        })
        .then(dates => {
            if (!Array.isArray(dates)) {
                throw new Error("Ожидался массив дат, но получено: " + JSON.stringify(dates));
            }

            const dateContainer = document.getElementById("dateButtons");
            dateContainer.innerHTML = ''; // Очищаем предыдущие кнопки

            dates.forEach(date => {
                const dateObj = new Date(date);

                const weekday = dateObj.toLocaleDateString('ru-RU', { weekday: 'short' });
                const formattedDate = dateObj.toLocaleDateString('ru-RU');

                const btn = document.createElement("button");
                btn.className = "btn btn-outline-dark fw-semibold m-1";
                btn.textContent = `${weekday} ${formattedDate}`;
                btn.dataset.date = date;

                btn.style.borderWidth = "2px";

                btn.addEventListener("click", () => {
                    const allDateButtons = document.querySelectorAll("#dateButtons button");
                    allDateButtons.forEach(b => {
                        b.classList.remove("bg-dark", "text-white");
                        b.classList.add("btn-outline-dark");
                    });

                    btn.classList.remove("btn-outline-dark");
                    btn.classList.add("bg-dark", "text-white");

                    onDateClick(date, masterId);
                });

                dateContainer.appendChild(btn);
            });
        })
        .catch(error => {
            console.error("Ошибка загрузки дат:", error);
        });
}

document.addEventListener("DOMContentLoaded", function () {
    const scheduleContainer = document.getElementById("scheduleContainer");
    const masterSelect = document.getElementById("selectMaster");

    if (masterSelect) {
        // Обработка изменения выбора мастера
        masterSelect.addEventListener("change", function () {
            const selectedMasterId = this.value;
            loadScheduleDates(selectedMasterId);
        });

        // Если мастер уже выбран при загрузке
        if (masterSelect.value) {
            loadScheduleDates(masterSelect.value);
        }
    } else if (scheduleContainer && scheduleContainer.dataset.masterId) {
        // Если select-а нет, но есть data-master-id
        loadScheduleDates(scheduleContainer.dataset.masterId);
    }
});

// Функция для бронирования окошек мастером
function onDateClick(selectedDate, masterId) {
    const blockBtn = document.getElementById("blockSlotsBtn");
    const selectDateMessage = document.getElementById("selectDateMessage");

    // Скрываем сообщение, если дата выбрана
    selectDateMessage.style.display = 'none';

    fetch(`/get-schedule-slots?masterId=${masterId}&date=${selectedDate}`)
        .then(response => response.json())
        .then(slots => {
            const slotContainer = document.getElementById("slotButtons");
            blockingMode = false;
            selectedSlotIds = [];
            blockBtn.textContent = "Заблокировать окошки";
            slotContainer.innerHTML = '';

            // Фильтруем слоты с статусом "FREE"
            const freeSlots = slots.filter(slot => slot.status === "FREE");

            // Показываем кнопку, если есть хотя бы один слот со статусом "FREE"
            if (freeSlots.length > 0) {
                blockBtn.style.display = 'block';
            } else {
                blockBtn.style.display = 'none';
            }

            // Показываем сообщение, если нет слотов
            if (slots.length === 0) {
                selectDateMessage.style.display = 'block';
            }

            if (!Array.isArray(slots) || slots.length === 0) {
                slotContainer.innerHTML = "<p>Нет окошек на выбранную дату.</p>";
                return;
            }

            slots.sort((a, b) => a.time.localeCompare(b.time));

            slots.forEach(slot => {
                const btn = document.createElement("button");
                btn.textContent = slot.time.substring(0, 5);
                btn.classList.add("btn", "m-1", "fw-semibold");
                btn.setAttribute("data-slot-id", slot.id);
                btn.setAttribute("data-slot-status", slot.status);

                btn.style.borderWidth = "2px";
                btn.style.borderStyle = "solid";
                btn.style.backgroundColor = "transparent";
                btn.style.boxShadow = "none";

                if (slot.status === "FREE") {
                    btn.style.borderColor = "#117002";
                    btn.style.color = "#117002";
                    btn.disabled = true;
                } else if (slot.status === "TAKEN") {
                    btn.style.borderColor = "#f00";
                    btn.style.color = "#f00";
                    btn.disabled = true;
                } else if (slot.status === "OLD") {
                    btn.style.opacity = "0.2";
                    btn.style.pointerEvents = "none";
                    btn.disabled = true;
                }

                slotContainer.appendChild(btn);
            });

            blockBtn.onclick = () => {
                if (!blockingMode) {
                    blockingMode = true;
                    selectedSlotIds = [];
                    blockBtn.textContent = "Подтвердить";

                    const allButtons = slotContainer.querySelectorAll("button");

                    allButtons.forEach(btn => {
                        const status = btn.getAttribute("data-slot-status");
                        const id = btn.getAttribute("data-slot-id");

                        if (status === "FREE") {
                            btn.disabled = false;

                            btn.onmouseenter = () => {
                                if (!btn.classList.contains("selected")) {
                                    btn.style.borderColor = "#0400e5";
                                    btn.style.color = "#0400e5";
                                }
                            };

                            btn.onmouseleave = () => {
                                if (!btn.classList.contains("selected")) {
                                    btn.style.borderColor = "#117002";
                                    btn.style.color = "#117002";
                                }
                            };

                            btn.onclick = () => {
                                btn.classList.toggle("selected");
                                const isSelected = btn.classList.contains("selected");

                                btn.style.backgroundColor = isSelected ? "#0400e5" : "transparent";
                                btn.style.color = isSelected ? "white" : "#117002";
                                btn.style.borderColor = isSelected ? "#0400e5" : "#117002";

                                if (isSelected) {
                                    selectedSlotIds.push(id);
                                } else {
                                    selectedSlotIds = selectedSlotIds.filter(sid => sid !== id);
                                }
                            };
                        }
                    });
                } else {
                    blockingMode = false;
                    blockBtn.textContent = "Заблокировать окошки";
                    const allButtons = slotContainer.querySelectorAll("button");
                    allButtons.forEach(btn => {
                        const status = btn.getAttribute("data-slot-status");
                        if (status === "FREE") {
                            btn.disabled = true;
                            btn.onmouseenter = null;
                            btn.onmouseleave = null;
                            btn.onclick = null;

                            btn.classList.remove("selected");
                            btn.style.backgroundColor = "transparent";
                            btn.style.color = "#117002";
                            btn.style.borderColor = "#117002";
                        }
                    });

                    if (selectedSlotIds.length > 0) {
                        fetch("/block-slots", {
                            method: "POST",
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ slotIds: selectedSlotIds })
                        })
                        .then(response => {
                            if (!response.ok) throw new Error("Ошибка при обновлении слотов");
                            alert("Окошки успешно заблокированы!");
                            location.reload();
                        })
                        .catch(error => console.error("Ошибка:", error));
                    }
                }
            };
        })
        .catch(error => console.error("Ошибка загрузки слотов:", error));
}

// Функция для отмены заявок
document.querySelectorAll('.cancelReqButton').forEach(button => {
    button.addEventListener('click', function () {
        // Получаем requestId из скрытого столбца в той же строке
        const requestId = this.closest('tr').querySelector('td:last-child').innerText;

        // Проверим, что id действительно передается
        if (requestId) {
            if (confirm("Вы действительно хотите отменить запись с ID: " + requestId)) {
                // Делаем POST-запрос для отмены записи
                fetch(`/lkemployee/employee-cancel-request?requestId=${requestId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                })
                .then(response => {
                    if (response.ok) {
                        alert('Заявка отменена успешно');
                        location.reload(); // Перезагружаем страницу
                    } else {
                        alert('Ошибка при отмене заявки');
                    }
                })
                .catch(error => {
                    alert('Ошибка связи с сервером');
                });
            }
        }
    });
});
