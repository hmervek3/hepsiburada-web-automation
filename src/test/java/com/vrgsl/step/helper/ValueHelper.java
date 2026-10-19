package com.vrgsl.step.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ==============================================================================
 * ValueHelper (Data-Driven Test Repository)
 * ==============================================================================
 * Test senaryolarında kullanılan test verilerini (kullanıcı bilgileri, arama terimleri vb.)
 * 'src/test/resources/value-infos/*.json' dosyalarından okur ve hafızaya alır.
 * 
 * Avantajı:
 * Senaryo adımlarında doğrudan "hmervek3@gmail.com" yazmak yerine "defaultUserEmail"
 * takma adı (alias) kullanılır. Test verisi değiştiğinde senaryo adımları bozulmaz.
 */
public class ValueHelper {

    private static final ConcurrentHashMap<String, String> valueMap = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    static {
        initValueList();
    }

    /**
     * value-infos dizinindeki tüm JSON dosyalarını tarar ve anahtar-değer haritasına ekler.
     */
    public static synchronized void initValueList() {
        if (initialized && !valueMap.isEmpty()) {
            return;
        }
        valueMap.clear();
        try {
            List<File> jsonFiles = new ArrayList<>();
            File valueDir = new File("src/test/resources/value-infos");
            if (valueDir.exists() && valueDir.isDirectory()) {
                collectJsonFiles(valueDir, jsonFiles);
            } else {
                URL resource = ValueHelper.class.getClassLoader().getResource("value-infos");
                if (resource != null) {
                    File cpDir = new File(resource.getFile());
                    if (cpDir.exists()) {
                        collectJsonFiles(cpDir, jsonFiles);
                    }
                }
            }

            Gson gson = new Gson();

            for (File file : jsonFiles) {
                try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                    JsonElement root = gson.fromJson(reader, JsonElement.class);
                    if (root != null) {
                        if (root.isJsonArray()) {
                            JsonArray arr = root.getAsJsonArray();
                            for (JsonElement el : arr) {
                                if (el.isJsonObject()) {
                                    JsonObject obj = el.getAsJsonObject();
                                    if (obj.has("key") && obj.has("value")) {
                                        valueMap.put(obj.get("key").getAsString(), obj.get("value").getAsString());
                                    }
                                }
                            }
                        } else if (root.isJsonObject()) {
                            JsonObject obj = root.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                if (entry.getValue().isJsonPrimitive()) {
                                    valueMap.put(entry.getKey(), entry.getValue().getAsString());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ValueHelper] Hata: [" + file.getName() + "] dosyası okunurken: " + e.getMessage());
                }
            }
            initialized = true;
        } catch (Exception e) {
            System.err.println("[ValueHelper] Test verileri başlatılamadı: " + e.getMessage());
        }
    }

    private static void collectJsonFiles(File dir, List<File> jsonFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectJsonFiles(f, jsonFiles);
            } else if (f.getName().toLowerCase().endsWith(".json")) {
                jsonFiles.add(f);
            }
        }
    }

    /**
     * Verilen anahtara karşılık gelen test değerini döner.
     * Eğer anahtar value-infos içinde bulunamazsa, kullanıcının doğrudan
     * bir metin girdiğini varsayarak girilen değeri aynen döner.
     */
    public static String getValue(String key) {
        if (!initialized || valueMap.isEmpty()) {
            initValueList();
        }
        String val = valueMap.get(key);
        return val != null ? val : key;
    }

    /**
     * Kesin anahtar kontrolü: Değer bulunamazsa hata fırlatır.
     */
    public static String getValueStrict(String key) {
        if (!initialized || valueMap.isEmpty()) {
            initValueList();
        }
        String val = valueMap.get(key);
        if (val == null) {
            throw new IllegalArgumentException("❌ [" + key + "] anahtarı value-infos dosyalarında bulunamadı!");
        }
        return val;
    }

    public static boolean hasValue(String key) {
        if (!initialized || valueMap.isEmpty()) {
            initValueList();
        }
        return valueMap.containsKey(key);
    }
}
