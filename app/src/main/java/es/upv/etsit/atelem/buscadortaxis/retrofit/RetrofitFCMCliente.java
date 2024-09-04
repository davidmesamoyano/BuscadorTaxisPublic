package es.upv.etsit.atelem.buscadortaxis.retrofit;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFCMCliente {

    private static OkHttpClient getHttpClient(Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context)) // Añadir el interceptor
                .build();
    }

    public static Retrofit getCliente(Context context, String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(getHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
