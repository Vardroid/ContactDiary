package com.example.contactdiary

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.contactdiary.models.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class EditEntryActivity : AppCompatActivity() {
    lateinit var editEntryDateTxt: TextView
    lateinit var editEntryPlaceTxt: TextView
    lateinit var editEntryTimeTxt: TextView
    lateinit var editEntryDescTxt: TextView
    lateinit var entryEditBtn: Button
    var dayId = ""
    var entryId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)

        editEntryDateTxt = findViewById(R.id.editEntryDateTxt)
        editEntryPlaceTxt = findViewById(R.id.editEntryPlaceTxt)
        editEntryTimeTxt = findViewById(R.id.editEntryTimePickerTxt)
        editEntryDescTxt = findViewById(R.id.editEntryDescTxt)
        entryEditBtn = findViewById(R.id.entryEditBtn)

        supportActionBar?.title = "Edit"

        //get the id of the day and the entry
        dayId = intent.getStringExtra("dayId").toString()
        entryId = intent.getStringExtra("entryId").toString()
        Log.d("EditEntryActivity", entryId)

        putValuesToFields(dayId, entryId)

        entryEditBtn.setOnClickListener {
            saveValuesToFirebaseDatabase()
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun saveValuesToFirebaseDatabase(){
        val entryTimeTxt = findViewById<TextView>(R.id.editEntryTimePickerTxt).text.toString()
        val entryPlaceTxt = findViewById<TextView>(R.id.editEntryPlaceTxt).text.toString()
        val entryDescTxt = findViewById<TextView>(R.id.editEntryDescTxt).text.toString()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/$dayId")

        val entry = Entry(entryId, entryTimeTxt, entryPlaceTxt, entryDescTxt)
        ref.child(entryId).setValue(entry)

        Toast.makeText(applicationContext, "Entry Updated.", Toast.LENGTH_LONG).show()
    }

    private fun putValuesToFields(dayId: String, entryId: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/$dayId")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                editEntryDateTxt.text = snapshot.key.toString()
                snapshot.children.forEach { it ->
                    if(it.key.toString() == entryId){ //if true then it is the entry we're looking for
                        val entry = it.getValue(Entry::class.java)
                        Log.d("Test", entry!!.time)
                        editEntryPlaceTxt.text = entry.place
                        editEntryTimeTxt.text = entry.time
                        editEntryDescTxt.text = entry.moreInfo
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}