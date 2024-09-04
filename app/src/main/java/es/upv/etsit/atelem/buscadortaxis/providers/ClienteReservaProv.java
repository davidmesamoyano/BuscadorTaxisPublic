package es.upv.etsit.atelem.buscadortaxis.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.modelos.ClienteReserva;

public class ClienteReservaProv {

    private DatabaseReference mDatabase;

    public ClienteReservaProv() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClienteReserva");


    }

    public Task<Void> create(ClienteReserva clienteReserva){
        return mDatabase.child(clienteReserva.getIdCliente()).setValue(clienteReserva);
    }

    public Task<Void> actualizarStatus(String idClienteReserva, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);


        return mDatabase.child(idClienteReserva).updateChildren(map);

    }

    public Task<Void> actualizarIdHistorialReserva(String idClienteReserva){
        String idPush = mDatabase.push().getKey();//genera un identificador unico en la base de datos
        Map<String, Object> map = new HashMap<>();
        map.put("idHistorialReserva", idPush);


        return mDatabase.child(idClienteReserva).updateChildren(map);

    }

    public DatabaseReference getStatus(String idClienteReserva){
        return mDatabase.child(idClienteReserva).child("status");
    }

    public DatabaseReference getClienteReserva(String idClienteReserva){
        return mDatabase.child(idClienteReserva);
    }
}
