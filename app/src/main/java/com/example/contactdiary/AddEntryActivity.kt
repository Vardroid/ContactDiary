package com.example.contactdiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.contactdiary.models.Day
import com.example.contactdiary.models.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AddEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        val entryDateTxt = findViewById<TextView>(R.id.entryDateTxt)
        val entryTimeTxt = findViewById<TextView>(R.id.entryTimePickerTxt)

        val saveEntryBtn = findViewById<Button>(R.id.entrySaveBtn)

        saveEntryBtn.setOnClickListener { saveEntryToFirebaseDatabase() }

        entryDateTxt.setOnClickListener{
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                entryDateTxt.setText(""+ (month+1) + " - " + dayOfMonth + " - " + year)
            }, year, month, day)
            dpd.show()
        }

        entryTimeTxt.setOnClickListener {
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                var additional = "AM"
                var newHour = hourOfDay
                if(hourOfDay > 12){
                    additional = "PM"
                    newHour = hourOfDay-12
                }
                val hourEdited = String.format("%02d", newHour)
                val minuteEdited = String.format("%02d", minute)
                entryTimeTxt.setText("$hourEdited:$minuteEdited $additional")
            }, hour, minute, false)
            tpd.show()
        }


    }

    private fun saveEntryToFirebaseDatabase(){
        val entryDateTxt = findViewById<TextView>(R.id.entryDateTxt).text.toString()
        val entryTimeTxt = findViewById<TextView>(R.id.entryTimePickerTxt).text.toString()
        val entryPlaceTxt = findViewById<TextView>(R.id.addEntryPlaceTxt).text.toString()
        val entryDescTxt = findViewById<TextView>(R.id.entryDescTxt).text.toString()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/$entryDateTxt")

        var dateExist = false
        val refTest = FirebaseDatabase.getInstance().getReference("/users/$uid")
        refTest.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if(it.key == entryDateTxt && it.hasChildren()){
                        dateExist = true
                    }
                }
                if(!dateExist){
                    val day = Day(entryDateTxt)
                    ref.setValue(day)
                        .addOnSuccessListener {
                            Log.d("AddEntry", "Successfully added day to database.")
                        }
                        .addOnFailureListener { task ->
                            Log.d("AddEntry", "Failed to add day to database: ${task.message}")
                            Toast.makeText(applicationContext, "Failed to add day to database: ${task.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })



        val uuid = UUID.randomUUID().toString()
        val newRef = FirebaseDatabase.getInstance().getReference("/users/$uid/$entryDateTxt/$uuid")

        val entry = Entry(uuid, entryTimeTxt, entryPlaceTxt, entryDescTxt)

        newRef.setValue(entry)
            .addOnSuccessListener {
                Log.d("AddEntry", "Successfully added entry to database.")
                Toast.makeText(applicationContext, "Successfully added entry to database.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { task ->
                Log.d("AddEntry", "Failed to add entry to database: ${task.message}")
                Toast.makeText(applicationContext, "Failed to add entry to database: ${task.message}", Toast.LENGTH_LONG).show()
            }
    }
}