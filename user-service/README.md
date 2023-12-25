# :blush: user service

## :small_blue_diamond: Описание

- функции пользователя : регистрация, аутентификация, разлогинивание, удаление аккаунта
- refresh и access токены: генерация токенов, обновление истекшего access токена
  - [подпись](./src/main/kotlin/ru/handh/project/service/SignatureService.kt) токена с помощью секретного или RSA ключа
  - [шифрование](./src/main/kotlin/ru/handh/project/service/EncryptionService.kt) токена с помощью RSA ключа
- отзыв истекших refresh токенов с помощью воркера
- использование kafka :
  - при удалении пользователя в топик отправляется событие для уведомление других сервисов, содержащих связанные с этим пользователем данные
- телеграм бот: генерация токена для использования бота
- [тесты](./src/test/kotlin/ru/handh/project)

## :small_blue_diamond: Использование

Запуск сервиса с помощью [docker compose](./docker-compose.yml):

```
docker-compose up -d
```

- сервис доступен по ссылке : http://localhost:8086
- [postman collection](./hh-user-service.postman_collection.json)
- swagger ui : http://localhost:8086/swagger-ui.html

> дополнительно нужно запустить брокер сообщений [kafka](./docker-compose-kafka.yaml)
