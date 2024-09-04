package es.upv.etsit.atelem.buscadortaxis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SeleccionOpcionAuth extends AppCompatActivity {
    Toolbar Toolbar;
    Button ButtonIrLogin;
    Button ButtonIrRegister;

    SharedPreferences Pref;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_opcion_auth);
        Toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(Toolbar);
        getSupportActionBar().setTitle("Seleccionar opcion");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButtonIrLogin = findViewById(R.id.btnIrLogin);
        ButtonIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrLogin();

            }
        });
        ButtonIrRegister = findViewById(R.id.btnIrRegistro);
        ButtonIrRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrRegister();
            }
        });

        Pref = getApplicationContext().getSharedPreferences("TypeUser", MODE_PRIVATE);

    }
    public void IrLogin(){
        Intent intent = new Intent( SeleccionOpcionAuth.this, Login.class);
        startActivity(intent);
    }

    public void IrRegister(){
        String TipoUsuario = Pref.getString("user", "");
        if(TipoUsuario.equals("cliente")) {

            Intent intent = new Intent(SeleccionOpcionAuth.this, Register.class);//Si es usuario
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(SeleccionOpcionAuth.this, RegisterConductor.class);//Si es conductor va a RegisterConductor
            startActivity(intent);
        }
    }

}