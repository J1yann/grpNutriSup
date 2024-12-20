package com.myapp.grpnutrisup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myapp.grpnutrisup.R
import com.myapp.grpnutrisup.adapters.MealHistoryAdapter
import com.myapp.grpnutrisup.models.Meal
import com.myapp.grpnutrisup.models.MealGroup
import java.text.SimpleDateFormat
import java.util.*

class MealHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mealHistoryAdapter: MealHistoryAdapter
    private val groupedMealsList = mutableListOf<MealGroup>()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_history)

        // Back Button setup
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerViewMeals)
        mealHistoryAdapter = MealHistoryAdapter(this, groupedMealsList)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MealHistoryActivity)
            adapter = mealHistoryAdapter
        }

        loadMealHistory()
    }

    private fun loadMealHistory() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("eatenFoods")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val mealsMap = mutableMapOf<String, MealGroup>()

                    for (document in querySnapshot.documents) {
                        val meal = document.toObject(Meal::class.java)
                        meal?.let {
                            // Group by date (formatted date string)
                            val dateString = formatDate(meal.timestamp?.toDate())
                            val group = mealsMap.getOrPut(dateString) { MealGroup(dateString) }

                            // Add meal to the correct date group and sum calories
                            group.meals.add(meal)
                            group.totalCalories += meal.calories
                        }
                    }

                    groupedMealsList.clear()
                    groupedMealsList.addAll(mealsMap.values.sortedByDescending { it.date })
                    mealHistoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load meal history", Toast.LENGTH_SHORT).show()
                    Log.e("MealHistoryActivity", "Error loading meal history", e)
                }
        } else {
            Toast.makeText(this, "Please log in to view meal history", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(date: Date?): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return date?.let { dateFormat.format(it) } ?: ""
    }
}