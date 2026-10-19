package com.vrgsl.step.model;

/**
 * Harici JSON dosyalarındaki (src/test/resources/element-infos/*.json)
 * locator tanımlarını Java nesnesine dönüştürmek için kullanılan model sınıfı.
 */
public class ElementInfo {
    private String key;
    private String value;
    private String type;
    private String description;

    public ElementInfo() {}

    public ElementInfo(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public ElementInfo(String key, String value, String type, String description) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ElementInfo{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                (description != null ? ", description='" + description + '\'' : "") +
                '}';
    }
}
