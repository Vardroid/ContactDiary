package com.example.contactdiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.contactdiary.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.signupBtn).setOnClickListener {
            performRegister()
        }
    }

    //Method that registers the user in the firebase database using email and password
    private fun performRegister(){
        val email = findViewById<EditText>(R.id.signupEmailTxt).text.toString()
        val password = findViewById<EditText>(R.id.signupPasswordTxt).text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext, "Please enter email/password.", Toast.LENGTH_LONG).show()
        }

        //Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful) {
                    return@addOnCompleteListener
                }else{
                    // else if successful
                    Log.d("Signup", "Successfully created user with uid: ${task.result?.user?.uid}")
                    saveUserToFirebaseDatabase()
                }
            }
            .addOnFailureListener { task ->
                Log.d("Signup", "Failed to create user: ${task.message}")
                Toast.makeText(applicationContext, "Failed to create user: ${task.message}", Toast.LENGTH_LONG).show()
            }
    }

    //Method that saves the users information to the Firebase database
    private fun saveUserToFirebaseDatabase(){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val name = findViewById<TextView>(R.id.signupNameTxt).text.toString()
        val barangay = findViewById<TextView>(R.id.signupBarangayTxt).text.toString()
        val age = findViewById<TextView>(R.id.signupAgeTxt).text.toString().toInt()

        val user = User(uid, name, barangay, age)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Signup", "Successfully added user to database.")
            }
            .addOnFailureListener { task ->
                Log.d("Signup", "Failed to add user to database: ${task.message}")
                Toast.makeText(applicationContext, "Failed to add to database: ${task.message}", Toast.LENGTH_LONG).show()
            }
    }
}