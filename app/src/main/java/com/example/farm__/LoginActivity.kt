////package com.example.farm__
////import android.content.Intent
////import android.os.Bundle
////import android.widget.*
////import androidx.appcompat.app.AppCompatActivity
////import com.google.firebase.auth.FirebaseAuth
////
////class LoginActivity: AppCompatActivity() {
////    private lateinit var auth: FirebaseAuth
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_login)
////
////        auth = FirebaseAuth.getInstance()
////
////        val etEmail = findViewById<EditText>(R.id.etEmail)
////        val etPassword = findViewById<EditText>(R.id.etPassword)
////        val btnLogin = findViewById<Button>(R.id.btnLogin)
////        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
////
////        btnLogin.setOnClickListener {
////            val email = etEmail.text.toString().trim()
////            val password = etPassword.text.toString().trim()
////
////            if (email.isEmpty() || password.isEmpty()) {
////                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
////
////            auth.signInWithEmailAndPassword(email, password)
////                .addOnCompleteListener { task ->
////                    if (task.isSuccessful) {
////                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
////                        // 다음 화면으로 이동할 수 있음
////                    } else {
////                        Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
////                    }
////                }
////        }
////
////        btnSignUp.setOnClickListener {
////            // 회원가입 화면으로 이동하거나 회원가입 로직 구현
////            val intent = Intent(this, SignUpActivity::class.java)
////            startActivity(intent)
////        }
////    }
////}
//
//package com.example.farm__
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.auth.api.signin.*
//import com.google.android.gms.common.api.ApiException
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.DocumentSnapshot
//
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var googleClient: GoogleSignInClient
//    private val RC_GOOGLE_SIGN_IN = 9001
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        auth = FirebaseAuth.getInstance()
//
//        // 🔹 이메일/비밀번호 로그인 관련 뷰
//        val etEmail = findViewById<EditText>(R.id.etEmail)
//        val etPassword = findViewById<EditText>(R.id.etPassword)
//        val btnLogin = findViewById<Button>(R.id.btnLogin)
//        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
//        val btnGoogleSignIn = findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogleSignIn)
//
//        // 🔹 구글 로그인 옵션 설정
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id)) // 🔸 strings.xml에 설정된 client ID 필요
//            .requestEmail()
//            .build()
//
//        googleClient = GoogleSignIn.getClient(this, gso)
//
//        // 🔹 이메일 로그인 버튼 클릭
//        btnLogin.setOnClickListener {
//            val email = etEmail.text.toString().trim()
//            val password = etPassword.text.toString().trim()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
//                        startActivity(Intent(this, FarmDashboardActivity::class.java))
//                        finish()
//                    } else {
//                        Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//
//        // 🔹 회원가입 버튼 클릭
//        btnSignUp.setOnClickListener {
//            startActivity(Intent(this, SignUpActivity::class.java))
//        }
//
//        // 🔹 구글 로그인 버튼 클릭
//        btnGoogleSignIn.setOnClickListener {
//            val signInIntent = googleClient.signInIntent
//            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
//        }
//    }
//
//    // 🔹 구글 로그인 응답 처리
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_GOOGLE_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                Toast.makeText(this, "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val email = auth.currentUser?.email ?: return@addOnCompleteListener
//                    val db = FirebaseFirestore.getInstance()
//
//                    db.collection("users").document(email)
//
//                        .get()
//                        .addOnSuccessListener { doc ->
//                            val hasFarmName = doc.contains("farmName") && !doc.getString("farmName").isNullOrEmpty()
//                            if (hasFarmName) {
//
//                                startActivity(Intent(this, FarmDashboardActivity::class.java))
//                            } else {
//
//                                startActivity(Intent(this, FarmNameActivity::class.java))
//                            }
//                            finish()
//                        }
//                } else {
//                    Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//
////    // 🔹 Firebase에 구글 계정으로 로그인
////    private fun firebaseAuthWithGoogle(idToken: String) {
////        val credential = GoogleAuthProvider.getCredential(idToken, null)
////        auth.signInWithCredential(credential)
////            .addOnCompleteListener(this) { task ->
////                if (task.isSuccessful) {
////                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
////                    val db = FirebaseFirestore.getInstance()
////
////                    // Firestore에 사용자 정보 있는지 확인
////                    db.collection("users").document(uid).get()
////                        .addOnSuccessListener { documentSnapshot ->
////                            if (documentSnapshot.exists()) {
////                                startActivity(Intent(this, FarmDashboardActivity::class.java))
////                            } else {
////                                startActivity(Intent(this, FarmNameActivity::class.java))
////                            }
////                            finish()
////                        }
////
////                } else {
////                    Toast.makeText(this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show()
////                }
////            }
////    }
//
//}
package com.example.farm__

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnGoogleSignIn = findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogleSignIn)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        // 🔹 이메일 로그인 버튼
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userEmail = auth.currentUser?.email ?: return@addOnCompleteListener
                        val db = FirebaseFirestore.getInstance()

                        val userMap = mapOf(
                            "email" to userEmail,
                            "farmName" to "",
                            "createdAt" to System.currentTimeMillis(),
                            "humidity" to 0,
                            "light" to 0,
                            "soilMoisture" to 0,
                            "temperature" to 0,
                            "photoTime" to "05:00",
                            "pumpStatus" to "off"
                        )

                        db.collection("users").document(userEmail).get()
                            .addOnSuccessListener { doc ->
                                if (!doc.exists()) {
                                    db.collection("users").document(userEmail)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            val dummyPhoto = mapOf(
                                                "imageBase64" to "",
                                                "timestamp" to com.google.firebase.Timestamp.now()
                                            )

                                            val userRef = db.collection("users").document(userEmail)

// photos가 없으면 생성
                                            userRef.collection("photos").limit(1).get()
                                                .addOnSuccessListener { snap ->
                                                    if (snap.isEmpty) {
                                                        userRef.collection("photos").document("dummy").set(dummyPhoto)
                                                    }
                                                }

// gallery가 없으면 생성
                                            userRef.collection("gallery").limit(1).get()
                                                .addOnSuccessListener { snap ->
                                                    if (snap.isEmpty) {
                                                        userRef.collection("gallery").document("dummy").set(dummyPhoto)
                                                    }
                                                }

                                            startActivity(Intent(this, FarmNameActivity::class.java))
                                            finish()
                                        }
                                } else {
                                    val hasFarmName = doc.contains("farmName") && !doc.getString("farmName").isNullOrEmpty()
                                    if (hasFarmName) {
                                        startActivity(Intent(this, FarmDashboardActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, FarmNameActivity::class.java))
                                    }
                                    finish()
                                }
                            }
                    } else {
                        Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }

                }
        }


        // 🔹 회원가입 버튼 클릭
        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 🔹 구글 로그인 버튼
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    // 🔹 구글 로그인 응답 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val email = auth.currentUser?.email ?: return@addOnCompleteListener
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(email).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val userMap = mapOf(
                                    "email" to email,
                                    "farmName" to "",
                                    "createdAt" to System.currentTimeMillis(),
                                    "humidity" to 0,
                                    "light" to 0,
                                    "soilMoisture" to 0,
                                    "temperature" to 0,
                                    "photoTime" to "05:00",
                                    "pumpStatus" to "off"
                                )
                                db.collection("users").document(email).set(userMap)
                            }

                            val hasFarmName = doc.contains("farmName") && !doc.getString("farmName").isNullOrEmpty()
                            if (hasFarmName) {
                                startActivity(Intent(this, FarmDashboardActivity::class.java))
                            } else {
                                startActivity(Intent(this, FarmNameActivity::class.java))
                            }
                            finish()
                        }
                } else {
                    Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
