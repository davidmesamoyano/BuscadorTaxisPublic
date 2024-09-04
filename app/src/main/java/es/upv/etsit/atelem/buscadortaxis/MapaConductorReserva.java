package es.upv.etsit.atelem.buscadortaxis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.modelos.ClienteReserva;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMBody;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMResponse;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteProvider;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GoogleApiProv;
import es.upv.etsit.atelem.buscadortaxis.providers.NotificationProv;
import es.upv.etsit.atelem.buscadortaxis.providers.TokenProv;
import es.upv.etsit.atelem.buscadortaxis.utils.DecodificadorPuntos;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaConductorReserva extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap Map;
    private SupportMapFragment MapFragment;

    private es.upv.etsit.atelem.buscadortaxis.providers.AutProviders AutProviders;

    private GeofireProv geofireProv;

    private ClienteProvider mClienteProvider;

    private TokenProv mTokenProv;

    private ClienteReservaProv mClienteReservaProv;


    private com.google.android.gms.location.LocationRequest LocationRequest;
    private FusedLocationProviderClient FLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int CONFIGURACION_REQUEST_CODE = 2;


    private Marker marcadorPosicion;


    private LatLng actualLatLng;

    private TextView mTextViewClienteReserva;
    private TextView mTextViewEmailClienteReserva;

    private TextView mTextViewOriginClienteReserva;
    private TextView mTextViewDestinationClienteReserva;

    private String mExtraClienteid;

    private LatLng origenLatLng;
    private LatLng destinoLatLng;

    private GoogleApiProv mGoogleApiProv;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private boolean primeraVez = true;
    private boolean mCercaCliente = false;

    private Button mButtonStartReserva;
    private Button mButtonFinishReserva;

    private NotificationProv mNotificationProv;




    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if(marcadorPosicion!= null){
                        marcadorPosicion.remove();
                    }
                    marcadorPosicion = Map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("posicion actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_coche_60))
                    );
                    //OBTENER LOCALIZ DEL USUARIO TIEMPO REAL
                    Map.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));

                    actualizarLocalizacion();

                    if (primeraVez) {
                        primeraVez = false;
                        getClienteReserva();//info de la solicitud del viaje
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_conductor_reserva);

        AutProviders = new AutProviders();
        geofireProv = new GeofireProv("Conductores_trabajando");

        mTokenProv = new TokenProv();

        mClienteProvider = new ClienteProvider();

        mClienteReservaProv = new ClienteReservaProv();

        FLocation = LocationServices.getFusedLocationProviderClient(this);

        MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);

        mTextViewClienteReserva = findViewById(R.id.textViewClienteReserva);
        mTextViewEmailClienteReserva = findViewById(R.id.textViewEmailClienteReserva);
        mTextViewOriginClienteReserva = findViewById(R.id.textViewOriginClienteReserva);
        mTextViewDestinationClienteReserva = findViewById(R.id.textViewDestinationClienteReserva);
        mButtonStartReserva = findViewById(R.id.btnStartReserva);
        mButtonFinishReserva = findViewById(R.id.btnFinishReserva);

        mExtraClienteid = getIntent().getStringExtra("idCliente");
        Log.d("MapaConductorReserva", "idCliente: " + mExtraClienteid);

        mGoogleApiProv = new GoogleApiProv(MapaConductorReserva.this);

        mNotificationProv = new NotificationProv();

        getCliente();//info del cliente

        //para darle a click setonclclick
        mButtonStartReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCercaCliente){
                    startReserva();
                }
                else{
                    Toast.makeText(MapaConductorReserva.this, "El conductor debe estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFinishReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishReserva();
            }
        });
    }

    private void finishReserva() {
        mClienteReservaProv.actualizarStatus(mExtraClienteid, "finish");
        mClienteReservaProv.actualizarIdHistorialReserva(mExtraClienteid);
        sendNotification("Viaje finalizado");
        if(FLocation != null){
            FLocation.removeLocationUpdates(locationCallback);
        }
        geofireProv.eliminaLocalizacion(AutProviders.getId());

        Intent intent = new Intent(MapaConductorReserva.this, CalificacionCliente.class);
        intent.putExtra("idCliente", mExtraClienteid);
        startActivity(intent);
        finish();
    }

    private void startReserva() {
        mClienteReservaProv.actualizarStatus(mExtraClienteid, "start");
        //cuando se inicie ocultaremos el boton de comenzar viaje y saldra el de finalizar
        mButtonStartReserva.setVisibility(View.GONE);
        mButtonFinishReserva.setVisibility(View.VISIBLE);
        Map.clear(); //eliminar el marcador y la ruta trazada del coche al punto de recogida del cliente
        Map.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_mapa_pin_azul)));
        dibujaRuta(destinoLatLng);//ahora trazar ruta desde cliente al destino
        sendNotification("Viaje iniciado");
    }

    private double getDistanciaEntre(LatLng clienteLatLng, LatLng conductorLatLng){
        double distance = 0;
        Location clienteLocation = new Location("");
        Location conductorLocation = new Location("");
        clienteLocation.setLatitude(clienteLatLng.latitude);
        clienteLocation.setLongitude(clienteLatLng.longitude);
        conductorLocation.setLatitude(conductorLatLng.latitude);
        conductorLocation.setLongitude(conductorLatLng.longitude);

        //quiero obtener la distancia entre la pos del cliente y el conductor
        distance = clienteLocation.distanceTo(conductorLocation);
        return distance;
    }

    private void getClienteReserva() {
        mClienteReservaProv.getClienteReserva(mExtraClienteid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());

                    origenLatLng = new LatLng(originLat,originLng);
                    destinoLatLng = new LatLng(destinationLat,destinationLng);

                    mTextViewOriginClienteReserva.setText("Recogida en: " + origin);
                    mTextViewDestinationClienteReserva.setText("Destino: " + destination);
                    Map.addMarker(new MarkerOptions().position(origenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_mapa_pin_rojo)));
                    dibujaRuta(origenLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dibujaRuta(LatLng latLng){
        //traza ruta entre la pos del conductor y la posicion de recogida del cliente
        mGoogleApiProv.getDirecciones(actualLatLng,latLng).enqueue(new Callback<String>() {
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

    private void getCliente() {
        mClienteProvider.getCliente(mExtraClienteid).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void actualizarLocalizacion(){
        if(AutProviders.existenciaSesion() && actualLatLng != null){
            geofireProv.guardaLocalizacion(AutProviders.getId(),actualLatLng);
            if(!mCercaCliente){
                if(origenLatLng != null && actualLatLng != null){
                    //posicion recogida cliente y actual conductor
                    //distance en metros
                    double distance = getDistanciaEntre(origenLatLng, actualLatLng);
                    if(distance <= 200){
                        mCercaCliente = true;
                        Toast.makeText(this, "Esta cerca de la posicion de recogida del cliente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Map = googleMap;
        Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Map.setMyLocationEnabled(false);//punto para ver la ubicacion en el mapa
        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(1000);
        LocationRequest.setFastestInterval(1000);
        LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(activoGPS()){
                        FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                    }else{
                        AlertDialogGPS();
                    }
                }  else{
                    checkLocationPermissions();
                }
            }else{
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIGURACION_REQUEST_CODE) {
            if (activoGPS()) {
                // Comprobar si se otorgaron los permisos de ubicación
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                } else {
                    // Si no se otorgaron los permisos, solicitarlos nuevamente
                    checkLocationPermissions();
                }
            } else {
                AlertDialogGPS();
            }
        }
    }

    private void AlertDialogGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debes habilitar la Ubicacion para poder continuar")
                .setPositiveButton("Configuracion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),CONFIGURACION_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean activoGPS(){
        boolean gpsActivado= false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){//SI ESTA EL GPS ACTIVADO
            gpsActivado= true;
        }
        return gpsActivado;
    }

    private void desconectarse(){
        if(FLocation!=null){
            FLocation.removeLocationUpdates(locationCallback);
            if(AutProviders.existenciaSesion()){
                geofireProv.eliminaLocalizacion(AutProviders.getId());
            }
        }
        else{
            Toast.makeText(this, "No puede desconectarse", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(activoGPS()){
                    FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                }else{
                    AlertDialogGPS();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if(activoGPS()){
                FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
            }else{
                AlertDialogGPS();
            }
        }
    }

    //Chequeo para ver si los permisos de ubicacion habilitados
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona permisos para continuar")
                        .setMessage("La app requiere permisos de ubicacion para su uso")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapaConductorReserva.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);//HABILITA PERMISOS PARA SABER UBICACION DEL MOVIL
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapaConductorReserva.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {
        mTokenProv.getToken(mExtraClienteid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //contiene la info dentro del nodo tokens del id del usuario
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.child("token").getValue().toString();
                    String title = "ESTADO DE TU VIAJE";
                    String body = "Tu estado del viaje es: " + status;
                    java.util.Map<String, String> map = new HashMap<>();
                    map.put("idCliente", mExtraClienteid);

                    FCMBody fcmBody = new FCMBody(token, title, body, map);
                    mNotificationProv.sendNotification(MapaConductorReserva.this, fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null && response.isSuccessful()) {
                                Log.d("FCM", "Notification sent successfully: " + response.body().getName());
                            } else {
                                Log.e("FCM", "Failed to send notification: " + response.message());
                                Toast.makeText(MapaConductorReserva.this, "No se envio la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());
                        }
                    });

                } else {
                    Toast.makeText(MapaConductorReserva.this, "No se envio la notificacion, conductor no tiene token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
