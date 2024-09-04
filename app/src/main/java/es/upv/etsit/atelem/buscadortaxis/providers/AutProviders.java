package es.upv.etsit.atelem.buscadortaxis.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AutProviders {
    FirebaseAuth Auth;

    public AutProviders(){
        Auth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> registros(String email, String password){
        return Auth.createUserWithEmailAndPassword(email, password);

    }

    public Task<AuthResult> logins(String email, String password){
        return Auth.signInWithEmailAndPassword(email, password);

    }
    public void logout(){
        Auth.signOut();//cerrar sesion metodo
    }

    public String getId(){
        return Auth.getCurrentUser().getUid();
    }

    public boolean existenciaSesion(){
        boolean existe = false;
        if(Auth.getCurrentUser() != null){
            existe= true;
        }
        return existe;
    }
}
