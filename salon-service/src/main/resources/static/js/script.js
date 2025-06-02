let selectedDuration = 0;
let selectedForm = null;
// Перевод времени в человекочитаемый формат
function formatDuration(duration) {
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;

    let result = '';
    if (hours > 0) {
        result += hours + ' час' + (hours > 1 ? 'а' : '') + (minutes > 0 ? ' ' : '');
    }
    if (minutes > 0 || hours === 0) {
        result += minutes + ' минут';
    }

    return result || '0 минут';  // Если длительность равна 0, показывать "0 минут"
}

// Перевод времени в минуты из ГГГГ-ММ-ДД
function convertToMinutes(durationStr) {
    const [hours, minutes] = durationStr.split(':').map(num => parseInt(num, 10));
    return (hours * 60) + minutes;  // Преобразуем в минуты
}

// Подгрузка мастеров по услуге
$('#procedure').on('change', function () {
    const selectedOption = $(this).find('option:selected');
    const price = selectedOption.data('price');
    const durationStr = selectedOption.data('duration');  // Получаем строку времени (например, "00:30")
    selectedDuration = convertToMinutes(durationStr);  // Преобразуем строку в минуты
    const procedureId = selectedOption.val();

    $('#procedurePriceLabel').text('Стоимость: ' + price + '₽');
    $('#procedureDurationLabel').text('Длительность: ' + formatDuration(selectedDuration)); // Выводим длительность
    $('#procedureDetails').show();

    if (procedureId) {
        $.ajax({
            url: '/api/masters-by-procedure',
            method: 'GET',
            data: { procedureId: procedureId },
            success: function (masters) {
                 const $masterSelect = $('#master');
                 $masterSelect.empty();
                 $masterSelect.prop('disabled', false);
                 $masterSelect.append('<option value="" disabled selected>Выберите мастера</option>');
                 masters.forEach(master => {
                     const fullName = `${master.surname} ${master.name}`;
                     $masterSelect.append(`<option value="${master.id}">${fullName}</option>`);
                 });
                 // очищаем кнопки дат
                 $('#date-buttons').empty();
             },
            error: function () {
                alert('Ошибка загрузки мастеров.');
            }
        });
    }
});

// Подгрузка дат для записи
$('#master').on('change', function () {
    const masterId = $(this).val();
    const procedureId = $('#procedure').val();

    if (masterId && procedureId && selectedDuration) {
        $.ajax({
            url: '/api/available-dates',
            method: 'GET',
            data: {
                masterId: masterId,
                procedureId: procedureId
            },
            success: function (dates) {
                const $dateButtons = $('#date-buttons');
                $dateButtons.empty();

               dates.forEach(dateStr => {
                    const dateObj = new Date(dateStr);
                    const formattedDate = dateObj.toLocaleDateString('ru-RU', {
                        day: 'numeric',
                        month: 'long'
                    });

                    const button = $('<button>')
                        .addClass('btn btn-outline-primary btn-pill me-2 mb-2')  // Класс для pills
                        .text(formattedDate)
                        .val(dateStr)
                        .on('click', function () {
                            $('#date-buttons button').removeClass('active');  // Убираем класс у всех
                            $(this).addClass('active');  // Добавляем класс к текущей
                            $('#date').val(dateStr).change();  // Устанавливаем значение даты
                        });

                    // Если эта дата уже выбрана, то сделать ее активной
                    if (dateStr === $('#date').val()) {
                        button.addClass('active');
                    }

                    $dateButtons.append(button);
                });
                $('#date-buttons').prop('disabled', false);
            },
            error: function () {
                alert('Ошибка загрузки доступных дат.');
            }
        });
    }
});

// Подгрузка временных окошек для записи
$('#date').on('change', function () {
    const selectedDate = $(this).val();
    const masterId = $('#master').val();
    const procedureId = $('#procedure').val();

    if (selectedDate && masterId && procedureId && selectedDuration) {
        $.ajax({
            url: '/api/available-slots',
            method: 'GET',
            data: {
                masterId: masterId,
                procedureId: procedureId,
                date: selectedDate
            },
            success: function (slots) {
                const $timeSelect = $('#time');
                $timeSelect.empty();
                $timeSelect.append('<option value="" disabled selected>Выберите время</option>');
                slots.forEach(slot => {
                    $timeSelect.append(`<option value="${slot}">${slot}</option>`);
                });
            },

            error: function () {
                alert('Ошибка загрузки слотов времени.');
            }
        });
    }
});

$('#date-buttons').on('click', 'button', function (event) {
    event.preventDefault();  // Предотвращаем стандартное поведение кнопки (отправку формы)

    const selectedDate = $(this).val();
    $('#date-buttons button').removeClass('active');  // Убираем активный класс у всех кнопок
    $(this).addClass('active');  // Добавляем активный класс к выбранной кнопке
    $('#date').val(selectedDate);  // Устанавливаем выбранную дату в скрытое поле

    // Теперь подгружаем доступные временные слоты для этой даты
    const masterId = $('#master').val();
    const procedureId = $('#procedure').val();
    const duration = selectedDuration;

    if (masterId && procedureId && selectedDate && duration) {
        $.ajax({
            url: '/api/available-slots',
            method: 'GET',
            data: {
                masterId: masterId,
                procedureId: procedureId,
                date: selectedDate
            },
            success: function (slots) {
                const $timeSelect = $('#time');
                $timeSelect.empty();
                $timeSelect.append('<option value="" disabled selected>Выберите время</option>');
                slots.forEach(slot => {
                    $timeSelect.append(`<option value="${slot}">${slot}</option>`);
                });
            },
            error: function () {
                alert('Ошибка загрузки слотов времени.');
            }
        });
    }
});

// Ограничение на ввод только цифр и максимум 10 символов
$('#clientPhone').on('input', function () {
    this.value = this.value.replace(/\D/g, '').slice(0, 10);
});

// Скрываем текст "сначала выберите мастера...", когда мастер выбран
$('#master').on('change', function () {
    $('#date-instruction').text('');
});

// Скрываем текст "сначала выберите услугу...", когда услуга выбрана
$('#procedure').on('change', function () {
    $('#master-instruction').text('');
});

// Скрываем текст "сначала выберите дату...", когда кнопка выбрана
$('#date-buttons').on('click', 'button', function () {
    $('#time-instruction').text('');
});

$('#booking-form').on('submit', function (event) {
    event.preventDefault();

    const clientName = $('#clientName').val();
    const clientSurname = $('#clientSurname').val();
    const clientPhone = $('#clientPhone').val();
    const procedureId = $('#procedure').val();
    const masterId = $('#master').val();
    const date = $('#date').val();
    const time = $('#time').val();

    const preferredMethod = $('input[name="preferredMethod"]:checked').val();
    const email = $('#email').val();
    const telegramUsername = $('#telegramUsername').val();

    // Проверка на заполненность всех обязательных полей
    if (!clientName || !clientSurname || !clientPhone || !procedureId || !masterId || !date || !time) {
        alert('Пожалуйста, заполните все поля.');
        return;
    }

    // Проверка заполненности соответствующего поля уведомления
    if (preferredMethod === "1" && !email) {
        alert('Пожалуйста, введите Email.');
        return;
    }
    if (preferredMethod === "2" && !telegramUsername) {
        alert('Пожалуйста, введите Telegram username.');
        return;
    }

    const requestData = {
        clientName: clientName,
        clientSurname: clientSurname,
        clientPhone: clientPhone,
        procedureId: procedureId,
        masterId: masterId,
        date: date,
        time: time,
        preferredMethod: preferredMethod
    };

    // Добавляем email или telegram в зависимости от выбора
    if (preferredMethod === "1") {
        requestData.email = email;
    } else if (preferredMethod === "2") {
        requestData.telegramUsername = telegramUsername;
    }

    $.ajax({
        url: '/submit-booking',
        method: 'POST',
        data: requestData,
        success: function (response) {
            alert('Вы успешно записались!');
            location.reload();
            $('#bookingModal').modal('hide');
            $('#booking-form')[0].reset();
            $('#procedureDetails').hide();
            $('#master').empty().prop('disabled', true);
            $('#date-buttons').empty();
            $('#time').empty();
        },
        error: function () {
            alert('Ошибка при записи');
        }
    });
});

$('#bookingModal').on('click', '.close', function () {
    $('#bookingModal').modal('hide');
});

// Подтверждение отмены
document.querySelectorAll('.cancel-btn').forEach(button => {
        button.addEventListener('click', event => {
            event.preventDefault();
            selectedForm = button.closest('form');
            const modal = new bootstrap.Modal(document.getElementById('confirmCancelModal'));
            modal.show();
        });
    });
const confirmCancelBtn = document.getElementById('confirmCancelBtn');
if (confirmCancelBtn) {
    confirmCancelBtn.addEventListener("click", function(e) {
        if (selectedForm) {
            selectedForm.submit();
        }
    });
}

// Отображение старых записей в ЛК
const toggleButton = document.getElementById("toggleButton");
if (toggleButton) {
    toggleButton.addEventListener("click", function(e) {
        e.preventDefault();

        const doneRows = document.querySelectorAll(".row-done");
        const cancelledRows = document.querySelectorAll(".row-cancelled");

        const isHidden = doneRows.length > 0 && doneRows[0].style.display === "none";

        doneRows.forEach(row => row.style.display = isHidden ? "table-row" : "none");
        cancelledRows.forEach(row => row.style.display = isHidden ? "table-row" : "none");

        this.textContent = isHidden ? "Скрыть прошлые записи" : "Посмотреть прошлые записи";
    });
}