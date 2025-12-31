package app.config;

import com.stripe.Stripe;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {
    
    private final StripeProperties stripeProperties;
    
    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }
    
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        Stripe.setMaxNetworkRetries(2);
    }
}
