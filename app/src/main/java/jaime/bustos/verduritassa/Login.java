package jaime.bustos.verduritassa;

import static android.content.ContentValues.TAG;


import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;

import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import androidx.credentials.PasswordCredential;

import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.google.firebase.auth.GoogleAuthProvider;


import java.util.concurrent.Executors;




public class Login extends AppCompatActivity {

    EditText email;
    EditText pass;
    TextView registrate;
    Toast mensaje;


    private static final int REQUEST_CODE_ADD_ACCOUNT = 1;

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

        enterImmersiveMode();

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
                google_id();
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

    private void google_id(){
        CredentialManager credentialManager = CredentialManager.create(Login.this);

        String client_id = getString((R.string.server_client_id));

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(client_id)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption).build();

        credentialManager.getCredentialAsync(
                Login.this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        handleFailure(e,credentialManager,client_id);
                    }
                });
    }

    private void handleFailure(GetCredentialException e, CredentialManager credentialManager, String client_id) {
        if (e instanceof NoCredentialException) {
            GetSignInWithGoogleOption googleIdOption = new GetSignInWithGoogleOption.Builder(client_id)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();



            credentialManager.getCredentialAsync(
                    Login.this,
                    request,
                    new CancellationSignal(),
                    Executors.newSingleThreadExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleSignIn(result);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            Log.e("ERROR", "Error retrieving credentials:"+e.getMessage());
                        }
                    });
        } else{
            Log.e("ERROR", "Error retrieving credentials:"+e.getMessage());
        }
    }

    public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();
        Log.d(TAG, "Credentials: " + credential);

        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();

        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();

            Log.d("Login", "Username: " + username + " Password:" + password);

        } else if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                    String IdToken = googleIdTokenCredential.getIdToken();
                    Log.i("Login", "IdToken: " + IdToken);
                    vincular_cuenta(IdToken);

                } catch (Exception e) {
                    Log.e(TAG, "Error processing Google ID token", e);
                }
            } else {
                // Catch any unrecognized custom credential type here.
                Log.e(TAG, "Unexpected type of credential");
            }
        } else {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential");
        }
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

    private void vincular_cuenta(String idToken) {
        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // mAuth.fetchSignInMethodsForEmail(email)
        // Primero intentamos iniciar sesión con Google
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "User credenciales: " + user);

                        if (user != null) {
                            // Verificamos si el usuario ya está autenticado con el correo y contraseña
                            if (user.getProviderData().size() > 1) {
                                // El usuario ya tiene credenciales de Google y correo, por lo que ya está vinculado
                                Toast.makeText(Login.this, "Cuenta ya vinculada con google", Toast.LENGTH_SHORT).show();
                                Intent intents = new Intent(Login.this, Post_login.class);

                                startActivity(intents);
                            } else {
                                // El usuario no tiene credenciales de Google, entonces las vinculamos
                                linkWithGoogleCredentials(user, credential);
                                Toast.makeText(Login.this, "Cuenta ya vinculada desde google", Toast.LENGTH_SHORT).show();
                                Intent intents = new Intent(Login.this, Post_login.class);
                            }
                        }
                    } else {
                        Toast.makeText(Login.this, "Error, no se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void linkWithGoogleCredentials(FirebaseUser user, AuthCredential googleCredential) {
        // Vinculamos las credenciales de Google con la cuenta de correo y contraseña

        user.linkWithCredential(googleCredential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        // Vinculación exitosa
                        Toast.makeText(Login.this, "Cuenta vinculada con Google", Toast.LENGTH_SHORT).show();
                    } else {
                        // Si la vinculación falla, muestra el error
                        Toast.makeText(Login.this, "Error al vincular cuenta", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}