package es.upv.etsit.atelem.buscadortaxis.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProv {
    private final DatabaseReference Database;
    private final GeoFire Geof;

    public GeofireProv(String reference){
        Database = FirebaseDatabase.getInstance().getReference().child(reference);
        Geof = new GeoFire(Database);
    }

    //EL SIGUIENTE METODO NOS PERMITIRA GUARDAR LA LOCALIZACION DEL USUARIO EN LA BASE DE DATOS FIREBASE

    public void guardaLocalizacion(String idConductor, LatLng latLng){
        Geof.setLocation(idConductor, new GeoLocation(latLng.latitude, latLng.longitude));

    }

    //EL SIGUIENTE METODO NOS PERMITIRA ELIMINAR LA LOCALIZACION DEL USUARIO EN LA BASE DE DATOS FIREBASE

    public void eliminaLocalizacion(String idConductor){
        Geof.removeLocation(idConductor);
    }



    public GeoQuery obtieneConductoresActivos(LatLng latLng, double radius){

     GeoQuery geoQuery = Geof.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),radius);
     geoQuery.removeAllListeners();
     return geoQuery;
    }

    public DatabaseReference getConductorLocation(String idConductor){
        return Database.child(idConductor).child("l");
    }


    public DatabaseReference conductorTrabajando(String idConductor){
        return FirebaseDatabase.getInstance().getReference().child("Conductores_trabajando").child(idConductor);

    }


}