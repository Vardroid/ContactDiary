package com.example.contactdiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.contactdiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import net.glxn.qrgen.android.QRCode

class MeActivity : AppCompatActivity() {
    lateinit var nameTxt: TextView
    lateinit var barangayTxt: TextView
    lateinit var ageTxt: TextView
    lateinit var imageView: ImageView
    lateinit var userInfoArray: MutableList<Any>
    lateinit var userInfoString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me)
        supportActionBar?.title = "Me"

        nameTxt = findViewById(R.id.QRnameTxt)
        barangayTxt = findViewById(R.id.QRbarangayTxt)
        ageTxt = findViewById(R.id.QRageTxt)
        userInfoArray = ArrayList()
        userInfoString = ""
        imageView = findViewById(R.id.imgQR)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it ->
                    if(!it.hasChildren()){
                        if(it.key != "uid"){
                            val info = it.value.toString()
                            userInfoArray.add(info)
                            if(userInfoString == ""){
                                userInfoString = info
                            }else{
                                userInfoString = userInfoString + " - " + info
                            }
                        }
                    }
                }
                //userInfoArray is arrange like this: age, barangay, name
                nameTxt.text = userInfoArray[2].toString()
                barangayTxt.text = userInfoArray[1].toString()
                ageTxt.text = userInfoArray[0].toString()+" years old"

                //generate QR code
                val bitmap = QRCode.from(userInfoString).withSize(600,600).bitmap()
                imageView.setImageBitmap(bitmap)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}