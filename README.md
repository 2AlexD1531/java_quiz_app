
# 🎯 Java Quiz App

Интеллектуальный тестер по Java с генерацией вопросов ИИ, админ-панелью и полным CI/CD-конвейером.
Проект содержит базовые реализации основных функций.
## 📦 Что внутри

| Компонент       | Стек                                      |
|----------------|-------------------------------------------|
| **Backend**     | Spring Boot 3.2 + Java 21 + MySQL 8 + JWT |
| **Frontend**    | Чистый HTML/CSS/JS (SPA), Nginx           |
| **AI-вопросы**  | OpenRouter (GPT-3.5/4)                    |
| **DevOps**      | Docker + Docker Compose, WSL2/Hyper-V     |
| **Тесты**       | JUnit 5 + Mockito, покрытие > 80%         |
| **API тесты**   | В составе проекта тесты Postman           |

## 🚀 Быстрый старт

### Linux / macOS / Windows (PowerShell)

**Запустить можно локально из проекта:**
- `JavaQuizApplication.java`
- http://localhost:8080

**Docker:**
```bash
docker compose up -d
```

Через 30 секунд открываем в браузере: 👉 http://localhost — фронт

---

## 📋 Переменные окружения

Создайте `.env` в корне проекта:

```env
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
```

---

## 🧪 Запуск тестов

```bash
cd backend
mvn test        # unit-тесты
mvn verify      # интеграционные + отчёт
```

---

## 📊 Админ-режим

1. Зарегистрируйте пользователя
2. При перезапуске пользователь с id 1 становится админом
3. Войдите как админ → доступ к управлению пользователями и тестами.

---

## 🛠️ Полезные команды

| Задача                        | Команда                                                        |
|------------------------------|----------------------------------------------------------------|
| Остановить всё               | `docker compose down`                                          |
| Удалить всё (включая БД)     | `docker compose down -v`                                       |
| Логи бэка                    | `docker logs -f quiz_backend`                                  |
| Бэкап БД                     | `docker exec quiz_mysql mysqldump -uroot -p12345 tester > dump.sql` |

---

## 📡 API

Основные энд-поинты:

| Метод | Энд-поинт                        | Описание                          |
|-------|----------------------------------|-----------------------------------|
| POST  | `/api/auth/register`             | Регистрация                       |
| POST  | `/api/auth/login`                | Авторизация                       |
| GET   | `/api/auth/me`                   | Текущий пользователь              |
| GET   | `/api/quizzes/all-quizzes`       | Все тесты                         |
| POST  | `/api/quizzes/generate`          | Сгенерировать тест                |
| POST  | `/api/quizzes/ai-submit`         | Отправить ответы на проверку ИИ   |
| GET   | `/admin/all-users`               | Список пользователей (админ)      |
| PUT   | `/admin/user/{id}/role`          | Изменить роль пользователя        |

---

## ⚠️ Особенности

- В случае, если AI генерация недоступна, программа генерирует вопросы-заглушки.
- Обязательно настройте доступ к AI перед использованием.
