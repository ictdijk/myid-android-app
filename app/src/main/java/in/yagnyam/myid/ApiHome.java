package in.yagnyam.myid;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

import in.yagnyam.digid.registerApi.RegisterApi;


public class ApiHome {

    private static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    private static final String TAG = "AppConstants";

    private static HttpRequestInitializer httpRequestInitializer(final String userNumber, final String password) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.getHeaders().setBasicAuthentication(userNumber, password);
                request.setConnectTimeout(60000);
                request.setReadTimeout(60000);
            }
        };
    }

    private static HttpRequestInitializer httpRequestInitializer() {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setConnectTimeout(60000);
                request.setReadTimeout(60000);
            }
        };
    }

    public static RegisterApi getRegisterApiHandle(Context context, String digid, String password) {
        RegisterApi.Builder registerApiBuilder = new RegisterApi.Builder(ApiHome.HTTP_TRANSPORT,
                ApiHome.JSON_FACTORY, httpRequestInitializer(digid, password));
        registerApiBuilder.setApplicationName("Android App");
        return registerApiBuilder.build();
    }

}
