package com.example.contactdiary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.example.contactdiary.models.Day
import com.example.contactdiary.models.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    lateinit var days: ArrayList<Day>
    lateinit var mainList: ExpandableListView
    lateinit var entries: ArrayList<Day>

    lateinit var entriesComp: MutableList<Entry>
    lateinit var entriesCompComp: MutableList<MutableList<Entry>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyUserIsLoggedIn()

        entries = ArrayList()
        days = ArrayList()
        entriesComp = ArrayList()
        entriesCompComp = ArrayList()
        fetchDays()

        val fabAdd: View = findViewById(R.id.mainAddBtn)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        val fabCalendar: View = findViewById(R.id.mainCalendarBtn)
        fabCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        val fabMe: View = findViewById(R.id.mainMeBtn)
        fabMe.setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }
    }

    //get the days data with their child entries from the users inside the firebase
    private fun fetchDays() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        Log.d("Test", "$uid")
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                days.clear()
                snapshot.children.forEach { it ->
                    if(it.hasChildren()){ //if it is the day path
                        val date = it.getValue(Day::class.java)
                        days.add(date!!)

                        //go to child to check for entries
                        entriesComp.clear()
                        it.children.forEach{ twoit ->
                            if(twoit.hasChildren()){ //if it is an entry path
                                val entry = twoit.getValue(Entry::class.java)
                                entriesComp.add(entry!!)
                            }
                        }
                        Log.d("Test", "${entriesComp.size}")
                        entriesCompComp.add(entriesComp.toMutableList())
                    }
                }
                val adapter = MainExpandableListViewAdapter(applicationContext, days, entriesCompComp.toMutableList())
                mainList = findViewById(R.id.mainList)
                mainList.isClickable = false
                mainList.setAdapter(adapter)

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

    //adapter for the expandablelistview in the MainActivity
    class MainExpandableListViewAdapter (context: Context, items: ArrayList<Day>, entryEntries: MutableList<MutableList<Entry>>) : BaseExpandableListAdapter(){
        private val mContext: Context = context
        private val mItems: MutableList<Day> = items
        private val mEntryEntries: MutableList<MutableList<Entry>> = entryEntries
        lateinit var mExpandableListView: ExpandableListView

        override fun getGroupCount(): Int {
            return mItems.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return mEntryEntries[groupPosition].size
        }

        override fun getGroup(groupPosition: Int): Any {
            return mItems[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return mEntryEntries[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            mExpandableListView = parent as ExpandableListView
            mExpandableListView.expandGroup(groupPosition)
            val layoutInflater = LayoutInflater.from(mContext)
            val itemView = layoutInflater.inflate(R.layout.main_list_item, parent, false)

            val date = itemView.findViewById<TextView>(R.id.mainListDateTxt)
            val dates = mItems[groupPosition].date.split(" - ").toTypedArray()
            val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            val m = months[dates[0].toInt()-1]

            date.text = "$m ${dates[1]}, ${dates[2]}"
            //Log.d("Test", "entryEntries size: ${entryEntries[groupPosition].size} $groupPosition")

            return itemView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val itemView = layoutInflater.inflate(R.layout.entry_list_item, parent, false)

            val time = itemView.findViewById<TextView>(R.id.entryTimeTxt)
            val timeAdditional = itemView.findViewById<TextView>(R.id.entryTime2Txt)
            val place = itemView.findViewById<TextView>(R.id.entryPlaceTxt)
            val desc = itemView.findViewById<TextView>(R.id.entryListDescTxt)

            val times = mEntryEntries[groupPosition][childPosition].time.split(" ").toTypedArray()
            time.text = "${times[0]}"
            timeAdditional.text = "${times[1]}"
            place.text = mEntryEntries[groupPosition][childPosition].place
            desc.text = mEntryEntries[groupPosition][childPosition].moreInfo

            return itemView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }
    }
}



