package com.example.dentflow_android.data

data class Visit(
    val id: Int,
    val patientName: String,
    val doctorName: String,
    val serviceName: String, // np. "Przegląd", "Plombowanie" [cite: 123]
    val date: String,        // np. "2026-03-20 10:30"
    val status: String       // DONE, CONFIRMED, NO_SHOW [cite: 134]
)