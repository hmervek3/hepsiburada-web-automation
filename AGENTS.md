# Proje Hafızası ve Geliştirme Kuralları (Project Memory)

Bu dosya, Antigravity IDE ve yapay zeka asistanının her oturumda projeyi, mimari kararları ve geçmişi hatırlaması için tanımlanmıştır.

## 📌 Proje Genel Bakışı
- **Proje Adı:** Hepsiburada Web Test Otomasyonu (Süper Proje)
- **Teknoloji Yığını:** Java 17/21, Selenium 4.25.0, Gauge Framework 1.0.3, AssertJ 3.26.3, Maven, Gson, SnakeYAML
- **Test Hesabı:** `hmervek3@gmail.com` / `Bp+iCbsnz7Xi2Q@`
- **Hedef Senaryo (HB-TC01):** Hepsiburada üzerinde login olma, 'bilgisayar' arama, 2. satır 1. ürünü seçip sepete ekleme ve sepette olduğunu doğrulama.

## 🏗️ Mimari Prensipler ve Standartlar
1. **Harici JSON Element Havuzu (External JSON Locators):**
   - Locator'lar (`By`) Java sınıflarında hardcoded olarak bulunmaz.
   - Tüm locator'lar `src/test/resources/element-infos/` altındaki JSON dosyalarında yönetilir (`home.json`, `login.json`, `search.json`, `product_detail.json`, `cart.json`).
   - `ElementHelper.getBy(key)` veya `ElementHelper.getBy(key, params)` kullanılır.

2. **Kırılmaz 2. Satır 1. Ürün Seçim Algoritması:**
   - `SearchResultsPage` içinde sabit `index = 4` KULLANILMAZ.
   - 4 katmanlı hibrit sistem devrededir:
     1. JavaScript `getBoundingClientRect` ile tarayıcı içi dikey/yatay kümeleme.
     2. Container / Card genişliği orantısı ile responsive sütun hesabı.
     3. Piksel `getY()` koordinat eşikleme.
     4. Reklam banner'ları temizlenmiş listeden İndeks 4 Fallback'i.

3. **Çerez Yönetimi (Shadow DOM):**
   - Hepsiburada `efilli-layout-dynamic` web bileşeni kullanır.
   - `BasePage.dismissCookiesIfPresent()` içinde JS ile shadowRoot açılarak çerez kapatılır.

4. **Bot Koruması ve İnsan Simülasyonu (CDP):**
   - `DriverManager`: `Page.addScriptToEvaluateOnNewDocument` ile `navigator.webdriver` izini gizler.
   - `CdpHelper`: `Input.dispatchMouseEvent` ve `Input.dispatchKeyEvent` ile insani refleks gecikmeleriyle tıklar ve yazar.

5. **Thread-Safety ve Veri Paylaşımı:**
   - `DriverManager`: `ThreadLocal<WebDriver>` kullanır (paralel teste hazır).
   - Adımlar arası seçilen ürün başlığı `StoreHelper.putScenario()` (Gauge `ScenarioDataStore`) ile taşınır.
