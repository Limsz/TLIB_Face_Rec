package net.simplifiedcoding.mlkitsample.facedetector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import net.simplifiedcoding.mlkitsample.R

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)


        val scanAgainButton: Button = findViewById(R.id.btn_scan_again)
        val exitButton: Button = findViewById(R.id.btn_exit)

        scanAgainButton.setOnClickListener {
            startActivity(Intent(this, FaceDetectionActivity::class.java))
            finish()
        }

        exitButton.setOnClickListener {
            finishAffinity() // Closes the app
        }
    }
}
