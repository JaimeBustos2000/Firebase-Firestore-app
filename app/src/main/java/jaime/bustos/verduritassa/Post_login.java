package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Post_login extends AppCompatActivity {

    ImageButton log_out;
    ImageButton add_button;
    ImageButton perfil;

    TextView bienvenida;

    HashMap<String,Object> datos;

    String nombre;
    FirebaseAuth mAuth;
    FirebaseUser current_user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Cultivo> listaCultivos = new ArrayList<>();

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

        // Limpiar la lista para que se renderice la tabla cada vez que se inicie la actividad
        listaCultivos.clear();

        // Mensaje de bienvenida
        bienvenida = findViewById(R.id.Bienvenida);
        // Boton de añadir nuevo cultivo
        add_button = findViewById(R.id.add);
        // Desconectarse
        log_out = findViewById(R.id.desconectar);
        // Referencia al tableLayout
        mitabla = findViewById(R.id.tabla);
        // Boton perfil
        perfil = findViewById(R.id.perfil);

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salir();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Post_login.this, Nuevo_cultivo.class);
                startActivity(intents);
            }
        });

        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Post_login.this,Perfil.class);
                intents.putExtra("datos_usuario",datos);
                startActivity(intents);
            }
        });

        // Mensaje de bienvenida
        obtener_data();

        // Esperar respuesta asincrona de manera sincronica
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Obtener_datos();
            }
        }, 1000);
    }

    // Desconexion desde firebase auth
    public void salir() {
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        if (current_user != null) {
            mAuth.signOut();
            Intent intents = new Intent(Post_login.this, Login.class);
            Toast.makeText(Post_login.this, "Saliendo....", Toast.LENGTH_SHORT).show();
            startActivity(intents);
        } else {
            Toast.makeText(Post_login.this, "Error inesperado al intentar desconectarse", Toast.LENGTH_SHORT).show();
        }
    }

    // Obtener los datos de los cultivos ya almacenados
    public void Obtener_datos() {

        listaCultivos.clear(); // Limpiar la lista antes de agregar los nuevos cultivos

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getUid();

        if (user_id != null) {
            CollectionReference cultivos_ref = db.collection("users")
                    .document(user_id)
                    .collection("cultivos");

            cultivos_ref.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String alias_name = document.getString("Alias");
                                String fecha_cultivo = document.getString("FIngreso");
                                String fecha_cosecha = document.getString("FCosecha");
                                String tipo = document.getString("TCultivo");

                                // Añadir cada cultivo almacenado en formato de clase
                                Cultivo cultivo = new Cultivo(alias_name, fecha_cultivo, fecha_cosecha, tipo);
                                listaCultivos.add(cultivo);
                            }
                            add_rows();  // Actualizar la tabla con los datos obtenidos
                        } else {
                            Log.d("Cultivo", "Error al obtener cultivos", task.getException());
                        }
                    });
        } else {
            Log.d("TAG", "Error: user_id es null");
        }
    }

    // Clase para operar con los cultivos obtenidos en la base de datos
    public static class Cultivo {
        private String alias;
        private String fechaCultivo;
        private String fechaCosecha;
        private String Tipo;

        public Cultivo(String alias, String fechaCultivo, String fechaCosecha, String Tipo) {
            this.alias = alias;
            this.fechaCultivo = fechaCultivo;
            this.fechaCosecha = fechaCosecha;
            this.Tipo = Tipo;
        }

        public String getAlias() {
            return alias;
        }

        public String getFechaCultivo() {
            return fechaCultivo;
        }

        public String getFechaCosecha() {
            return fechaCosecha;
        }

        public String getTipo() {
            return Tipo;
        }
    }

    // Añadir datos de la tabla de manera dinamica
    public void add_rows() {
        tabla_dinamica miTablaDinamica = new tabla_dinamica(Post_login.this, mitabla);
        miTablaDinamica.addRows(listaCultivos);
    }


    public void obtener_data(){
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();

        if (uid != null) {
            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(Task -> {
                        if(Task.isSuccessful()){
                            DocumentSnapshot document = Task.getResult();
                            System.out.println("DATOS:"+document);

                            nombre = document.getString("nombre");
                            String pais = document.getString("pais");
                            String genero = document.getString("genero");

                            //Guardar datos para enviarlos a la actividad del perfil
                            datos = new HashMap<>();
                            datos.put("nombre",nombre);
                            datos.put("pais",pais);
                            datos.put("genero",genero);

                            bienvenida.setText("Bienvenid@ "+nombre);
                        }else{
                            Log.d(TAG,"Error en la tarea");
                        }
                    });
        } else{
            Log.d("ERROR","No se pudo obtener el uid");
        }

    }
}

