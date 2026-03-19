package com.example.dentflow_android.data

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class VisitViewModel @Inject constructor(
    private val repository: MockVisitRepository
) : ViewModel() {

    private val _visits = MutableStateFlow<List<Visit>>(emptyList())
    val visits: StateFlow<List<Visit>> = _visits

    init {
        _visits.value = repository.getDummyVisits()
    }
}