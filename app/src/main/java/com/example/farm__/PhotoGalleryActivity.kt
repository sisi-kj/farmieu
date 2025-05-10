//package com.example.farm__
//
//import android.util.Log
//import android.app.TimePickerDialog
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import java.util.*
//import androidx.appcompat.app.AlertDialog
//
//import android.util.Base64
//
//import android.graphics.BitmapFactory
//
//class PhotoGalleryActivity : AppCompatActivity() {
//
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//    private lateinit var galleryLayout: LinearLayout
//    private lateinit var tvSelectedTime: TextView
//    private var selectedTime: String = "08:00"
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_photo_gallery)
//
//        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
//
//        galleryLayout = findViewById(R.id.galleryLayout)
//        tvSelectedTime = findViewById(R.id.tvSelectedTime)
//        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
//        val btnSaveTime = findViewById<Button>(R.id.btnSaveTime)
//
//        val email = auth.currentUser?.email ?: return
//        // 🔹 저장된 시간 불러오기
//        db.collection("users").document(email)
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val savedTime = snapshot.getString("photoTime")
//                if (!savedTime.isNullOrBlank()) {
//                    selectedTime = savedTime
//                    tvSelectedTime.text = "선택된 시간: $selectedTime"
//                }
//            }
//
//
//        db.collection("users").document(email)
//
//            .collection("photos")
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { documents ->
//                for (doc in documents) {
//                    val base64 = doc.getString("imageBase64") ?: continue
//                    val pureBase64 = base64.substringAfter("base64,", "").replace("\\s".toRegex(), "")
//                    try {
//                        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
//                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//
//                        val imageView = ImageView(this)
//                        imageView.layoutParams = LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            400
//                        )
//                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//                        imageView.setImageBitmap(bitmap)
//                        galleryLayout.addView(imageView)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        Log.e("PhotoGallery", "Base64 디코딩 실패: ${e.message}")
//                    }
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "이미지 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
//            }
//
//
//        // 🔹 시간 선택 버튼
//        btnPickTime.setOnClickListener {
//            val now = Calendar.getInstance()
//            val hour = now.get(Calendar.HOUR_OF_DAY)
//            val min = now.get(Calendar.MINUTE)
//            val docRef = doc.reference
//            imageView.setOnLongClickListener {
//                AlertDialog.Builder(this)
//                    .setTitle("사진 삭제")
//                    .setMessage("이 사진을 삭제할까요?")
//                    .setPositiveButton("삭제") { _, _ ->
//                        docRef.delete()
//                    }
//                    .setNegativeButton("취소", null)
//                    .show()
//                true
//            }
//
//            TimePickerDialog(this, { _, h, m ->
//                selectedTime = String.format("%02d:%02d", h, m)
//                tvSelectedTime.text = "선택된 시간: $selectedTime"
//            }, hour, min, true).show()
//        }
//
//        // 🔹 저장 버튼
//        btnSaveTime.setOnClickListener {
//            db.collection("users").document(email)
//                .update("photoTime", selectedTime)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "사진 저장 시간이 설정되었습니다", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, "시간 저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//}
package com.example.farm__

import android.content.Intent
import android.util.Log
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import androidx.appcompat.app.AlertDialog
import android.util.Base64
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import android.graphics.Color


class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var galleryLayout: LinearLayout
    private lateinit var tvSelectedTime: TextView
    private var selectedTime: String = "08:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        galleryLayout = findViewById(R.id.galleryLayout)
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        val btnSaveTime = findViewById<Button>(R.id.btnSaveTime)

        val email = auth.currentUser?.email ?: return

        // 🔹 저장된 시간 불러오기
        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { snapshot ->
                val savedTime = snapshot.getString("photoTime")
                if (!savedTime.isNullOrBlank()) {
                    selectedTime = savedTime
                    tvSelectedTime.text = "선택된 시간: $selectedTime"
                }
            }

        // 🔹 사진 리스트 불러오기
        db.collection("users").document(email)
            .collection("gallery")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val base64 = doc.getString("imageBase64") ?: continue
                    val timestamp = doc.getTimestamp("timestamp")
                    val dateText = timestamp?.toDate()?.let {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it)
                    } ?: "시간 정보 없음"

                    val pureBase64 = base64.substringAfter("base64,", "").replace("\\s".toRegex(), "")
                    val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    // 🔹 카드 레이아웃 생성
                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 0, 0, 32) }
                    }

                    // 🔹 시간 표시 텍스트
                    val timestampView = TextView(this).apply {
                        text = dateText
                        textSize = 16f
                        setTextColor(Color.parseColor("#3E803F"))
                    }

                    // 🔹 이미지 뷰
                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400
                        )
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageBitmap(bitmap)
                    }

                    // 🔹 삭제 버튼
                    val deleteButton = Button(this).apply {
                        text = "삭제"
                        setBackgroundColor(Color.parseColor("#FFCDD2"))
                        setTextColor(Color.BLACK)
                        setOnClickListener {
                            doc.reference.delete()
                        }
                    }

                    card.addView(timestampView)
                    card.addView(imageView)
                    card.addView(deleteButton)

                    galleryLayout.addView(card)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "이미지 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        // 🔹 시간 선택 버튼
        btnPickTime.setOnClickListener {
            val now = Calendar.getInstance()
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val min = now.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                tvSelectedTime.text = "선택된 시간: $selectedTime"
            }, hour, min, true).show()
        }

        // 🔹 저장 버튼
        btnSaveTime.setOnClickListener {
            db.collection("users").document(email)
                .update("photoTime", selectedTime)
                .addOnSuccessListener {
                    Toast.makeText(this, "사진 저장 시간이 설정되었습니다", Toast.LENGTH_SHORT).show()

                    // ✅ FarmDashboardActivity의 타이머 재등록
                    sendBroadcast(Intent("RELOAD_PHOTO_TIMER"))
                    val intent = Intent("RELOAD_PHOTO_TIMER")
                    sendBroadcast(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "시간 저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
