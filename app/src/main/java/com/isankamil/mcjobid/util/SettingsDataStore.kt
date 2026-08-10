package com.isankamil.mcjobid.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

object SettingsKeys {
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val APP_LANGUAGE = stringPreferencesKey("app_language")
    val APP_PIN_ENABLED = booleanPreferencesKey("app_pin_enabled")
    val APP_PIN_CODE = stringPreferencesKey("app_pin_code")
    val PIN_TIMEOUT_MINUTES = intPreferencesKey("pin_timeout_minutes")
    val APP_THEME = stringPreferencesKey("app_theme")
    val EVENT_REMINDER_DAYS = intPreferencesKey("event_reminder_days")       // legacy, kept for compat
    val REMINDER_DAYS_SET = stringPreferencesKey("reminder_days_set")         // multi-day: "0,1,5,7"
    val SECURITY_BACKUP_KEY = stringPreferencesKey("security_backup_key")
    
    // Pintasan Cepat Dasbor (Quick Floating Action)
    val QUICK_ACTION_ENABLED = booleanPreferencesKey("quick_action_enabled")
    val QA_ADD_JOB = booleanPreferencesKey("qa_add_job")
    val QA_ADD_CLIENT = booleanPreferencesKey("qa_add_client")
    val QA_ADD_PAYMENT = booleanPreferencesKey("qa_add_payment")
    val QA_ADD_EXPENSE = booleanPreferencesKey("qa_add_expense")
    val QA_REMINDER = booleanPreferencesKey("qa_reminder")
    val QA_RATE_CARD = booleanPreferencesKey("qa_rate_card")
    val QA_EXPENSE_SIMULATOR = booleanPreferencesKey("qa_expense_simulator")
    val QA_INVOICE = booleanPreferencesKey("qa_invoice")
    val QA_ANALYTICS = booleanPreferencesKey("qa_analytics")
    val QA_NOTIFICATIONS = booleanPreferencesKey("qa_notifications")
    val QA_PROFILE = booleanPreferencesKey("qa_profile")
    val QA_SETTINGS = booleanPreferencesKey("qa_settings")
    val QA_TODO = booleanPreferencesKey("qa_todo")
}
