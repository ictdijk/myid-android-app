package in.yagnyam.myid;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;


import java.io.IOException;

import in.yagnyam.myid.nodeApi.NodeApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppConstants {

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();


    public static HttpRequestInitializer httpRequestInitializer() {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
            }
        };
    }

    public static NodeApi getNodeApi() {
        log.debug("getNodeApi");
        NodeApi.Builder transferApi = new NodeApi.Builder(AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY, httpRequestInitializer());
        transferApi.setApplicationName("MijD Validator");
        return transferApi.build();
    }

}
