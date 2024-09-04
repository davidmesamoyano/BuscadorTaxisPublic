package es.upv.etsit.atelem.buscadortaxis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import es.upv.etsit.atelem.buscadortaxis.modelos.ClienteReserva;
import es.upv.etsit.atelem.buscadortaxis.modelos.HistorialReserva;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;
import es.upv.etsit.atelem.buscadortaxis.providers.HistorialReservaProv;

public class CalificacionConductor extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatingBar;
    private Button mButtonCalificacion;

    private ClienteReservaProv mClienteReservaProv;

    private AutProviders mAutProviders;


    private HistorialReserva mHistorialReserva;
    private HistorialReservaProv mHistorialReservaProv;

    private float mCalificacion = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificacion_conductor);

        mTextViewDestination = findViewById(R.id.textViewDestinationCalificacion);
        mTextViewOrigin = findViewById(R.id.textViewOriginCalificacion);
        mRatingBar = findViewById(R.id.ratingbarCalificacion);
        mButtonCalificacion = findViewById(R.id.btnCalificacion);
        mClienteReservaProv = new ClienteReservaProv();
        mHistorialReservaProv = new HistorialReservaProv();
        mAutProviders = new AutProviders();



        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calificacion, boolean fromUser) {
                mCalificacion = calificacion;

            }
        });


        mButtonCalificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificar();

            }
        });

        getClienteReserva();


    }

    private void getClienteReserva() {
        mClienteReservaProv.getClienteReserva(mAutProviders.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ClienteReserva clienteReserva = snapshot.getValue(ClienteReserva.class);
                    mTextViewOrigin.setText(clienteReserva.getOrigin());
                    mTextViewDestination.setText(clienteReserva.getDestination());
                    //el id del historial lo deberia crear el conductor cuando finaliza el viaje
                    mHistorialReserva = new HistorialReserva(
                            clienteReserva.getIdHistorialReserva(),
                            clienteReserva.getIdCliente(),
                            clienteReserva.getIdConductor(),
                            clienteReserva.getDestination(),
                            clienteReserva.getOrigin(),
                            clienteReserva.getTime(),
                            clienteReserva.getKm(),
                            clienteReserva.getStatus(),
                            clienteReserva.getOriginLat(),
                            clienteReserva.getOriginLng(),
                            clienteReserva.getDestinationLat(),
                            clienteReserva.getDestinationLng()


                    );


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void calificar() {
        if (mCalificacion > 0) {
            mHistorialReserva.setCalificacionConductor(mCalificacion);
            mHistorialReserva.setTimestamp(new Date().getTime());
            mHistorialReservaProv.getHistorialReserva(mHistorialReserva.getIdHistorialReserva()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mHistorialReservaProv.actualizarCalificacionConductor(mHistorialReserva.getIdHistorialReserva(), mCalificacion).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionConductor.this, "calificacion guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionConductor.this, MapaCliente.class);
                                startActivity(intent);
                                finish();

                            }
                        });
                    } else {
                        mHistorialReservaProv.create(mHistorialReserva).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionConductor.this, "calificacion guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionConductor.this, MapaCliente.class);
                                startActivity(intent);
                                finish();

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        } else {
            Toast.makeText(this, "Tienes que ingresar la calificacion", Toast.LENGTH_SHORT).show();

        }
    }
}