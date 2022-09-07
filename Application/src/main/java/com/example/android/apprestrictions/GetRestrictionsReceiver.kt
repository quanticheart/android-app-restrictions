package com.example.android.apprestrictions

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.RestrictionEntry
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager

class GetRestrictionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_GET_RESTRICTION_ENTRIES -> {
                // If app restriction settings are already created, they will be included in the Bundle
                // as key/value pairs.
                val existingRestrictions = intent.getBundleExtra(Intent.EXTRA_RESTRICTIONS_BUNDLE)
                Log.i(TAG, "existingRestrictions = $existingRestrictions")
                object : Thread() {
                    override fun run() {
                        createRestrictions(context, goAsync(), existingRestrictions)
                    }
                }.start()
            }
        }
    }

    private fun createRestrictions(
        context: Context, result: PendingResult,
        existingRestrictions: Bundle?
    ) {
        // The incoming restrictions bundle contains key/value pairs representing existing app
        // restrictions for this package. In order to retain existing app restrictions, you need to
        // construct new restriction entries and then copy in any existing values for the new keys.
        val newEntries = initRestrictions(context)

        // If app restrictions were not previously configured for the package, create the default
        // restrictions entries and return them.
        if (existingRestrictions == null) {
            val extras = Bundle()
            extras.putParcelableArrayList(Intent.EXTRA_RESTRICTIONS_LIST, newEntries)
            result.setResult(Activity.RESULT_OK, null, extras)
            result.finish()
            return
        }

        // Retains current restriction settings by transferring existing restriction entries to
        // new ones.
        for (entry in newEntries) {
            val key = entry.key
            if (KEY_BOOLEAN == key) {
                entry.selectedState = existingRestrictions.getBoolean(KEY_BOOLEAN)
            } else if (KEY_CHOICE == key) {
                if (existingRestrictions.containsKey(KEY_CHOICE)) {
                    entry.selectedString = existingRestrictions.getString(KEY_CHOICE)
                }
            } else if (KEY_MULTI_SELECT == key) {
                if (existingRestrictions.containsKey(KEY_MULTI_SELECT)) {
                    entry.allSelectedStrings = existingRestrictions.getStringArray(key)
                }
            }
        }
        val extras = Bundle()

        // This path demonstrates the use of a custom app restriction activity instead of standard
        // types.  When a custom activity is set, the standard types will not be available under
        // app restriction settings.
        //
        // If your app has an existing activity for app restriction configuration, you can set it
        // up with the intent here.
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean(MainActivity.CUSTOM_CONFIG_KEY, false)) {
            val customIntent = Intent()
            customIntent.setClass(context, CustomRestrictionsActivity::class.java)
            extras.putParcelable(Intent.EXTRA_RESTRICTIONS_INTENT, customIntent)
        }
        extras.putParcelableArrayList(Intent.EXTRA_RESTRICTIONS_LIST, newEntries)
        result.setResult(Activity.RESULT_OK, null, extras)
        result.finish()
    }

    // Demonstrates the creation of standard app restriction types: boolean, single choice, and
    // multi-select.
    private fun initRestrictions(context: Context): ArrayList<RestrictionEntry> {
        val newRestrictions = ArrayList<RestrictionEntry>()
        val res = context.resources
        val reBoolean = RestrictionEntry(KEY_BOOLEAN, false)
        populateBooleanEntry(res, reBoolean)
        newRestrictions.add(reBoolean)
        val reSingleChoice = RestrictionEntry(KEY_CHOICE, null as String?)
        populateChoiceEntry(res, reSingleChoice)
        newRestrictions.add(reSingleChoice)
        val reMultiSelect = RestrictionEntry(KEY_MULTI_SELECT, null as Array<String?>?)
        populateMultiEntry(res, reMultiSelect)
        newRestrictions.add(reMultiSelect)
        return newRestrictions
    }


    companion object {
        private val TAG = GetRestrictionsReceiver::class.java.simpleName

        // Keys for referencing app restriction settings from the platform.
        const val KEY_BOOLEAN = "boolean_key"
        const val KEY_CHOICE = "choice_key"
        const val KEY_MULTI_SELECT = "multi_key"

        // Initializes a boolean type restriction entry.
        fun populateBooleanEntry(res: Resources, entry: RestrictionEntry) {
            entry.type = RestrictionEntry.TYPE_BOOLEAN
            entry.title = res.getString(R.string.boolean_entry_title)
        }

        // Initializes a single choice type restriction entry.
        fun populateChoiceEntry(res: Resources, reSingleChoice: RestrictionEntry) {
            val choiceEntries = res.getStringArray(R.array.choice_entry_entries)
            val choiceValues = res.getStringArray(R.array.choice_entry_values)
            if (reSingleChoice.selectedString == null) {
                reSingleChoice.selectedString = choiceValues[0]
            }
            reSingleChoice.title = res.getString(R.string.choice_entry_title)
            reSingleChoice.choiceEntries = choiceEntries
            reSingleChoice.choiceValues = choiceValues
            reSingleChoice.type = RestrictionEntry.TYPE_CHOICE
        }

        // Initializes a multi-select type restriction entry.
        fun populateMultiEntry(res: Resources, reMultiSelect: RestrictionEntry) {
            val multiEntries = res.getStringArray(R.array.multi_entry_entries)
            val multiValues = res.getStringArray(R.array.multi_entry_values)
            if (reMultiSelect.allSelectedStrings == null) {
                reMultiSelect.allSelectedStrings = arrayOfNulls(0)
            }
            reMultiSelect.title = res.getString(R.string.multi_entry_title)
            reMultiSelect.choiceEntries = multiEntries
            reMultiSelect.choiceValues = multiValues
            reMultiSelect.type = RestrictionEntry.TYPE_MULTI_SELECT
        }
    }
}