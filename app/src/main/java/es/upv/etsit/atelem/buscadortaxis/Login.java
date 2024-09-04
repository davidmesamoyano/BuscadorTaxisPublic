package es.upv.etsit.atelem.buscadortaxis;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;

public class Login extends AppCompatActivity {

    TextInputEditText TextInputEmail;
    TextInputEditText TextInputPassword;
    Button ButtonLogin;

    FirebaseAuth Auth;
    DatabaseReference Database;

    SharedPreferences Pref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MiToolbar.show(this,"Login del usuario",true);

        TextInputEmail = findViewById(R.id.textInputEmail);
        TextInputPassword = findViewById(R.id.textInputPassword);
        ButtonLogin = findViewById(R.id.btnLogin);

        Auth = FirebaseAuth.getInstance();
        Database = FirebaseDatabase.getInstance().getReference();
        Pref = getApplicationContext().getSharedPreferences("TypeUser", MODE_PRIVATE);

        ButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });

    }
    private void login() {
        String email = TextInputEmail.getText().toString();
        String password = TextInputPassword.getText().toString();

        if(!email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                Auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String user = Pref.getString("user", "");
                            if(user.equals("cliente")){
                                Intent intent = new Intent(Login.this, MapaCliente.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando el usuario haga el registro ya no pueda volver a la pantalla de registro y se dirija al mapa
                                startActivity(intent);

                            }
                            else{
                                Intent intent = new Intent(Login.this, MapaConductor.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando el usuario haga el login ya no pueda volver a la pantalla de login y se dirija al mapa si le da a volver a atras no deja actividad existente
                                startActivity(intent);


                            }


                        }else{
                            Toast.makeText(Login.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            }else{ Toast.makeText(Login.this, "La contraseña ha de ser de mas de 6 caracteres", Toast.LENGTH_SHORT).show();

            }
        }
    }
}