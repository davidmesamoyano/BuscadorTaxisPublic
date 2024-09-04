package es.upv.etsit.atelem.buscadortaxis.providers;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import es.upv.etsit.atelem.buscadortaxis.modelos.Token;


public class TokenProv {

    DatabaseReference mDatabase;

    public TokenProv() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                Token tokenObj = new Token(token);
                mDatabase.child(idUser).setValue(tokenObj);
            }
        });
    }

    public DatabaseReference getToken(String idUser) {
        return mDatabase.child(idUser);
    }
}