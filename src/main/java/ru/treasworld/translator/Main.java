package ru.treasworld.translator;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final Map<UUID, String> playerLanguages = new ConcurrentHashMap<>();
    private final Set<UUID> translationDisabled = ConcurrentHashMap.newKeySet();
    private final Map<String, String> globalTranslationCache = new ConcurrentHashMap<>();

    private static final String DEFAULT_LANG = "en";
    private static final List<String> AVAILABLE_LANGS = Arrays.asList(
            "ru", "en", "he", "de", "fr", "es", "zh", "ja", "ar", "pt",
            "it", "ko", "pl", "tr", "nl", "sv", "uk", "cs", "off"
    );

    // Переводы сообщений плагина
    private static final Map<String, Map<String, String>> MESSAGES = new HashMap<>();

    static {
        // Русский
        Map<String, String> ru = new HashMap<>();
        ru.put("lang_set",       "§aЯзык установлен: §e{lang} §7(перевод включён)");
        ru.put("lang_off",       "§cПеревод отключён. Включить: §e/lang <код>");
        ru.put("lang_current",   "§7Твой язык: §e{lang}");
        ru.put("lang_disabled",  "§7Твой язык: §cвыключен");
        ru.put("lang_list",      "§bДоступные языки: §f{list}");
        ru.put("only_player",    "§cКоманда только для игроков.");
        ru.put("error",          "§cОшибка. Попробуй ещё раз.");
        ru.put("auto_detected",  "§7Язык клиента определён: §e{lang}§7. Используй §f/lang§7 для смены.");
        ru.put("help_title",     "§b ChatTranslator §7— помощь");
        ru.put("help_1",         "§f/lang §7— показать этот список");
        ru.put("help_2",         "§f/lang <код> §7— установить язык §7(ru, en, he, de...)");
        ru.put("help_3",         "§f/lang list §7— список всех языков");
        ru.put("help_4",         "§f/lang off §7— отключить перевод");
        ru.put("sign_translation", "§b[Табличка] §7Перевод: §f{text}");
        ru.put("sign_empty",     "§7Табличка пустая.");
        MESSAGES.put("ru", ru);

        // Английский
        Map<String, String> en = new HashMap<>();
        en.put("lang_set",       "§aLanguage set to: §e{lang} §7(translation enabled)");
        en.put("lang_off",       "§cTranslation disabled. Enable: §e/lang <code>");
        en.put("lang_current",   "§7Your language: §e{lang}");
        en.put("lang_disabled",  "§7Your language: §cdisabled");
        en.put("lang_list",      "§bAvailable languages: §f{list}");
        en.put("only_player",    "§cThis command is for players only.");
        en.put("error",          "§cError. Please try again.");
        en.put("auto_detected",  "§7Client language detected: §e{lang}§7. Use §f/lang§7 to change.");
        en.put("help_title",     "§b ChatTranslator §7— help");
        en.put("help_1",         "§f/lang §7— show this list");
        en.put("help_2",         "§f/lang <code> §7— set language §7(ru, en, he, de...)");
        en.put("help_3",         "§f/lang list §7— list all languages");
        en.put("help_4",         "§f/lang off §7— disable translation");
        en.put("sign_translation", "§b[Sign] §7Translation: §f{text}");
        en.put("sign_empty",     "§7The sign is empty.");
        MESSAGES.put("en", en);

        // Иврит
        Map<String, String> he = new HashMap<>();
        he.put("lang_set",       "§aשפה הוגדרה: §e{lang} §7(תרגום פעיל)");
        he.put("lang_off",       "§cתרגום כבוי. הפעל: §e/lang <קוד>");
        he.put("lang_current",   "§7השפה שלך: §e{lang}");
        he.put("lang_disabled",  "§7השפה שלך: §cכבויה");
        he.put("lang_list",      "§bשפות זמינות: §f{list}");
        he.put("only_player",    "§cפקודה זו מיועדת לשחקנים בלבד.");
        he.put("error",          "§cשגיאה. נסה שוב.");
        he.put("auto_detected",  "§7שפת הלקוח זוהתה: §e{lang}§7. השתמש ב-§f/lang§7 לשינוי.");
        he.put("help_title",     "§b ChatTranslator §7— עזרה");
        he.put("help_1",         "§f/lang §7— הצג רשימה זו");
        he.put("help_2",         "§f/lang <קוד> §7— הגדר שפה §7(ru, en, he, de...)");
        he.put("help_3",         "§f/lang list §7— רשימת כל השפות");
        he.put("help_4",         "§f/lang off §7— כבה תרגום");
        he.put("sign_translation", "§b[שלט] §7תרגום: §f{text}");
        he.put("sign_empty",     "§7השלט ריק.");
        MESSAGES.put("he", he);

        // Немецкий
        Map<String, String> de = new HashMap<>();
        de.put("lang_set",       "§aSprache gesetzt: §e{lang} §7(Übersetzung aktiv)");
        de.put("lang_off",       "§cÜbersetzung deaktiviert. Aktivieren: §e/lang <Code>");
        de.put("lang_current",   "§7Deine Sprache: §e{lang}");
        de.put("lang_disabled",  "§7Deine Sprache: §cdeaktiviert");
        de.put("lang_list",      "§bVerfügbare Sprachen: §f{list}");
        de.put("only_player",    "§cDieser Befehl ist nur für Spieler.");
        de.put("error",          "§cFehler. Bitte versuche es erneut.");
        de.put("auto_detected",  "§7Clientsprache erkannt: §e{lang}§7. Nutze §f/lang§7 zum Ändern.");
        de.put("help_title",     "§b ChatTranslator §7— Hilfe");
        de.put("help_1",         "§f/lang §7— Diese Liste anzeigen");
        de.put("help_2",         "§f/lang <Code> §7— Sprache setzen §7(ru, en, he, de...)");
        de.put("help_3",         "§f/lang list §7— Alle Sprachen auflisten");
        de.put("help_4",         "§f/lang off §7— Übersetzung deaktivieren");
        de.put("sign_translation", "§b[Schild] §7Übersetzung: §f{text}");
        de.put("sign_empty",     "§7Das Schild ist leer.");
        MESSAGES.put("de", de);

        // Французский
        Map<String, String> fr = new HashMap<>();
        fr.put("lang_set",       "§aLangue définie: §e{lang} §7(traduction activée)");
        fr.put("lang_off",       "§cTraduction désactivée. Activer: §e/lang <code>");
        fr.put("lang_current",   "§7Ta langue: §e{lang}");
        fr.put("lang_disabled",  "§7Ta langue: §cdésactivée");
        fr.put("lang_list",      "§bLangues disponibles: §f{list}");
        fr.put("only_player",    "§cCette commande est réservée aux joueurs.");
        fr.put("error",          "§cErreur. Réessaie.");
        fr.put("auto_detected",  "§7Langue client détectée: §e{lang}§7. Utilise §f/lang§7 pour changer.");
        fr.put("help_title",     "§b ChatTranslator §7— aide");
        fr.put("help_1",         "§f/lang §7— afficher cette liste");
        fr.put("help_2",         "§f/lang <code> §7— définir la langue §7(ru, en, he, de...)");
        fr.put("help_3",         "§f/lang list §7— lister toutes les langues");
        fr.put("help_4",         "§f/lang off §7— désactiver la traduction");
        fr.put("sign_translation", "§b[Panneau] §7Traduction: §f{text}");
        fr.put("sign_empty",     "§7Le panneau est vide.");
        MESSAGES.put("fr", fr);

        // Испанский
        Map<String, String> es = new HashMap<>();
        es.put("lang_set",       "§aIdioma establecido: §e{lang} §7(traducción activada)");
        es.put("lang_off",       "§cTraducción desactivada. Activar: §e/lang <código>");
        es.put("lang_current",   "§7Tu idioma: §e{lang}");
        es.put("lang_disabled",  "§7Tu idioma: §cdesactivado");
        es.put("lang_list",      "§bIdiomas disponibles: §f{list}");
        es.put("only_player",    "§cEste comando es solo para jugadores.");
        es.put("error",          "§cError. Inténtalo de nuevo.");
        es.put("auto_detected",  "§7Idioma del cliente detectado: §e{lang}§7. Usa §f/lang§7 para cambiar.");
        es.put("help_title",     "§b ChatTranslator §7— ayuda");
        es.put("help_1",         "§f/lang §7— mostrar esta lista");
        es.put("help_2",         "§f/lang <código> §7— establecer idioma §7(ru, en, he, de...)");
        es.put("help_3",         "§f/lang list §7— listar todos los idiomas");
        es.put("help_4",         "§f/lang off §7— desactivar traducción");
        es.put("sign_translation", "§b[Letrero] §7Traducción: §f{text}");
        es.put("sign_empty",     "§7El letrero está vacío.");
        MESSAGES.put("es", es);

        // Украинский
        Map<String, String> uk = new HashMap<>();
        uk.put("lang_set",       "§aМову встановлено: §e{lang} §7(переклад увімкнено)");
        uk.put("lang_off",       "§cПереклад вимкнено. Увімкнути: §e/lang <код>");
        uk.put("lang_current",   "§7Твоя мова: §e{lang}");
        uk.put("lang_disabled",  "§7Твоя мова: §cвимкнено");
        uk.put("lang_list",      "§bДоступні мови: §f{list}");
        uk.put("only_player",    "§cЦя команда тільки для гравців.");
        uk.put("error",          "§cПомилка. Спробуй ще раз.");
        uk.put("auto_detected",  "§7Мову клієнта визначено: §e{lang}§7. Використай §f/lang§7 для зміни.");
        uk.put("help_title",     "§b ChatTranslator §7— допомога");
        uk.put("help_1",         "§f/lang §7— показати цей список");
        uk.put("help_2",         "§f/lang <код> §7— встановити мову §7(ru, en, he, de...)");
        uk.put("help_3",         "§f/lang list §7— список усіх мов");
        uk.put("help_4",         "§f/lang off §7— вимкнути переклад");
        uk.put("sign_translation", "§b[Табличка] §7Переклад: §f{text}");
        uk.put("sign_empty",     "§7Табличка порожня.");
        MESSAGES.put("uk", uk);
    }

    private File langsFile;
    private boolean autoDetect;
    private int cacheMaxSize;
    private final Map<UUID, Long> signCooldown = new ConcurrentHashMap<>();
    private static final long SIGN_COOLDOWN_MS = 1500;

    private String msg(Player player, String key) {
        String uiLang = getUiLang(player);
        Map<String, String> msgs = MESSAGES.getOrDefault(uiLang, MESSAGES.get("en"));
        return msgs.getOrDefault(key, MESSAGES.get("en").getOrDefault(key, key));
    }

    private String msg(Player player, String key, String... replacements) {
        String text = msg(player, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        return text;
    }

    private String getUiLang(Player player) {
        String locale = player.getLocale();
        if (locale != null && locale.length() >= 2) {
            String lang = locale.substring(0, 2).toLowerCase();
            if (MESSAGES.containsKey(lang)) return lang;
        }
        return "en";
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        autoDetect = getConfig().getBoolean("auto-detect", true);
        cacheMaxSize = getConfig().getInt("cache-max-size", 500);
        loadLangsFromFile();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("lang") != null) {
            getCommand("lang").setExecutor(this);
            getCommand("lang").setTabCompleter(this);
        }
        getLogger().info("ChatTranslator enabled! Players: " + playerLanguages.size()
                + " | auto-detect: " + autoDetect + " | cache-max: " + cacheMaxSize);
    }

    @Override
    public void onDisable() {
        saveLangsToFile();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!autoDetect) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!playerLanguages.containsKey(uuid) && !translationDisabled.contains(uuid)) {
            String locale = player.getLocale();
            if (locale != null && locale.length() >= 2) {
                String detectedLang = locale.substring(0, 2).toLowerCase();
                playerLanguages.put(uuid, detectedLang);
                saveLangsToFile();
                Bukkit.getScheduler().runTaskLater(this, () ->
                    player.sendMessage(msg(player, "auto_detected", "{lang}", detectedLang)), 40L);
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        // Фикс двойного срабатывания: обрабатываем только основную руку
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign sign)) return;

        // Cooldown 1.5 секунды между переводами
        long now = System.currentTimeMillis();
        Long last = signCooldown.get(player.getUniqueId());
        if (last != null && now - last < SIGN_COOLDOWN_MS) return;
        signCooldown.put(player.getUniqueId(), now);

        event.setCancelled(true);

        String[] lines = sign.getLines();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String stripped = line.replaceAll("§[0-9a-fk-or]", "").trim();
            if (!stripped.isEmpty()) sb.append(stripped).append(" ");
        }
        String signText = sb.toString().trim();

        if (signText.isEmpty()) {
            player.sendMessage(msg(player, "sign_empty"));
            return;
        }

        String targetLang = playerLanguages.getOrDefault(player.getUniqueId(), DEFAULT_LANG);
        String finalText = signText;

        runAsync(() -> {
            String translated = translateWithFallback(finalText, "auto", targetLang);
            String result = (translated != null && !translated.equals(finalText)) ? translated : finalText;
            Bukkit.getScheduler().runTask(this, () ->
                player.sendMessage(msg(player, "sign_translation", "{text}", result)));
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(MESSAGES.get("en").get("only_player"));
                return true;
            }

            if (args.length == 0) {
                sendHelp(player);
                return true;
            }

            String arg = args[0].toLowerCase();

            if (arg.equals("off")) {
                translationDisabled.add(player.getUniqueId());
                playerLanguages.remove(player.getUniqueId());
                player.sendMessage(msg(player, "lang_off"));
                saveLangsToFile();
                return true;
            }

            if (arg.equals("list")) {
                player.sendMessage(msg(player, "lang_list", "{list}", String.join("§7, §f", AVAILABLE_LANGS)));
                return true;
            }

            translationDisabled.remove(player.getUniqueId());
            playerLanguages.put(player.getUniqueId(), arg);
            player.sendMessage(msg(player, "lang_set", "{lang}", arg));
            saveLangsToFile();
            return true;
        } catch (Exception e) {
            getLogger().severe("Error in /lang: " + e.getMessage());
            if (sender instanceof Player p) {
                p.sendMessage(msg(p, "error"));
            }
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(AVAILABLE_LANGS);
            completions.add("list");
            String typed = args[0].toLowerCase();
            completions.removeIf(s -> !s.startsWith(typed));
            return completions;
        }
        return Collections.emptyList();
    }

    private void sendHelp(Player player) {
        String current = translationDisabled.contains(player.getUniqueId())
                ? msg(player, "lang_disabled")
                : msg(player, "lang_current", "{lang}", playerLanguages.getOrDefault(player.getUniqueId(), DEFAULT_LANG));
        player.sendMessage("§8§m--------------------");
        player.sendMessage(msg(player, "help_title"));
        player.sendMessage("§8§m--------------------");
        player.sendMessage(msg(player, "help_1"));
        player.sendMessage(msg(player, "help_2"));
        player.sendMessage(msg(player, "help_3"));
        player.sendMessage(msg(player, "help_4"));
        player.sendMessage("§8§m--------------------");
        player.sendMessage(current);
        player.sendMessage("§8§m--------------------");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String message = event.getMessage();
        String senderLang = playerLanguages.getOrDefault(sender.getUniqueId(), "auto");

        runAsync(() -> {
            String detectedLang = senderLang.equals("auto") ? detectLanguage(message) : senderLang;
            String sourceLang = (detectedLang != null) ? detectedLang : "auto";
            String translatablePart = extractTranslatable(message);

            for (Player recipient : Bukkit.getOnlinePlayers()) {
                if (translationDisabled.contains(recipient.getUniqueId())) {
                    recipient.sendMessage("§f" + sender.getName() + " §7» §f" + message);
                    continue;
                }

                String targetLang = playerLanguages.getOrDefault(recipient.getUniqueId(), DEFAULT_LANG);
                String finalMessage;

                if (targetLang.equalsIgnoreCase(sourceLang)) {
                    finalMessage = message;
                } else {
                    String cacheKey = sourceLang + ">" + targetLang + ":" + translatablePart;
                    if (globalTranslationCache.size() >= cacheMaxSize) {
                        globalTranslationCache.clear();
                    }
                    String translatedPart = globalTranslationCache.computeIfAbsent(cacheKey, k -> {
                        String translated = translateWithFallback(translatablePart, sourceLang, targetLang);
                        return (translated != null) ? translated : translatablePart;
                    });
                    finalMessage = restoreProtected(message, translatedPart);
                }

                String original = finalMessage.equals(message) ? "" : " §8§o(" + message + ")";
                recipient.sendMessage("§8[" + sourceLang.toUpperCase() + "→" + targetLang.toUpperCase() + "] §f"
                        + sender.getName() + " §7» §f" + finalMessage + original);
            }
        });
    }

    private static final boolean IS_FOLIA;
    static {
        boolean folia = false;
        try { Class.forName("io.papermc.paper.threadedregions.RegionizedServer"); folia = true; }
        catch (ClassNotFoundException ignored) {}
        IS_FOLIA = folia;
    }

    private void runAsync(Runnable task) {
        if (IS_FOLIA) {
            try {
                Object asyncScheduler = getServer().getClass().getMethod("getAsyncScheduler").invoke(getServer());
                asyncScheduler.getClass()
                        .getMethod("runNow", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class)
                        .invoke(asyncScheduler, this, (java.util.function.Consumer<Object>) t -> task.run());
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskAsynchronously(this, task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, task);
        }
    }

    private static final String PLACEHOLDER_PREFIX = "NAMEPLACEHOLDER";

    private String extractTranslatable(String message) {
        return message.replaceAll("\\[([^\\]]+)\\]", PLACEHOLDER_PREFIX);
    }

    private String restoreProtected(String original, String translated) {
        List<String> names = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[([^\\]]+)\\]").matcher(original);
        while (m.find()) names.add(m.group(1));
        String result = translated;
        for (String name : names) {
            result = result.replaceFirst(java.util.regex.Pattern.quote(PLACEHOLDER_PREFIX), "§e" + name + "§f");
        }
        return result.replace(PLACEHOLDER_PREFIX, "");
    }

    private String detectLanguage(String text) {
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&dt=ld&q="
                    + URLEncoder.encode(text, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            if (conn.getResponseCode() != 200) return null;
            JsonArray array = JsonParser.parseReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonArray();
            if (array.size() > 2 && !array.get(2).isJsonNull()) return array.get(2).getAsString();
            return null;
        } catch (Exception e) { return null; }
    }

    private String translateWithFallback(String text, String sourceLang, String targetLang) {
        // Уровень 1: Google Translate
        String result = translate(text, sourceLang, targetLang);
        if (result != null) return result;

        // Уровень 2: MyMemory
        result = translateMyMemory(text, sourceLang, targetLang);
        if (result != null) return result;

        // Уровень 3: оригинал
        return null;
    }

    private String translateMyMemory(String text, String sourceLang, String targetLang) {
        try {
            String langPair = sourceLang + "|" + targetLang;
            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&langpair=" + URLEncoder.encode(langPair, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            if (conn.getResponseCode() != 200) return null;
            JsonArray root = JsonParser.parseReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("matches");
            if (root != null && root.size() > 0) {
                String translated = root.get(0).getAsJsonObject()
                        .get("translation").getAsString();
                return (translated != null && !translated.isEmpty()) ? translated : null;
            }
            return null;
        } catch (Exception e) { return null; }
    }

    private String translate(String text, String sourceLang, String targetLang) {
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="
                    + sourceLang + "&tl=" + targetLang
                    + "&dt=t&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            if (conn.getResponseCode() != 200) return null;
            JsonArray array = JsonParser.parseReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonArray();
            StringBuilder result = new StringBuilder();
            JsonArray translations = array.get(0).getAsJsonArray();
            for (int i = 0; i < translations.size(); i++) {
                JsonArray pair = translations.get(i).getAsJsonArray();
                if (pair.size() > 0 && !pair.get(0).isJsonNull()) result.append(pair.get(0).getAsString());
            }
            return result.toString();
        } catch (Exception e) { return null; }
    }

    private void loadLangsFromFile() {
        langsFile = new File(getDataFolder(), "langs.dat");
        if (!langsFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(langsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(parts[0]);
                    if (parts[1].equals("off")) translationDisabled.add(uuid);
                    else playerLanguages.put(uuid, parts[1]);
                }
            }
        } catch (Exception e) { getLogger().warning("Could not load langs.dat: " + e.getMessage()); }
    }

    private void saveLangsToFile() {
        try {
            getDataFolder().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(langsFile))) {
                for (Map.Entry<UUID, String> entry : playerLanguages.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
                for (UUID uuid : translationDisabled) {
                    writer.write(uuid + "=off");
                    writer.newLine();
                }
            }
        } catch (Exception e) { getLogger().warning("Could not save langs.dat: " + e.getMessage()); }
    }
}
