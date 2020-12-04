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

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
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
                        finish()
                    }
                }
                .addOnFailureListener { task ->
                    Log.d("Login", "Login Failed: ${task.message}")
                    Toast.makeText(applicationContext, "Login Failed.", Toast.LENGTH_LONG).show()
                }
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
                    finish()
                }
            }
            .addOnFailureListener { task ->
                Log.d("Login", "Login Failed: ${task.message}")
                Toast.makeText(applicationContext, "Login Failed.", Toast.LENGTH_LONG).show()
            }
    }
}