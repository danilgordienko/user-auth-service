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



