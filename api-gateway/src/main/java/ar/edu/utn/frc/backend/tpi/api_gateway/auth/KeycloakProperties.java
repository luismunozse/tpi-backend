package ar.edu.utn.frc.backend.tpi.api_gateway.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /**
     * Base URL of the Keycloak server, e.g. http://keycloak:8080
     */
    private String serverUrl = "http://keycloak:8080";

    /**
     * Realm where the application client is registered.
     */
    private String realm = "tpi-realm";

    /**
     * Client ID configured in Keycloak for this gateway.
     */
    private String clientId = "tpi-backend-client";

    /**
     * Optional client secret if the client type is confidential.
     */
    private String clientSecret = "";

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenUrl() {
        String base = StringUtils.trimTrailingCharacter(getServerUrl(), '/');
        return base + "/realms/" + getRealm() + "/protocol/openid-connect/token";
    }
}
