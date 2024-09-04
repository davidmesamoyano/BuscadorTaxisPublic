package es.upv.etsit.atelem.buscadortaxis.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.modelos.Cliente;

public class ClienteProvider {
    DatabaseReference Database;

    public ClienteProvider(){
        Database = FirebaseDatabase.getInstance().getReference().child("Users").child("clientes");//inicializar

    }

    public Task<Void> create(Cliente cliente){
        Map<String,Object> map = new HashMap<>();
        map.put("nombre", cliente.getNombre());
        map.put("email", cliente.getEmail());

        return Database.child(cliente.getId()).setValue(map);
    }

    public DatabaseReference getCliente(String idCliente){

        return Database.child(idCliente);
    }
}
