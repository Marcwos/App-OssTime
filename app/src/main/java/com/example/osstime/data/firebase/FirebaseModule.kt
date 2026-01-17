package com.example.osstime.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Módulo de configuración de Firebase
 * Inicializa y configura Firebase Firestore con persistencia habilitada
 */
object FirebaseModule {
    
    /**
     * Inicializa Firebase con configuración personalizada
     * Habilita la persistencia offline para trabajar sin conexión
     */
    fun initialize() {
        val firestore = FirebaseFirestore.getInstance()
        
        // Configurar Firestore para persistencia offline
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Habilita caché offline
            .build()
        
        firestore.firestoreSettings = settings
    }
    
    /**
     * Obtiene una instancia de Firestore
     */
    fun getFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
