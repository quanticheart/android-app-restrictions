package com.example.android.apprestrictions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.RestrictionEntry
import android.os.Bundle
import android.os.UserManager
import androidx.preference.*
import java.util.*

/**
 * This fragment is included in `CustomRestrictionsActivity`.  It demonstrates how an app
 * can integrate its own custom app restriction settings with the restricted profile feature.
 *
 *
 * This sample app maintains custom app restriction settings in shared preferences.  Your app
 * can use other methods to maintain the settings.  When this activity is invoked
 * (from Settings > Users > Restricted Profile), the shared preferences are used to initialize
 * the custom configuration on the user interface.
 *
 *
 * Three sample input types are shown: checkbox, single-choice, and multi-choice.  When the
 * settings are modified by the user, the corresponding restriction entries are saved in the
 * platform.  The saved restriction entries are retrievable when the app is launched under a
 * restricted profile.
 */
class CustomRestrictionsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {
    private var mRestrictions: MutableList<RestrictionEntry>? = null
    private var mRestrictionsBundle: Bundle? = null

    // Shared preferences for each of the sample input types.
    private var mBooleanPref: CheckBoxPreference? = null
    private var mChoicePref: ListPreference? = null
    private var mMultiPref: MultiSelectListPreference? = null

    // Restriction entries for each of the sample input types.
    private var mBooleanEntry: RestrictionEntry? = null
    private var mChoiceEntry: RestrictionEntry? = null
    private var mMultiEntry: RestrictionEntry? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.custom_prefs)

        // This sample app uses shared preferences to maintain app restriction settings.  Your app
        // can use other methods to maintain the settings.
        mBooleanPref = findPreference(KEY_BOOLEAN_PREF)
        mChoicePref = findPreference(KEY_CHOICE_PREF)
        mMultiPref = findPreference(KEY_MULTI_PREF)
        mBooleanPref!!.onPreferenceChangeListener = this
        mChoicePref!!.onPreferenceChangeListener = this
        mMultiPref!!.onPreferenceChangeListener = this
    }

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity? = activity

        // BEGIN_INCLUDE (GET_CURRENT_RESTRICTIONS)
        // Existing app restriction settings, if exist, can be retrieved from the Bundle.
        mRestrictionsBundle = activity!!.intent.getBundleExtra(Intent.EXTRA_RESTRICTIONS_BUNDLE)
        if (mRestrictionsBundle == null) {
            mRestrictionsBundle = (activity.getSystemService(Context.USER_SERVICE) as UserManager)
                .getApplicationRestrictions(activity.packageName)
        }
        if (mRestrictionsBundle == null) {
            mRestrictionsBundle = Bundle()
        }
        mRestrictions = activity.intent.getParcelableArrayListExtra(
            Intent.EXTRA_RESTRICTIONS_LIST
        )
        // END_INCLUDE (GET_CURRENT_RESTRICTIONS)

        // Transfers the saved values into the preference hierarchy.
        mRestrictions?.forEach { entry ->
            when (entry.key) {
                GetRestrictionsReceiver.KEY_BOOLEAN -> {
                    mBooleanPref!!.isChecked = entry.selectedState
                    mBooleanEntry = entry
                }
                GetRestrictionsReceiver.KEY_CHOICE -> {
                    mChoicePref!!.value = entry.selectedString
                    mChoiceEntry = entry
                }
                GetRestrictionsReceiver.KEY_MULTI_SELECT -> {
                    val set = HashSet<String>()
                    Collections.addAll(set, *entry.allSelectedStrings)
                    mMultiPref!!.values = set
                    mMultiEntry = entry
                }
            }
        } ?: run {
            mRestrictions = ArrayList()

            // Initializes the boolean restriction entry and updates its corresponding shared
            // preference value.
            mBooleanEntry = RestrictionEntry(
                GetRestrictionsReceiver.KEY_BOOLEAN,
                mRestrictionsBundle!!.getBoolean(GetRestrictionsReceiver.KEY_BOOLEAN, false)
            )
            mBooleanEntry!!.type = RestrictionEntry.TYPE_BOOLEAN
            mBooleanPref!!.isChecked = mBooleanEntry!!.selectedState

            // Initializes the single choice restriction entry and updates its corresponding
            // shared preference value.
            mChoiceEntry = RestrictionEntry(
                GetRestrictionsReceiver.KEY_CHOICE,
                mRestrictionsBundle!!.getString(GetRestrictionsReceiver.KEY_CHOICE)
            )
            mChoiceEntry!!.type = RestrictionEntry.TYPE_CHOICE
            mChoicePref!!.value = mChoiceEntry!!.selectedString

            // Initializes the multi-select restriction entry and updates its corresponding
            // shared preference value.
            mMultiEntry = RestrictionEntry(
                GetRestrictionsReceiver.KEY_MULTI_SELECT,
                mRestrictionsBundle!!.getStringArray(
                    GetRestrictionsReceiver.KEY_MULTI_SELECT
                )
            )
            mMultiEntry!!.type = RestrictionEntry.TYPE_MULTI_SELECT
            if (mMultiEntry!!.allSelectedStrings != null) {
                val set = HashSet<String>()
                val values = mRestrictionsBundle!!.getStringArray(
                    GetRestrictionsReceiver.KEY_MULTI_SELECT
                )
                if (values != null) {
                    Collections.addAll(set, *values)
                }
                mMultiPref!!.values = set
            }
            mRestrictions?.add(mBooleanEntry!!)
            mRestrictions?.add(mChoiceEntry!!)
            mRestrictions?.add(mMultiEntry!!)
        }
        // Prepares result to be passed back to the Settings app when the custom restrictions
        // activity finishes.
        val intent = Intent(requireActivity().intent)
        intent.putParcelableArrayListExtra(
            Intent.EXTRA_RESTRICTIONS_LIST,
            mRestrictions?.let { ArrayList(it) }
        )
        requireActivity().setResult(Activity.RESULT_OK, intent)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference === mBooleanPref) {
            mBooleanEntry!!.selectedState = (newValue as Boolean)
        } else if (preference === mChoicePref) {
            mChoiceEntry!!.selectedString = newValue as String
        } else if (preference === mMultiPref && newValue is Set<*>) {
            // newValue is a Set<String>, skip the lint warning.
            val selectedStrings = arrayOfNulls<String>(newValue.size)
            var i = 0
            for (value in newValue) {
                selectedStrings[i++] = value as String
            }
            mMultiEntry!!.allSelectedStrings = selectedStrings
        }

        // Saves all the app restriction configuration changes from the custom activity.
        val intent = Intent(requireActivity().intent)
        intent.putParcelableArrayListExtra(
            Intent.EXTRA_RESTRICTIONS_LIST,
            mRestrictions?.let { ArrayList(it) }
        )
        requireActivity().setResult(Activity.RESULT_OK, intent)
        return true
    }

    companion object {
        // Shared preference key for the boolean restriction.
        private const val KEY_BOOLEAN_PREF = "pref_boolean"

        // Shared preference key for the single-select restriction.
        private const val KEY_CHOICE_PREF = "pref_choice"

        // Shared preference key for the multi-select restriction.
        private const val KEY_MULTI_PREF = "pref_multi"
    }
}