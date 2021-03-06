package com.example.contactdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "Login"

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            performLogin()
        }

        findViewById<TextView>(R.id.signupTxt).setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(){
        val email = findViewById<EditText>(R.id.loginEmailTxt).text.toString()
        val password = findViewById<EditText>(R.id.loginPasswordTxt).text.toString()

        //Firebase Authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful) {
                    return@addOnCompleteListener
                }else{
                    // else if successful
                    Log.d("Login", "Successfully logged in.")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { task ->
                Log.d("Login", "Login Failed: ${task.message}")
                Toast.makeText(applicationContext, "Login Failed: ${task.message}", Toast.LENGTH_LONG).show()
            }
    }
}