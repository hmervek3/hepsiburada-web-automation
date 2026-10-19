package com.vrgsl.step.helper;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ==============================================================================
 * ConfigHelper
 * ==============================================================================
 * Projedeki YAML konfigürasyon dosyalarını (browser-options.yaml, qa-web.yaml) okur,
 * hiyerarşik yapıları birleştirir (deep merge) ve thread-safe bir önbellekte (ConcurrentHashMap) saklar.
 * 
 * Özellikler:
 * - Nokta notasyonu ile nested değerlere erişim (Örn: ConfigHelper.get("chrome.stealth.enabled"))
 * - Ortam değişkeni veya sistem özelliği (-Dbrowser=firefox) ile çalışma anında ezebilme (override)
 * - ${user.home} veya ${env} gibi yer tutucuları (placeholder) dinamik çözme
 */
public class ConfigHelper {

    private static final Map<String, Object> configCache = new ConcurrentHashMap<>();
    private static volatile boolean loaded = false;

    static {
        loadConfigurations();
    }

    /**
     * Konfigürasyon dosyalarını kaynak dizininden veya classpath'ten yükler.
     * Birden fazla YAML dosyası varsa birbirinin üzerine merge edilir.
     */
    @SuppressWarnings("unchecked")
    public static synchronized void loadConfigurations() {
        if (loaded && !configCache.isEmpty()) {
            return;
        }
        configCache.clear();
        Yaml yaml = new Yaml();

        String[] configFiles = {
                "browser-options.yaml",
                "qa-web.yaml"
        };

        for (String fileName : configFiles) {
            try {
                InputStream is = null;
                File file = new File("src/test/resources/" + fileName);
                if (file.exists()) {
                    is = new FileInputStream(file);
                } else {
                    URL res = ConfigHelper.class.getClassLoader().getResource(fileName);
                    if (res != null) {
                        is = res.openStream();
                    }
                }

                if (is != null) {
                    try (InputStream stream = is) {
                        Map<String, Object> data = yaml.load(stream);
                        if (data != null) {
                            deepMerge(configCache, data);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ConfigHelper] Bilgi: [" + fileName + "] dosyası yüklenirken: " + e.getMessage());
            }
        }
        loaded = true;
    }

    /**
     * İç içe geçmiş Map yapılarını hiyerarşik olarak birleştirir.
     */
    @SuppressWarnings("unchecked")
    private static void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map && target.get(key) instanceof Map) {
                deepMerge((Map<String, Object>) target.get(key), (Map<String, Object>) value);
            } else {
                target.put(key, value);
            }
        }
    }

    /**
     * Nokta notasyonuyla konfigürasyon değerini getirir.
     * Örnek: "chrome.args", "timeouts.explicitWait"
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String dotKey) {
        if (!loaded) {
            loadConfigurations();
        }

        // JVM parametresi (-Dkey=value) ile ezilmişse öncelikli olarak onu kullan
        String sysProp = System.getProperty(dotKey);
        if (sysProp != null) {
            return (T) sysProp;
        }

        String[] keys = dotKey.split("\\.");
        Object current = configCache;
        for (String k : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(k);
            } else {
                return null;
            }
        }

        if (current instanceof String strVal) {
            return (T) resolvePlaceholders(strVal);
        }

        return (T) current;
    }

    public static String getString(String key, String defaultValue) {
        Object val = get(key);
        return val != null ? String.valueOf(val) : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        Object val = get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val != null) {
            try {
                return Integer.parseInt(val.toString().trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Object val = get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        } else if (val != null) {
            return Boolean.parseBoolean(val.toString().trim());
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getList(String key) {
        Object val = get(key);
        if (val instanceof List) {
            List<?> rawList = (List<?>) val;
            List<String> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item != null) {
                    result.add(resolvePlaceholders(item.toString()));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(String key) {
        Object val = get(key);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        return Collections.emptyMap();
    }

    private static String resolvePlaceholders(String text) {
        if (text == null) return null;
        String userHome = System.getProperty("user.home", "");
        String env = System.getProperty("env", "qa-web");
        return text.replace("${user.home}", userHome)
                   .replace("${env}", env);
    }
}
