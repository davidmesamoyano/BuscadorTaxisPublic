package es.upv.etsit.atelem.buscadortaxis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;
import es.upv.etsit.atelem.buscadortaxis.providers.ConductorProvider;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GoogleApiProv;
import es.upv.etsit.atelem.buscadortaxis.providers.TokenProv;
import es.upv.etsit.atelem.buscadortaxis.utils.DecodificadorPuntos;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaClienteReserva extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap Map;
    private SupportMapFragment MapFragment;


    private boolean primeraVez = true;


    private es.upv.etsit.atelem.buscadortaxis.providers.AutProviders AutProviders;
    private GeofireProv geofireProv;

    private TokenProv mTokenProv;

    private ClienteReservaProv mClienteReservaProv;
    private ConductorProvider mConductorProvider;






    private Marker marcadorPosicionConductor;
    private PlacesClient mPlaces;



    public String origen;
    private LatLng origenLatLng;

    public String destino;
    private LatLng destinoLatLng;

    private LatLng ConductorLatLng;

    private TextView mTextViewClienteReserva;
    private TextView mTextViewEmailClienteReserva;

    private TextView mTextViewOriginClienteReserva;
    private TextView mTextViewDestinationClienteReserva;
    private TextView mTextViewStatusReserva;


    private GoogleApiProv mGoogleApiProv;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private String mIdConductor;

    private ValueEventListener mListener;
    private ValueEventListener mListenerStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_cliente_reserva);

        AutProviders = new AutProviders();
        geofireProv = new GeofireProv("Conductores_trabajando");
        mTokenProv = new TokenProv();
        mClienteReservaProv = new ClienteReservaProv();
        mGoogleApiProv = new GoogleApiProv(MapaClienteReserva.this);
        mConductorProvider = new ConductorProvider();


        MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);



        // Inicializa la API de Google Places si no está inicializada
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }


        mTextViewClienteReserva = findViewById(R.id.textViewConductorReserva);
        mTextViewEmailClienteReserva = findViewById(R.id.textViewEmailConductorReserva);
        mTextViewOriginClienteReserva = findViewById(R.id.textViewOriginConductorReserva);
        mTextViewDestinationClienteReserva = findViewById(R.id.textViewDestinationConductorReserva);
        mTextViewStatusReserva = findViewById(R.id.textViewStatusReserva);

        getStatus();

        getClienteReserva();



    }

    private void getStatus() {
        mListenerStatus = mClienteReservaProv.getStatus(AutProviders.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue().toString(); //no uso child porque getstatus ya apunta al propio status

                    if(status.equals("accept")){
                        mTextViewStatusReserva.setText("Estado: Aceptado");

                    }
                    if(status.equals("start")){
                        mTextViewStatusReserva.setText("Estado: Viaje Iniciado");

                        startReserva();
                    }
                    else if(status.equals("finish")){
                        mTextViewStatusReserva.setText("Estado: Viaje Finalizado");

                        finishReserva();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishReserva() {
        Intent intent = new Intent(MapaClienteReserva.this, CalificacionConductor.class);
        startActivity(intent);
        finish();
    }

    private void startReserva() {
        Map.clear();
        Map.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_mapa_pin_azul)));
        dibujaRuta(destinoLatLng);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null){
            geofireProv.getConductorLocation(mIdConductor).removeEventListener(mListener);//despues de cerrar pantalla no siga escuchando los cambios

        }
        if(mListenerStatus != null){
            mClienteReservaProv.getStatus(AutProviders.getId()).removeEventListener(mListenerStatus);

        }
    }

    private void getClienteReserva() {
        mClienteReservaProv.getClienteReserva(AutProviders.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    String idConductor = snapshot.child("idConductor").getValue().toString();
                    mIdConductor = idConductor;
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());

                    origenLatLng = new LatLng(originLat,originLng);
                    destinoLatLng = new LatLng(destinationLat,destinationLng);

                    mTextViewOriginClienteReserva.setText("Recogida en: " + origin);
                    mTextViewDestinationClienteReserva.setText("Destino: " + destination);
                    Map.addMarker(new MarkerOptions().position(origenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_mapa_pin_rojo)));
                    getConductor(idConductor);

                    getConductorLocation(idConductor);






                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getConductor(String idConductor) {
        mConductorProvider.getConductor(idConductor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    mTextViewClienteReserva.setText(nombre);
                    mTextViewEmailClienteReserva.setText(email);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getConductorLocation(String idConductor) {

        //se actualiza la pos del conductor en tiempo real gracias al addvalueevent usando tambien fakegps

       mListener = geofireProv.getConductorLocation(idConductor).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());

                    ConductorLatLng = new LatLng(lat,lng);

                    if(marcadorPosicionConductor != null){
                        marcadorPosicionConductor.remove();
                    }

                  marcadorPosicionConductor = Map.addMarker(new MarkerOptions()
                          .position(new LatLng(lat,lng))
                          .title("Tu conductor")
                          .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_coche_60)));

                    if(primeraVez){
                        primeraVez = false;
                        Map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(ConductorLatLng)
                                        .zoom(13f)
                                        .build()
                        ));
                        dibujaRuta(origenLatLng);//con el fin de trazar la ruta una sola vez

                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void dibujaRuta(LatLng latLng){
        //traza ruta entre la pos del conductor y la posicion de recogida del cliente
        mGoogleApiProv.getDirecciones(ConductorLatLng,latLng).enqueue(new Callback<String>() {
            @Override
            //recibimos respuestas del servidor
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObj = new JSONObject(response.body());
                    JSONArray jsonArr =  jsonObj.getJSONArray("routes");
                    JSONObject ruta = jsonArr.getJSONObject(0);
                    JSONObject polylines = ruta.getJSONObject("overview_polyline");
                    String puntos = polylines.getString("points");
                    mPolylineList = DecodificadorPuntos.decodePoly(puntos);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(14f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    Map.addPolyline(mPolylineOptions);

                    JSONArray legs = ruta.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");





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


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Map = googleMap;
        Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Map.setMyLocationEnabled(true);



    }
}