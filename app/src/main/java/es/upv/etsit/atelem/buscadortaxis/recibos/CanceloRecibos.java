package es.upv.etsit.atelem.buscadortaxis.recibos;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.upv.etsit.atelem.buscadortaxis.providers.ClienteReservaProv;

public class CanceloRecibos extends BroadcastReceiver {
    private ClienteReservaProv mClienteReservaProv;

    //se ejecuta cuando presionemos cancelar en la notificacion
    @Override
    public void onReceive(Context context, Intent intent) {

        String idCliente = intent.getExtras().getString("idCliente");

        mClienteReservaProv = new ClienteReservaProv();
        mClienteReservaProv.actualizarStatus(idCliente, "cancel");

        //Ahora para cuando acepte desaparezca la notificacion

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);


    }
}
