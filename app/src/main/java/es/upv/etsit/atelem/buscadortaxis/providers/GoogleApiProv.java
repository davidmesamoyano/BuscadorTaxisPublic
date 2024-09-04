package es.upv.etsit.atelem.buscadortaxis.providers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import es.upv.etsit.atelem.buscadortaxis.R;
import es.upv.etsit.atelem.buscadortaxis.retrofit.InterfaceGoogleAPI;
import es.upv.etsit.atelem.buscadortaxis.retrofit.RetrofitCliente;
import io.grpc.ManagedChannel;
import retrofit2.Call;

public class GoogleApiProv {
    private ManagedChannel channel;

    private Context context;

    public GoogleApiProv(Context context){
        this.context = context;

    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public Call<String> getDirecciones(LatLng origenLatLng, LatLng destinoLatLng){
        String baseurl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + origenLatLng.latitude + "," + origenLatLng.longitude + "&" +
                "destination=" + destinoLatLng.latitude + "," + destinoLatLng.longitude + "&" +
                "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&" +
                "traffic_model=best_guess&" + "key=" + context.getResources().getString(R.string.google_maps_key);


        // Agregar registro de log
        Log.d("GoogleApiProv", "Request URL: " + baseurl + query);
        return RetrofitCliente.getCliente(baseurl).create(InterfaceGoogleAPI.class).getDirecciones(baseurl + query);




    }
}
