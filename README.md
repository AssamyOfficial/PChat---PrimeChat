# 💬 PrimeChat

**Modern and customizable Minecraft chat formatter by PrimeDev.**

PrimeChat — лёгкий и удобный плагин для настройки Minecraft-чата.

Настраивайте формат сообщений, используйте стандартные Minecraft-цвета, HEX-цвета и различные стили текста — всё через простой `config.yml`.

---

## ✨ Возможности

- 🎨 Стандартные Minecraft-цвета
- 🌈 HEX-цвета
- **Жирный текст**
- *Курсив*
- Подчёркивание
- ~~Зачёркивание~~
- Искажённый текст
- 👤 `%player%`
- 👤 `%displayname%`
- 💬 `%message%`
- ⚙️ Простая настройка через `config.yml`
- 🔄 Перезагрузка конфигурации без перезапуска сервера
- ⚡ Лёгкий и быстрый

---

## 🔌 PlaceholderAPI

PrimeChat поддерживает PlaceholderAPI как **необязательную зависимость**.

PrimeChat продолжает работать без PlaceholderAPI, однако после его установки становится доступно большое количество дополнительных placeholders.

### Установка

1. Установите PlaceholderAPI на сервер.
2. Перезапустите сервер.
3. При необходимости установите нужные expansions.
4. Используйте placeholders в `chat-format`.

### Пример

```yaml
chat-format: "<gray>[<white>%player_name%</white>]</gray> <#00BFFF>»</#00BFFF> <white>%message%</white>"
```

# 📦 Установка

### 1. Скачайте PrimeChat

Скачайте файл:

```text
PrimeChat-1.1.0.jar