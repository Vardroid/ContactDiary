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
    var dateFormat = ""
    var timeMilitary = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        supportActionBar?.title = "Add an Entry"

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        val entryDateTxt = findViewById<TextView>(R.id.entryDateTxt)
        val entryTimeTxt = findViewById<TextView>(R.id.entryTimePickerTxt)

        val saveEntryBtn = findViewById<Button>(R.id.entrySaveBtn)

        saveEntryBtn.setOnClickListener {
            if(dateFormat == "" || findViewById<TextView>(R.id.entryTimePickerTxt).text.toString() == "Pick Time"){
                Toast.makeText(applicationContext, "Please pick a date and time.", Toast.LENGTH_LONG).show()
            }else{
                saveEntryToFirebaseDatabase()
            }
        }

        entryDateTxt.setOnClickListener{
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                val dayOfMonthFormatted = String.format("%02d", dayOfMonth)
                dateFormat = "" + (month+1) + " - " + dayOfMonthFormatted + " - " + year
                entryDateTxt.setText(""+ months[month] + " " + dayOfMonthFormatted + ", " + year)
            }, year, month, day)
            dpd.show()
        }

        entryTimeTxt.setOnClickListener {
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                var additional = "AM"
                var newHour = hourOfDay
                if(hourOfDay > 12){
                    additional = "PM"
                    newHour = hourOfDay-12
                }
                val hourEdited = String.format("%02d", newHour)
                val hourMilitaryEdited = String.format("%02d", hourOfDay)
                val minuteEdited = String.format("%02d", minute)
                timeMilitary = "$hourMilitaryEdited:$minuteEdited"
                entryTimeTxt.setText("$hourEdited:$minuteEdited $additional")
            }, hour, minute, false)
            tpd.show()
        }
    }

    private fun saveEntryToFirebaseDatabase(){
        val entryDateTxt = dateFormat
        val entryTimeTxt = findViewById<TextView>(R.id.entryTimePickerTxt).text.toString()
        val entryPlaceTxt = findViewById<TextView>(R.id.addEntryPlaceTxt).text.toString()
        val entryDescTxt = findViewById<TextView>(R.id.entryDescTxt).text.toString()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/$entryDateTxt")

        var dateExist = false
        val refTest = FirebaseDatabase.getInstance().getReference("/users/$uid")
        refTest.addListenerForSingleValueEvent(object : ValueEventListener {
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

                //create a random id for the entry
                val uuid = timeMilitary
                val newRef = FirebaseDatabase.getInstance().getReference("/users/$uid/$entryDateTxt/$uuid")

                val entry = Entry(uuid, entryTimeTxt, entryPlaceTxt, entryDescTxt)

                Log.d("AddEntry", "$uuid $entryTimeTxt $entryPlaceTxt $entryDescTxt $dateFormat $uuid")
                newRef.setValue(entry)
                        .addOnSuccessListener {
                            Log.d("AddEntry", "Successfully added entry to database.")
                            Toast.makeText(applicationContext, "Successfully added entry to database.", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { task ->
                            Log.d("AddEntry", "Failed to add entry to database: ${task.message}")
                            Toast.makeText(applicationContext, "Failed to add entry to database: ${task.message}", Toast.LENGTH_LONG).show()
                        }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}