package app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    private String publishableKey;
    private String webhookSecret;
    private String currency = "EUR";
    private boolean testMode = true;
    
    // getters and setters
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    
    public String getPublishableKey() { return publishableKey; }
    public void setPublishableKey(String publishableKey) { this.publishableKey = publishableKey; }
    
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
}
