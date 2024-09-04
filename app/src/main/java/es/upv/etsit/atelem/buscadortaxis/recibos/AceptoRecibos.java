package es.upv.etsit.atelem.buscadortaxis.recibos;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.upv.etsit.atelem.buscadortaxis.MapaConductorReserva;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;
import es.upv.etsit.atelem.buscadortaxis.providers.GeofireProv;

public class AceptoRecibos extends BroadcastReceiver {
    private ClienteReservaProv mClienteReservaProv;
    private GeofireProv geofireProv;
    private AutProviders mAutProviders;

    //se ejecuta cuando presionemos aceptar en la notificacion
    @Override
    public void onReceive(Context context, Intent intent) {
        mAutProviders = new AutProviders();
        geofireProv = new GeofireProv("Conductores_activos");
        geofireProv.eliminaLocalizacion(mAutProviders.getId());
        String idCliente = intent.getExtras().getString("idCliente");
        mClienteReservaProv = new ClienteReservaProv();
        mClienteReservaProv.actualizarStatus(idCliente,"accept");

        //Ahora para cuando acepte desaparezca la notificacion
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapaConductorReserva.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//CUANDO EL CONDUCTOR ACEPTE NO PUEDA VOLVER PANTALLA
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idCliente", idCliente);
        context.startActivity(intent1);





    }
}
