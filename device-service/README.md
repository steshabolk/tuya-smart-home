# :gear: device service

## :small_blue_diamond: Описание

Сервис для управления устройствами [Tuya](https://www.tuya.com/)

- функции: создание и удаление устройств, получение и изменение данных об устройстве, получение списка устройств по заданным параметрам: дом, комната
- управление устройствами:
  - для категории light доступны команды: включение / выключение, изменение температуры, цвета, яркости
- использование kafka :
  - при получении события об удалении дома в home-service, происходит удаление связанных с этим домом устройств
  - при получении события об удалении комнаты в home-service, эта комната отвязывается от устройств
- [тесты](./src/test/kotlin/ru/handh/project)

## :small_blue_diamond: Использование

> для использования сервиса необходимо добавить [.env](.env.example) файл с переменными ACCESS_ID и ACCESS_SECRET, связанными с аккаунтом Tuya

Запуск сервиса с помощью [docker compose](./docker-compose.yml):

```
docker-compose up -d
```

- сервис доступен по ссылке : http://localhost:8087
- [postman collection](./hh-device-service.postman_collection.json)
- swagger ui : http://localhost:8087/swagger-ui.html

> дополнительно нужно запустить брокер сообщений [kafka](./docker-compose-kafka.yaml)
