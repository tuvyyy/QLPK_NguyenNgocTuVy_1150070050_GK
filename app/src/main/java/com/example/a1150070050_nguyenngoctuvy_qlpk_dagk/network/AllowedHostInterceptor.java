package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.network;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

public class AllowedHostInterceptor implements Interceptor {
    private final String allowedHost;
    private final int allowedPort;        // -1: bỏ qua kiểm tra port
    private final String allowedScheme;   // "http" cho LAN

    public AllowedHostInterceptor(String allowedHost, int allowedPort, String allowedScheme) {
        this.allowedHost = allowedHost;
        this.allowedPort = allowedPort;
        this.allowedScheme = allowedScheme;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        HttpUrl url = req.url();

        boolean schemeOk = url.scheme().equalsIgnoreCase(allowedScheme);
        boolean hostOk   = url.host().equalsIgnoreCase(allowedHost);
        boolean portOk   = (allowedPort == -1) || (url.port() == allowedPort);

        if (!schemeOk || !hostOk || !portOk) {
            throw new IOException("Blocked request to non-whitelisted endpoint: " + url);
        }
        return chain.proceed(req);
    }
}
