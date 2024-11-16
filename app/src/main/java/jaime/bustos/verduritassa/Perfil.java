package jaime.bustos.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.HashMap;

public class Perfil extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser current_user;
    EditText nombre_usuario;
    EditText genero;
    EditText pais;

    ImageButton desconectar;
    ImageButton volver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombre_usuario = findViewById(R.id.nombre_usuario);
        genero = findViewById(R.id.genero_usuario);
        pais = findViewById(R.id.pais_usuario);
        desconectar = findViewById(R.id.desconectar_perfil);
        volver = findViewById(R.id.arrow_perfil);

        // Obtener los intents de la actividad que inicio el cambio y poner la informacion

        Intent intents = getIntent();
        if (intents!=null){
            Serializable serializableExtra = intents.getSerializableExtra("datos_usuario");
            if (serializableExtra instanceof HashMap) {
                HashMap<String, Object> hashMap = (HashMap<String, Object>) serializableExtra;

                String nombre_guardado = (String) hashMap.get("nombre");
                nombre_usuario.setText(nombre_guardado);

                String genero_guardado = (String) hashMap.get("genero");
                genero.setText(genero_guardado);

                String pais_guardado = (String) hashMap.get("pais");
                pais.setText(pais_guardado);
        }
    }

        desconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salir();
            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Perfil.this,Post_login.class);
                startActivity(intents);
            }
        });
    }

    public void salir() {
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        if (current_user != null) {
            mAuth.signOut();
            Intent intents = new Intent(Perfil.this, Login.class);
            Toast.makeText(Perfil.this, "Saliendo....", Toast.LENGTH_SHORT).show();
            startActivity(intents);
        } else {
            Toast.makeText(Perfil.this, "Error inesperado al intentar desconectarse", Toast.LENGTH_SHORT).show();
        }
    }

}