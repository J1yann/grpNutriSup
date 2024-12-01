package com.myapp.grpnutrisup

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editTextAge: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextAllergens: EditText
    private lateinit var editTextWeightGoal: EditText
    private lateinit var autoCompleteGoal: AutoCompleteTextView
    private lateinit var autoCompleteWeeklyChange: AutoCompleteTextView
    private lateinit var autoCompleteActivityLevel: AutoCompleteTextView
    private lateinit var buttonSaveProfile: Button
    private lateinit var buttonBack: Button
    private lateinit var loadingDialog: Dialog
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var gender: String = "male" // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        editTextAge = findViewById(R.id.editTextAge)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextAllergens = findViewById(R.id.editTextAllergens)
        editTextWeightGoal = findViewById(R.id.editTextWeightGoal)
        autoCompleteGoal = findViewById(R.id.spinnerGoal)
        autoCompleteWeeklyChange = findViewById(R.id.spinnerWeeklyWeightChange)
        autoCompleteActivityLevel = findViewById(R.id.spinnerActivityLevel)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)
        buttonBack = findViewById(R.id.buttonBack)

        // Set up the loading dialog
        setupLoadingDialog()

        // Set up the dropdown menus
        setupDropdowns()

        // Load user profile data from Firestore
        loadProfileData()

        // Save profile data when button is clicked
        buttonSaveProfile.setOnClickListener {
            saveProfileData()
        }

        // Back button functionality
        buttonBack.setOnClickListener {
            finish() // Close the current activity and return to the previous one
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading) // Use your custom loading layout
        loadingDialog.setCancelable(false) // Prevent dismissing by clicking outside
    }

    private fun setupDropdowns() {
        val goals = arrayOf("Maintain Weight", "Lose Weight", "Gain Weight")
        val weeklyWeightChanges = arrayOf("0.25kg", "0.5kg", "0.75kg")
        val activityLevels = arrayOf(
            "Sedentary (little or no exercise)",
            "Lightly Active (light exercise/sports 1-3 days/week)",
            "Moderately Active (moderate exercise/sports 3-5 days/week)",
            "Very Active (hard exercise/sports 6-7 days a week)",
            "Extra Active (very hard exercise/physical job)"
        )

        // Set up Goal dropdown
        val goalAdapter = ArrayAdapter(this, R.layout.dropdown_item, goals)
        autoCompleteGoal.setAdapter(goalAdapter)

        // Set up Weekly Weight Change dropdown
        val weeklyChangeAdapter = ArrayAdapter(this, R.layout.dropdown_item, weeklyWeightChanges)
        autoCompleteWeeklyChange.setAdapter(weeklyChangeAdapter)

        // Set up Activity Level dropdown
        val activityAdapter = ArrayAdapter(this, R.layout.dropdown_item, activityLevels)
        autoCompleteActivityLevel.setAdapter(activityAdapter)

        // Handle Goal selection to enable/disable fields
        autoCompleteGoal.setOnItemClickListener { _, _, position, _ ->
            val selectedGoal = goals[position]
            if (selectedGoal == "Maintain Weight") {
                autoCompleteWeeklyChange.isEnabled = false
                autoCompleteWeeklyChange.setText(weeklyWeightChanges[0], false)
                editTextWeightGoal.isEnabled = false
                editTextWeightGoal.setText("")
            } else {
                autoCompleteWeeklyChange.isEnabled = true
                editTextWeightGoal.isEnabled = true
            }
        }
    }

    private fun loadProfileData() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Populate fields with user's data
                        editTextAge.setText(document.getLong("age")?.toString() ?: "")
                        editTextHeight.setText(document.getDouble("height")?.toString() ?: "")
                        editTextWeight.setText(document.getDouble("weight")?.toString() ?: "")

                        // Retrieve the allergens as a list of strings
                        val allergensList = document.get("allergens") as? List<String> ?: emptyList()
                        val allergensString = allergensList.joinToString(", ")
                        editTextAllergens.setText(allergensString)

                        // Set dropdown values
                        val goal = document.getString("goal") ?: "Maintain Weight"
                        val weeklyWeightChange = document.getDouble("weeklyWeightChange") ?: 0.25
                        val activityLevel = document.getString("activityLevel") ?: "Sedentary (little or no exercise)"
                        gender = document.getString("gender") ?: "male"

                        // Set text for dropdowns
                        autoCompleteGoal.setText(goal, false)
                        autoCompleteActivityLevel.setText(activityLevel, false)

                        // Set Weekly Weight Change based on value
                        val weeklyChangeText = when (weeklyWeightChange) {
                            0.25 -> "0.25kg"
                            0.5 -> "0.5kg"
                            0.75 -> "0.75kg"
                            else -> "0.25kg"
                        }
                        autoCompleteWeeklyChange.setText(weeklyChangeText, false)

                        // Handle weight goal field
                        if (goal == "Maintain Weight") {
                            autoCompleteWeeklyChange.isEnabled = false
                            editTextWeightGoal.isEnabled = false
                            editTextWeightGoal.setText("")
                        } else {
                            autoCompleteWeeklyChange.isEnabled = true
                            editTextWeightGoal.isEnabled = true
                            editTextWeightGoal.setText(document.getDouble("weightGoal")?.toString() ?: "")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileData() {
        val userEmail = auth.currentUser?.email

        if (userEmail != null) {
            val age = editTextAge.text.toString().toIntOrNull() ?: 0
            val height = editTextHeight.text.toString().toDoubleOrNull() ?: 0.0
            val weight = editTextWeight.text.toString().toDoubleOrNull() ?: 0.0
            val weightGoal = editTextWeightGoal.text.toString().toDoubleOrNull() ?: weight

            // Convert the allergens string back to a list of strings
            val allergensString = editTextAllergens.text.toString()
            val allergensList = allergensString.split(",").map { it.trim() }

            val goal = autoCompleteGoal.text.toString()
            val weeklyWeightChange = when (autoCompleteWeeklyChange.text.toString()) {
                "0.25kg" -> 0.25
                "0.5kg" -> 0.5
                "0.75kg" -> 0.75
                else -> 0.25
            }

            val activityLevel = autoCompleteActivityLevel.text.toString()

            // Calculate BMR (Basal Metabolic Rate) based on gender
            val bmr = calculateBMR(age, height, weight, gender)

            // Calculate TDEE (Total Daily Energy Expenditure)
            val tdee = calculateTDEE(bmr, activityLevel)

            // Adjust calories based on weight goal
            val adjustedCalories = adjustCaloriesForGoal(tdee, weight, weightGoal, weeklyWeightChange)

            // Prepare profile data to save
            val profileData = mapOf(
                "age" to age,
                "height" to height,
                "weight" to weight,
                "allergens" to allergensList, // Save as a list
                "goal" to goal,
                "weeklyWeightChange" to if (goal != "Maintain Weight") weeklyWeightChange else 0.0,
                "activityLevel" to activityLevel,
                "gender" to gender,
                "BMR" to bmr,  // Save BMR
                "TDEE" to tdee, // Save TDEE
                "calorieResult" to adjustedCalories, // Save adjusted calorie result
                "weightGoal" to weightGoal // Save weight goal
            )

            // Show loading dialog
            loadingDialog.show()

            // Update Firestore
            db.collection("users").document(userEmail)
                .update(profileData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    // Send broadcast to notify other parts of the app
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("profile_updated"))

                    // Delay before navigating back to ProfileActivity
                    Handler().postDelayed({
                        loadingDialog.dismiss() // Dismiss the loading dialog
                        goToProfileActivity()
                    }, 2000) // 2000 milliseconds delay (2 seconds)
                }
                .addOnFailureListener { e ->
                    loadingDialog.dismiss() // Dismiss the loading dialog
                    Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish() // Optionally finish the EditProfileActivity
    }

    private fun calculateBMR(age: Int, height: Double, weight: Double, gender: String): Double {
        return if (gender == "male") {
            66.47 + (13.75 * weight) + (5.003 * height) - (6.755 * age)
        } else {
            655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        }
    }

    private fun calculateTDEE(bmr: Double, activityLevel: String): Double {
        val activityMultiplier = when (activityLevel) {
            "Sedentary (little or no exercise)" -> 1.2
            "Lightly Active (light exercise/sports 1-3 days/week)" -> 1.375
            "Moderately Active (moderate exercise/sports 3-5 days/week)" -> 1.55
            "Very Active (hard exercise/sports 6-7 days a week)" -> 1.725
            "Extra Active (very hard exercise/physical job)" -> 1.9
            else -> 1.2
        }
        return bmr * activityMultiplier
    }

    private fun adjustCaloriesForGoal(tdee: Double, weight: Double, weightGoal: Double, weeklyWeightChange: Double): Double {
        val calorieDeficit = when (weeklyWeightChange) {
            0.25 -> 250
            0.5 -> 500
            0.75 -> 750
            else -> 0
        }

        // If the goal is to lose weight, subtract the deficit
        return if (weightGoal < weight) {
            tdee - calorieDeficit
        } else {
            tdee + calorieDeficit // If the goal is to gain weight, add the surplus
        }
    }
}
