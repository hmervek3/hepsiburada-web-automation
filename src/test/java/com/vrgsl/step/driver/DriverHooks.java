package com.vrgsl.step.driver;

import com.thoughtworks.gauge.*;
import com.vrgsl.step.helper.ConfigHelper;
import com.vrgsl.step.helper.ElementHelper;
import com.vrgsl.step.helper.ValueHelper;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * ==============================================================================
 * DriverHooks (Gauge Test Yaşam Döngüsü Yöneticisi)
 * ==============================================================================
 * Gauge framework'ünün Before/After hook'larını dinleyerek:
 * 1. Test suite başlamadan önce konfigürasyonları ve harici JSON depolarını yükler.
 * 2. Her senaryo öncesinde izole bir tarayıcı oturumu başlatır.
 * 3. Senaryo başarısız olursa otomatik ekran görüntüsü (screenshot) alıp rapora ekler.
 * 4. Senaryo sonunda tarayıcıyı kapatarak kaynak sızıntısını önler.
 */
public class DriverHooks {

    /**
     * Tüm test paketi (Suite) başlamadan önce bir defa çalışır.
     * YAML ve JSON dosyalarını belleğe yükler.
     */
    @BeforeSuite
    public void beforeSuite() {
        ConfigHelper.loadConfigurations();
        ElementHelper.initElementList();
        ValueHelper.initValueList();
    }

    /**
     * Her bir senaryo koşulmadan hemen önce çalışır.
     * WebDriver'ı ayağa kaldırır.
     */
    @BeforeScenario
    public void beforeScenario(ExecutionContext context) {
        DriverManager.createDriver();
        Gauge.writeMessage("🚀 Senaryo Başlatıldı: " + context.getCurrentScenario().getName());
    }

    /**
     * Her senaryo tamamlandığında (başarılı veya başarısız) çalışır.
     * Hata varsa ekran görüntüsü yakalar, ardından oturumu kapatır.
     */
    @AfterScenario
    public void afterScenario(ExecutionContext context) {
        try {
            if (context.getCurrentScenario().getIsFailing()) {
                WebDriver driver = DriverManager.getDriver();
                if (driver instanceof TakesScreenshot takesScreenshot) {
                    byte[] screenshotBytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
                    Gauge.captureScreenshot();
                }
                Gauge.writeMessage("❌ Senaryo Başarısız Oldu: " + context.getCurrentScenario().getName());
            } else {
                Gauge.writeMessage("✅ Senaryo Başarıyla Tamamlandı: " + context.getCurrentScenario().getName());
            }
        } finally {
            DriverManager.quitDriver();
        }
    }
}
