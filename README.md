# LangSync

**Minecraft chat translation plugin for Bukkit / Spigot / Paper / Purpur / Folia**

Automatically translates chat messages for each player into their preferred language in real-time using Google Translate. No API key required.

---

## Features

- **Auto-detection** — detects each player's Minecraft client language on first join and sets it automatically
- **Per-player language** — every player sees chat in their own language simultaneously
- **Multilingual UI** — plugin messages (commands, hints) are shown in the player's own language (ru, en, he, de, fr, es, uk)
- **Name protection** — words in `[brackets]` are never translated
- **Original text** — original message shown in gray next to the translation
- **Translation cache** — repeated messages are not re-translated (configurable size)
- **Persistent storage** — player language settings saved to `langs.dat`, survive restarts
- **Folia support** — detected at runtime via reflection, uses async scheduler automatically

---

## Commands

| Command | Description |
|---|---|
| `/lang` | Show help and current language |
| `/lang <code>` | Set your translation language (e.g. `ru`, `en`, `he`) |
| `/lang list` | List all supported languages |
| `/lang off` | Disable translation |

Tab completion is supported for all arguments.

---

## Supported Languages

`ru` `en` `he` `de` `fr` `es` `zh` `ja` `ar` `pt` `it` `ko` `pl` `tr` `nl` `sv` `uk` `cs`

---

## Chat Format

```
[RU→EN] PlayerName » Hello! (Привет!)
```

- `[SRC→TGT]` — source and target language codes
- Gray text in parentheses — original message
- `[name]` in brackets — protected from translation, highlighted in yellow

---

## Installation

1. Download `LangSync-1.0.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server

**Requirements:** Java 21+ · Minecraft 1.21.1 · Bukkit / Spigot / Paper / Purpur / Folia

---

## Configuration

`plugins/LangSync/config.yml`:

```yaml
default-lang: en
show-original: true

# Auto-set language from player's Minecraft client on first join
auto-detect: true

# Max cached translations in memory (cleared when limit reached)
cache-max-size: 500
```

---

## Building from Source

```bash
git clone https://github.com/Sharon5500/LangSync
cd LangSync
mvn package
```

Output: `target/LangSync-1.0.jar`

---

## License

MIT License — Copyright (c) 2026 Sharon5500
