package com.example.aplicativo_horacerta

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        // 1. Verifica se os Termos foram aceitos
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)

        if (!onboardingDone) {
            // Se NÃO aceito: Vai para os Termos
            startActivity(Intent(this, TermosIniciaisActivity::class.java))
            finish()
        } else {
            // Se JÁ aceito: Vai para a tela inicial de Login/Registro
            startActivity(Intent(this, InicioTelaActivity::class.java)) // <-- AQUI USAMOS A NOVA TELA DE INÍCIO!
            finish()
        }
    }
}


//aaa123