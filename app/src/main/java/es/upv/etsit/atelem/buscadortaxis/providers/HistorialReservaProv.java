package es.upv.etsit.atelem.buscadortaxis.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.modelos.ClienteReserva;
import es.upv.etsit.atelem.buscadortaxis.modelos.HistorialReserva;

public class HistorialReservaProv {

    private DatabaseReference mDatabase;

    public HistorialReservaProv() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistorialReserva");


    }

    public Task<Void> create(HistorialReserva historialReserva){
        return mDatabase.child(historialReserva.getIdHistorialReserva()).setValue(historialReserva);
    }


    public Task<Void> actualizarCalificacionCliente(String idHistorialReserva, float calificacionCliente){
        Map<String, Object> map = new HashMap<>();
        map.put("calificacionCliente", calificacionCliente);
        return mDatabase.child(idHistorialReserva).updateChildren(map);

    }

    public Task<Void> actualizarCalificacionConductor(String idHistorialReserva, float calificacionConductor){
        Map<String, Object> map = new HashMap<>();
        map.put("calificacionConductor", calificacionConductor);
        return mDatabase.child(idHistorialReserva).updateChildren(map);

    }

    public DatabaseReference getHistorialReserva(String idHistorialReserva){
        return  mDatabase.child(idHistorialReserva);
    }

}
