package com.stride.cashflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_templates")
data class ItemTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String // e.g., "Income", "Receivables"
)
    