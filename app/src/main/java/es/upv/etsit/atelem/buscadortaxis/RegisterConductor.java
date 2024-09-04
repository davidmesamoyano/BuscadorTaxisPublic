package es.upv.etsit.atelem.buscadortaxis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;
import es.upv.etsit.atelem.buscadortaxis.modelos.Cliente;
import es.upv.etsit.atelem.buscadortaxis.modelos.Conductor;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteProvider;
import es.upv.etsit.atelem.buscadortaxis.providers.ConductorProvider;

public class RegisterConductor extends AppCompatActivity {

    SharedPreferences Pref;

    AutProviders AutProviders;
    ConductorProvider ConductorProvider;


    Button ButtonRegister;
    TextInputEditText TextInputName;
    TextInputEditText TextInputEmail;
    TextInputEditText TextInputPassword;

    TextInputEditText TextInputMarcaVehiculo;

    TextInputEditText TextInputMatriculaVehiculo;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_conductor);

        MiToolbar.show(this,"Registro del conductor",true);

        AutProviders = new AutProviders();
        ConductorProvider = new ConductorProvider();




        ButtonRegister = findViewById(R.id.btnRegister);
        TextInputName = findViewById(R.id.textInputName);
        TextInputEmail = findViewById(R.id.textInputEmail);
        TextInputMarcaVehiculo = findViewById(R.id.textInputMarcaVehiculo);
        TextInputMatriculaVehiculo = findViewById(R.id.textInputMatriculaVehiculo);
        TextInputPassword = findViewById(R.id.textInputPassword);


        ButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegistra();
            }
        });
    }

    void clickRegistra() {
        final String nombre = TextInputName.getText().toString();
        final String email = TextInputEmail.getText().toString();
        final String marcaVehiculo = TextInputMarcaVehiculo.getText().toString();
        final String matriculaVehiculo = TextInputMatriculaVehiculo.getText().toString();
        final String password = TextInputPassword.getText().toString();

        if (!nombre.isEmpty() && !email.isEmpty() && !password.isEmpty() && !marcaVehiculo.isEmpty() && !matriculaVehiculo.isEmpty()) {
            if (password.length() >= 6) {
                registra(nombre, email, password, marcaVehiculo, matriculaVehiculo);


            } else {
                Toast.makeText(this, "Contraseña tiene que tener 6 caracteres minimo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Rellene el formulario", Toast.LENGTH_SHORT).show();
        }


    }

    void registra(final String nombre, String email, String password, String marcaVehiculo, String matriculaVehiculo){

        AutProviders.registros(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getUid();
                    Conductor conductor = new Conductor(id,nombre, email, marcaVehiculo, matriculaVehiculo);

                    crear(conductor);



                } else {
                    Toast.makeText(RegisterConductor.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void crear(Conductor conductor){
        ConductorProvider.create(conductor).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(RegisterConductor.this,"Registro exitoso", Toast.LENGTH_SHORT).show();
                  Intent intent = new Intent(RegisterConductor.this, MapaConductor.class);
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando el usuario haga el registro ya no pueda volver a la pantalla de registro y se dirija al mapa
                  startActivity(intent);
                }
                else{

                    Toast.makeText(RegisterConductor.this, "No se creo el cliente", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}