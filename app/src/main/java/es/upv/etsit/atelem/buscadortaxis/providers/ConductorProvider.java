package es.upv.etsit.atelem.buscadortaxis.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.upv.etsit.atelem.buscadortaxis.modelos.Cliente;
import es.upv.etsit.atelem.buscadortaxis.modelos.Conductor;

public class ConductorProvider {

    DatabaseReference Database;

    public ConductorProvider(){
        Database = FirebaseDatabase.getInstance().getReference().child("Users").child("conductores");//inicializar

    }

    public Task<Void> create(Conductor conductor){
        return Database.child(conductor.getId()).setValue(conductor);
    }

    public DatabaseReference getConductor(String idConductor){
        return Database.child(idConductor);

    }
}
