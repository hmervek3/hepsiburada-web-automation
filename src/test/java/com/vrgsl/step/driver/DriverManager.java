package com.vrgsl.step.driver;

import com.vrgsl.step.helper.ConfigHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * DriverManager (Thread-Safe WebDriver & Multi-Browser Manager)
 * ==============================================================================
 * Tarayıcı başlatma, konfigüre etme ve sonlandırma işlemlerini merkezi olarak yönetir.
 * 
 * Ana Yetenekler:
 * 1. ThreadLocal<WebDriver>: Her test thread'ine bağımsız driver nesnesi atar; paralel testlerde çarpışmayı engeller.
 * 2. Remote Debugging Desteği: 9222 portunda açık hazır bir Chrome varsa ona bağlanabilir.
 * 3. CDP Stealth Script Enjeksiyonu: Tarayıcı açılır açılmaz navigator.webdriver bayrağını ve bot imzalarını gizler.
 * 4. YAML Konfigürasyon Entegrasyonu: Argümanlar, tercihler ve timeout'lar browser-options.yaml'dan otomatik okunur.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();

    /**
     * Aktif thread'e ait WebDriver nesnesini döner.
     */
    public static WebDriver getDriver() {
        return threadDriver.get();
    }

    /**
     * Konfigürasyon ayarlarına göre WebDriver'ı başlatır ve ThreadLocal havuzuna kaydeder.
     */
    public static synchronized WebDriver createDriver() {
        if (threadDriver.get() != null) {
            return threadDriver.get();
        }

        String defaultBrowser = ConfigHelper.getString("browser", "chrome");
        String browser = System.getProperty("browser", defaultBrowser).toLowerCase();

        // 1. Remote Debugging (Port 9222) Kontrolü
        boolean remoteDebugEnabled = ConfigHelper.getBoolean("remoteDebug.enabled", false);
        String remoteHost = ConfigHelper.getString("remoteDebug.host", "127.0.0.1");
        int remotePort = ConfigHelper.getInt("remoteDebug.port", 9222);

        boolean isRemoteDebugOpen = false;
        if (remoteDebugEnabled && browser.contains("chrome")) {
            try (Socket socket = new Socket(remoteHost, remotePort)) {
                isRemoteDebugOpen = true;
            } catch (Exception ignored) {}
        }

        WebDriver driver;
        if (isRemoteDebugOpen) {
            System.out.println("🔗 Remote Debugging Portu (" + remotePort + ") aktif! Mevcut Chrome oturumuna bağlanılıyor...");
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("debuggerAddress", remoteHost + ":" + remotePort);
            driver = new ChromeDriver(options);
        } else if (browser.contains("firefox")) {
            System.out.println("🦊 Firefox WebDriver başlatılıyor...");
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            List<String> ffArgs = ConfigHelper.getList("firefox.args");
            for (String arg : ffArgs) {
                options.addArguments(arg);
            }
            if (ConfigHelper.getBoolean("firefox.headless", false)) {
                options.addArguments("-headless");
            }
            driver = new FirefoxDriver(options);
            driver.manage().window().maximize();
        } else {
            System.out.println("🌐 Chrome WebDriver (Anti-Bot Stealth Destekli) başlatılıyor...");
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            // YAML'dan Chrome argümanlarını yükle
            List<String> chromeArgs = ConfigHelper.getList("chrome.args");
            for (String arg : chromeArgs) {
                options.addArguments(arg);
            }

            if (ConfigHelper.getBoolean("chrome.headless", false)) {
                options.addArguments("--headless=new");
            }

            // Özel kullanıcı profili (user data dir) tanımlıysa ekle
            String rawUserDataDir = ConfigHelper.getString("chrome.userDataDir", "");
            if (!rawUserDataDir.isEmpty()) {
                options.addArguments("--user-data-dir=" + rawUserDataDir);
            }

            // Tarayıcı tercihleri (prefs)
            Map<String, Object> prefs = ConfigHelper.getMap("chrome.prefs");
            if (!prefs.isEmpty()) {
                options.setExperimentalOption("prefs", prefs);
            }

            // excludeSwitches (enable-automation bayrağını gizleme)
            List<String> excludeSwitches = ConfigHelper.getList("chrome.excludeSwitches");
            if (!excludeSwitches.isEmpty()) {
                options.setExperimentalOption("excludeSwitches", excludeSwitches);
            }

            // Automation extension
            boolean useAutomationExtension = ConfigHelper.getBoolean("chrome.useAutomationExtension", false);
            options.setExperimentalOption("useAutomationExtension", useAutomationExtension);

            driver = new ChromeDriver(options);

            // CDP Stealth script enjeksiyonu: Sayfa açılmadan önce bot izlerini yok et
            boolean stealthEnabled = ConfigHelper.getBoolean("chrome.stealth.enabled", true);
            if (stealthEnabled) {
                String stealthJs = ConfigHelper.getString("chrome.stealth.script", "");
                if (!stealthJs.isEmpty()) {
                    ((ChromeDriver) driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument",
                            Map.of("source", stealthJs));
                }
            }
        }

        // Genel zaman aşımı tanımlamaları
        int pageLoadTimeout = ConfigHelper.getInt("timeouts.pageLoadTimeout", 60);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        threadDriver.set(driver);
        return driver;
    }

    /**
     * Aktif thread'e ait tarayıcı oturumunu güvenle kapatır ve hafızadan temizler.
     */
    public static synchronized void quitDriver() {
        WebDriver driver = threadDriver.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
            threadDriver.remove();
        }
    }
}
