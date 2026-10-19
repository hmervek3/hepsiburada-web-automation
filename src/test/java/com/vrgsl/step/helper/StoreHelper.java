package com.vrgsl.step.helper;

import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import com.thoughtworks.gauge.datastore.SpecDataStore;
import com.thoughtworks.gauge.datastore.SuiteDataStore;

/**
 * ==============================================================================
 * StoreHelper (Gauge DataStore Facade)
 * ==============================================================================
 * Gauge test adımları (Steps) arasında veri taşımayı sağlayan merkezi yardımcı sınıf.
 * 
 * Neden Statik Değişken Yerine DataStore Kullanılır?
 * - Statik değişkenler (`private static String savedTitle`) paralel test koşumlarında
 *   thread'ler arasında birbirinin üzerine yazar ve testlerin kararsız (flaky) olmasına yol açar.
 * - Gauge'ın ScenarioDataStore yapısı her senaryo için bağımsız bir hafıza alanı sunar
 *   ve senaryo bittiğinde otomatik temizlenir.
 */
public class StoreHelper {

    // ==========================================
    // Senaryo Düzeyi Hafıza (Scenario Level DataStore)
    // ==========================================

    /**
     * Senaryo hafızasına anahtar-değer kaydeder.
     * Örnek: Arama sayfasında seçilen ürünün başlığı
     */
    public static void putScenario(String key, Object value) {
        ScenarioDataStore.put(key, value);
    }

    /**
     * Senaryo hafızasından değeri getirir.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getScenario(String key) {
        return (T) ScenarioDataStore.get(key);
    }

    /**
     * Senaryo hafızasından String değer döner; null ise varsayılan değeri döner.
     */
    public static String getScenarioString(String key, String defaultValue) {
        Object val = getScenario(key);
        return val != null ? String.valueOf(val) : defaultValue;
    }

    /**
     * Senaryo hafızasında anahtarın bulunup bulunmadığını kontrol eder.
     */
    public static boolean hasScenarioKey(String key) {
        return ScenarioDataStore.get(key) != null;
    }

    // ==========================================
    // Spesifikasyon Düzeyi Hafıza (Spec Level DataStore)
    // ==========================================

    public static void putSpec(String key, Object value) {
        SpecDataStore.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSpec(String key) {
        return (T) SpecDataStore.get(key);
    }

    // ==========================================
    // Test Koşum Paketi Düzeyi (Suite Level DataStore)
    // ==========================================

    public static void putSuite(String key, Object value) {
        SuiteDataStore.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSuite(String key) {
        return (T) SuiteDataStore.get(key);
    }
}
