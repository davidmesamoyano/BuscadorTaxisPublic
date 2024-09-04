package es.upv.etsit.atelem.buscadortaxis.providers;

import android.content.Context;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMBody;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMResponse;
import es.upv.etsit.atelem.buscadortaxis.retrofit.InterfaceFirebaseCloudMessageApi;
import es.upv.etsit.atelem.buscadortaxis.retrofit.RetrofitFCMCliente;
import retrofit2.Call;

public class NotificationProv {
    private String url = "https://fcm.googleapis.com/";

    public NotificationProv() {
    }

    public Call<FCMResponse> sendNotification(Context context, FCMBody body) {
        return RetrofitFCMCliente.getCliente(context, url).create(InterfaceFirebaseCloudMessageApi.class).send(body);
    }
}
