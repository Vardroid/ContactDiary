package com.example.contactdiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    var dateFormat = ""
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

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        //get the id of the day and the entry
        dayId = intent.getStringExtra("dayId").toString()
        entryId = intent.getStringExtra("entryId").toString()

        putValuesToFields(dayId, entryId)

        entryEditBtn.setOnClickListener {
            saveValuesToFirebaseDatabase()
        }

        editEntryTimeTxt.setOnClickListener {
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                var additional = "AM"
                var newHour = hourOfDay
                if(hourOfDay > 12){
                    additional = "PM"
                    newHour = hourOfDay-12
                }
                val hourEdited = String.format("%02d", newHour)
                val minuteEdited = String.format("%02d", minute)
                editEntryTimeTxt.setText("$hourEdited:$minuteEdited $additional")
            }, hour, minute, false)
            tpd.show()
        }
    }

    private fun saveValuesToFirebaseDatabase(){
        val entryTimeTxt = findViewById<TextView>(R.id.editEntryTimePickerTxt).text.toString()
        val entryPlaceTxt = findViewById<TextView>(R.id.editEntryPlaceTxt).text.toString()
        val entryDescTxt = findViewById<TextView>(R.id.editEntryDescTxt).text.toString()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/$dayId")

        val entry = Entry(entryId, entryTimeTxt, entryPlaceTxt, entryDescTxt)
        ref.child("$entryId").setValue(entry)

        Toast.makeText(applicationContext, "Entry Updated.", Toast.LENGTH_LONG).show()
    }

    private fun putValuesToFields(dayId: String, entryId: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/$dayId")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                editEntryDateTxt.text = snapshot.key.toString()
                snapshot.children.forEach { it ->
                    if(it.key.toString() == entryId){ //if true then it is an entry
                        val entry = it.getValue(Entry::class.java)
                        Log.d("Test", "${entry!!.time}")
                        editEntryPlaceTxt.text = entry.place
                        editEntryTimeTxt.text = entry.time
                        editEntryDescTxt.text = entry.moreInfo
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}