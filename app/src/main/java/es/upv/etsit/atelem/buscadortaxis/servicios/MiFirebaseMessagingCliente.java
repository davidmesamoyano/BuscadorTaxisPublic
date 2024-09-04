package es.upv.etsit.atelem.buscadortaxis.servicios;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import es.upv.etsit.atelem.buscadortaxis.R;
import es.upv.etsit.atelem.buscadortaxis.canal.NotificacionHelp;
import es.upv.etsit.atelem.buscadortaxis.recibos.AceptoRecibos;
import es.upv.etsit.atelem.buscadortaxis.recibos.CanceloRecibos;

public class MiFirebaseMessagingCliente extends FirebaseMessagingService {

    private static final String TAG = "MiFirebaseMsgService";
    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "From: " + message.getFrom());

        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
            handleDataMessage(message.getData());
        }

        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            handleNotificationMessage(message.getNotification());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        String idCliente = data.get("idCliente");

        if (title != null && body != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    showNotificationsAPIOreoAcciones(title, body, idCliente);
                } else {
                    showNotificationsAPIOreo(title, body);
                }
            } else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    showNotificationsAcciones(title, body, idCliente);
                } else {
                    showNotifications(title, body);
                }
            }
        }
    }

    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();


        if (title != null && body != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    // Llamar a la versión de Oreo con acciones
                    showNotificationsAPIOreoAcciones(title, body, notification.getClickAction());
                } else {
                    showNotificationsAPIOreo(title, body);
                }
            } else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    // Llamar a la versión antigua con acciones
                    showNotificationsAcciones(title, body, notification.getClickAction());
                } else {
                    showNotifications(title, body);
                }
            }
        }
    }

    private void showNotifications(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificacionHelp notificacionHelp = new NotificacionHelp(getBaseContext());
        NotificationCompat.Builder builder = notificacionHelp.obtenerNotificacionOldAPI(title, body, intent, sound);
        notificacionHelp.obtenerManager().notify(1, builder.build());
    }

    private void showNotificationsAcciones(String title, String body, String idCliente) {
        Intent acceptIntent = new Intent(this, AceptoRecibos.class);
        acceptIntent.putExtra("idCliente", idCliente);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        Intent cancelIntent = new Intent(this, CanceloRecibos.class);
        cancelIntent.putExtra("idCliente", idCliente);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificacionHelp notificacionHelp = new NotificacionHelp(getBaseContext());
        NotificationCompat.Builder builder = notificacionHelp.obtenerNotificacionOldAPIAcciones(title, body, sound, acceptAction, cancelAction);
        notificacionHelp.obtenerManager().notify(2, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationsAPIOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificacionHelp notificacionHelp = new NotificacionHelp(getBaseContext());
        Notification.Builder builder = notificacionHelp.obtenerNotificacion(title, body, intent, sound);
        notificacionHelp.obtenerManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationsAPIOreoAcciones(String title, String body, String idCliente) {
        Intent acceptIntent = new Intent(this, AceptoRecibos.class);
        acceptIntent.putExtra("idCliente", idCliente);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Action acceptAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        Intent cancelIntent = new Intent(this, CanceloRecibos.class);
        cancelIntent.putExtra("idCliente", idCliente);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificacionHelp notificacionHelp = new NotificacionHelp(getBaseContext());
        Notification.Builder builder = notificacionHelp.obtenerNotificacionAcciones(title, body, sound, acceptAction, cancelAction);
        notificacionHelp.obtenerManager().notify(2, builder.build());
    }
}
