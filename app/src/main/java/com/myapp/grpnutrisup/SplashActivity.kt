package com.myapp.grpnutrisup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set window flags for smooth transition
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                
        setContentView(R.layout.activity_splash)

        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        
        // Start with logo invisible
        logoImageView.alpha = 0f
        
        // Load and start the animation
        val splashAnimation = AnimationUtils.loadAnimation(this, R.anim.smooth_splash)
        logoImageView.startAnimation(splashAnimation)
        
        // Fade in the logo
        logoImageView.animate()
            .alpha(1f)
            .setDuration(100)
            .withEndAction {
                // Delay before starting HomeActivity
                Handler(Looper.getMainLooper()).postDelayed({
                    // Create smooth transition to next activity
                    val intent = Intent(this, HomeActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    startActivity(intent, options.toBundle())
                    finish()
                }, 1500)
            }
            .start()
    }

    override fun onBackPressed() {
        // Disable back button during splash
    }
}
