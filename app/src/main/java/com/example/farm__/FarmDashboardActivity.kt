//package com.example.farm__
//
//import android.os.Bundle
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import android.graphics.Color
//import android.widget.ProgressBar
//class FarmDashboardActivity : AppCompatActivity() {
//
//
//    private lateinit var tvFarmName: TextView
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    private lateinit var tvTemperature: TextView
//    private lateinit var pbTemperature: ProgressBar
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_farm_dashboard)
//        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
//
//        val db = FirebaseFirestore.getInstance()
//        val tvFarmName = findViewById<TextView>(R.id.tvFarmName)
//
//        db.collection("users")
//            .document(email)
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val farmName = snapshot.getString("farmName") ?: "내 스마트팜"
//                tvFarmName.text = farmName
//            }
//            .addOnFailureListener {
//                tvFarmName.text = "스마트팜 정보 없음"
//            }
//
//
////        // 🔹 Firebase 초기화
////        auth = FirebaseAuth.getInstance()
////        db = FirebaseFirestore.getInstance()
//
//        // 🔹 뷰 초기화
//        tvTemperature = findViewById(R.id.tvTemperature)
//        pbTemperature = findViewById(R.id.pbTemperature)
//
//        // 🔹 Firestore에서 실시간 온도 구독
//        val uid = auth.currentUser?.uid ?: return
//
//        db.collection("users")
//            .document(uid)
//            .collection("sensors")
//            .document("latest")
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
//
//                val temperature = snapshot.getDouble("temperature") ?: return@addSnapshotListener
//                updateTemperatureUI(temperature)
//            }
//    }
//
//    private fun updateTemperatureUI(temp: Double) {
//        val progress = temp.toInt().coerceIn(0, 50)
//        pbTemperature.progress = progress
//        tvTemperature.text = "온도: ${"%.1f".format(temp)}°C"
//
//        val color = when {
//            temp < 10 -> Color.parseColor("#42A5F5") // 파랑
//            temp < 25 -> Color.parseColor("#66BB6A") // 초록
//            else -> Color.parseColor("#EF5350") // 빨강
//        }
//
//        pbTemperature.progressTintList = android.content.res.ColorStateList.valueOf(color)
//    }
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_farm_dashboard)
////
////        tvFarmName = findViewById(R.id.tvFarmName)
////
////        val uid = auth.currentUser?.uid ?: return
////
////        db.collection("users").document(uid).get()
////            .addOnSuccessListener { doc ->
////                val farmName = doc.getString("farmName") ?: "내 팜"
////                tvFarmName.text = farmName
////            }
////    }
//
//}
package com.example.farm__

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.Query
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Bitmap
import android.util.Log
import android.os.Handler
import android.os.Looper
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.Context.RECEIVER_NOT_EXPORTED


import androidx.appcompat.app.AlertDialog



class FarmDashboardActivity : AppCompatActivity() {

    private lateinit var tvFarmName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var pbTemperature: ProgressBar
    private lateinit var tvHumidity: TextView
    private lateinit var pbHumidity: ProgressBar
    private lateinit var tvSoil: TextView
    private lateinit var pbSoil: ProgressBar
    private lateinit var tvLight: TextView
    private lateinit var pbLight: ProgressBar
    private lateinit var auth: FirebaseAuth
    private val email: String by lazy {
        FirebaseAuth.getInstance().currentUser?.email ?: ""
    }
    private lateinit var imgLatestPhoto: ImageView
    private var photoCopyHandler: Handler? = null
    private var photoCopyRunnable: Runnable? = null
    private lateinit var tvPumpStatus: TextView
    private lateinit var btnPumpIndicator: Button




    private lateinit var db: FirebaseFirestore
    private fun base64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farm_dashboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        // 🔹 Firebase 초기화
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val imgLatestPhoto = findViewById<ImageView>(R.id.imgLatestPhoto)
        //val email = auth.currentUser?.email ?: return

        //imgLatestPhoto = findViewById(R.id.imgLatestPhoto)
        startPollingLatestPhoto()
        schedulePhotoCopyAtUserTime()
        val reloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "RELOAD_PHOTO_TIMER") {
                    schedulePhotoCopyAtUserTime()
                }
            }
        }

// Android 13 이상에서 반드시 export 여부 지정 필요!
        registerReceiver(reloadReceiver, IntentFilter("RELOAD_PHOTO_TIMER"), RECEIVER_NOT_EXPORTED)





        db.collection("users").document(email)
            .collection("photos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val base64 = docs.first().getString("imageBase64")
                    base64?.let {
                        // 🔹 base64 문자열 정리: 줄바꿈 제거 + null 방지
                        val pureBase64 = it.substringAfter("base64,", "").replace("\\s".toRegex(), "")
                        if (pureBase64.isNotBlank()) {
                            try {
                                val bitmap = base64ToBitmap(pureBase64)
                                imgLatestPhoto.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(this, "이미지를 디코딩할 수 없습니다", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("FarmDashboard", "Base64 문자열이 비어 있거나 잘못됨")
                        }
                    }
                } else {
                    Log.d("FarmDashboard", "사진 문서 없음")
                }
            }
            .addOnFailureListener {
                Log.e("FarmDashboard", "Firestore 이미지 로드 실패: ${it.message}")
            }


        imgLatestPhoto.setOnClickListener {
            startActivity(Intent(this, PhotoGalleryActivity::class.java))
        }




        // 🔹 뷰 초기화
        tvFarmName = findViewById(R.id.tvFarmName)
        tvTemperature = findViewById(R.id.tvTemperature)
        pbTemperature = findViewById(R.id.pbTemperature)
        tvHumidity = findViewById(R.id.tvHumidity)
        pbHumidity = findViewById(R.id.pbHumidity)
        tvSoil = findViewById(R.id.tvSoil)
        pbSoil = findViewById(R.id.pbSoil)
        tvLight = findViewById(R.id.tvLight)
        pbLight = findViewById(R.id.pbLight)
        tvPumpStatus = findViewById(R.id.tvPumpStatus)
        btnPumpIndicator = findViewById(R.id.btnPumpIndicator)


        // 🔹 로그인된 사용자 이메일로 문서 접근
        //val email = auth.currentUser?.email ?: return

        // 🔹 farmName 불러오기
        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { snapshot ->
                val farmName = snapshot.getString("farmName") ?: "내 스마트팜"
                tvFarmName.text = farmName
            }
            .addOnFailureListener {
                tvFarmName.text = "스마트팜 정보 없음"
            }




        // 🔹 실시간 온도 수신
//        db.collection("users")
//            .document(email)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
//
//                val temp = snapshot.getDouble("temperature") ?: return@addSnapshotListener
//                updateTemperatureUI(temp)
//            }
        db.collection("users").document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val temp = snapshot.getDouble("temperature")
                val humidity = snapshot.getDouble("humidity")
                val soil = snapshot.getDouble("soilMoisture")
                val light = snapshot.getDouble("light")
                val pump = snapshot.getString("pumpStatus") ?: "unknown"



                temp?.let { updateTemperatureUI(it) }
                humidity?.let { updateHumidityUI(it) }
                soil?.let { updateSoilUI(it) }
                light?.let { updateLightUI(it) }
                tvPumpStatus.text = "펌프 상태: ${pump.uppercase()}"

                if (pump == "on") {
                    btnPumpIndicator.setBackgroundColor(Color.parseColor("#66BB6A")) // 초록
                } else {
                    btnPumpIndicator.setBackgroundColor(Color.parseColor("#EF5350")) // 빨강
                }
            }


    }

    private fun updateTemperatureUI(temp: Double) {
        val progress = temp.toInt().coerceIn(0, 50)
        pbTemperature.progress = progress
        tvTemperature.text = "온도: %.1f°C".format(temp)

        val color = when {
            temp < 10 -> Color.parseColor("#42A5F5") // 파랑
            temp < 25 -> Color.parseColor("#66BB6A") // 초록
            else -> Color.parseColor("#EF5350") // 빨강
        }

        pbTemperature.progressTintList = ColorStateList.valueOf(color)
    }
    private fun updateHumidityUI(value: Double) {
        val clamped = value.coerceIn(0.0, 100.0)
        pbHumidity.progress = clamped.toInt()
        tvHumidity.text = "습도: %.1f%%".format(clamped)

        val alpha = (clamped * 255 / 100).toInt().coerceIn(0, 255)
        val color = Color.argb(255, 0, 105, alpha)
        pbHumidity.progressTintList = ColorStateList.valueOf(color)
    }


    private fun updateSoilUI(value: Double) {
        val clamped = value.coerceIn(0.0, 100.0)
        pbSoil.progress = clamped.toInt()
        tvSoil.text = "토양 수분: %.1f%%".format(clamped)
        pbSoil.progressTintList = ColorStateList.valueOf(Color.parseColor("#8BC34A"))
    }


    private fun updateLightUI(value: Double) {
        val lx = value.toInt().coerceIn(0, 1000)
        val percent = (lx * 100 / 1000).coerceIn(0, 100)
        pbLight.progress = percent
        tvLight.text = "조도: ${lx}lx"

        // 밝기에 따라 회색 → 흰색으로
        val brightness = (percent * 255 / 100).coerceIn(0, 255)
        val color = Color.rgb(brightness, brightness, brightness)
        pbLight.progressTintList = ColorStateList.valueOf(color)
    }

//    private fun startPollingLatestPhoto() {
//        val handler = Handler(Looper.getMainLooper())
//        val runnable = object : Runnable {
//            override fun run() {
//                //val email = auth.currentUser?.email ?: return
//                db.collection("users").document(email)
//                    .collection("latestPhoto")
//                    .document("current")
//                    .get()
//                    .addOnSuccessListener { doc ->
//                        val base64 = doc.getString("imageBase64")
//                        base64?.let {
//                            val pure = it.substringAfter("base64,", "").replace("\\s".toRegex(), "")
//                            try {
//                                val bitmap = base64ToBitmap(pure)
//                                imgLatestPhoto.setImageBitmap(bitmap)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                handler.postDelayed(this, 5 * 60 * 1000)
//            }
//        }
//        handler.post(runnable)
//    }
private fun startPollingLatestPhoto() {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return

            FirebaseFirestore.getInstance().collection("users")
                .document(email)
                .collection("photos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val base64 = docs.first().getString("imageBase64")
                        base64?.let {
                            val pure = it.substringAfter("base64,", "").replace("\\s".toRegex(), "")
                            val decoded = Base64.decode(pure, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                            findViewById<ImageView>(R.id.imgLatestPhoto).setImageBitmap(bitmap)
                        }
                    }
                }

            handler.postDelayed(this, 30 * 1000)
        }
    }

    handler.post(runnable)
}

    private fun schedulePhotoCopyAtUserTime() {
        photoCopyHandler?.removeCallbacks(photoCopyRunnable!!) // 이전 타이머 제거

        photoCopyHandler = Handler(Looper.getMainLooper())
        val handler = photoCopyHandler!!

        db.collection("users").document(email).get()
            .addOnSuccessListener { userDoc ->
                val timeStr = userDoc.getString("photoTime") ?: return@addOnSuccessListener
                val (hour, min) = timeStr.split(":").map { it.toInt() }

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                }

                // 다음 실행까지 남은 시간 계산
                var delayMillis = cal.timeInMillis - System.currentTimeMillis()
                if (delayMillis < 0) {
                    // 이미 오늘 시간이 지났으면 내일로 예약
                    delayMillis += 24 * 60 * 60 * 1000
                }

                photoCopyRunnable = Runnable {
                    copyLatestPhotoToGallery(email)
                    // 내일 다시 등록
                    schedulePhotoCopyAtUserTime()
                }

                handler.postDelayed(photoCopyRunnable!!, delayMillis)
            }
    }

    private fun copyLatestPhotoToGallery(email: String) {
        db.collection("users").document(email)
            .collection("photos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.first()
                    val base64 = doc.getString("imageBase64") ?: return@addOnSuccessListener
                    val timestamp = Timestamp.now()
                    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val galleryRef = db.collection("users").document(email)
                        .collection("gallery").document(dateKey)

                    galleryRef.set(
                        mapOf(
                            "imageBase64" to base64,
                            "timestamp" to timestamp
                        )
                    )
                }
            }
    }



//    private fun copyLatestPhotoToGallery(email: String) {
//        val latestRef = db.collection("users").document(email)
//            .collection("latestPhoto").document("current")
//
//        latestRef.get().addOnSuccessListener { doc ->
//            val base64 = doc.getString("imageBase64") ?: return@addOnSuccessListener
//            val timestamp = Timestamp.now()
//            val dateKey = SimpleDateFormat("yyyy-MM-dd_HH:mm", Locale.getDefault()).format(Date())
//
//            val galleryRef = db.collection("users").document(email)
//                .collection("photos").document(dateKey)
//
//            galleryRef.set(
//                mapOf(
//                    "imageBase64" to base64,
//                    "timestamp" to timestamp
//                )
//            )
//        }
//    }



}
