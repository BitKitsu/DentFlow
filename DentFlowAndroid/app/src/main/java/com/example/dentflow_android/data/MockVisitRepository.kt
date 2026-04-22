package com.example.dentflow_android.data

import javax.inject.Inject
import javax.inject.Singleton

// Dodajemy @Singleton i @Inject, żeby Hilt mógł to później łatwo "podmienić"
@Singleton
class MockVisitRepository @Inject constructor() {

    fun getDummyVisits(): List<Visit> {
        return listOf(
            Visit(
                id = 1,
                patientName = "Jan Kowalski",
                doctorName = "dr Karol Ogonowski",
                serviceName = "Przegląd stomatologiczny",
                date = "2026-03-20 10:30",
                status = "CONFIRMED"
            ),
            Visit(
                id = 2,
                patientName = "Anna Nowak",
                doctorName = "dr Piotr Pająk",
                serviceName = "Leczenie kanałowe",
                date = "2026-03-21 12:00",
                status = "PENDING"
            ),
            Visit(
                id = 3,
                patientName = "Marek Borowy",
                doctorName = "dr Karol Ogonowski",
                serviceName = "Higienizacja",
                date = "2026-03-22 09:15",
                status = "DONE"
            )
        )
    }
}
