# :twisted_rightwards_arrows: api gateway service

## :small_blue_diamond: Описание

Сервис предоставляет интерфейс для клиентов для доступа к микросервисам

- [фильтр](./src/main/kotlin/ru/handh/project/config/filter/TokenFilter.kt) для обработки входящий запросов с jwt-токеном: проверка наличия заголовка с токеном в запросе, расшифровка токена и проверка подписи
- маршрутизация запросов от клиентов к соответствующим сервисам
- [тесты](./src/test/kotlin/ru/handh/project)

## :small_blue_diamond: Использование

Запуск сервиса с помощью [docker compose](./docker-compose.yml):

```
docker-compose up -d
```

- сервис доступен по ссылке : http://localhost:8088
- [postman collection](./hh-api-gateway.postman_collection.json)
