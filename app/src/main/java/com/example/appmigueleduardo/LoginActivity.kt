package com.example.appmigueleduardo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Registramos el manejador para el resultado del login [cite: 142]
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay un usuario logueado, saltamos directo a la MainActivity [cite: 150, 183-184]
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 1. Especificamos el tipo explícitamente <AuthUI.IdpConfig>
        val providers: List<AuthUI.IdpConfig> = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder()
                .setRequireName(true)
                .build()
        )

// 2. Configuramos el Intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setAlwaysShowSignInMethodScreen(true) // Esto ayuda a refrescar el flujo
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Éxito: Vamos a la pantalla principal [cite: 148-151]
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}