package com.example.farm__

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val correctCode = "123456" // 보안코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()
        val etFarmName = findViewById<EditText>(R.id.etFarmName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        //val etCode = findViewById<EditText>(R.id.etCode)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val farmName = etFarmName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            //val code = etCode.text.toString().trim()

            if (farmName.isEmpty() || email.isEmpty() || password.isEmpty() //|| code.isEmpty()
                 ) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            if (code != correctCode) {
//                Toast.makeText(this, "보안 코드가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val user = hashMapOf(
                            "farmName" to farmName,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Firestore 저장 실패: ${it.localizedMessage ?: "알 수 없는 오류"}", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        if (task.exception?.message?.contains("email address is already in use") == true) {
                            Toast.makeText(this, "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}
