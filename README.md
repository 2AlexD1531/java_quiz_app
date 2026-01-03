🎯 Java Quiz App
Интеллектуальный тестер по Java с генерацией вопросов ИИ, админ-панелью и полным CI/CD-конвейером.
📦 Что внутри
Table
Copy
Компонент	Стек
Backend	Spring Boot 3.2 + Java 21 + MySQL 8 + JWT
Frontend	Чистый HTML/CSS/JS (SPA), Nginx
AI-вопросы	OpenRouter (GPT-3.5/4)
DevOps	Docker + Docker Compose, WSL2/Hyper-V
Тесты	JUnit 5 + Mockito, покрытие > 80 %
В составе проекта тесты Postman


# Linux / macOS / Windows (PowerShell)
Запустить можно локально из проекта:
JavaQuizApplication.java
http://localhost:8080
Docker:
docker-compose up -d

Через 30 секунд открываем в браузере:
👉 http://localhost — фронт
📋 Переменные окружения
Создайте .env в корне проекта:
env
Copy

# MySQL
DB_HOST=db
DB_PORT=3306
DB_NAME=tester
DB_USER=root
DB_PASS=12345
DB_ROOT_PASS=12345

# JWT
JWT_SECRET=your-256-bit-secret

# AI
OPENROUTER_KEY=sk-or-v1-...

# CORS
CORS_ORIGINS=http://localhost,http://127.0.0.1:3000

🧪 Запуск тестов
bash
Copy

cd backend
mvn test                    # unit-тесты
mvn verify                  # интеграционные + отчёт

📊 Админ-режим

    Зарегистрируйте пользователя
    Выполните SQL:

sql
Copy

UPDATE users SET role = 'ROLE_ADMIN' WHERE id = 1;

    Войдите как админ → доступ к управлению пользователями и тестами.

🛠️ Полезные команды
Table
Copy
Задача	Команда
Остановить всё	docker-compose down
Удалить всё (включая БД)	docker-compose down -v
Логи бэка	docker logs -f quiz_backend
Бэкап БД	docker exec quiz_mysql mysqldump -uroot -p12345 tester > dump.sql
📸 Скриншоты`docker compose up -d`

Основные энд-поинты:
Copy

POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me
GET    /api/quizzes/all-quizzes
POST   /api/quizzes/generate
POST   /api/quizzes/ai-submit
GET    /admin/all-users
PUT    /admin/user/{id}/role

Особенности
В случае, если AI генерация не доступна, программа генерирует
вопросы "заглушки".
Обязательно сначала настройте доступ к AI