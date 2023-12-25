# :robot: telegram service

## :small_blue_diamond: Описание

Телеграм бот для управления устройствами пользователя

- доступные команды:
  - **\start** - стартовое сообщение
  - **\account** - для использования бота нужно авторизоваться с помощью токена, сгенерированного для пользователя в user-service
  - **\control** - просмотр информации об устройствах и управление ими

## :small_blue_diamond: Использование

> для использования сервиса необходимо добавить [.env](.env.example) файл с переменными TELEGRAM_BOT_NAME и TELEGRAM_BOT_TOKEN, полученные от BotFather

Запуск сервиса с помощью [docker compose](./docker-compose.yml):

```
docker-compose up -d
```
