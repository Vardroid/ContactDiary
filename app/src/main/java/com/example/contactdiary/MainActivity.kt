package com.example.contactdiary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.contactdiary.models.Day
import com.example.contactdiary.models.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    lateinit var days: ArrayList<Day>
    lateinit var mainList: ListView
    lateinit var entryList: ListView
    lateinit var entries: ArrayList<Day>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyUserIsLoggedIn()

        entries = ArrayList()
        days = ArrayList()
        fetchDays()

        val fabAdd: View = findViewById(R.id.mainAddBtn)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        val fabCalendar: View = findViewById(R.id.mainCalendarBtn)
        fabCalendar.setOnClickListener {
            //startActivity(Intent(this, CalendarActivity::class.java))
        }

        val fabMe: View = findViewById(R.id.mainMeBtn)
        fabMe.setOnClickListener {
            //startActivity(Intent(this, MeActivity::class.java))
        }
    }

    //get the days data from the users inside the firebase
    private fun fetchDays() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        Log.d("Test", "$uid")
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                days.clear()

                snapshot.children.forEach {
                    if(it.hasChildren()){
                        Log.d("Test", "Successfully added day to database.")
                        val date = it.getValue(Day::class.java)
                        days.add(date!!)
                    }
                }
                val adapter = MainListAdapter(applicationContext, days)

                mainList = findViewById(R.id.mainList)
                mainList.adapter = adapter
                if(days.isNotEmpty()){
                    findViewById<TextView>(R.id.mainListPlaceholderTxt).visibility = View.INVISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    //verify if user is logged in
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    //method when an option item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.for_organizations -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    //create the options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    class MainListAdapter(context: Context, items: ArrayList<Day>): BaseAdapter(){
        private val mContext: Context = context
        private val mItems: MutableList<Day> = items

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val itemView = layoutInflater.inflate(R.layout.main_list_item, parent, false)

            val date = itemView.findViewById<TextView>(R.id.mainListDateTxt)
            date.text = mItems[position].date

            val entryList = itemView.findViewById<ListView>(R.id.entryList)

            var entries:ArrayList<Entry> = ArrayList()

            //go through the entries of each day
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/${mItems[position].date}")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    entries.clear()
                    snapshot.children.forEach {
                        if(it.hasChildren()){
                            Log.d("Test", "child found")
                            val entry = it.getValue(Entry::class.java)
                            entries.add(entry!!)
                        }
                    }
                    val adapter = EntryListAdapter(mContext, entries)
                    entryList.adapter = adapter
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            return itemView
        }

        //get the entries of a certain day of the users inside the firebase
//        private fun fetchEntries(date: Day, entryList: ListView){
//            Log.d("Test", "fetchentries")
//            var entries:ArrayList<Entry> = ArrayList()
//            val uid = FirebaseAuth.getInstance().uid ?: ""
//            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/${date.date}")
//            userRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    entries.clear()
//                    snapshot.children.forEach {
//                        if(it.hasChildren()){
//                            Log.d("Test", "child found")
//                            val entry = it.getValue(Entry::class.java)
//                            entries.add(entry!!)
//                        }
//                    }
//                    val adapter = EntryListAdapter(mContext, entries)
//
//                    entryList.adapter = adapter
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//        }
    }

    class EntryListAdapter(context: Context, items: ArrayList<Entry>): BaseAdapter(){
        private val mContext: Context = context
        private val mItems: MutableList<Entry> = items

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return position
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
            Log.d("Test", "${mItems.size}")
            time.text = "${times[0]}"
            timeAdditional.text = "${times[1]}"
            place.text = mItems[position].place
            desc.text = mItems[position].moreInfo

            return itemView
        }
    }
}



