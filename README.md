<div align="center">
  <img src="docs/01-cover.svg" alt="PrimeChat 1.3 cover">
</div>

# PrimeChat 1.3.0

<div align="center">
  <strong>Современная система чата для Paper</strong><br>
  Каналы • Личные сообщения • Упоминания • AFK • Spy • Контроль команд
</div>

## Документация

Ниже — визуальная часть документации PrimeChat. Все страницы выполнены в едином стиле и служат быстрым обзором возможностей плагина. Под ними находится подробная техническая документация и рабочие примеры.

<div align="center">
  <img src="docs/02-features.svg" alt="PrimeChat features">
  <br><br>
  <img src="docs/03-commands.svg" alt="PrimeChat commands">
  <br><br>
  <img src="docs/04-settings.svg" alt="PrimeChat settings and permissions">
  <br><br>
  <img src="docs/05-installation.svg" alt="PrimeChat installation and support">
</div>

---

## О плагине

PrimeChat — современный, гибкий и производительный чат-плагин для Paper. Он объединяет локальный и глобальный чат, собственные каналы, личные сообщения, упоминания, AFK, SocialSpy, CommandSpy, контроль команд, очистку чата и удобную настройку через YAML.

Плагин рассчитан на серверы, которым нужна полноценная система общения без необходимости собирать десятки небольших плагинов.

## Возможности

- Локальный чат с настраиваемым радиусом.
- Глобальный чат через триггер `!`.
- Неограниченное количество собственных каналов.
- Командные каналы с aliases и отдельными permissions.
- Автоматический Tab Completion для новых командных каналов.
- Админ-чат и Donate-чат.
- HEX, MiniMessage, стили и градиенты.
- Цветной чат с отдельным permission.
- Hover и интерактивный клик по никам.
- Упоминания через `@ник`.
- Несколько упоминаний в одном сообщении.
- Уведомление об упоминании только если игрок реально получил сообщение.
- Звук при упоминании с настройкой в `config.yml`.
- Сообщения для офлайн-игроков и неизвестных ников.
- Защита от уведомления при самоупоминании.
- Уведомление «Вас никто не услышал» с отдельной настройкой для каналов.
- `/msg`, `/m`, `/message`.
- Многострочный формат личных сообщений.
- `/r` и `/reply` для быстрого ответа.
- Звук и кликабельный ответ на входящее ЛС.
- `/ss` / `/socialspy` для просмотра ЛС.
- `/cs` / `/commandspy` для просмотра команд игроков.
- `/cc` / `/clearchat` для очистки чата.
- `/afk` с автоматическим выходом из AFK при движении.
- Уведомление отправителю при обращении к AFK-игроку.
- `/chat` и `/ch` для включения и отключения чата.
- Permission для обхода отключённого чата.
- Whitelist и blacklist команд.
- Задержки выполнения команд.
- Permission для уменьшения или полного обхода задержки.
- Необязательная интеграция с PlaceholderAPI.
- Перезагрузка конфигурации без перезапуска сервера.

## Требования

- Paper 1.21.x.
- Java 21.
- PlaceholderAPI — необязательно.

## Установка

1. Скачайте `PrimeChat-1.3.0.jar`.
2. Поместите JAR в `plugins/`.
3. Запустите или перезапустите сервер.
4. Настройте `config.yml` и `command.yml`.
5. Для применения изменений используйте `/pc reload`.

Плагин создаёт и использует:

```text
plugins/PrimeChat/config.yml
plugins/PrimeChat/command.yml
```

## Конфигурация

### config.yml

`config.yml` отвечает за основные функции и внешний вид:

- каналы;
- радиус локального чата;
- формат сообщений;
- HEX и градиенты;
- интерактивные ники;
- упоминания;
- звук упоминаний;
- цветной чат;
- «Вас никто не услышал».

### command.yml

`command.yml` отвечает за команды и их оформление:

- `/msg`, `/r`;
- SocialSpy и CommandSpy;
- очистку чата;
- AFK;
- включение и отключение чата;
- whitelist/blacklist;
- задержки команд;
- permissions;
- все сообщения и форматы команд.

Подробные пояснения и рабочие примеры находятся непосредственно внутри этих двух файлов.

## MiniMessage и HEX

PrimeChat использует MiniMessage.

Обычный HEX:

```yaml
format: "<#00E5FF>%player%</#00E5FF> <gray>»</gray> <white>%message%</white>"
```

Градиент:

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

При установленном PlaceholderAPI доступны его placeholders.

## Каналы

Каналы создаются в `config.yml` в разделе `channels`.

### Локальный

```yaml
local:
  enabled: true
  mode: "local"
  radius: 100
  format: "<#00E5FF>Ⓛ</#00E5FF> <#00E5FF>%player%</#00E5FF> <gray>»</gray> <white>%message%</white>"
```

Игроки в том же мире получают сообщение в пределах указанного радиуса.

### Глобальный

```yaml
global:
  enabled: true
  mode: "global"
  trigger: "!"
  format: "<#7C4DFF>Ⓖ</#7C4DFF> <#7C4DFF>%player%</#7C4DFF> <gray>»</gray> <white>%message%</white>"
```

`!Привет` отправляет сообщение через глобальный канал.

### Командный

```yaml
vip:
  enabled: true
  mode: "command"
  command: "vip"
  aliases:
    - "v"
  permission: "primechat.vip"
  format: "<#FFD54F>◆ VIP</#FFD54F> <#00E5FF>%player%</#00E5FF> <gray>»</gray> <white>%message%</white>"
```

После `/pc reload` автоматически появятся `/vip`, `/v` и Tab Completion.

### Стандартные каналы

| Канал | Использование | Permission |
|---|---|---|
| Local | обычный чат | нет |
| Global | `!сообщение` | нет |
| Admin | `/ac`, `/adminchat` | `primechat.admin` |
| Donate | `/dc`, `/donatechat` | `primechat.donate` |

Staff Chat в 1.3.0 намеренно не поставляется: сервер может создать его самостоятельно как обычный command-канал.

## Личные сообщения

```text
/msg <игрок> <сообщение>
/m <игрок> <сообщение>
/message <игрок> <сообщение>
```

Ответ:

```text
/r <сообщение>
/reply <сообщение>
```

Формат ЛС поддерживает несколько строк. Например:

```yaml
format:
  incoming:
    - "<#00E5FF>◆ ЛС</#00E5FF> <gray>← %sender%</gray>"
    - "<gray>%message%</gray>"
```

Входящее сообщение может иметь звук и кликабельную область для `/r`.

Если адресат находится в AFK, отправитель получает отдельное уведомление. Само получение ЛС не снимает AFK.

## Упоминания

```text
@Assamy_ @Assamy_2 Привет!
```

Поддерживаются несколько игроков в одном сообщении, включая ники с цифрами и `_`.

Онлайн-игрок получает уведомление только если он действительно получил исходное сообщение. Это особенно важно для локального чата: игрок вне радиуса не получит уведомление.

Если игрок офлайн, используется сообщение `mentions.notification.offline`; если сервер не знает такого игрока — `mentions.notification.not-found`.

Звук упоминания настраивается в `config.yml`:

```yaml
sound:
  enabled: true
  name: "ENTITY_EXPERIENCE_ORB_PICKUP"
  volume: 1.0
  pitch: 1.0
```

## SocialSpy и CommandSpy

```text
/socialspy
/ss

/commandspy
/cs
```

Формат отображения полностью меняется в `command.yml`. По умолчанию эти сообщения сделаны ненавязчивыми, чтобы не мешать обычному чату.

## AFK

```text
/afk
```

Игрок может вручную перейти в AFK. При движении он автоматически возвращается в активное состояние и получает уведомление.

Получение `/msg` не снимает AFK. Если другой игрок пишет AFK-пользователю, отправитель получает уведомление об AFK-статусе.

## Управление чатом

```text
/chat on
/chat off
/chat
/ch on
/ch off
```

`primechat.chat.toggle` управляет состоянием чата.

`primechat.chat.bypass` позволяет писать при выключенном чате.

## Контроль команд

В `command.yml` можно включить whitelist:

```yaml
whitelist-enabled: true
whitelist:
  - "msg"
  - "r"
  - "spawn"
```

Или blacklist:

```yaml
blacklist-enabled: true
blacklist:
  - "op"
  - "stop"
```

Задержки задаются коротко:

```yaml
delays:
  spawn: 5
  home: 10
```

`primechat.commanddelay.reduce` уменьшает задержку до `reduced-seconds`, а `primechat.commanddelay.bypass` полностью её отключает.

`primechat.commandcontrol.bypass` обходит ограничения whitelist/blacklist.

## Очистка чата

```text
/clearchat
/cc
```

Permission: `primechat.clearchat`.

Сообщения команды настраиваются в `command.yml`.

## Permissions

| Permission | Назначение | По умолчанию |
|---|---|---|
| `primechat.admin` | Управление PrimeChat | OP |
| `primechat.chatcolor` | Цветной и форматированный чат | Нет |
| `primechat.msg` | Личные сообщения | Да |
| `primechat.socialspy` | SocialSpy | OP |
| `primechat.commandspy` | CommandSpy | OP |
| `primechat.clearchat` | Очистка чата | OP |
| `primechat.afk` | AFK | Да |
| `primechat.chat.toggle` | Включение/выключение чата | OP |
| `primechat.chat.bypass` | Обход отключения чата | OP |
| `primechat.commandcontrol.bypass` | Обход whitelist/blacklist | OP |
| `primechat.commanddelay.reduce` | Уменьшение задержек | Нет |
| `primechat.commanddelay.bypass` | Полный обход задержек | OP |

Permissions каналов задаются непосредственно в `config.yml`. Например:

```yaml
permission: "primechat.vip"
```

## Команды PrimeChat

```text
/primechat help
/primechat reload
/primechat version
```

Алиасы:

```text
/pchat
/pc
```

`/pc reload` — основной способ применить изменения конфигурации без перезапуска сервера.

## PlaceholderAPI

PlaceholderAPI является необязательной зависимостью. Без него PrimeChat работает самостоятельно.

Если PlaceholderAPI установлен, его placeholders можно использовать в форматах каналов, hover-тексте и сообщениях.

## Сборка

```text
mvn clean package
```

Готовый JAR появится в `target/`.

## Структура проекта

```text
PrimeChat/
├── src/main/java/corp/prime/chat/
├── src/main/resources/
│   ├── config.yml
│   ├── command.yml
│   └── plugin.yml
├── docs/
│   ├── 01-cover.svg
│   ├── 02-features.svg
│   ├── 03-commands.svg
│   ├── 04-settings.svg
│   └── 05-installation.svg
├── pom.xml
├── LICENSE
└── README.md
```

## PrimeDev

PrimeChat разработан PrimeDev.
