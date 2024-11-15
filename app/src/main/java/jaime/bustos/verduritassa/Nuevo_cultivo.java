package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Nuevo_cultivo extends AppCompatActivity {

    String nuevaFecha;

    Button nuevo_cultivo;

    EditText alias;
    EditText fecha;

    Spinner tipo_cultivo;

    ImageButton volver;

    FirebaseAuth mAuth;
    FirebaseUser User;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.nuevo_cultivo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar referencias
        nuevo_cultivo = findViewById(R.id.add_cultivo);
        alias = findViewById(R.id.alias);
        fecha = findViewById(R.id.Date);
        tipo_cultivo = findViewById(R.id.spinner_cultivo);
        volver = findViewById(R.id.arrow);
        // Datepicker dialog
        fecha.setOnClickListener(View -> ShowPickerDialog());

        nuevo_cultivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarDatos()){

                    // Obtener datos de los edit_text
                    String Nombre_cultivo = tipo_cultivo.getSelectedItem().toString();
                    String fecha_str = fecha.getText().toString();
                    // Separar le fecha en un array de strings DD/MM/YYYY
                    String[] formato_fecha = fecha_str.split("/");

                    int day = Integer.parseInt(formato_fecha[0]);
                    int month = Integer.parseInt(formato_fecha[1]);
                    int year = Integer.parseInt(formato_fecha[2]);

                    // Calcular fecha de cosecha
                    calcularFecha(day,month,year,Nombre_cultivo);

                    anadir_cultivo_firestore();

                    Intent intents = new Intent(Nuevo_cultivo.this,Post_login.class);
                    startActivity(intents);

                }else{
                    Log.d(TAG,"Error campos vacios");
                    Toast.makeText(Nuevo_cultivo.this,"Algun campo esta vacio",Toast.LENGTH_SHORT).show();
                }

            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intents = new Intent(Nuevo_cultivo.this,Post_login.class);
                startActivity(intents);
            }
        });
    }


    public void ShowPickerDialog(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datepicker = new DatePickerDialog(
                this,
                (DatePicker view, int selected_year, int select_month, int select_day)->
                {
                    String fecha_default = select_day + "/" + (select_month + 1)+ "/" + selected_year;

                    fecha.setText(fecha_default);
                },
                year, month,day);
        datepicker.show();
    }

    public void anadir_cultivo_firestore(){
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();

        // Referencia al uid del usuario que fue usado para crear y relacionar sus propios datos
        if (User != null){
            String user_id;
            user_id = User.getUid();

            // Map para guardar los cultivos que va añadir el usuario
            Map <String, Object> cultivos_usuario = new HashMap<>();

            cultivos_usuario.put("Alias",alias.getText().toString());
            cultivos_usuario.put("TCultivo",tipo_cultivo.getSelectedItem().toString());
            cultivos_usuario.put("FIngreso",fecha.getText().toString());
            cultivos_usuario.put("FCosecha",nuevaFecha);

            // Operar con la firestore
            db.collection("users")
                    .document(user_id)
                    .collection("cultivos")
                    .document()
                    .set(cultivos_usuario)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG,"Documento del cultivo añadido");
                            Toast.makeText(Nuevo_cultivo.this, "Cultivo añadido satisfactoriamente", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"Problemas al añadir el cultivo a firestore");
                            Toast.makeText(Nuevo_cultivo.this, "No se pudo añadir el cultivo intenta nuevamente", Toast.LENGTH_SHORT).show();
                        }
                    });


        }else{
            Toast.makeText(Nuevo_cultivo.this,"Compruebe los datos",Toast.LENGTH_SHORT).show();
        }
    }

    private void calcularFecha(int day,int month,int year,String tipocultivo) {
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

    public boolean validarDatos(){
        if(alias.getText().toString().isEmpty() ||
                fecha.getText().toString().isEmpty() ||
                tipo_cultivo.getSelectedItem().toString().isEmpty()
        ){
            return false;
        } else{
            return true;
        }
    }
}