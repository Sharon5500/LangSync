# LangSync

**Minecraft chat translation plugin for Bukkit / Spigot / Paper / Purpur / Folia**

Automatically translates chat messages for each player into their preferred language in real-time using Google Translate. No API key required.

## Features

- **Auto-detection** — detects each player's Minecraft client language on first join
- **Per-player language** — every player sees chat in their own language simultaneously
- **Multilingual UI** — plugin messages shown in the player's own language (ru, en, he, de, fr, es, uk)
- **Name protection** — words in `[brackets]` are never translated
- **Translation cache** — configurable size, saves repeated API calls
- **Folia support** — detected at runtime automatically

## Commands

| Command | Description |
|---|---|
| `/lang` | Show help and current language |
| `/lang <code>` | Set your language (`ru`, `en`, `he`...) |
| `/lang list` | List all supported languages |
| `/lang off` | Disable translation |

## Chat Format
