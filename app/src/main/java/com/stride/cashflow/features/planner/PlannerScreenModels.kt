package com.stride.cashflow.features.planner

// This data class holds everything the UI needs for a single row.
data class UiPlannerItem(
    val entryId: Int,
    val templateId: Int,
    val name: String,
    val category: String,
    val amount: Long,
    val isDone: Boolean
)
