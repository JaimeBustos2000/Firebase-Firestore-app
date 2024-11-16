package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.credentials.Credential;
import android.credentials.GetCredentialException;
import android.credentials.GetCredentialRequest;
import android.credentials.GetCredentialResponse;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {

    EditText email;
    EditText pass;
    TextView registrate;
    Toast mensaje;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        mAuth = FirebaseAuth.getInstance();
        // Campos de entrada
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        registrate = findViewById(R.id.registrate);


        // Botones
        Button login = findViewById(R.id.iniciarSesion);
        Button inicio_google = findViewById(R.id.inicio_google);

        // CLIK LISTENER PARA EL INICIO DE SESION
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarDatos()){
                    ingresar();
                }else{
                    mensaje = Toast.makeText(Login.this,"Alguno de los campos esta vacio",Toast.LENGTH_SHORT);
                    mensaje.show();
                }
            }
        });

        // CLICK LISTENER PARA EL REGISTRO DE USUARIOS
        inicio_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        registrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Login.this, Registro.class);
                startActivity(intents);
            }
        });

    }


    private void ingresar(){
        mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuario firebase
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Mensaje de exito
                            Toast.makeText(Login.this,"Ingreso exitoso",
                                    Toast.LENGTH_SHORT).show();

                            //Redireccionar a la nueva pestaña
                            Intent intents = new Intent(Login.this, Post_login.class);
                            startActivity(intents);
                        } else {
                            // Mensaje de error si los campos estan incorrectos
                            Toast.makeText(Login.this, "Correo o contraseña incorrectos.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    private boolean validarDatos(){
        if(email.getText().toString().isEmpty() || pass.getText().toString().isEmpty()){
            return false;
        } else{
            return true;
        }
    }
}