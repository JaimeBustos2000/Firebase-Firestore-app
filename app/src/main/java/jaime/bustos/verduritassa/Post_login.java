package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TableLayout;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Post_login extends AppCompatActivity {

    ImageButton log_out;
    Button editar;
    
    FirebaseAuth mAuth;
    FirebaseUser current_user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText nombre_usuario;
    EditText pais_usuario;

    TableLayout mitabla;


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

        obtenerDatosUsuario();
        log_out = findViewById(R.id.desconectar);
        mitabla = findViewById(R.id.tabla);

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salir();
            }
        });
    }

    public void salir() {
        /* Inicializar el autenticador y verificar que el usuario actual es quien esta conectado,
           luego lo desconecta */
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        if (current_user != null) {
            mAuth.signOut();
            Intent intents = new Intent(Post_login.this, Login.class);
            Toast.makeText(Post_login.this, "Saliendo....", Toast.LENGTH_SHORT).show();
            startActivity(intents);

        } else {
            Toast.makeText(Post_login.this, "Error inesperado al intentar desconectarse",
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
        if (nombre_usuario.toString().isEmpty() || pais_usuario.toString().isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public void cambiarDatosUsuario() {
        mAuth = FirebaseAuth.getInstance();
        // OBTENER EL ID DEL USUARIO PARA LA COLECCION
        String id_usuario = Objects.requireNonNull(mAuth.getUid());

        boolean is_valid = validarDatos();

        if (is_valid) {
            Map<String, Object> datos_nuevos = new HashMap<>();
            datos_nuevos.put("nombre", nombre_usuario.toString());
            datos_nuevos.put("pais", pais_usuario.toString());

            db.collection("users").document(String.valueOf(id_usuario))
                    .set(datos_nuevos)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Post_login.this, "Datos editados correctamente", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Post_login.this, "Error al escribir en la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

