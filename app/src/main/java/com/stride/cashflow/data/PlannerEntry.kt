package com.stride.cashflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner_entries")
data class PlannerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plannerMonth: String, // Format: "YYYY-MM", e.g., "2025-11"
    val templateId: Int,      // This links to ItemTemplate's id
    val amount: Long = 0L,
    val isDone: Boolean = false
)
    