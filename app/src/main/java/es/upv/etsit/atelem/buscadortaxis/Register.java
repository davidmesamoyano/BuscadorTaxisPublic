package es.upv.etsit.atelem.buscadortaxis;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.upv.etsit.atelem.buscadortaxis.includes.MiToolbar;
import es.upv.etsit.atelem.buscadortaxis.modelos.Cliente;
import es.upv.etsit.atelem.buscadortaxis.modelos.User;
import es.upv.etsit.atelem.buscadortaxis.providers.AutProviders;
import es.upv.etsit.atelem.buscadortaxis.providers.ClienteProvider;
import es.upv.etsit.atelem.buscadortaxis.providers.ConductorProvider;

public class Register extends AppCompatActivity {

    SharedPreferences Pref;

    AutProviders AutProviders;
    ClienteProvider ClienteProvider;


    Button ButtonRegister;
    TextInputEditText TextInputName;
    TextInputEditText TextInputEmail;
    TextInputEditText TextInputPassword;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MiToolbar.show(this,"Registro del usuario",true);

        AutProviders = new AutProviders();
        ClienteProvider = new ClienteProvider();




        ButtonRegister = findViewById(R.id.btnRegister);
        TextInputName = findViewById(R.id.textInputName);
        TextInputEmail = findViewById(R.id.textInputEmail);
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
        final String password = TextInputPassword.getText().toString();

        if (!nombre.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if (password.length() >= 6) {
                registra(nombre, email, password);


            } else {
                Toast.makeText(this, "Contraseña tiene que tener 6 caracteres minimo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Rellene el formulario", Toast.LENGTH_SHORT).show();
        }


    }

    void registra(final String nombre, String email, String password){

        AutProviders.registros(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getUid();
                    Cliente cliente = new Cliente(id ,nombre, email);

                    crear(cliente);



                } else {
                    Log.e("Registro", "Error en el registro", task.getException()); // Agrega este Log para registrar cualquier error
                    Toast.makeText(Register.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void crear(Cliente cliente){
       ClienteProvider.create(cliente).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   //Toast.makeText(Register.this,"Registro exitoso", Toast.LENGTH_SHORT).show();
                   Intent intent = new Intent(Register.this, MapaCliente.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando el usuario haga el registro ya no pueda volver a la pantalla de registro y se dirija al mapa
                   startActivity(intent);
               }
               else{

                   Toast.makeText(Register.this, "No se creo el cliente", Toast.LENGTH_SHORT).show();

               }

           }
       });

    }
    /*
    void guardarUsuario(String id, String nombre, String email) {
        String selectedUser = Pref.getString("user", "");
        User user = new User();
        user.setEmail(email);
        user.setNombre(nombre);

        DatabaseReference usuariosRef = Database.child("Users");

        if (selectedUser.equals("conductor")) {
            usuariosRef.child("conductores").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Registro", "Error al guardar en base de datos el usuario", task.getException()); // Agrega este Log para registrar cualquier error
                        Toast.makeText(Register.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (selectedUser.equals("cliente")) {
            usuariosRef.child("clientes").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }*/
}
