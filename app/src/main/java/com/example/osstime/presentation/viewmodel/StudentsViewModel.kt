package com.example.osstime.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.osstime.domain.model.Student
import com.example.osstime.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para StudentsScreen
 * Maneja la lógica de negocio y el estado de los estudiantes
 * Observa cambios en tiempo real desde Firestore
 */
class StudentsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _studentCount = MutableStateFlow(0)
    val studentCount: StateFlow<Int> = _studentCount.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeStudents()
    }

    /**
     * Observa cambios en tiempo real de los estudiantes desde Firestore
     */
    private fun observeStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            studentRepository.observeStudents()
                .catch { e ->
                    Log.e("StudentsViewModel", "Error al observar estudiantes", e)
                    _error.value = "Error al cargar estudiantes: ${e.message}"
                    _isLoading.value = false
                }
                .collect { studentList ->
                    Log.d("StudentsViewModel", "Estudiantes recibidos: ${studentList.size}")
                    _students.value = studentList
                    _studentCount.value = studentList.size
                    _isLoading.value = false
                }
        }
    }

    /**
     * Agrega un nuevo estudiante
     */
    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                Log.d("StudentsViewModel", "Guardando estudiante: ${student.firstName} ${student.lastName}")
                studentRepository.insertStudent(student)
                Log.d("StudentsViewModel", "Estudiante guardado exitosamente")
            } catch (e: Exception) {
                Log.e("StudentsViewModel", "Error al agregar estudiante", e)
                _error.value = "Error al agregar estudiante: ${e.message}"
            }
        }
    }

    /**
     * Elimina un estudiante
     */
    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            try {
                studentRepository.deleteStudent(studentId)
                Log.d("StudentsViewModel", "Estudiante eliminado: $studentId")
            } catch (e: Exception) {
                Log.e("StudentsViewModel", "Error al eliminar estudiante", e)
                _error.value = "Error al eliminar estudiante: ${e.message}"
            }
        }
    }
}
