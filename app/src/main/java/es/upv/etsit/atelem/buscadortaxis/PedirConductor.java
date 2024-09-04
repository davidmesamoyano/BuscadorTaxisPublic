package es.upv.etsit.atelem.buscadortaxis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.modelos.ClienteReserva;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMBody;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMResponse;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GoogleApiProv;
import es.upv.etsit.atelem.buscadortaxis.providers.NotificationProv;
import es.upv.etsit.atelem.buscadortaxis.providers.TokenProv;
import es.upv.etsit.atelem.buscadortaxis.utils.DecodificadorPuntos;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedirConductor extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewBuscandoConductor;
    private Button buttonCancelarPeiddo;

    private GeofireProv mGeofireProv;

    private String extraOrigin;
    private String extraDestination;

    private GoogleApiProv mGoogleApiProv;


    private double extraOriginLat;
    private double extraOriginLng;
    private double extraDestinationLat;
    private double extraDestinationLng;

    private LatLng OriginLatLng;

    private LatLng DestinationLatLng;
    private double mRadius= 0.1;

    private boolean conductorEncontrado = false;
    private String idConductorEncontrado= "";
    private LatLng conductorEncontradoLatLng;

    private NotificationProv mNotificationProv;
    private TokenProv mTokenProv;

    private ClienteReservaProv mClienteReservaProv;

    private AutProviders mAutProviders;

    private ValueEventListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedir_conductor);

        mAnimation =findViewById(R.id.animation);
        mTextViewBuscandoConductor =findViewById(R.id.textViewBuscandoConductor);
        buttonCancelarPeiddo =findViewById(R.id.btnCancelarPedido);

        mAnimation.playAnimation();

        extraOrigin = getIntent().getStringExtra("origin");
        extraDestination = getIntent().getStringExtra("destination");


        extraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        extraOriginLng = getIntent().getDoubleExtra("origin_lng",0);

        extraDestinationLat = getIntent().getDoubleExtra("destination_lat",0);
        extraDestinationLng = getIntent().getDoubleExtra("destination_lng",0);
        OriginLatLng = new LatLng(extraOriginLat, extraOriginLng);

        DestinationLatLng = new LatLng(extraDestinationLat, extraDestinationLng);

        mGeofireProv = new GeofireProv("Conductores_activos");
        mNotificationProv = new NotificationProv();
        mTokenProv = new TokenProv();
        mClienteReservaProv = new ClienteReservaProv();
        mAutProviders = new AutProviders();
        mGoogleApiProv = new GoogleApiProv(PedirConductor.this);

        obtenerConductoresCercanos();


    }

    private void obtenerConductoresCercanos(){
        mGeofireProv.obtieneConductoresActivos(OriginLatLng,mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //se ejecuta cuando encuentra un conductor disponible
            public void onKeyEntered(String key, GeoLocation location) {
                if(!conductorEncontrado){
                    conductorEncontrado= true;
                    idConductorEncontrado = key; //nodo encontrado en conductores activos ASIGNAMOS ID Y LAT Y LNG
                    conductorEncontradoLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewBuscandoConductor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");

                    createClienteReserva();

                    Log.d("CONDUCTOR", "ID: " + idConductorEncontrado);
                }



            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }



            @Override
            public void onGeoQueryReady() {
                //CUANDO FINALIZA LA BUSQUEDA DE LOS CONDUCTORES EN EL RADIO 0.1 KM y no se ha encontrado
                if(!conductorEncontrado){
                    mRadius = mRadius + 0.1f;

                    //NO ENCONTRO CONDUCTOR

                    if(mRadius > 5){
                        mTextViewBuscandoConductor.setText("NO SE HA ENCONTRADO CONDUCTOR");
                        Toast.makeText(PedirConductor.this, "NO SE HA ENCONTRADO CONDUCTOR", Toast.LENGTH_SHORT).show();

                        return;

                    }
                    else{
                        obtenerConductoresCercanos();
                    }
                }




            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void createClienteReserva(){

        mGoogleApiProv.getDirecciones(OriginLatLng,conductorEncontradoLatLng).enqueue(new Callback<String>() {
            @Override
            //recibimos respuestas del servidor
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObj = new JSONObject(response.body());
                    JSONArray jsonArr =  jsonObj.getJSONArray("routes");
                    JSONObject ruta = jsonArr.getJSONObject(0);
                    JSONObject polylines = ruta.getJSONObject("overview_polyline");
                    String puntos = polylines.getString("points");


                    JSONArray legs = ruta.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");


                    sendNotification(durationText, distanceText);







                }catch (Exception e){
                    Log.d("Error", "Error encontrado" + e.getMessage());
                }

            }

            @Override
            //en caso de fallo
            public void onFailure(Call<String> call, Throwable t) {

            }
        });




    }

    private void sendNotification(final String time, final String km) {
        mTokenProv.getToken(idConductorEncontrado).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.child("token").getValue(String.class);
                    String title = "SOLICITUD DE SERVICIO A " + time + " DE TU UBICACION";
                    String body = "Cliente esta solicitando servicio a una distancia de " + km + "\n" +
                            "Recoger en: " + extraOrigin + "\n" + "Destino: " + extraDestination;
                    Map<String, String> map = new HashMap<>();
                    map.put("idCliente", mAutProviders.getId());

                    FCMBody fcmBody = new FCMBody(token, title, body, map);
                    Log.d("FCM", "Sending notification to token: " + token);
                    mNotificationProv.sendNotification(PedirConductor.this, fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null && response.isSuccessful()) {
                                Log.d("FCM", "Notification sent successfully: " + response.body().getName());
                                ClienteReserva clienteReserva = new ClienteReserva(
                                        mAutProviders.getId(),
                                        idConductorEncontrado,
                                        extraDestination,
                                        extraOrigin,
                                        time,
                                        km,
                                        "create",
                                        extraOriginLat,
                                        extraOriginLng,
                                        extraDestinationLat,
                                        extraDestinationLng
                                );

                                mClienteReservaProv.create(clienteReserva).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Reserva", "Cliente reserva creada exitosamente");
                                        checkStatusClienteReserva();
                                    }
                                });

                            } else {
                                Log.e("FCM", "Failed to send notification: " + response.message());
                                Toast.makeText(PedirConductor.this, "No se envio la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("Error", "Error al enviar la notificación: " + t.getMessage());
                        }
                    });

                } else {
                    Toast.makeText(PedirConductor.this, "No se envio la notificacion, conductor no tiene token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Error al obtener el token: " + error.getMessage());
            }
        });
    }



    private void checkStatusClienteReserva() {
        mListener = mClienteReservaProv.getStatus(mAutProviders.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue().toString();//en esto ya estamos en el campo que queremos obtener
                    if(status.equals("accept")){
                        Intent intent = new Intent(PedirConductor.this, MapaClienteReserva.class);
                        startActivity(intent);
                        finish();//para que la actividad finalice y no se pueda volver a atras
                    }
                    else if(status.equals("cancel")){
                        Toast.makeText(PedirConductor.this, "El conductor no ha aceptado el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PedirConductor.this, MapaCliente.class);
                        startActivity(intent);
                        finish();//para que la actividad finalice y no se pueda volver a atras


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null){
            mClienteReservaProv.getStatus(mAutProviders.getId()).removeEventListener(mListener);

        }

    }

}