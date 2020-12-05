package com.example.contactdiary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import com.example.contactdiary.models.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarActivity : AppCompatActivity() {
    lateinit var calendarView: CalendarView
    lateinit var calendarList: ListView
    lateinit var calendarDateTxt: TextView
    lateinit var entries: MutableList<Entry>
    var dateFormat = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        entries = ArrayList()
        calendarView = findViewById(R.id.calendarView)
        calendarDateTxt = findViewById(R.id.calendarDateTxt)
        calendarList = findViewById(R.id.calendarList)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            dateFormat = "" + (month+1) + " - " + dayOfMonth + " - " + year
            calendarDateTxt.text = ""+ months[month] + " " + dayOfMonth + ", " + year

            val uid = FirebaseAuth.getInstance().uid ?: ""
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it ->
                        if(setDataToCalendarList(applicationContext, it, dateFormat)){
                            return
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun setDataToCalendarList(context: Context, snapshot: DataSnapshot, date: String):Boolean{
        val entriesArray = entries.toMutableList()
        if(snapshot.key == date){
            entriesArray.clear()
            snapshot.children.forEach{
                if(it.hasChildren()){
                    val entry = it.getValue(Entry::class.java)
                    entriesArray.add(entry!!)
                }
            }
            val adapter = CalendarListAdapter(context, entriesArray)
            calendarList.adapter = adapter

            if(entriesArray.isNotEmpty()){
                findViewById<TextView>(R.id.calendarListPlaceholderTxt).visibility = View.INVISIBLE
            }else{
                findViewById<TextView>(R.id.calendarListPlaceholderTxt).visibility = View.VISIBLE
            }

            return true
        }else{
            val emptyArray:MutableList<Entry> = ArrayList()
            val adapter = CalendarListAdapter(context, emptyArray)
            calendarList.adapter = adapter

            findViewById<TextView>(R.id.calendarListPlaceholderTxt).visibility = View.VISIBLE

            return false
        }
    }

    private class CalendarListAdapter(context: Context, items: MutableList<Entry>) : BaseAdapter(){
        val mContext = context
        val mItems = items

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return mItems[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val itemView = layoutInflater.inflate(R.layout.entry_list_item, parent, false)

            val time = itemView.findViewById<TextView>(R.id.entryTimeTxt)
            val timeAdditional = itemView.findViewById<TextView>(R.id.entryTime2Txt)
            val place = itemView.findViewById<TextView>(R.id.entryPlaceTxt)
            val desc = itemView.findViewById<TextView>(R.id.entryListDescTxt)

            val times = mItems[position].time.split(" ").toTypedArray()
            time.text = "${times[0]}"
            timeAdditional.text = "${times[1]}"
            place.text = mItems[position].place
            desc.text = mItems[position].moreInfo

            return itemView
        }

    }
}