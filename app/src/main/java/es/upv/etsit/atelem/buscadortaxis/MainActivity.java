package es.upv.etsit.atelem.buscadortaxis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button ButtonCliente;
    Button ButtonConductor;

    SharedPreferences Pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        Pref = getApplicationContext().getSharedPreferences("TypeUser", MODE_PRIVATE);

        SharedPreferences.Editor editor = Pref.edit();

        ButtonCliente = findViewById(R.id.btnCliente);
        ButtonConductor = findViewById(R.id.btnConductor);

        ButtonCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","cliente");
                editor.apply();
                irSelecOpcionAuth();
            }
        });


        ButtonConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","conductor");
                editor.apply();
                irSelecOpcionAuth();
            }
        });
    }

    @Override
    protected void onStart() { // para que no haga falta logearse si el usuario existe en sesion
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String user = Pref.getString("user", "");
            if (user.equals("cliente")) {
                Intent intent = new Intent(MainActivity.this, MapaCliente.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, MapaConductor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void irSelecOpcionAuth() {
        Intent intent = new Intent(MainActivity.this, SeleccionOpcionAuth.class);
        startActivity(intent);
    }
}
