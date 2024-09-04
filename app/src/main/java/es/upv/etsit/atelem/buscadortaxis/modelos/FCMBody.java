package es.upv.etsit.atelem.buscadortaxis.modelos;

import java.util.Map;

public class FCMBody {
    private Message message;

    public FCMBody(String token, String title, String body, Map<String, String> data) {
        this.message = new Message(token, title, body, data);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message {
        private String token;
        private Notification notification;
        private Android android;
        private Map<String, String> data;

        public Message(String token, String title, String body, Map<String, String> data) {
            this.token = token;
            this.notification = new Notification(title, body);
            this.android = new Android("86400s",data.get("idCliente")); // Asegúrate de que clickAction no se usa incorrectamente
            this.data = data;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Notification getNotification() {
            return notification;
        }

        public void setNotification(Notification notification) {
            this.notification = notification;
        }

        public Android getAndroid() {
            return android;
        }

        public void setAndroid(Android android) {
            this.android = android;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }

    public static class Notification {
        private String title;
        private String body;

        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public static class Android {
        private String ttl;
        private AndroidNotification notification;

        public Android(String ttl, String clickAction) {
            this.ttl = ttl;
            this.notification = new AndroidNotification(clickAction);
        }

        public String getTtl() {
            return ttl;
        }

        public void setTtl(String ttl) {
            this.ttl = ttl;
        }

        public AndroidNotification getNotification() {
            return notification;
        }

        public void setNotification(AndroidNotification notification) {
            this.notification = notification;
        }

        public static class AndroidNotification {
            private String clickAction;

            public AndroidNotification(String clickAction) {
                this.clickAction = clickAction;
            }

            public String getClickAction() {
                return clickAction;
            }

            public void setClickAction(String clickAction) {
                this.clickAction = clickAction;
            }
        }
    }
}
