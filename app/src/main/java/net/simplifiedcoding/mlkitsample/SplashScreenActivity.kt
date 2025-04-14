package net.simplifiedcoding.mlkitsample.facedetector

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import net.simplifiedcoding.mlkitsample.MainActivity
import net.simplifiedcoding.mlkitsample.R

class SplashScreenActivity : AppCompatActivity() {

    private val splashDelay: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close SplashScreenActivity
        }, splashDelay)
    }
}
