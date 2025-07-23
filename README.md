# User Auth Service

Сервис аутентификации пользователей на Spring Boot

---

### Шаги запуска

1. Создать файл .env в корне проекта
## Переменные окружения (.env)

| Переменная                   | Описание                                    | 
|-----------------------------|---------------------------------------------|
| `DB_SOURCE`                 | JDBC URL для подключения к PostgreSQL       | 
| `DB_USERNAME`               | Имя пользователя базы данных                 |
| `DB_PASSWORD`               | Пароль пользователя базы                      | 
| `APP_SECURITY_SECRET`       | Секрет для подписи JWT                        | 
| `APP_SECURITY_REFRESH_EXPIRATION` | Время жизни refresh токена в миллисекундах | 
| `APP_SECURITY_ACCESS_EXPIRATION`  | Время жизни access токена в миллисекундах  | 

В качестве теста можете взять:

DB_SOURCE=jdbc:postgresql://postgres:5432/postgres

DB_USERNAME=postgres

DB_PASSWORD=admin

APP_SECURITY_SECRET=VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIEpXVHMhIQ==

APP_SECURITY_REFRESH_EXPIRATION=80000000

APP_SECURITY_ACCESS_EXPIRATION=800000

2. Запуск приложения

```bash
docker-compose up --build
```
## Описание функциональности

Сервис предоставляет REST API для работы с пользователями и ролями:

- `POST /register` — регистрация нового пользователя  
  Принимает `RegisterRequest` с обязательными полями, возвращает JWT-токены.
  ### RegisterRequest 

| Поле     | Описание                       | Валидация                                | 
|----------|-------------------------------|-----------------------------------------|
| `email`  | Email пользователя             | Должен быть валидным email, не пустым   | 
| `login`  | Логин пользователя            | Не пустой                              | 
| `password` | Пароль пользователя           | Минимум 6 символов, не пустой          | 

- `POST /login` — аутентификация пользователя  
  Принимает `LoginRequest`, проверяет логин и пароль, возвращает JWT-токены.
  
### LoginRequest 

| Поле     | Описание                       | Валидация                                | 
|----------|-------------------------------|-----------------------------------------|
| `login`  | Логин пользователя            | Не пустой                              | 
| `password` | Пароль пользователя           | Минимум 6 символов, не пустой          | 

- `POST /refresh` — обновление access-токена по refresh-токену  
  Обрабатывает refresh токен, возвращает новый access-токен.

- `POST /logout` — выход из системы  
  Инвалидирует текущий refresh-токен.

- `POST /add/admin/{login}` — назначение роли ADMIN пользователю (только для ADMIN)  
  Позволяет выдать роль администратора другому пользователю.

- `POST /add/premium/{login}` — назначение роли PREMIUM пользователю (только для ADMIN)  
  Позволяет выдать премиум-роль пользователю.



