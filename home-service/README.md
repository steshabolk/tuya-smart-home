# :house_with_garden: home service

## :small_blue_diamond: Описание

Сервис для управления домом и комнатами

- дом : создание и удаление дома, получение и изменение данных о доме
- комната : создание и удаление комнаты, изменение данных о комнате
- использование kafka :
  - при получении события об удалении пользователя в user-service, происходит удаление связанных с ним домов и комнат
  - при удалении дома или комнаты в топик отправляется событие для уведомление других сервисов, содержащих связанные с ними данные
- [тесты](./src/test/kotlin/ru/handh/project)

## :small_blue_diamond: Использование

Запуск сервиса с помощью [docker compose](./docker-compose.yml):

```
docker-compose up -d
```

- сервис доступен по ссылке : http://localhost:8085
- [postman collection](./hh-home-service.postman_collection.json)
- swagger ui : http://localhost:8085/swagger-ui.html

> дополнительно нужно запустить брокер сообщений [kafka](./docker-compose-kafka.yaml)
