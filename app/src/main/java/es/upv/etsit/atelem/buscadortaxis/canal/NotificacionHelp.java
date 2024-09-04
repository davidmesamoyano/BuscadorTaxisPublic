package es.upv.etsit.atelem.buscadortaxis.canal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import es.upv.etsit.atelem.buscadortaxis.R;

public class NotificacionHelp extends ContextWrapper {

    private static final String CANAL_ID = "es.upv.etsit.atelem.buscadortaxis";
    private static final String CANAL_NOMBRE = "BuscadorTaxis";

    private NotificationManager manager;


    public NotificacionHelp(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            crearCanales();

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void crearCanales(){
        NotificationChannel notificationChannel = new NotificationChannel(
                CANAL_ID,
                CANAL_NOMBRE,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        obtenerManager().createNotificationChannel(notificationChannel);

    }

    public NotificationManager obtenerManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder obtenerNotificacion(String title, String body, PendingIntent intent, Uri soundUri){
        return new Notification.Builder(getApplicationContext(), CANAL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) //cuando el usuario presione se cierre
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_coche)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder obtenerNotificacionAcciones(String title, String body, Uri soundUri, Notification.Action acceptAction, Notification.Action cancelAction){
        return new Notification.Builder(getApplicationContext(), CANAL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) //cuando el usuario presione se cierre
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_coche)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));

    }


    public NotificationCompat.Builder obtenerNotificacionOldAPI(String title, String body, PendingIntent intent, Uri soundUri){
        return new NotificationCompat.Builder(getApplicationContext(), CANAL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) //cuando el usuario presione se cierre
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_coche)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    }

    public NotificationCompat.Builder obtenerNotificacionOldAPIAcciones(String title, String body,  Uri soundUri, NotificationCompat.Action acceptAction, NotificationCompat.Action cancelAction){
        return new NotificationCompat.Builder(getApplicationContext(), CANAL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) //cuando el usuario presione se cierre
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_coche)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    }
}
