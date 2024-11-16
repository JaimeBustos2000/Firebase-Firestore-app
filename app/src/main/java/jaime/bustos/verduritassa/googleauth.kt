package jaime.bustos.verduritassa

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class GoogleAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.server_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)

        val googleButton: Button = findViewById(R.id.google_button)

        googleButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@GoogleAuthActivity
                    )

                    val credentialResult = result.credential

                    if (credentialResult is CustomCredential &&
                        credentialResult.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialResult.data)
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        val data = Firebase.auth.signInWithCredential(firebaseCredential).await()

                        Log.i("SUCCESS", "${data.user?.email} ${data.user?.displayName} ${data.user?.uid}")
                    } else {
                        Log.e("ERROR", "No se pudo obtener la credencial")
                    }
                } catch (e: NoCredentialException) {
                    Log.e("ERROR", "No credentials available: ${e.message}")
                    // Prompt the user to add a Google account if none are available
                    showAddAccountDialog()
                } catch (e: Exception) {
                    Log.e("ERROR", "Error retrieving credentials: ${e.message}")
                }
            }
        }
    }

    // Function to prompt the user to add a Google account (you can customize this method)
    private fun showAddAccountDialog() {
        val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        startActivityForResult(intent, REQUEST_CODE_ADD_ACCOUNT)
    }

    companion object {
        private const val REQUEST_CODE_ADD_ACCOUNT = 1
    }
}



