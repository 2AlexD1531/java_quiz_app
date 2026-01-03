// Глобальное состояние приложения
const AppState = {
    currentQuiz: null,
    currentQuestions: [],
    userAnswers: {},
    quizResult: null,
    currentQuestionIndex: 0,
    currentUser: null,
    isAdminMode: false
};

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Проверяем авторизацию при загрузке
    checkAuthStatus();
});

// Проверка статуса авторизации
async function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const adminToken = localStorage.getItem('adminToken');

    if (token) {
        QuizAPI.token = token;
        try {
            AppState.currentUser = await QuizAPI.getCurrentUser();
            updateUserGreeting();
            
            // Проверяем роль пользователя
            if (AppState.currentUser.role === 'ROLE_ADMIN' && adminToken) {
                // Автоматически входим в админ-режим если есть adminToken
                AppState.isAdminMode = true;
                showScreen('adminMainMenu');
            } else {
                AppState.isAdminMode = false;
                showScreen('mainMenu');
            }
            
            loadSavedQuizzes();
        } catch (error) {
            console.error('Auth check failed:', error);
            showScreen('welcome');
        }
    } else {
        showScreen('welcome');
    }
}

// Обновление приветствия пользователя
function updateUserGreeting() {
    const greetingElement = document.getElementById('userGreeting');
    const adminGreetingElement = document.getElementById('adminGreeting');
    
    if (greetingElement && AppState.currentUser) {
        greetingElement.textContent = AppState.currentUser.username;
    }
    if (adminGreetingElement && AppState.currentUser) {
        adminGreetingElement.textContent = AppState.currentUser.username;
    }
}

// Навигация между экранами
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.classList.remove('active');
        screen.style.opacity = '0';
    });

    const activeScreen = document.getElementById(screenId);
    activeScreen.classList.add('active');

    // Анимация появления
    setTimeout(() => {
        activeScreen.style.opacity = '1';
    }, 50);
}

// Аутентификация пользователя
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        alert('Пожалуйста, заполните все поля');
        return;
    }

    showLoading('Выполняем вход...');

    try {
        const response = await QuizAPI.login(username, password);
        AppState.currentUser = response.userDTO;
        AppState.isAdminMode = false;
        
        // Сохраняем обычный токен
        localStorage.setItem('authToken', response.token);
        localStorage.removeItem('adminToken'); // Удаляем админский токен если был
        
        hideLoading();
        updateUserGreeting();
        showScreen('mainMenu');
        loadSavedQuizzes();
    } catch (error) {
        hideLoading();
        alert('Ошибка входа: ' + error.message);
    }
}

// Аутентификация администратора
async function adminLogin() {
    const username = document.getElementById('adminUsername').value;
    const password = document.getElementById('adminPassword').value;

    if (!username || !password) {
        alert('Пожалуйста, заполните все поля');
        return;
    }

    showLoading('Проверяем права администратора...');

    try {
        const response = await QuizAPI.login(username, password);
        
        // Проверяем, что пользователь действительно администратор
        if (response.userDTO.role !== 'ROLE_ADMIN') {
            throw new Error('У вас нет прав администратора');
        }
        
        AppState.currentUser = response.userDTO;
        AppState.isAdminMode = true;
        
        // Сохраняем оба токена
        localStorage.setItem('authToken', response.token);
        localStorage.setItem('adminToken', response.token); // Отдельный маркер для админа
        
        hideLoading();
        updateUserGreeting();
        showScreen('adminMainMenu');
        
    } catch (error) {
        hideLoading();
        alert('Ошибка входа администратора: ' + error.message);
    }
}

// Выход администратора
function adminLogout() {
    if (confirm('Вы уверены, что хотите выйти из режима администратора?')) {
        localStorage.removeItem('adminToken');
        AppState.isAdminMode = false;
        showScreen('mainMenu');
    }
}

// Переключение в режим пользователя
function switchToUserMode() {
    if (confirm('Перейти в режим обычного пользователя?')) {
        localStorage.removeItem('adminToken');
        AppState.isAdminMode = false;
        showScreen('mainMenu');
    }
}

// Умная кнопка "Назад" в зависимости от роли
function goBackBasedOnRole() {
    if (AppState.isAdminMode) {
        showScreen('adminMainMenu');
    } else {
        showScreen('mainMenu');
    }
}

// Обновлённая функция выхода
function logout() {
    if (confirm('Вы уверены, что хотите выйти?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('adminToken');
        AppState.currentUser = null;
        AppState.isAdminMode = false;
        showScreen('welcome');
    }
}

// Обновлённая функция регистрации
async function register() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;

    if (!username || !email || !password || !confirmPassword) {
        alert('Пожалуйста, заполните все поля');
        return;
    }

    if (password !== confirmPassword) {
        alert('Пароли не совпадают');
        return;
    }

    showLoading('Регистрируем пользователя...');

    try {
        const response = await QuizAPI.register(username, email, password);
        AppState.currentUser = response.userDTO;
        AppState.isAdminMode = false;
        
        localStorage.setItem('authToken', response.token);
        localStorage.removeItem('adminToken');
        
        hideLoading();
        updateUserGreeting();
        showScreen('mainMenu');
        loadSavedQuizzes();
    } catch (error) {
        hideLoading();
        alert('Ошибка регистрации: ' + error.message);
    }
}

// Главное меню
function exitApp() {
    if (confirm('Вы уверены, что хотите выйти?')) {
        document.body.innerHTML = '<div class="container" style="text-align: center; padding: 50px;"><h1>👋 До свидания!</h1></div>';
    }
}

// Функции для управления загрузкой
function showLoading(message = 'Генерируем тест...') {
    const overlay = document.getElementById('loadingOverlay');
    const text = overlay.querySelector('.loading-text');
    text.textContent = message;
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    overlay.classList.remove('active');
    document.body.style.overflow = '';
}

// Функция для блокировки кнопки с анимацией
function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.classList.add('btn-loading');
        button.innerHTML = '⏳';
    } else {
        button.disabled = false;
        button.classList.remove('btn-loading');
        button.innerHTML = 'Сгенерировать тест';
    }
}

// Генерация теста
async function generateQuiz() {
    const topics = document.getElementById('topics').value;
    const questionCount = parseInt(document.getElementById('questionCount').value);
    const difficulty = document.getElementById('difficulty').value;
    const generateButton = document.querySelector('#generateQuiz .btn-primary');

    if (!topics) {
        alert('Пожалуйста, введите темы для теста');
        return;
    }

    if (!difficulty) {
        alert('Пожалуйста, введите сложность теста');
        return;
    }

    // Показываем анимацию загрузки
    showLoading('Генерируем вопросы...');
    setButtonLoading(generateButton, true);

    try {
        const quiz = await QuizAPI.generateQuiz(topics, difficulty, questionCount);
        AppState.currentQuiz = quiz;
        AppState.currentQuestions = convertQuizToMapList(quiz);
        AppState.userAnswers = {};
        AppState.currentQuestionIndex = 0;

        // Небольшая задержка чтобы анимация была заметной
        setTimeout(() => {
            hideLoading();
            setButtonLoading(generateButton, false);
            startQuiz();
        }, 500);

    } catch (error) {
        hideLoading();
        setButtonLoading(generateButton, false);
        alert('Ошибка при генерации теста: ' + error.message);
    }
}

// Загрузка сохраненных тестов
async function loadSavedQuizzes() {
    const quizzesList = document.getElementById('quizzesList');
    quizzesList.innerHTML = `
        <div class="loading">
            <div class="loading-spinner" style="width: 30px; height: 30px; margin: 0 auto;"></div>
            <div>Загружаем тесты...</div>
        </div>
    `;

    try {
        const quizzes = await QuizAPI.getAllQuizzes();
        displayQuizzesList(quizzes);
    } catch (error) {
        quizzesList.innerHTML = '<div class="error">Ошибка загрузки тестов</div>';
    }
}

// Запуск теста
function startQuiz() {
    if (!AppState.currentQuestions.length) {
        alert('Нет вопросов для теста');
        return;
    }

    document.getElementById('quizTitle').textContent =
        AppState.currentQuiz?.title || 'Новый тест';
    document.getElementById('totalQuestions').textContent =
        AppState.currentQuestions.length;

    showScreen('quizRunner');
    displayCurrentQuestion();
}

// Отображение текущего вопроса
function displayCurrentQuestion() {
    const question = AppState.currentQuestions[AppState.currentQuestionIndex];
    const questionNumber = AppState.currentQuestionIndex + 1;

    document.getElementById('currentQuestion').textContent = questionNumber;
    document.getElementById('questionText').textContent = question.text;

    const optionsContainer = document.getElementById('optionsContainer');
    optionsContainer.innerHTML = '';

    question.options.forEach((option, index) => {
        const optionChar = String.fromCharCode(65 + index); // A, B, C, D
        const optionElement = document.createElement('div');
        optionElement.className = 'option';
        optionElement.innerHTML = `
            <strong>${optionChar}</strong>. ${option}
        `;
        optionElement.onclick = () => selectAnswer(optionChar);

        // Подсветка выбранного ответа
        if (AppState.userAnswers[questionNumber] === optionChar) {
            optionElement.classList.add('selected');
        }

        optionsContainer.appendChild(optionElement);
    });

    // Управление кнопками навигации
    document.getElementById('prevBtn').disabled = AppState.currentQuestionIndex === 0;
    document.getElementById('nextBtn').style.display =
        AppState.currentQuestionIndex < AppState.currentQuestions.length - 1 ? 'block' : 'none';
    document.getElementById('finishBtn').style.display =
        AppState.currentQuestionIndex === AppState.currentQuestions.length - 1 ? 'block' : 'none';
}

// Выбор ответа
function selectAnswer(answer) {
    const questionNumber = AppState.currentQuestionIndex + 1;
    AppState.userAnswers[questionNumber] = answer;
    displayCurrentQuestion(); // Обновляем подсветку
}

// Навигация по вопросам
function nextQuestion() {
    if (AppState.currentQuestionIndex < AppState.currentQuestions.length - 1) {
        AppState.currentQuestionIndex++;
        displayCurrentQuestion();
    }
}

function previousQuestion() {
    if (AppState.currentQuestionIndex > 0) {
        AppState.currentQuestionIndex--;
        displayCurrentQuestion();
    }
}

// Завершение теста
async function finishQuiz() {
    const finishButton = document.getElementById('finishBtn');
    setButtonLoading(finishButton, true);
    finishButton.innerHTML = '⏳ Проверяем ответы...';

    try {
        const result = await QuizAPI.submitQuiz(
            AppState.currentQuestions,
            AppState.userAnswers
        );

        AppState.quizResult = result;

        // Небольшая задержка для плавного перехода
        setTimeout(() => {
            setButtonLoading(finishButton, false);
            finishButton.innerHTML = 'Завершить тест';
            showResults();
        }, 300);

    } catch (error) {
        setButtonLoading(finishButton, false);
        finishButton.innerHTML = 'Завершить тест';
        alert('Ошибка при отправке теста: ' + error.message);
    }
}

// Показ результатов
function showResults() {
    const resultsContent = document.getElementById('resultsContent');
    const score = AppState.quizResult.score;
    const totalQuestions = AppState.quizResult.totalQuestions;
    const percentage = (score / totalQuestions) * 100;

    let grade = '';
    if (percentage >= 90) grade = '🎉 ОТЛИЧНО!';
    else if (percentage >= 70) grade = '👍 ХОРОШО!';
    else if (percentage >= 50) grade = '⚠️ УДОВЛЕТВОРИТЕЛЬНО';
    else grade = '❌ НЕУДОВЛЕТВОРИТЕЛЬНО';

    resultsContent.innerHTML = `
        <div class="results-summary">
            <div class="score">${score}/${totalQuestions}</div>
            <div class="percentage">${percentage.toFixed(1)}% правильных ответов</div>
            <div class="grade">${grade}</div>
        </div>
    `;

    showScreen('quizResults');
}

// Детальные результаты
function viewDetailedResults() {
    if (!AppState.quizResult) {
        alert('Нет данных о последнем тесте!');
        return;
    }

    const detailedContent = document.getElementById('detailedContent');
    let html = '';

    AppState.currentQuestions.forEach((question, index) => {
        const questionNumber = index + 1;
        const userAnswer = AppState.userAnswers[questionNumber];
        const correctAnswer = question.correctAnswer;
        const isCorrect = userAnswer === correctAnswer;

        html += `
            <div class="question-result ${isCorrect ? 'correct' : 'incorrect'}">
                <h4>${isCorrect ? '✅' : '❌'} Вопрос ${questionNumber}</h4>
                <p><strong>Вопрос:</strong> ${question.text}</p>
                <p><strong>Ваш ответ:</strong> ${userAnswer || 'Нет ответа'}</p>
                <p><strong>Правильный ответ:</strong> ${correctAnswer}</p>
                ${!isCorrect ? `<p><strong>Объяснение:</strong> ${question.explanation || 'Объяснение недоступно'}</p>` : ''}
                <p><strong>Тэги:</strong> ${question.tags?.join(', ') || 'Нет тэгов'}</p>
            </div>
        `;
    });

    detailedContent.innerHTML = html;
    showScreen('detailedResults');
}

// Удаление теста
async function deleteQuiz(quizId, quizTitle, event) {
     event.stopPropagation(); // Предотвращаем запуск теста при клике на кнопку удаления

     if (!confirm(`Вы уверены, что хотите удалить тест "${quizTitle}"?`)) {
         return;
     }

    try {
            const result = await QuizAPI.deleteQuiz(quizId);

            // Обрабатываем разные форматы ответа
            const message = typeof result === 'string' ? result : result.message;
            alert(message || 'Тест успешно удален!');

            // Перезагружаем список тестов
            await loadSavedQuizzes();
        } catch (error) {
            console.error('Delete quiz error:', error);
            alert('Ошибка при удалении теста: ' + error.message);
        }
}

// Отображение списка тестов
function displayQuizzesList(quizzes) {
    const quizzesList = document.getElementById('quizzesList');

    if (!quizzes || quizzes.length === 0) {
        quizzesList.innerHTML = '<div class="loading">Нет сохраненных тестов</div>';
        return;
    }

    quizzesList.innerHTML = quizzes.map(quiz => `
        <div class="quiz-card" onclick="runSavedQuiz(${JSON.stringify(quiz).replace(/"/g, '&quot;')})">
            <div class="quiz-header">
                <h3>${quiz.title}</h3>
                <button class="btn-delete" onclick="deleteQuiz(${quiz.id}, '${quiz.title.replace(/'/g, "\\'")}', event)">
                    🗑️ Удалить
                </button>
            </div>
            <p><strong>Описание:</strong> ${quiz.description}</p>
            <p><strong>Тэги:</strong> ${quiz.tags?.join(', ') || 'Нет тэгов'}</p>
            <p><strong>Вопросов:</strong> ${quiz.questions?.length || 0}</p>
            <p><strong>Сложность:</strong> ${quiz.difficulty}</p>
            <p><strong>ID:</strong> ${quiz.id}</p>
        </div>
    `).join('');
}

// Функция для обновления списка тестов
async function refreshQuizzesList() {
    const quizzesList = document.getElementById('quizzesList');
    quizzesList.innerHTML = '<div class="loading">Загрузка тестов...</div>';
    await loadSavedQuizzes();
}

// Запуск сохраненного теста
function runSavedQuiz(quiz) {
    AppState.currentQuiz = quiz;
    AppState.currentQuestions = convertQuizToMapList(quiz);
    AppState.userAnswers = {};
    AppState.currentQuestionIndex = 0;
    startQuiz();
}

// Вспомогательные функции
function convertQuizToMapList(quiz) {
    if (!quiz.questions) return [];

    return quiz.questions.map(question => ({
        text: question.text,
        options: question.options || [],
        correctAnswer: question.correctAnswer,
        explanation: question.explanation,
        tags: question.tags ? Array.from(question.tags) : [],
        difficulty: question.difficulty
    }));
}

// Переключение между экранами аутентификации
function showLogin() {
    showScreen('login');
}

function showRegister() {
    showScreen('register');
}

// Функция для быстрого доступа к главному меню
function goToMainMenu() {
    if (AppState.currentUser) {
        if (AppState.isAdminMode) {
            showScreen('adminMainMenu');
        } else {
            showScreen('mainMenu');
        }
    } else {
        showScreen('welcome');
    }
}

// АДМИН-ФУНКЦИОНАЛ

// Показать панель администратора
function showAdminPanel() {
    showScreen('adminPanel');
}

// Показать список пользователей
function showUsers() {
    showScreen('usersScreen');
    loadSavedUsers();
}

// Загрузка пользователей
async function loadSavedUsers() {
    const usersList = document.getElementById('usersList');
    usersList.innerHTML = `
        <div class="loading">
            <div class="loading-spinner" style="width: 30px; height: 30px; margin: 0 auto;"></div>
            <div>Загружаем пользователей...</div>
        </div>
    `;

    try {
        const users = await QuizAPI.getAllUsers();
        displayUsersList(users);
        updateUsersStats(users);
    } catch (error) {
        usersList.innerHTML = '<div class="error">Ошибка загрузки пользователей</div>';
    }
}

// Обновление статистики пользователей
function updateUsersStats(users) {
    const totalUsers = users.length;
    const adminUsers = users.filter(user => user.role === 'ROLE_ADMIN').length;
    const regularUsers = totalUsers - adminUsers;

    const totalUsersElement = document.getElementById('totalUsers');
    const adminUsersElement = document.getElementById('adminUsers');
    const regularUsersElement = document.getElementById('regularUsers');

    if (totalUsersElement) totalUsersElement.textContent = totalUsers;
    if (adminUsersElement) adminUsersElement.textContent = adminUsers;
    if (regularUsersElement) regularUsersElement.textContent = regularUsers;
}

// Удаление пользователя
async function deleteUser(userId, username, event) {
    event.stopPropagation();

    if (!confirm(`Вы уверены, что хотите удалить пользователя "${username}"?`)) {
        return;
    }

    try {
        const result = await QuizAPI.deleteUser(userId);
        alert(result.message || 'Пользователь успешно удален!');
        await loadSavedUsers();
    } catch (error) {
        console.error('Delete user error:', error);
        alert('Ошибка при удалении пользователя: ' + error.message);
    }
}

// Обновление роли пользователя
async function updateUserRole(userId, currentRole) {
    const newRole = prompt('Введите новую роль пользователя (ROLE_USER, ROLE_ADMIN):', currentRole);

    if (!newRole || newRole === currentRole) {
        return;
    }

    if (!['ROLE_USER', 'ROLE_ADMIN'].includes(newRole.toUpperCase())) {
        alert('Неверная роль! Допустимые значения: ROLE_USER, ROLE_ADMIN');
        return;
    }

    try {
        const updatedUser = await QuizAPI.updateUserRole(userId, newRole.toUpperCase());
        alert(`Роль пользователя успешно изменена на: ${updatedUser.role}`);
        await loadSavedUsers();
    } catch (error) {
        console.error('Update role error:', error);
        alert('Ошибка при изменении роли: ' + error.message);
    }
}

// Отображение списка пользователей
function displayUsersList(users) {
    const usersList = document.getElementById('usersList');

    if (!users || users.length === 0) {
        usersList.innerHTML = '<div class="loading">Нет зарегистрированных пользователей</div>';
        return;
    }

    usersList.innerHTML = users.map(user => `
        <div class="user-card ${user.role === 'ROLE_ADMIN' ? 'admin-user' : ''}">
            <div class="user-header">
                <h3>${user.username}</h3>
                <div class="user-actions">
                    <button class="btn-role" onclick="updateUserRole(${user.id}, '${user.role}')">
                        🛠️ Изменить роль
                    </button>
                    ${user.id !== AppState.currentUser.id ? `
                        <button class="btn-delete" onclick="deleteUser(${user.id}, '${user.username}', event)">
                            🗑️ Удалить
                        </button>
                    ` : '<span style="color: #666; font-size: 12px;">(это вы)</span>'}
                </div>
            </div>
            <p><strong>Email:</strong> ${user.email}</p>
            <p><strong>Роль:</strong>
                <span class="role-badge ${user.role === 'ROLE_ADMIN' ? 'admin' : 'user'}">
                    ${user.role}
                </span>
            </p>
            <p><strong>ID:</strong> ${user.id}</p>
            ${user.createdAt ? `<p><strong>Зарегистрирован:</strong> ${new Date(user.createdAt).toLocaleDateString('ru-RU')}</p>` : ''}
            ${user.updatedAt ? `<p><strong>Обновлен:</strong> ${new Date(user.updatedAt).toLocaleDateString('ru-RU')}</p>` : ''}
        </div>
    `).join('');
}

// Создание формы нового пользователя (заглушка для будущей реализации)
function showCreateUserForm() {
    alert('Функция создания пользователя будет реализована в будущем обновлении');
}

// Добавление transition в CSS для плавности
const style = document.createElement('style');
style.textContent = `
    .screen {
        transition: opacity 0.3s ease-in-out;
    }
`;
document.head.appendChild(style);