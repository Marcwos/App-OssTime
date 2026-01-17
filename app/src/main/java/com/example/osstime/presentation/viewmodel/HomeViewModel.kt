package com.example.osstime.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.domain.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel para HomeScreen
 * Maneja la lógica de negocio y el estado de las clases del día
 * Observa cambios en tiempo real desde Firestore
 */
class HomeViewModel(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _allClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val allClasses: StateFlow<List<ClassSession>> = _allClasses.asStateFlow()

    private val _todayClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val todayClasses: StateFlow<List<ClassSession>> = _todayClasses.asStateFlow()

    private val _tomorrowClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val tomorrowClasses: StateFlow<List<ClassSession>> = _tomorrowClasses.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeClasses()
    }

    /**
     * Observa cambios en tiempo real de las clases desde Firestore
     */
    private fun observeClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            classRepository.observeClasses()
                .catch { e ->
                    Log.e("HomeViewModel", "Error al observar clases", e)
                    _error.value = "Error al cargar clases: ${e.message}"
                    _isLoading.value = false
                }
                .collect { classList ->
                    Log.d("HomeViewModel", "Clases recibidas: ${classList.size}")
                    _allClasses.value = classList
                    
                    // Filtrar clases por fecha
                    val today = Calendar.getInstance()
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

                    _todayClasses.value = classList.filter { isToday(it.date, today) }
                    _tomorrowClasses.value = classList.filter { isTomorrow(it.date, tomorrow) }
                    
                    Log.d("HomeViewModel", "Clases de hoy: ${_todayClasses.value.size}")
                    Log.d("HomeViewModel", "Clases de mañana: ${_tomorrowClasses.value.size}")
                    
                    _isLoading.value = false
                }
        }
    }

    /**
     * Verifica si una fecha corresponde al día de hoy
     */
    private fun isToday(date: String, calendar: Calendar): Boolean {
        // Implementación simplificada - en producción usar formato de fecha real
        return date.contains("hoy", ignoreCase = true) || 
               date.contains(calendar.get(Calendar.DAY_OF_MONTH).toString())
    }

    /**
     * Verifica si una fecha corresponde a mañana
     */
    private fun isTomorrow(date: String, calendar: Calendar): Boolean {
        // Implementación simplificada - en producción usar formato de fecha real
        return date.contains("mañana", ignoreCase = true) ||
               date.contains((calendar.get(Calendar.DAY_OF_MONTH) + 1).toString())
    }
}
