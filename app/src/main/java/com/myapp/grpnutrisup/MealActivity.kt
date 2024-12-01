package com.myapp.grpnutrisup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.myapp.grpnutrisup.adapters.MealPagerAdapter

class MealActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plan)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        bottomNavigationView = findViewById(R.id.bottom_nav_include)

        viewPager.adapter = MealPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Breakfast"
                1 -> "Lunch"
                2 -> "Dinner"
                else -> null
            }
        }.attach()

        bottomNavigationView?.selectedItemId = R.id.navigation_meal

        setupBottomNavigationBar()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                ActivityOptionsCompat.makeCustomAnimation(
                    this@MealActivity,
                    R.anim.fade_in,
                    R.anim.fade_out
                ).toBundle()
            }
        })
    }

private fun setupBottomNavigationBar() {
    bottomNavigationView?.setOnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.navigation_home -> {
                navigateTo(HomeActivity::class.java)
                true
            }
            R.id.navigation_search -> {
                navigateTo(FoodSearchActivity::class.java)
                true
            }
            R.id.navigation_activity -> {
                navigateTo(LogActivityPage::class.java)
                true
            }
            R.id.navigation_meal -> true
            R.id.navigation_profile -> {
                navigateTo(ProfileActivity::class.java)
                true
            }
            else -> false
        }
    }
}

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.fade_in,
            R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
    }

    override fun onResume() {
        super.onResume()
        // Ensure correct navigation item is selected
        findViewById<BottomNavigationView>(R.id.bottom_nav_include).selectedItemId = R.id.navigation_meal
    }
}
