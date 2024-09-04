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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;
import es.upv.etsit.atelem.buscadortaxis.providers.TokenProv;

public class MapaCliente extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap Map;
    private SupportMapFragment MapFragment;

    private List<Marker> marcadorConductores = new ArrayList<>();

    private boolean primeraVez = true;


    private AutProviders AutProviders;
    private GeofireProv geofireProv;

    private TokenProv mTokenProv;


    private com.google.android.gms.location.LocationRequest LocationRequest;
    private FusedLocationProviderClient FLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int CONFIGURACION_REQUEST_CODE = 2;
    private LatLng actualLatLng;
    private Marker marcadorPosicion;
    private PlacesClient mPlaces;

    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestino;

    public String origen;
    private LatLng origenLatLng;

    public String destino;
    private LatLng destinoLatLng;

    private GoogleMap.OnCameraIdleListener mCameraListener;

    private Button ButtonPedirConductor;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                  /*
                    if (marcadorPosicion != null) {
                        marcadorPosicion.remove();
                    }

                   */
                    actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    /*
                 marcadorPosicion = Map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("posicion actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_usuario_24))
                    );*/
                    //OBTENER LOCALIZ DEL USUARIO TIEMPO REAL
                    Map.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
                    if (primeraVez) {
                        primeraVez = false;
                        obtieneConductoresActivos();
                        limiteBusqueda();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_cliente);

        MiToolbar.show(this, "Mapa Cliente", false);

        AutProviders = new AutProviders();
        geofireProv = new GeofireProv("Conductores_activos");
        mTokenProv = new TokenProv();
        FLocation = LocationServices.getFusedLocationProviderClient(this);

        MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);//cargo el mapa asicrono
        ButtonPedirConductor = findViewById(R.id.btnPedirConductor);


        // Inicializa la API de Google Places si no está inicializada
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        mPlaces = Places.createClient(this);

        instanceAutoCompleteOrigin();
        instanceAutoCompleteDestino();
        onCameraMove();

        ButtonPedirConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirConductor();
            }
        });

        generarToken();


    }

    private void pedirConductor() {
        if (origenLatLng != null && destinoLatLng != null) {
            Intent intent = new Intent(MapaCliente.this, DetailPedido.class);
            intent.putExtra("origin_lat", origenLatLng.latitude);
            intent.putExtra("origin_lng", origenLatLng.longitude);
            intent.putExtra("destination_lat", destinoLatLng.latitude);
            intent.putExtra("destination_lng", destinoLatLng.longitude);
            intent.putExtra("origin", origen);
            intent.putExtra("destination", destino);


            startActivity(intent);


        } else {
            Toast.makeText(this, "Tiene que selccionar lugar de origen y destino", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    //Hay que ejecutar el siguente metodo cuando el usuario tenga establecida su ubicacion, en el metodo locationcallback

    private void limiteBusqueda() {
        LatLng norteSide = SphericalUtil.computeOffset(actualLatLng, 5000, 0);
        LatLng surSide = SphericalUtil.computeOffset(actualLatLng, 5000, 180);
        mAutocomplete.setCountries("ESP");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(surSide, norteSide));
        mAutocompleteDestino.setCountries("ESP");
        mAutocompleteDestino.setLocationBias(RectangularBounds.newInstance(surSide, norteSide));

    }

    private void onCameraMove() {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapaCliente.this);
                    origenLatLng = Map.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(origenLatLng.latitude, origenLatLng.longitude, 1);
                    String ciudad = addressList.get(0).getLocality();
                    String pais = addressList.get(0).getCountryName();
                    String direccion = addressList.get(0).getAddressLine(0);
                    origen = direccion + " " + ciudad;
                    mAutocomplete.setText(direccion + " " + ciudad);

                } catch (Exception e) {
                    Log.d("Error:", "Mensaje error" + e.getMessage());
                }
            }
        };
    }

    private void instanceAutoCompleteOrigin() {
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocomplete.setHint("Origen");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                origen = place.getName();
                origenLatLng = place.getLatLng();

                Log.d("PLACE", "Name" + origen);
                Log.d("PLACE", "Lat" + origenLatLng.latitude);
                Log.d("PLACE", "Lng" + origenLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("PLACE", "An error occurred: " + status);
            }
        });


    }

    private void instanceAutoCompleteDestino() {
        mAutocompleteDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestino);
        mAutocompleteDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocompleteDestino.setHint("Destino");
        mAutocompleteDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destino = place.getName();
                destinoLatLng = place.getLatLng();

                Log.d("PLACE", "Name" + destino);
                Log.d("PLACE", "Lat" + destinoLatLng.latitude);
                Log.d("PLACE", "Lng" + destinoLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("PLACE", "An error occurred: " + status);
            }
        });


    }

    private void obtieneConductoresActivos() {
        geofireProv.obtieneConductoresActivos(actualLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //En este Metodo es donde se iran marcando los conductores que se van conectando
            public void onKeyEntered(String key, GeoLocation location) {

                for (Marker marcador : marcadorConductores) {
                    if (marcador.getTag() != null) {
                        if (marcador.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng conductorLatLng = new LatLng(location.latitude, location.longitude);//posicion del nuevo conductor
                Marker marcador = Map.addMarker(new MarkerOptions().position(conductorLatLng).title("Conductor Libre").icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_coche_60)));
                marcador.setTag(key); //key vendria a ser el id del conductor
                marcadorConductores.add(marcador); //añadimos a la lista de marcadores
            }

            @Override
            //En este metodo se elimiran los marcadores de los conductores que se desconectan
            public void onKeyExited(String key) {
                for (Marker marcador : marcadorConductores) {
                    if (marcador.getTag() != null) {
                        if (marcador.getTag().equals(key)) {
                            marcador.remove();//eliminamos el marcador
                            marcadorConductores.remove(marcador);//tambien de la lista
                            return;
                        }
                    }
                }

            }

            @Override
            //Se actualiza en tiempo real la posicion del conductor
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marcador : marcadorConductores) {
                    if (marcador.getTag() != null) {
                        if (marcador.getTag().equals(key)) {
                            marcador.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }
//se llama automaticamente cuando el mapa haya cargado

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
        Map.setMyLocationEnabled(true);
        Map.setOnCameraIdleListener(mCameraListener);

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
                    if (activoGPS()) {
                        FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                        Map.setMyLocationEnabled(true);//  Para que establezca el punto de nuestra posicion exacta

                    } else {
                        AlertDialogGPS();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIGURACION_REQUEST_CODE && activoGPS()) {
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
            FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
            Map.setMyLocationEnabled(true);//  Para que establezca el punto de nuestra posicion exacta

        }
        else if (requestCode == CONFIGURACION_REQUEST_CODE && !activoGPS()){
            AlertDialogGPS();
        }

    }




    private void AlertDialogGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debes habilitar la Ubicacion para poder seguir")
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

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(activoGPS()){
                    FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                    Map.setMyLocationEnabled(true);//  Para que establezca el punto de nuestra posicion exacta

                }else{
                    AlertDialogGPS();
                }


            } else {
                checkLocationPermissions();
            }
        } else {
            if(activoGPS()){
                FLocation.requestLocationUpdates(LocationRequest, locationCallback, Looper.myLooper());
                Map.setMyLocationEnabled(true);//  Para que establezca el punto de nuestra posicion exacta

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
                                ActivityCompat.requestPermissions(MapaCliente.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);//HABILITA PERMISOS PARA SABER UBICACION DEL MOVIL

                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapaCliente.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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
        AutProviders.logout();
        Intent intent = new Intent(MapaCliente.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generarToken(){

        mTokenProv.create(AutProviders.getId());

    }
}