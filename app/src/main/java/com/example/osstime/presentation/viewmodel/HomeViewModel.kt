package com.example.osstime.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.osstime.domain.model.ClassSession
import com.example.osstime.domain.model.Student
import com.example.osstime.domain.repository.ClassRepository
import com.example.osstime.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para HomeScreen
 * Maneja la lógica de negocio y el estado de las clases del día
 * Observa cambios en tiempo real desde Firestore
 */
class HomeViewModel(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _allClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val allClasses: StateFlow<List<ClassSession>> = _allClasses.asStateFlow()

    private val _todayClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val todayClasses: StateFlow<List<ClassSession>> = _todayClasses.asStateFlow()

    private val _tomorrowClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val tomorrowClasses: StateFlow<List<ClassSession>> = _tomorrowClasses.asStateFlow()

    private val _upcomingClasses = MutableStateFlow<List<ClassSession>>(emptyList())
    val upcomingClasses: StateFlow<List<ClassSession>> = _upcomingClasses.asStateFlow()

    private val _recentStudents = MutableStateFlow<List<Student>>(emptyList())
    val recentStudents: StateFlow<List<Student>> = _recentStudents.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeClasses()
        observeStudents()
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
                    _upcomingClasses.value = classList.filter { isUpcoming(it.date) }.take(5)
                    
                    Log.d("HomeViewModel", "Clases de hoy: ${_todayClasses.value.size}")
                    Log.d("HomeViewModel", "Clases de mañana: ${_tomorrowClasses.value.size}")
                    Log.d("HomeViewModel", "Clases próximas: ${_upcomingClasses.value.size}")
                    
                    _isLoading.value = false
                }
        }
    }

    /**
     * Observa cambios en tiempo real de los estudiantes desde Firestore
     */
    private fun observeStudents() {
        viewModelScope.launch {
            studentRepository.observeStudents()
                .catch { e ->
                    Log.e("HomeViewModel", "Error al observar estudiantes", e)
                }
                .collect { studentList ->
                    Log.d("HomeViewModel", "Estudiantes recibidos: ${studentList.size}")
                    // Mostrar los 5 estudiantes más recientes
                    _recentStudents.value = studentList.take(5)
                }
        }
    }

    /**
     * Verifica si una fecha corresponde al día de hoy
     */
    private fun isToday(date: String, calendar: Calendar): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val classDate = dateFormat.parse(date)
            
            if (classDate != null) {
                val classCalendar = Calendar.getInstance()
                classCalendar.time = classDate
                
                calendar.get(Calendar.YEAR) == classCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == classCalendar.get(Calendar.DAY_OF_YEAR)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error parsing date: $date", e)
            false
        }
    }

    /**
     * Verifica si una fecha corresponde a mañana
     */
    private fun isTomorrow(date: String, calendar: Calendar): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val classDate = dateFormat.parse(date)
            
            if (classDate != null) {
                val classCalendar = Calendar.getInstance()
                classCalendar.time = classDate
                
                calendar.get(Calendar.YEAR) == classCalendar.get(Calendar.YEAR) &&
                (calendar.get(Calendar.DAY_OF_YEAR) + 1) == classCalendar.get(Calendar.DAY_OF_YEAR)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error parsing date: $date", e)
            false
        }
    }

    /**
     * Verifica si una fecha es próxima (futuras)
     */
    private fun isUpcoming(date: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val classDate = dateFormat.parse(date)
            val today = Calendar.getInstance()
            
            // Normalizar a medianoche para comparación
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            // La clase es próxima si es posterior a hoy
            classDate != null && classDate.after(today.time)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error parsing date: $date", e)
            false
        }
    }
}
