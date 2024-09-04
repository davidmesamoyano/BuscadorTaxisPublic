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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;
import es.upv.etsit.atelem.buscadortaxis.providers.TokenProv;

public class MapaConductor extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap Map;
    private SupportMapFragment MapFragment;

    private AutProviders AutProviders;

    private GeofireProv geofireProv;

    private TokenProv mTokenProv;


    private LocationRequest LocationRequest;
    private FusedLocationProviderClient FLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int CONFIGURACION_REQUEST_CODE = 2;

    private Button BotonUnirseTreal;

    private Marker marcadorPosicion;

    private boolean conectadoTreal;

    private LatLng actualLatLng;

    private ValueEventListener mListener;

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
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_conductor);

        MiToolbar.show(this, "Mapa Conductor", false);

        AutProviders = new AutProviders();
        geofireProv = new GeofireProv("Conductores_activos");

        mTokenProv = new TokenProv();

        FLocation = LocationServices.getFusedLocationProviderClient(this);

        MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);

        BotonUnirseTreal = findViewById(R.id.btnUnirseTiempoReal);
        BotonUnirseTreal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conectadoTreal){
                    desconectarse();
                }else{
                    startLocation(); //LO PONEMOS AQUI, CUANDO PRESIONAMOS UNIRSE EN TIEMPO REAL SE UNE
                }
            }
        });
        generarToken();

        conductorTrabajando();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null){
            geofireProv.conductorTrabajando(AutProviders.getId()).removeEventListener(mListener);

        }
    }

    private void conductorTrabajando() {
      mListener =  geofireProv.conductorTrabajando(AutProviders.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //si se creo el nodo conductores trabajando
                if(snapshot.exists()){

                    desconectarse();//dejar escuchar localizacion en tiempo real y elimina la referencia al nodo active drivers

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


        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Map = googleMap;
        Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
            BotonUnirseTreal.setText("Unirse en Tiempo Real");
            conectadoTreal= false;
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(activoGPS()){
                    BotonUnirseTreal.setText("Salir de Tiempo Real");
                    conectadoTreal= true;
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona permisos para continuar")
                        .setMessage("La app requiere permisos de ubicacion para su uso")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapaConductor.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);//HABILITA PERMISOS PARA SABER UBICACION DEL MOVIL

                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapaConductor.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conductor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        desconectarse();//CUANDO EL USUARIO CIERRE SESION, DEBERIAN ELIMINAR LOS DATOS DE SU UBICACION EN FIREBASE
        AutProviders.logout();
        Intent intent = new Intent(MapaConductor.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generarToken(){

        mTokenProv.create(AutProviders.getId());

    }
}

