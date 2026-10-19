# Hepsiburada E-Commerce Test Automation Framework

Bu proje, **Hepsiburada** üzerinde uçtan uca senaryoları koşmak için **Gauge Framework**, **Selenium 4**, **Java 17/21** ve kurumsal **Page Object Model (POM)** standartlarına uygun olarak tasarlanmış ileri düzey bir test otomasyon projesidir.

---

## 🎯 Temel Özellikler ve Mimari Yenilikler

1. **External JSON Element Repository (Harici Locator Yönetimi):**
   - Web elementleri (`By` locator'lar) Java kodları içine gömülmek yerine `src/test/resources/element-infos/*.json` dosyalarında yönetilir.
   - UI değişikliklerinde Java kodu derlenmeden sadece JSON güncellenir.
   - Parametrik locator (`getBy(key, params)`) desteği sunar.

2. **Kırılmaz & Kendi Kendini Onaran "2. Satır 1. Ürün" Algoritması (Self-Healing Grid):**
   - **1. Katman:** JavaScript `getBoundingClientRect` kümeleme (ekran genişliğine göre dikey/yatay piksel analizi).
   - **2. Katman:** Dinamik sütun orantısı (`Container Width / Card Width`).
   - **3. Katman:** Piksel Y-koordinatı eşikleme (`getLocation().getY() > firstRowY + 50`).
   - **4. Katman:** Reklam ve sponsorlu afişler filtrelenmiş temiz listeden İndeks 4 Fallback'i.

3. **Efilli Shadow DOM & OneTrust Çerez Yönetimi:**
   - Hepsiburada'nın güncel çerez motoru `efilli-layout-dynamic` web bileşeninin Shadow DOM'una JS ile erişip çerezleri otomatik kapatır.

4. **CDP (Chrome DevTools Protocol) Anti-Bot & Stealth Entegrasyonu:**
   - `Page.addScriptToEvaluateOnNewDocument` ile `navigator.webdriver`, WebGL, donanım izinleri maskelenir.
   - `CdpHelper.humanType` ve `CdpHelper.humanClick` ile insan reflekslerine uygun fare ve klavye simülasyonu yapılır.
   - İsteğe bağlı Remote Debugging (port 9222) desteği.

5. **Tek ve Çift Adımlı Dinamik Login:**
   - Arayüzün durumuna göre tek adımda veya kullanıcı adı -> sonraki -> şifre şeklinde 2 adımlı girişi otomatik tespit edip yürütür.

6. **Thread-Safe & Paralel Koşuma Hazır:**
   - `ThreadLocal<WebDriver>` ile izole oturum yönetimi.
   - Test adımları arasında veri transferi `StoreHelper` (Gauge `ScenarioDataStore`) ile güvenli şekilde yapılır.

---

## 📁 Proje Dizin Yapısı

```
hepsiburada-web-otomasyon/
├── specs/
│   ├── concepts/
│   │   └── login.cpt                        # Yeniden kullanılabilir login konsepti
│   └── hepsiburada_shopping.spec            # HB-TC01 (TR & EN) Gauge senaryoları
├── src/test/java/com/vrgsl/step/
│   ├── driver/
│   │   ├── DriverHooks.java                 # @BeforeSuite, @BeforeScenario, @AfterScenario
│   │   └── DriverManager.java               # ThreadLocal WebDriver, Remote Debug, CDP Stealth
│   ├── helper/
│   │   ├── CdpHelper.java                   # CDP fare ve insan benzeri klavye simülasyonu
│   │   ├── ConfigHelper.java                # YAML konfigürasyon okuyucusu ve deep merge
│   │   ├── ElementHelper.java               # JSON Element Repository yöneticisi
│   │   ├── StoreHelper.java                 # Gauge Scenario/Spec DataStore yöneticisi
│   │   └── ValueHelper.java                 # Test verileri repository yöneticisi
│   ├── model/
│   │   └── ElementInfo.java                 # JSON locator modeli
│   ├── pages/
│   │   ├── BasePage.java                    # Shadow DOM çerez, safeClick, waitForPageLoad
│   │   ├── HomePage.java                    # Arama, hover menü, login yönlendirmesi
│   │   ├── LoginPage.java                   # Tek/çift adımlı akıllı login
│   │   ├── SearchResultsPage.java           # Hibrit 2. satır 1. ürün tespit algoritması
│   │   ├── ProductDetailPage.java           # Detay sayfası, sepete ekleme ve sepet yönlendirmesi
│   │   └── CartPage.java                    # Fuzzy sepet doğrulama ve boş sepet kontrolü
│   └── steps/
│       ├── BaseSteps.java                   # Genel amaçlı zengin UI adım kütüphanesi
│       └── StepImplementation.java          # Senaryo adım tanımlamaları (TR & EN)
├── src/test/resources/
│   ├── browser-options.yaml                 # Tarayıcı argümanları, stealth scripti ve timeoutlar
│   ├── qa-web.yaml                          # Test ortamı URL ve temel ayarları
│   ├── element-infos/                       # Harici JSON Locator Havuzu
│   │   ├── cart.json
│   │   ├── home.json
│   │   ├── login.json
│   │   ├── product_detail.json
│   │   └── search.json
│   └── value-infos/                         # Test Verileri
│       └── users.json                       # Kullanıcı bilgileri ve test parametreleri
├── manifest.json                            # Gauge eklenti manifestosu
├── pom.xml                                  # Maven bağımlılıkları (Selenium 4, Gauge, AssertJ)
└── README.md
```

---

## 🚀 Çalıştırma Talimatları

### 1. Terminal / Maven İle Çalıştırma
```bash
# Tüm senaryoları koşturmak için:
mvn test

# Belirli bir tag'e sahip senaryoları koşturmak için:
mvn test -Dtags="turkish"
# veya
mvn test -Dtags="english"
```

### 2. IntelliJ IDEA İle Çalıştırma
1. **File > Open** ile bu projeyi seçip açın.
2. Maven bağımlılıklarının yüklenmesini bekleyin.
3. `specs/hepsiburada_shopping.spec` dosyasını açıp senaryo yanındaki yeşil **Play** butonuna tıklayarak testi koşturabilirsiniz.
