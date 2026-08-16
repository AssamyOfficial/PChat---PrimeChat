# PrimeChat 1.2.0

**PrimeChat** — настраиваемая система чатов для Paper с каналами, локальным радиусом, цветным форматированием, упоминаниями игроков и PlaceholderAPI.

## Возможности

- Локальный чат с настраиваемым радиусом.
- Глобальный чат.
- Неограниченное количество дополнительных каналов через `config.yml`.
- Командные каналы: например `/ac`, `/dc`, `/sc`.
- Алиасы командных каналов.
- Отдельные permissions для каждого канала.
- Включение и отключение каналов без изменения Java-кода.
- Настраиваемые форматы сообщений через MiniMessage.
- Поддержка стандартных цветов, HEX и форматирования текста.
- Цветной чат с отдельным permission.
- Интерактивный ник игрока: hover и действие при клике.
- Упоминания через `@ник`.
- Упоминание нескольких игроков в одном сообщении.
- Уведомление об упоминании только если игрок действительно получил сообщение.
- Отдельные сообщения для онлайн-игрока, известного офлайн-игрока и неизвестного ника.
- Уведомление «Вас никто не услышал», если сообщение никто не получил.
- Возможность включать, отключать и переопределять уведомление «Вас никто не услышал» для отдельных каналов.
- PlaceholderAPI как необязательная интеграция.
- Перезагрузка конфигурации без перезапуска сервера.

## Требования

- Paper 1.21.1 или совместимая версия Paper API 1.21.x.
- Java 21.
- PlaceholderAPI — необязательно.

## Установка

1. Скачайте `PrimeChat-1.2.0.jar`.
2. Поместите файл в папку `plugins` сервера.
3. Запустите сервер.
4. После первого запуска откройте `plugins/PrimeChat/config.yml`.
5. Настройте каналы и форматирование.
6. Для применения изменений используйте `/primechat reload`.

## Основная конфигурация

Весь пользовательский функционал настраивается в `config.yml`.

### Формат сообщения

Основные placeholders PrimeChat:

| Placeholder | Значение |
|---|---|
| `%player%` | Ник игрока |
| `%displayname%` | Display Name игрока |
| `%display_name%` | Альтернативное имя для `%displayname%` |
| `%message%` | Текст сообщения |

Пример:

```yaml
format: "<gray>[L]</gray> <aqua>%player%</aqua> <gray>»</gray> <white>%message%</white>"
```

PrimeChat использует MiniMessage. Например:

```yaml
format: "<gradient:#00BFFF:#8A2BE2>%player%</gradient> <gray>»</gray> <white>%message%</white>"
```

## Каналы

Каналы находятся в разделе `channels`.

### Локальный канал

```yaml
local:
  enabled: true
  mode: "local"
  trigger: ""
  command: ""
  permission: ""
  radius: 100
  format: "<gray>[L]</gray> <aqua>%player%</aqua> <gray>»</gray> <white>%message%</white>"
```

`radius` задаёт максимальное расстояние между отправителем и получателем. `0` используется для каналов без ограничения расстоянием.

### Глобальный канал

```yaml
global:
  enabled: true
  mode: "global"
  trigger: "!"
  command: ""
  permission: ""
  radius: 0
  format: "<gray>[G]</gray> <green>%player%</green> <gray>»</gray> <white>%message%</white>"
```

С таким `trigger` сообщение `!Привет` отправляется в глобальный канал как `Привет`.

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
  format: "<red>[ADMIN]</red> <red>%player%</red> <gray>»</gray> <white>%message%</white>"
```

После этого канал доступен через `/ac сообщение` и `/adminchat сообщение`.

Чтобы создать новый канал, достаточно добавить новый раздел в `channels`. Java-код менять не требуется.

## Уведомление «Вас никто не услышал»

Глобальная настройка:

```yaml
unheard-message:
  enabled: true
  message: "<red>⚠</red> <grey>Вас никто не услышал.</grey>"
```

Если после применения правил канала сообщение не получил ни один другой игрок, отправителю показывается это уведомление.

Функцию можно отключить полностью:

```yaml
unheard-message:
  enabled: false
```

Для отдельного канала можно задать собственную настройку:

```yaml
local:
  unheard-message:
    enabled: true
    message: "<yellow>В радиусе никого нет.</yellow>"
```

Если настройки канала нет, используется глобальная настройка.

## Упоминания

Упоминания настраиваются в разделе `mentions`.

```yaml
mentions:
  enabled: true
  symbol: "@"
  color: "<aqua>"
  clickable: true
  hover: true
  notification:
    enabled: true
    message: "<yellow>🔔 <white>%player%</white> упомянул Вас в чате.</yellow>"
    offline: "<yellow>⚠ Игрок <white>%player%</white> сейчас не в сети.</yellow>"
    not-found: "<red>⚠ Игрок <white>%player%</white> не найден.</red>"
```

Можно упомянуть несколько игроков в одном сообщении:

```text
@Assamy_ @Assamy_2 привет всем
```

Онлайн-игрок получает уведомление только в том случае, если он действительно входит в аудиторию сообщения. Например, игрок за пределами радиуса локального чата уведомление не получит.

Упоминание самого себя уведомление не создаёт.

Если игрок известен серверу, но находится офлайн, используется `mentions.notification.offline`. Если сервер никогда не видел такого игрока, используется `mentions.notification.not-found`.

## Цветной чат

```yaml
chat-color:
  enabled: true
  permission: "primechat.chatcolor"
```

Permission `primechat.chatcolor` разрешает игроку использовать цветное и форматированное сообщение в обычном чате.

## PlaceholderAPI

PlaceholderAPI является необязательной зависимостью. PrimeChat продолжает работать без него.

При установленном PlaceholderAPI в форматах каналов и уведомлений можно использовать его placeholders.

Пример:

```yaml
format: "<gray>[<white>%player_name%</white>]</gray> <white>%message%</white>"
```

## Команды

### `/primechat`

Основная команда управления плагином.

```text
/primechat help
/primechat reload
/primechat version
```

- `help` — список доступных подкоманд.
- `reload` — перечитать `config.yml` и загрузить каналы заново.
- `version` — показать версию PrimeChat.

### Команды каналов

Команды командных каналов создаются автоматически из `config.yml`.

В стандартной конфигурации:

```text
/ac <сообщение>
/dc <сообщение>
/sc <сообщение>
```

Набор команд и их aliases можно полностью изменить через конфигурацию.

## Permissions

| Permission | Назначение | По умолчанию |
|---|---|---|
| `primechat.admin` | Управление PrimeChat и доступ к административному каналу в стандартной конфигурации | OP |
| `primechat.chatcolor` | Цветное и форматированное сообщение | Нет |
| `primechat.donate` | Доступ к стандартному Donate Chat | Настраивается |
| `primechat.staff` | Доступ к стандартному Staff Chat | Настраивается |

Для любого нового канала можно указать собственный permission:

```yaml
permission: "primechat.example"
```

Пустое значение `permission: ""` означает отсутствие отдельного ограничения по permission.

## Структура проекта

```text
PrimeChat/
├── src/main/java/corp/prime/chat/
│   ├── ChatChannel.java
│   ├── ChatChannelCommand.java
│   ├── ChatChannelCommandHandler.java
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

Для сборки из исходников используется Maven:

```text
mvn clean package
```

Готовый JAR появится в `target/`.

## Лицензия

Проект распространяется на условиях лицензии, указанной в `LICENSE`.

## PrimeDev

PrimeChat разработан PrimeDev.
