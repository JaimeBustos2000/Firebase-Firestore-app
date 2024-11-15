package jaime.bustos.verduritassa;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Editar_cultivo extends AppCompatActivity {

    EditText Fcultivo;
    EditText Fecha_cultivo_nueva;
    Button guardar;

    EditText alias_antiguo;
    EditText alias;

    String nuevaFecha;

    String alias_db;
    String fechaCultivo_db;
    String fechaCosecha_db;
    String tipo_db;


    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.editar_cultivo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciar_intents();

        Fecha_cultivo_nueva = findViewById(R.id.fecha_cultivo_actual);
        alias = findViewById(R.id.alias_cultivo_actual);

        Fcultivo.setOnClickListener(View -> ShowPickerDialog());


        ImageButton arrow = findViewById(R.id.arrow2);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Editar_cultivo.this,Post_login.class);
                startActivity(intents);
            }
        });

        guardar = findViewById(R.id.guardar);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Editar_cultivo.this,Post_login.class);

                List<Integer> fecha_dividida = dividir_fecha(Fecha_cultivo_nueva.getText().toString());

                calcularFecha(fecha_dividida,tipo_db);

                Map<String, Object> cultivo_actualizado = new HashMap<>();

                cultivo_actualizado.put("Alias",alias.getText().toString());
                cultivo_actualizado.put("FCosecha",nuevaFecha);
                cultivo_actualizado.put("FIngreso",Fecha_cultivo_nueva.getText().toString());
                cultivo_actualizado.put("TCultivo",tipo_db);

                Log.d("HASHMAP","Cultivo actualizado: " + cultivo_actualizado);

                obtener_uid_y_actualizar(alias_db,cultivo_actualizado);

                intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Toast.makeText(Editar_cultivo.this,"Cambios guardados",Toast.LENGTH_SHORT).show();
                startActivity(intents);
            }
        });

    }

    public void ShowPickerDialog(){
        // Obtener la fecha actual del EditText
        String fechaActual = Fcultivo.getText().toString();
        int year, month, day;

        if (!fechaActual.isEmpty()) {
            // Dividir la fecha en día, mes y año
            String[] partes = fechaActual.split("/");
            day = Integer.parseInt(partes[0]);
            month = Integer.parseInt(partes[1]) - 1; // Meses en DatePickerDialog son de 0-11
            year = Integer.parseInt(partes[2]);
        } else {
            // Usar la fecha actual si no hay fecha en el EditText
            final Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datepicker = new DatePickerDialog(
                this,
                (DatePicker view, int selected_year, int select_month, int select_day) -> {
                    String fecha_default = select_day + "/" + (select_month + 1) + "/" + selected_year;
                    Fcultivo.setText(fecha_default);
                },
                year, month, day);
        datepicker.show();
    }

    @SuppressLint("CutPasteId")
    public void iniciar_intents(){

        Fcultivo = findViewById(R.id.fecha_cultivo_actual);
        // Obtener el Intent que lanzó la actividad
        Intent intent = getIntent();

        // Recuperar el HashMap pasado a través del Intent
        Object extra = intent.getSerializableExtra("CULTIVOS");

        if (extra != null && extra instanceof HashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> grupoCultivos = (HashMap<String, Object>) extra;

            // Acceder a los datos dentro del HashMap
            alias_db = (String) grupoCultivos.get("Alias");
            fechaCultivo_db = (String) grupoCultivos.get("Fecha_cultivo");
            fechaCosecha_db = (String) grupoCultivos.get("Fecha_cosecha");
            tipo_db = (String) grupoCultivos.get("Tipo");

            System.out.println("ALIAS: "+ alias_db + "\n" +
                    "FECHA CULTIVO: " +fechaCultivo_db + "\n" +
                    "FECHA COSECHA: " +fechaCosecha_db + "\n" +
                    "Tipo: " + tipo_db
            );


            // Establecer los valores en los campos correspondientes
            alias_antiguo = findViewById(R.id.alias_cultivo_actual);

            EditText fecha_cultivo = findViewById(R.id.fecha_cultivo_actual);

            alias_antiguo.setText(alias_db);
            fecha_cultivo.setText(fechaCultivo_db);
        } else {
            Log.w("Error", "El extra no es un HashMap válido");
        }
    }

    private void obtener_uid_y_actualizar(String alias, Map<String, Object> nuevoCultivoData) {
        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getUid();

        Log.d("userid","USER ID:" + user_id);
        Log.d("ALIAS","alias antiguo: "+ alias);


        if (user_id != null) {
            DocumentReference users_ref = db.collection("users")
                    .document(user_id);

            users_ref.collection("cultivos")
                    .whereEqualTo("Alias", alias)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                String uid_objeto = documentSnapshot.getId();
                                Log.d("UID", "uid: " + uid_objeto);

                                DocumentReference cultivo_ref = users_ref.collection("cultivos").document(uid_objeto);

                                cultivo_ref.set(nuevoCultivoData)
                                        .addOnSuccessListener(aVoid -> Log.d("UPDATE", "Documento sobrescrito exitosamente"))
                                        .addOnFailureListener(e -> Log.d("ERROR", "Error sobrescribiendo el documento", e));
                            } else {
                                Log.d("ERROR", "Error: No se pudo encontrar el objeto");
                            }
                        } else {
                            Log.d("ERROR", "Error en la tarea de obtención de documentos", task.getException());
                        }
                    });
        } else {
            Log.d("ERROR", "No se pudo obtener la id del usuario conectado");
        }
    }



    private List<Integer> dividir_fecha (String fecha){
        int day;
        int month;
        int year;

        String[] fecha_str = fecha.split("/");
        day = Integer.parseInt(fecha_str[0]);
        month = Integer.parseInt(fecha_str[1]);
        year = Integer.parseInt(fecha_str[2]);

        return Arrays.asList(day, month, year);
    }

    private void calcularFecha(List<Integer> lista,String tipocultivo) {

        int day = lista.get(0);
        int month = lista.get(1);
        int year = lista.get(2);

        int diasCultivo = 0;

        // Obtener seleccion para calcular los dias segun el tipo
        switch (tipocultivo) {
            case "Tomates":
                diasCultivo = 80;
                break;
            case "Cebollas":
                diasCultivo = 120;
                break;
            case "Lechugas":
                diasCultivo = 60;
                break;
            case "Apio":
                diasCultivo = 85;
                break;
            case "Maiz":
                diasCultivo = 90;
                break;
        }

        // Sumar dias al calendario
        Calendar calendario = Calendar.getInstance();

        // Indexacion de indices desde 0-11
        calendario.set(year, month - 1, day);
        calendario.add(Calendar.DAY_OF_MONTH, diasCultivo);

        // Nueva fecha, nuevo mes indexado se le suma 1
        int nuevoDia = calendario.get(Calendar.DAY_OF_MONTH);
        int nuevoMes = calendario.get(Calendar.MONTH) + 1;
        int nuevoAno = calendario.get(Calendar.YEAR);

        //Definir la nueva fecha en la variable
        nuevaFecha = nuevoDia + "/" + nuevoMes + "/" + nuevoAno;
    }
}