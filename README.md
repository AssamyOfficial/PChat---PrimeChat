# PrimeChat 1.2.0

PrimeChat — настраиваемая система чатов для Paper с локальным и глобальным режимами, командными каналами, HEX/MiniMessage, цветным чатом, интерактивными никами, упоминаниями и PlaceholderAPI.

## Возможности

- Локальный чат с настраиваемым радиусом.
- Глобальный чат через триггер.
- Командные каналы через `config.yml`.
- Неограниченное количество собственных каналов.
- Алиасы командных каналов.
- Отдельный permission для каждого канала.
- Включение и отключение каналов без изменения Java-кода.
- MiniMessage и HEX-цвета.
- Градиенты, стили и стандартные `&`-цвета.
- Цветной чат с отдельным permission.
- Hover-информация при наведении на ник.
- Клик по нику с предложением команды `/msg`.
- Упоминания через `@ник`.
- Несколько упоминаний в одном сообщении.
- Уведомления об упоминаниях только для игроков, которые реально получили сообщение.
- Отдельные сообщения для известного офлайн-игрока и неизвестного ника.
- Отключение уведомления при упоминании самого себя.
- Уведомление «Вас никто не услышал».
- Отдельная настройка уведомления «Вас никто не услышал» для каждого канала.
- Необязательная интеграция с PlaceholderAPI.
- Перезагрузка конфигурации без перезапуска сервера.

## Требования

- Paper 1.21.x.
- Java 21.
- PlaceholderAPI — необязательно.

## Установка

1. Скопируйте `PrimeChat-1.2.0.jar` в папку `plugins`.
2. Запустите сервер.
3. Откройте `plugins/PrimeChat/config.yml`.
4. Настройте каналы и оформление.
5. Примените изменения командой `/primechat reload`.

## Конфигурация

Основной файл — `plugins/PrimeChat/config.yml`.

PrimeChat использует MiniMessage. HEX указывается непосредственно в формате:

```yaml
format: "<#00E5FF>%player%</#00E5FF> <gray>»</gray> <white>%message%</white>"
```

Также доступны градиенты:

```yaml
format: "<gradient:#00E5FF:#7C4DFF>%player%</gradient> <gray>»</gray> <white>%message%</white>"
```

Основные placeholders PrimeChat:

| Placeholder | Значение |
|---|---|
| `%player%` | Ник игрока |
| `%displayname%` | Display Name игрока |
| `%display_name%` | Альтернативная запись Display Name |
| `%message%` | Сообщение |

При установленном PlaceholderAPI дополнительно доступны его placeholders.

Подробные готовые шаблоны и пояснения находятся непосредственно в `config.yml`.

## Каналы

Все каналы находятся в разделе `channels`. Новый канал добавляется только через конфигурацию.

### Локальный канал

```yaml
local:
  enabled: true
  mode: "local"
  trigger: ""
  command: ""
  aliases: []
  permission: ""
  radius: 100
  format: "<#5C6BC0>[L]</#5C6BC0> <#00E5FF>%player%</#00E5FF> <gray>»</gray> <white>%message%</white>"
```

Сообщение получают игроки в том же мире в пределах `radius`.

### Глобальный канал

```yaml
global:
  enabled: true
  mode: "global"
  trigger: "!"
  command: ""
  aliases: []
  permission: ""
  radius: 0
  format: "<gradient:#00E5FF:#7C4DFF>[G]</gradient> <#7C4DFF>%player%</#7C4DFF> <gray>»</gray> <white>%message%</white>"
```

Сообщение `!Привет` будет отправлено как `Привет` через глобальный канал.

### Командный канал

```yaml
admin:
  enabled: true
  mode: "command"
  trigger: ""
  command: "ac"
  aliases:
    - "adminchat"
  permission: "primechat.admin"
  radius: 0
  format: "<gradient:#FF5252:#FF1744>[ADMIN]</gradient> <#FF5252>%player%</#FF5252> <gray>»</gray> <white>%message%</white>"
```

После настройки доступны `/ac сообщение` и `/adminchat сообщение`.

Параметр `permission` ограничивает отправку и получение сообщений канала.

После изменения каналов выполните `/primechat reload`.

## Стандартные каналы

В поставляемом `config.yml` настроены:

| Канал | Режим | Использование | Permission |
|---|---|---|---|
| `local` | local | обычный чат | нет |
| `global` | global | `!сообщение` | нет |
| `admin` | command | `/ac`, `/adminchat` | `primechat.admin` |
| `donate` | command | `/dc`, `/donatechat` | `primechat.donate` |
| `staff` | command | `/sc`, `/staffchat` | `primechat.staff` |

`donate` и `staff` используют permissions из конфигурации. Эти permissions не обязаны быть отдельно объявлены в `plugin.yml`.

## Упоминания

Упоминание выполняется через `@Ник`.

```text
@Assamy_ @Assamy_2 Привет всем!
```

Поддерживается несколько игроков в одном сообщении. Ники с цифрами и `_` обрабатываются корректно.

Для онлайн-игрока уведомление отправляется только после фильтрации аудитории сообщения. Поэтому игрок за пределами радиуса локального канала уведомление не получит.

При упоминании самого себя уведомление не отправляется.

Если упомянутый игрок известен серверу, но офлайн, используется `mentions.notification.offline`. Если сервер не знает такого игрока, используется `mentions.notification.not-found`.

Все тексты и оформление этих уведомлений настраиваются в `config.yml`.

## Цветной чат

```yaml
chat-color:
  enabled: true
  permission: "primechat.chatcolor"
```

Игрок с `primechat.chatcolor` может использовать MiniMessage и HEX непосредственно в тексте сообщения:

```text
<#00E5FF>Привет!</#00E5FF>
```

## «Вас никто не услышал»

Глобальная настройка:

```yaml
unheard-message:
  enabled: true
  message: "<red>⚠</red> <gray>Вас никто не услышал.</gray>"
```

Уведомление появляется, если после всех правил канала сообщение не получил ни один другой игрок.

Его можно переопределить для отдельного канала:

```yaml
local:
  unheard-message:
    enabled: true
    message: "<yellow>⚠</yellow> <gray>В радиусе никто не услышал Вас.</gray>"
```

Чтобы оставить функцию только в одном канале, отключите глобальную настройку и включите её в нужном канале.

## Интерактивный ник

В `player-interaction` можно настроить:

- текст при наведении;
- включение/отключение hover;
- включение/отключение клика;
- действие клика.

Стандартное действие `suggest-message` предлагает `/msg <ник>`.

## PlaceholderAPI

PlaceholderAPI — необязательная зависимость. Без него PrimeChat продолжает работать.

При наличии PlaceholderAPI его placeholders можно использовать в форматах каналов, hover-тексте и уведомлениях.

## Команды

### Управление PrimeChat

```text
/primechat help
/primechat reload
/primechat version
```

`/primechat` требует `primechat.admin`.

- `help` — справка.
- `reload` — перезагрузка `config.yml` и каналов.
- `version` — версия плагина.

### Каналы

Командные каналы создаются из `config.yml`. В стандартной конфигурации:

```text
/ac <сообщение>
/adminchat <сообщение>
/dc <сообщение>
/donatechat <сообщение>
/sc <сообщение>
/staffchat <сообщение>
```

## Permissions

| Permission | Назначение | По умолчанию |
|---|---|---|
| `primechat.admin` | Управление PrimeChat и стандартный Admin Chat | OP |
| `primechat.chatcolor` | Цветной и форматированный чат | Нет |
| `primechat.donate` | Стандартный Donate Chat | Настраивается сервером |
| `primechat.staff` | Стандартный Staff Chat | Настраивается сервером |

Для любого нового канала можно создать собственное право:

```yaml
permission: "primechat.example"
```

Пустое значение `permission: ""` отключает ограничение permission для канала.

## Структура проекта

```text
PrimeChat/
├── src/main/java/corp/prime/chat/
│   ├── ChatChannel.java
│   ├── ChatChannelCommandListener.java
│   ├── ChatChannelFormatter.java
│   ├── ChatChannelManager.java
│   ├── ChatChannelMessageSender.java
│   ├── ChatChannelRouter.java
│   ├── ChatFormatRenderer.java
│   ├── ChatListener.java
│   ├── PrimeChat.java
│   ├── PrimeChatCommand.java
│   └── PrimeChatTabCompleter.java
├── src/main/resources/
│   ├── config.yml
│   └── plugin.yml
├── pom.xml
├── LICENSE
└── README.md
```

## Сборка

```text
mvn clean package
```

Готовый JAR появится в `target/`.

## Лицензия

Проект распространяется на условиях лицензии, указанной в `LICENSE`.

## PrimeDev

PrimeChat разработан PrimeDev.
