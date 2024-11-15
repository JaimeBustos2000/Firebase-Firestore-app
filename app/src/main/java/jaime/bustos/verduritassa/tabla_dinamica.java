package jaime.bustos.verduritassa;

import jaime.bustos.verduritassa.Editar_cultivo;
import jaime.bustos.verduritassa.Post_login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;


public class tabla_dinamica {
    private Context context;
    private TableLayout tableLayout;
    private BottomSheetDialog dialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    public tabla_dinamica(Context context, TableLayout tableLayout) {
        this.context = context;
        this.tableLayout = tableLayout;
    }


    // Método para agregar múltiples filas a la tabla
    // Pide un arraylist con la clase cultivos llamada data
    public void addRows(ArrayList<Post_login.Cultivo> data) {
        for (Post_login.Cultivo cultivo : data) {
            TableRow tableRow = new TableRow(context);

            // Columna 1: Alias
            TextView aliasTextView = new TextView(context);
            aliasTextView.setText(cultivo.getAlias());
            aliasTextView.setPadding(16, 16, 16, 16);
            aliasTextView.setBackgroundColor(Color.parseColor("#f0f0f0"));
            aliasTextView.setTextColor(Color.BLACK);
            tableRow.addView(aliasTextView);

            // Columna 2: Fecha de cosecha
            TextView fechaCosechaTextView = new TextView(context);
            fechaCosechaTextView.setText(cultivo.getFechaCosecha());
            fechaCosechaTextView.setPadding(16, 16, 16, 16);
            fechaCosechaTextView.setBackgroundColor(Color.parseColor("#f0f0f0"));
            fechaCosechaTextView.setTextColor(Color.BLACK);
            fechaCosechaTextView.setGravity(Gravity.CENTER);
            tableRow.addView(fechaCosechaTextView);

            // Columna 3: ImageButton de configuraciones
            ImageButton imageButton = new ImageButton(context);
            imageButton.setImageResource(R.drawable.baseline_settings_off);
            imageButton.setBackgroundColor(Color.parseColor("#f0f0f0"));
            imageButton.setPadding(16, 16, 16, 16);


            // OnClickListener para el ImageButton
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Crea un BottomSheetDialog e inflar el layout
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                    View bottomSheetView = View.inflate(context, R.layout.button_dialog, null);

                    // Añadir atributos al objeto del boton editar
                    Button Editar_btn = bottomSheetView.findViewById(R.id.Editar);
                    Editar_btn.setText("Configuración de " + cultivo.getAlias());
                    Editar_btn.setBackgroundColor(Color.parseColor("#bd8e37"));
                    Editar_btn.setTextColor(Color.WHITE);
                    Editar_btn.setTextSize(20);

                    // Añadir atributos al objeto del boton eliminar
                    Button delete_btn = bottomSheetView.findViewById(R.id.delete);
                    delete_btn.setText("Eliminar");
                    delete_btn.setBackgroundColor(Color.RED);
                    delete_btn.setTextSize(20);

                    // Click listener para editar (Boton flotante)
                    Editar_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            HashMap<String, Object> grupo_cultivos  = new HashMap<>();
                            grupo_cultivos.put("Alias",cultivo.getAlias());
                            grupo_cultivos.put("Fecha_cultivo",cultivo.getFechaCultivo());
                            grupo_cultivos.put("Fecha_cosecha",cultivo.getFechaCosecha());
                            grupo_cultivos.put("Tipo",cultivo.getTipo());


                            if (context instanceof Activity){
                                Activity activity = (Activity) context;
                                activity.finish();
                            }

                            Intent intents = new Intent(context, Editar_cultivo.class);
                            intents.putExtra("CULTIVOS", grupo_cultivos);

                            context.startActivity(intents);
                        }
                    });

                    // Click listener para eliminar (Boton flotante)
                    delete_btn.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("UnsafeIntentLaunch")
                        @Override
                        public void onClick(View view) {
                            obtener_y_eliminar_cultivo(cultivo.getAlias(),context);

                        }
                    });


                    // Se añade lo que aparecera en la ventana flotante y se muestra
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();

                }
            });

            // Se añade a las filas el boton de configuracion
            tableRow.addView(imageButton);
            // Se renderiza lo añadido a la fila por cada iteracion
            tableLayout.addView(tableRow);
        }
    }

    // Funcion para obtener la uid autogenerada del objeto y eliminar
    private void obtener_y_eliminar_cultivo(String alias, Context context) {
        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getUid();

        if (user_id != null) {
            DocumentReference users_ref = db.collection("users").document(user_id);

            // 1. Consulta para obtener el id del objeto segun el alias
            users_ref.collection("cultivos")
                    .whereEqualTo("Alias", alias)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Obtener el primer atributo de la consulta (en este caso el id)
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                String uid_objeto = documentSnapshot.getId();

                                // Eliminar el documento con el uid obtenido
                                eliminar_cultivo(user_id, uid_objeto);

                                // Saltar a la actividad
                                Intent intents = new Intent(context,Post_login.class);
                                context.startActivity(intents);
                            } else {
                                Log.d("ERROR", "Error: No se pudo encontrar el objeto");
                            }
                        } else {
                            Log.d("ERROR", "Error: No se pudo encontrar el objeto");
                        }
                    });
        } else {
            Log.d("Error", "No se pudo obtener la id del usuario conectado");
        }
    }

    // Funcion para eliminar explicitamente el cultivo obtenido
    private void eliminar_cultivo(String user_id, String uid) {
        CollectionReference users_ref = db.collection("users");

        users_ref.document(user_id)
                .collection("cultivos")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Documento eliminado con éxito");
                })
                .addOnFailureListener(e -> {
                    Log.w("Error", "Error al eliminar el documento: " + e);
                });
    }

}
