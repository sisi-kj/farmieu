//package com.example.farm__
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class FarmNameActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private val db = FirebaseFirestore.getInstance()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_farm_name)
//
//        auth = FirebaseAuth.getInstance()
//
//        val etFarmName = findViewById<EditText>(R.id.etFarmName)
//        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
//
//        btnSubmit.setOnClickListener {
//            val farmName = etFarmName.text.toString().trim()
//            val uid = auth.currentUser?.uid ?: return@setOnClickListener
//            val email = auth.currentUser?.email ?: "unknown"
//
//            if (farmName.isEmpty()) {
//                Toast.makeText(this, "팜 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val userData = mapOf(
//                "farmName" to farmName,
//                "email" to email,
//                "createdAt" to System.currentTimeMillis()
//            )
//
//            db.collection("users")
//                .document(uid)
//                .set(userData)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "팜 이름 저장 완료!", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(this, FarmDashboardActivity::class.java))
//
//                    finish()
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, "저장 실패: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//}
package com.example.farm__

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FarmNameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farm_name)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etFarmName = findViewById<EditText>(R.id.etFarmName)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val farmName = etFarmName.text.toString().trim()
            val email = auth.currentUser?.email ?: return@setOnClickListener

            if (farmName.isEmpty()) {
                Toast.makeText(this, "팜 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = mapOf(
                "farmName" to farmName,
                "email" to email,
                "temperature" to 0.0,
                "humidity" to 0,
                "soilMoisture" to 0,
                "light" to 0,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(email) // ✅ UID → 이메일
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "팜 이름 저장 완료!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, FarmDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
