package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class Registro extends AppCompatActivity {

    String user_id;

    Button registrarbtn;
    Button pagina_anterior;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText nombre;
    EditText pais;
    EditText email;
    EditText passw;
    Spinner genero;

    Toast mensaje;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // CAMPOS DE TEXTO
        pais = findViewById(R.id.pais_add);
        nombre = findViewById(R.id.nombre_add);
        email = findViewById(R.id.email_add);
        passw = findViewById(R.id.contrasena);
        genero = findViewById(R.id.generoadd);

        mAuth = FirebaseAuth.getInstance();

        // BOTONES
        pagina_anterior = findViewById(R.id.volver);
        registrarbtn = findViewById(R.id.btn_registro);

        // BOTON REGISTRO
        registrarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarDatos()){
                    Intent intents = new Intent(Registro.this,Login.class);
                    registrar();
                    new Handler().postDelayed(() -> {
                        Log.w("Esperar","Esperando x segundos");
                        mensaje = Toast.makeText(Registro.this,"Usuario creado, inicie sesion con sus credenciales",Toast.LENGTH_SHORT);
                        mensaje.show();
                        startActivity(intents);
                            },3000);

                }else{
                    mensaje = Toast.makeText(Registro.this,"Compruebe que los campos no esten vacios",Toast.LENGTH_SHORT);
                    mensaje.show();
                }

            }
        });

        // BOTON VOLVER
        pagina_anterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Registro.this,Login.class);
                startActivity(intents);
            }
        });

    }

    // FUNCION PARA REGISTRAR USUARIOS
    private void registrar(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), passw.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            new Handler().postDelayed(() -> {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    almacenarDatosFirestore(user);
                                    Log.d("Firebase", "User after delay: " + user.getEmail());
                                } else {
                                    Log.d("Firebase", "User still null after delay");
                                }
                            }, 3000);

                        } else {
                            // Obtener el error especifico al intentar crear usuario
                            String error = (task.getException() != null) ? task.getException().toString() : "Error desconocido";
                            Toast.makeText(Registro.this, "Hubo un error al crear el usuario: "+ error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // FUNCION PARA ALMACENAR DATOS DEL USUARIO
    private void almacenarDatosFirestore(FirebaseUser user) {
        if (user == null) {
            Log.d(TAG, "Usuario no autenticado");
            return;
        }

        user_id = user.getUid();
        Log.d(TAG, "USER ID: " + user_id);

        String nombreStr = nombre.getText().toString();
        String paisStr = pais.getText().toString();
        String generoStr = genero.getSelectedItem().toString();

        if (nombreStr.isEmpty() || paisStr.isEmpty() || generoStr.isEmpty()) {
            Log.d(TAG, "Uno o más campos están vacíos.");
            return;
        }

        Map<String, Object> datos_usuario = new HashMap<>();
        datos_usuario.put("nombre", nombreStr);
        datos_usuario.put("pais", paisStr);
        datos_usuario.put("genero", generoStr);

        db.collection("users").document(user_id)
                .set(datos_usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Documento añadido");
                        System.out.println("DOCUMENTO AÑADIDO");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "No se pudo añadir, error: " + e);
                        System.out.println("No se pudo añadir el documento error: " + e);
                    }
                });
    }


    // Verificar que los datos ingresados no esten vacios
    public boolean validarDatos(){
        if(email.getText().toString().isEmpty() ||
                passw.getText().toString().isEmpty() ||
                nombre.getText().toString().isEmpty() ||
                pais.getText().toString().isEmpty()
            ){
            return false;
        } else{
            return true;
        }
    }


}