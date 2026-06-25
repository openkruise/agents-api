package io.openkruise.agents.client.e2b;

import okhttp3.OkHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Proxy configuration for HTTP clients with SSL/TLS support.
 * Customers should construct this class with their own proxy, SSLContext, TrustManager, and HostnameVerifier.
 */
public class ProxyConfig {

    private final Proxy proxy;
    private final SSLContext sslContext;
    private final X509TrustManager trustManager;
    private final HostnameVerifier hostnameVerifier;

    private ProxyConfig(Builder builder) {
        this.proxy = builder.proxy;
        this.sslContext = builder.sslContext;
        this.trustManager = builder.trustManager;
        this.hostnameVerifier = builder.hostnameVerifier;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Applies proxy configuration to an OkHttpClient.Builder.
     */
    public void applyTo(OkHttpClient.Builder builder) {
        if (proxy != null) {
            builder.proxy(proxy);
        }
        
        if (sslContext != null && trustManager != null) {
            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
        }
        
        if (hostnameVerifier != null) {
            builder.hostnameVerifier(hostnameVerifier);
        }
    }

    /**
     * Creates an OkHttpClient with this proxy configuration.
     */
    public OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        applyTo(builder);
        return builder.build();
    }

    public static class Builder {
        private Proxy proxy;
        private SSLContext sslContext;
        private X509TrustManager trustManager;
        private HostnameVerifier hostnameVerifier;

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxy(String host, int port) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            return this;
        }

        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder trustManager(X509TrustManager trustManager) {
            this.trustManager = trustManager;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public ProxyConfig build() {
            return new ProxyConfig(this);
        }
    }
}
