Проект представляет собой приложение, организованное в виде микросервисов, предоставляющее различные сервисы для управления 
умными устройствами дома и взаимодействия с пользователями. 

## :small_blue_diamond: Доступные сервисы

- [api gateway service](./api-gateway) : клиентский интерфейс для доступа к микросервисам

- [user service](./user-service) : функции пользователей, refresh и access токенов

- [home service](./home-service) : управление домами и комнатами пользователей

- [device service](./device-service) : управление устройствами [Tuya](https://www.tuya.com/) для умного дома

- [telegram service](./telegram-service) : телеграм бот для управления устройствами пользователя

## :small_blue_diamond: Доступ и документация

- api gateway доступен по ссылке : http://localhost:8088

- у каждого микросервиса доступны коллекция postman и swagger ui

## :small_blue_diamond: Компоненты

- для обмена сообщениями между микросервисами используется Kafka