package com.andre.fitnesstracker

data class StrengthLevel(
    val name: String,
    val fromDays: Int,
    val toDays: Int,
    val iconRes: Int
)

val strengthLevels = listOf(
    StrengthLevel("Новичок", 0, 6, R.drawable.silhouette_novice),
    StrengthLevel("Спортсмен", 7, 20, R.drawable.silhouette_active),
    StrengthLevel("Атлет", 21, 59, R.drawable.silhouette_athlete),
    StrengthLevel("Мастер", 60, 119, R.drawable.silhouette_master),
    StrengthLevel("Легенда", 120, 364, R.drawable.silhouette_master),
    StrengthLevel("Титан", 365, Int.MAX_VALUE, R.drawable.silhouette_legend)
)