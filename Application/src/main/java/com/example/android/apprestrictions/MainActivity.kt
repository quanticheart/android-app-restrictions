package com.example.android.apprestrictions

import android.os.Bundle
import android.os.UserManager
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

/**
 * This is the main user interface of the App Restrictions sample app.  It demonstrates the use
 * of the App Restriction feature, which is available on Android 4.3 and above tablet devices
 * with the multi-user feature.
 *
 *
 * When launched under the primary User account, you can toggle between standard app restriction
 * types and custom.  When launched under a restricted profile, this activity displays app
 * restriction settings, if available.
 *
 *
 * Follow these steps to exercise the feature:
 * 1. If this is the primary user, go to Settings > Users.
 * 2. Create a restricted profile, if one doesn't exist already.
 * 3. Open the profile settings, locate the sample app, and tap the app restriction settings
 * icon. Configure app restrictions for the app.
 * 4. In the lock screen, switch to the user's restricted profile, launch this sample app,
 * and see the configured app restrictions displayed.
 */
class MainActivity : AppCompatActivity() {
    // Checkbox to indicate whether custom or standard app restriction types are selected.
    private var mCustomConfig: CheckBox? = null
    private var mMultiEntryValue: TextView? = null
    private var mChoiceEntryValue: TextView? = null
    private var mBooleanEntryValue: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets up  user interface elements.
        setContentView(R.layout.main)
        mCustomConfig = findViewById(R.id.custom_app_limits)
//        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
//        val customChecked = prefs.getBoolean(CUSTOM_CONFIG_KEY, false)
//        if (customChecked) mCustomConfig?.isChecked = true
        mMultiEntryValue = findViewById(R.id.multi_entry_id)
        mChoiceEntryValue = findViewById(R.id.choice_entry_id)
        mBooleanEntryValue = findViewById(R.id.boolean_entry_id)
    }

    override fun onResume() {
        super.onResume()

        // If app restrictions are set for this package, when launched from a restricted profile,
        // the settings are available in the returned Bundle as key/value pairs.
        var restrictionsBundle: Bundle?
        val userManager = getSystemService(USER_SERVICE) as UserManager
        restrictionsBundle = userManager.getApplicationRestrictions(packageName)
        if (restrictionsBundle == null) {
            restrictionsBundle = Bundle()
        }

        // Reads and displays values from a boolean type restriction entry, if available.
        // An app can utilize these settings to restrict its content under a restricted profile.
        val booleanRestrictionValue =
            if (restrictionsBundle.containsKey(GetRestrictionsReceiver.KEY_BOOLEAN)) restrictionsBundle.getBoolean(
                GetRestrictionsReceiver.KEY_BOOLEAN
            ).toString() + "" else getString(R.string.na)
        mBooleanEntryValue!!.text = booleanRestrictionValue

        // Reads and displays values from a single choice restriction entry, if available.
        val singleChoiceRestrictionValue =
            if (restrictionsBundle.containsKey(GetRestrictionsReceiver.KEY_CHOICE)) restrictionsBundle.getString(
                GetRestrictionsReceiver.KEY_CHOICE
            ) else getString(R.string.na)
        mChoiceEntryValue!!.text = singleChoiceRestrictionValue

        // Reads and displays values from a multi-select restriction entry, if available.
        val multiSelectValues =
            restrictionsBundle.getStringArray(GetRestrictionsReceiver.KEY_MULTI_SELECT)
        if (multiSelectValues == null || multiSelectValues.isEmpty()) {
            mMultiEntryValue!!.text = getString(R.string.na)
        } else {
            val builder = StringBuilder()
            for (value in multiSelectValues) {
                builder.append(value)
                builder.append(" ")
            }
            mMultiEntryValue!!.text = builder.toString()
        }
    }

    /**
     * Saves custom app restriction to the shared preference.
     *
     *
     * This flag is used by `GetRestrictionsReceiver` to determine if a custom app
     * restriction activity should be used.
     */
    fun onCustomClicked() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit()
            .putBoolean(CUSTOM_CONFIG_KEY, mCustomConfig!!.isChecked)
            .apply()
    }

    companion object {
        const val CUSTOM_CONFIG_KEY = "custom_config"
    }
}