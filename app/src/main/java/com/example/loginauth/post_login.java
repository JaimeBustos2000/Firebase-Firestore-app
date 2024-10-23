package com.example.loginauth;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class post_login extends AppCompatActivity {

    Button log_out;
    Button editar;
    
    FirebaseAuth mAuth;
    FirebaseUser current_user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText nombre_usuario;
    EditText pais_usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.post_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombre_usuario = findViewById(R.id.nombre_actual);
        pais_usuario = findViewById(R.id.pais_actual);

        obtenerDatosUsuario();
        log_out = findViewById(R.id.desconectar);
        editar = findViewById(R.id.editar_datos);
        
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salir();
            }
        });

    }

        editar.setOnClickListener(new View.OnClickListener(){
            @override
            public void onClick(View view){
                cambiarDatosUsuario();
            }
        });

    
    public void salir() {
        /* Inicializar el autenticador y verificar que el usuario actual es quien esta conectado,
           luego lo desconecta */
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        if (current_user != null) {
            mAuth.signOut();
            Intent intents = new Intent(post_login.this, Login.class);
            Toast.makeText(post_login.this, "Saliendo....", Toast.LENGTH_SHORT).show();
            startActivity(intents);

        } else {
            Toast.makeText(post_login.this, "Error inesperado al intentar desconectarse",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void obtenerDatosUsuario(){
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        // Verificar el estado del usuario y retornar su id
        String id_user = current_user != null ? current_user.getUid() : null;

        // Buscar los datos del documento en base a la id, en este caso id del usuario
        if(current_user!=null){
            db.collection("users").document(id_user)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                // Obtiene el documento
                                DocumentSnapshot document = task.getResult();
                                // Verifica si el documento existe
                                if (document != null && document.exists() && document.getData()!=null) {
                                    // Transforma los datos obtenidos a un map
                                    Map<String,Object> datos = new HashMap<>(document.getData());

                                    // Obtiene los datos especificos segun las claves que se guardaron y se transforma a str
                                    String name = datos.get("nombre").toString();
                                    String pais = datos.get("pais").toString();

                                    // Usa los editText para mostrar los datos actuales en la bsd
                                    pais_usuario.setText(pais);
                                    nombre_usuario.setText(name);

                                } else {
                                    // Mensaje de error si no encuentra nada
                                    Log.d(TAG, "No existe el documento!");
                                }
                            } else {
                                // Error si no se pudo obtener el documento especifico con la id
                                Log.w(TAG, "Error al obtener el documento.", task.getException()); // Manejo de errores
                            }
                        }
                    });
        };

    }


    public boolean validarDatos(){
        if (nombre_usuario.to_String().isEmpty() || pais_usuario.to_String().isEmpty()){
            return false;
        }else{
            return True;
        }
    }

    public void cambiarDatosUsuario(){
        mAuth = FirebaseAuth.getInstance();
        // OBTENER EL ID DEL USUARIO PARA LA COLECCION
        int id_usuario = mAuth.getUid().to_String();

        boolean is_valid = validarDatos();

        if(is_valid){
            id_usuario.put("nombre", nombre_usuario.to_String());
            id_usuario.put("pais", pais_usuario.to_String());
            Map<String, object> datos_nuevos = new HashMap<>();
            
            db.collection("users").document(id_usuario)
            .set(datos_nuevos)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(post_login.this,"Datos editados correctamente",Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(post_login.this,"Error al escribir en la base de datos",Toast.LENGTH_SHORT).show();
                }
            }
        });
        }
    }
}
