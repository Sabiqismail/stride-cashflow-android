package com.stride.cashflow

import android.app.Application
import com.stride.cashflow.data.StrideDatabase
import com.stride.cashflow.data.StrideRepository


class StrideApplication : Application() {
    // Using 'lazy' ensures the database and repository are only created when they're first needed.
    val database by lazy { StrideDatabase.getDatabase(this) }
    val repository by lazy { StrideRepository(database.strideDao()) }
}
