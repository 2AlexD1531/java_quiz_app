const API_BASE = 'http://localhost:8080';

class QuizAPI {
    static token = localStorage.getItem('authToken');

    static async makeRequest(url, options = {}) {
        console.log(`🌐 Запрос: ${API_BASE}${url}`); // Добавим логирование

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        };

        // Добавляем токен авторизации, если он есть
        if (this.token) {
            config.headers.Authorization = `Bearer ${this.token}`;
        }

        try {
            const response = await fetch(`${API_BASE}${url}`, config);
            console.log(`📨 Ответ ${url}: статус ${response.status}`);

            if (response.status === 401) {
                // Токен истек или невалиден
                this.handleUnauthorized();
                throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
            }

            if (!response.ok) {
                let errorText = '';
                try {
                    errorText = await response.text();
                } catch (e) {
                    errorText = 'Ошибка чтения ответа';
                }
                console.error(`❌ Ошибка ${response.status}:`, errorText);
                throw new Error(errorText || `Ошибка ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ Успех ${url}:`, data);
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    static handleUnauthorized() {
        localStorage.removeItem('authToken');
        this.token = null;
        if (typeof showScreen === 'function') {
            showScreen('login');
        }
    }

    // ============ АУТЕНТИФИКАЦИЯ ============
    // ЭТО РАБОТАЕТ ПРАВИЛЬНО (с /api/)
    static async login(username, password) {
        const response = await this.makeRequest('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password }),
        });

        this.token = response.token;
        localStorage.setItem('authToken', this.token);
        return response;
    }

    static async register(username, email, password) {
        const response = await this.makeRequest('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, email, password }),
        });

        this.token = response.token;
        localStorage.setItem('authToken', this.token);
        return response;
    }

    static async getCurrentUser() {
        return await this.makeRequest('/api/auth/me');
    }

    // ============ ТЕСТЫ ============
    // НЕПРАВИЛЬНО! Нужно добавить /api/
    static async getAllQuizzes() {
        return await this.makeRequest('/api/quizzes/all-quizzes'); // Добавить /api/
    }

    static async generateQuiz(tags, difficulty, questionCount) {
        return await this.makeRequest('/api/quizzes/generate', { // Добавить /api/
            method: 'POST',
            body: JSON.stringify({
                tags: tags.split(',').map(tag => tag.trim()),
                difficulty: difficulty,
                questionCount: questionCount
            })
        });
    }

    static async submitQuiz(questions, userAnswers) {
        return await this.makeRequest('/api/quizzes/ai-submit', { // Добавить /api/
            method: 'POST',
            body: JSON.stringify({
                quizData: { questions },
                answers: userAnswers
            })
        });
    }

    static async deleteQuiz(quizId) {
        return await this.makeRequest(`/api/quizzes/delete-quiz/${quizId}`, { // Добавить /api/
            method: 'DELETE'
        });
    }

    static async getQuiz(quizId) {
        return await this.makeRequest(`/api/quizzes/${quizId}`); // Добавить /api/
    }

    // ============ АДМИН ============
    // НЕПРАВИЛЬНО! Нужно добавить /api/
    static async getAllUsers() {
        return await this.makeRequest('/api/admin/all-users'); // Добавить /api/
    }

    static async getUserById(userId) {
        return await this.makeRequest(`/api/admin/user/${userId}`); // Добавить /api/
    }

    static async updateUserRole(userId, newRole) {
        return await this.makeRequest(`/api/admin/user/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ role: newRole })
        });
    }

    static async deleteUser(userId) {
        return await this.makeRequest(`/api/admin/user/${userId}`, {
            method: 'DELETE'
        });
    }

    static async checkAdminAccess() {
        try {
            const user = await this.getCurrentUser();
            return user.role === 'ROLE_ADMIN' || user.role === 'ADMIN';
        } catch (error) {
            return false;
        }
    }

    static logout() {
        this.token = null;
        localStorage.removeItem('authToken');
        if (typeof showScreen === 'function') {
            showScreen('login');
        }
    }
}

// Инициализация
console.log('QuizAPI загружен, API Base:', API_BASE);
window.QuizAPI = QuizAPI;