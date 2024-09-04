package es.upv.etsit.atelem.buscadortaxis.retrofit;


import android.content.Context;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String SCOPES = "https://www.googleapis.com/auth/firebase.messaging";
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Obtener el token de acceso dinámicamente
        String token = getAccessToken();

        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(newRequest);
    }

    private String getAccessToken() throws IOException {
        InputStream serviceAccountStream = context.getAssets().open("buscadortaxis-firebase-adminsdk-viclr-de9bafa03b.json");

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccountStream)
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
