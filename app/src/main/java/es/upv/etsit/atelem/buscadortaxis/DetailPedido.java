package es.upv.etsit.atelem.buscadortaxis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;
import es.upv.etsit.atelem.buscadortaxis.providers.GoogleApiProv;
import es.upv.etsit.atelem.buscadortaxis.utils.DecodificadorPuntos;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPedido extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap Map;
    private SupportMapFragment MapFragment;

    private double extraOrigenLat;
    private double extraOrigenLng;
    private double extraDestinoLat;
    private double extraDestinoLng;

    private String extraOrigen;
    private String extraDestino;





    private LatLng origenLatLng;
    private LatLng destinoLatLng;

    private GoogleApiProv mGoogleApiProv;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private TextView mtextViewOrigen;
    private TextView mtextViewDestino;
    private TextView mtextViewDistancia;
    private TextView mtextViewTiempo;


    private Button buttonPedir;








    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pedido);
        MiToolbar.show(this, "DATOS", true);

        MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);

        extraOrigenLat = getIntent().getDoubleExtra("origin_lat", 0);
        extraOrigenLng = getIntent().getDoubleExtra("origin_lng", 0);
        extraDestinoLat = getIntent().getDoubleExtra("destination_lat", 0);
        extraDestinoLng = getIntent().getDoubleExtra("destination_lng", 0);

        extraOrigen = getIntent().getStringExtra("origin");
        extraDestino = getIntent().getStringExtra("destination");

        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);
        mGoogleApiProv = new GoogleApiProv(DetailPedido.this);

        mtextViewOrigen = findViewById(R.id.textViewOrigen);
        mtextViewDestino = findViewById(R.id.textViewDestino);
        mtextViewTiempo = findViewById(R.id.textViewTiempo);
        mtextViewDistancia = findViewById(R.id.textViewDistancia);
        buttonPedir = findViewById(R.id.btnPedirAhora);

        mtextViewOrigen.setText(extraOrigen);
        mtextViewDestino.setText(extraDestino);

        buttonPedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irPedirConductor();
            }
        });
    }

    private void irPedirConductor() {
        Intent intent = new Intent(DetailPedido.this, PedirConductor.class);
        intent.putExtra("origin_lat", origenLatLng.latitude);
        intent.putExtra("origin_lng", origenLatLng.longitude);
        intent.putExtra("origin", extraOrigen);
        intent.putExtra("destination", extraDestino);
        intent.putExtra("destination_lat", destinoLatLng.latitude);
        intent.putExtra("destination_lng", destinoLatLng.longitude);
        startActivity(intent);
        finish();
    }

    private void dibujaRuta() {
        mGoogleApiProv.getDirecciones(origenLatLng, destinoLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body() != null) {
                    try {
                        Log.d("GoogleApiProv", "Response body: " + response.body());
                        JSONObject jsonObj = new JSONObject(response.body());
                        String status = jsonObj.getString("status");

                        if ("ZERO_RESULTS".equals(status)) {
                            Log.d("Info", "No se encontraron rutas entre el origen y el destino proporcionados.");
                            return;
                        }

                        JSONArray jsonArr = jsonObj.getJSONArray("routes");
                        if (jsonArr.length() == 0) {
                            Log.d("Error", "No hay rutas disponibles en la respuesta.");
                            return;
                        }

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
                        mtextViewTiempo.setText(durationText);
                        mtextViewDistancia.setText(distanceText);

                    } catch (Exception e) {
                        Log.d("Error", "Error al procesar la respuesta JSON: " + e.getMessage());
                    }
                } else {
                    Log.d("Error", "Respuesta no exitosa: " + response.message());
                    Log.d("Error", "Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Error", "Error en la solicitud: " + t.getMessage());
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;
        Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Map.getUiSettings().setZoomControlsEnabled(true);

        Map.addMarker(new MarkerOptions().position(origenLatLng).title("Origen").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        Map.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        Map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(origenLatLng)
                        .zoom(13f)
                        .build()
        ));

        dibujaRuta();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiProv != null) {
            mGoogleApiProv.shutdown();
        }
    }
}