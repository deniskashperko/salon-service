$(document).ready(function () {
    let masterId = $('#masterSelect').length > 0
        ? ($('#masterSelect').val() === "0" ? null : $('#masterSelect').val())  // если значение "0", ставим null
        : $('#masterContainer').data('master-id');  // иначе — берём из data-master-id

    let useLastMonth = false;
    let startDate = new Date();
    let endDate = new Date();

    // Устанавливаем начальный диапазон: год назад и сегодня
    function setInitialDateRange() {
        startDate = new Date();
        startDate.setFullYear(startDate.getFullYear() - 1); // год назад
        endDate = new Date(); // сегодня
    }

    // Обработчик изменения мастера в select
        $('#masterSelect').on('change', function () {
            masterId = $(this).val();  // обновляем переменную
            loadStatistics();          // и перезапрашиваем статистику
        });

    // Функция для получения первого дня месяца
    function getFirstDayOfMonth(date) {
        return new Date(date.getFullYear(), date.getMonth(), 1);
    }

    // Кнопка фильтра
    $('#filterLastMonth').click(function () {
        useLastMonth = !useLastMonth;

        if (useLastMonth) {
            $(this).addClass('btn-primary').removeClass('btn-outline-primary').text('Показать всё');
            // Устанавливаем диапазон на последний месяц
            startDate = new Date();
            startDate.setMonth(startDate.getMonth() - 1); // 1 месяц назад
            endDate = new Date(); // Сегодня
        } else {
            $(this).addClass('btn-outline-primary').removeClass('btn-primary').text('За последний месяц');
            // Устанавливаем диапазон на последний год
            setInitialDateRange();  // вернуться к диапазону за год
        }

        loadStatistics();
    });

    // Устанавливаем начальные значения для диапазона дат (последний год)
    setInitialDateRange();

    // Первая загрузка статистики
    loadStatistics();

    function loadStatistics() {
        const params = {
            masterId,
            startDate: startDate.toISOString().split('T')[0],  // форматируем в yyyy-MM-dd
            endDate: endDate.toISOString().split('T')[0]  // форматируем в yyyy-MM-dd
        };

        // Заявки
        $.get('/api/statistics/requests', params, function (data) {
            $('#totalRequests').text(data.total);
            $('#activeRequests').text(data.active);
            $('#completedRequests').text(data.completed);
            $('#cancelledRequests').text(data.cancelled);
        }).fail(function () {
            $('#totalRequests, #activeRequests, #completedRequests, #cancelledRequests').text('Ошибка');
        });

        // Распределение по услугам
        $.get('/api/statistics/requests-by-procedure', params, function (data) {
            const container = $('#procedureStats');
            container.empty();

            if ($.isEmptyObject(data)) {
                container.append('<p>Нет данных об услугах.</p>');
            } else {
                $.each(data, function (procedure, count) {
                    container.append(`<div class="mb-2"><strong>${procedure}:</strong> ${count}</div>`);
                });
            }
        }).fail(function () {
            $('#procedureStats').html('<p>Ошибка загрузки данных об услугах.</p>');
        });

        // Выручка и уникальные клиенты
        $.get('/api/statistics/revenue-and-clients', params, function (data) {
            $('#revenue').text(data.revenue + ' ₽');
            $('#uniqueClients').text(data.uniqueClients);
        }).fail(function () {
            $('#revenue, #uniqueClients').text('Ошибка');
        });

        // Средняя стоимость услуги
        $.get('/api/statistics/average-service-price', params, function (data) {
            $('#averageServicePrice').text(Math.round(data) + ' ₽');
        }).fail(function () {
            $('#averageServicePrice').text('Ошибка');
        });

        // Топ популярных услуг
        $.get('/api/statistics/top-popular-services', params, function (data) {
            const container = $('#topPopularServices');
            container.empty();

            if (data.length === 0) {
                container.append('<p>Нет данных.</p>');
            } else {
                data.slice(0, 2).forEach(function (item) {
                    container.append(`<div class="mb-2"><strong>${item.name}:</strong> ${item.count} заявок</div>`);
                });
            }
        }).fail(function () {
            $('#topPopularServices').html('<p>Ошибка загрузки популярных услуг.</p>');
        });

        // 2 самые редкие услуги
        $.get('/api/statistics/least-popular-services', params, function (data) {
            const container = $('#leastPopularServices');
            container.empty();

            if (data.length === 0) {
                container.append('<p>Нет данных.</p>');
            } else {
                data.slice(0, 2).forEach(function (item) {
                    container.append(`<div class="mb-2"><strong>${item.name}:</strong> ${item.count} заявок</div>`);
                });
            }
        }).fail(function () {
            $('#leastPopularServices').html('<p>Ошибка загрузки редких услуг.</p>');
        });

        // Постоянные клиенты
        $.get('/api/statistics/repeat-clients', params, function (data) {
            const container = $('#repeatClients');
            container.empty();

            if (data.length === 0) {
                container.append('<p>Нет постоянных клиентов.</p>');
            } else {
                data.forEach(function (item) {
                    let fullName = typeof item.client === 'object'
                        ? `${item.client.name} ${item.client.surname}`
                        : item.client;
                    container.append(`<div class="mb-2"><strong>${fullName}:</strong> ${item.count} выполненных заявок</div>`);
                });
            }
        }).fail(function () {
            $('#repeatClients').html('<p>Ошибка загрузки данных о клиентах.</p>');
        });
    }
});
