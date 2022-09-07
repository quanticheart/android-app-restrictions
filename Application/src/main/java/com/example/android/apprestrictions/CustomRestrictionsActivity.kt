package com.example.android.apprestrictions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This activity demonstrates how an app can integrate its own custom app restriction settings
 * with the restricted profile feature.
 *
 *
 * This sample app maintains custom app restriction settings in shared preferences.  When
 * the activity is invoked (from Settings > Users), the stored settings are used to initialize
 * the custom configuration on the user interface.  Three sample input types are
 * shown: checkbox, single-choice, and multi-choice.  When the settings are modified by the user,
 * the corresponding restriction entries are saved, which are retrievable under a restricted
 * profile.
 */
class CustomRestrictionsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(androidx.appcompat.R.id.content, CustomRestrictionsFragment())
                .commitNow()
        }
    }
}