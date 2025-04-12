package com.example.dream.data.model // Or com.example.dream.utils

/**
 * Constants related to the Seasons system logic.
 */
object SeasonConstants {
    // --- Reset Logic Thresholds ---
    /** Minimum level user must achieve in the ended season to qualify for a partial reset */
    const val RESET_THRESHOLD_LEVEL = 10
    /** The starting level for the new season for users who met or exceeded the RESET_THRESHOLD_LEVEL */
    const val RESET_TO_LEVEL = 5
    /** Default starting level for users who didn't meet the threshold or are new to the season */
    const val STARTING_LEVEL = 0

    // --- Database Collection/Field Names (Optional but good practice) ---
    const val SEASONS_COLLECTION = "seasons"
    const val USER_SEASON_PROGRESS_SUBCOLLECTION = "seasonProgress" // If using subcollection

    // --- Points to Level Calculation (Example) ---
    // Define constants used in your level calculation logic here if needed
    // const val POINTS_PER_LEVEL = 100
} 