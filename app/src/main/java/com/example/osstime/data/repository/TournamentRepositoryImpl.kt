package com.example.osstime.data.repository

import android.util.Log
import com.example.osstime.data.firebase.FirebaseModule
import com.example.osstime.domain.model.Tournament
import com.example.osstime.domain.repository.TournamentRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Implementación del repositorio de torneos usando Firestore.
 */
class TournamentRepositoryImpl : TournamentRepository {
    
    private val firestore: FirebaseFirestore = FirebaseModule.getFirestore()
    private val tournamentsCollection = firestore.collection("tournaments")
    
    companion object {
        private const val TAG = "TournamentRepository"
    }
    
    override suspend fun getAllTournaments(): List<Tournament> {
        return try {
            val snapshot = tournamentsCollection
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toTournament() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tournaments", e)
            emptyList()
        }
    }
    
    override suspend fun getTournamentById(id: String): Tournament? {
        return try {
            val doc = tournamentsCollection.document(id).get().await()
            doc.toTournament()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tournament by id", e)
            null
        }
    }
    
    override suspend fun insertTournament(tournament: Tournament) {
        try {
            tournamentsCollection.document(tournament.id).set(tournament.toMap()).await()
            Log.d(TAG, "Tournament inserted: ${tournament.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting tournament", e)
            throw e
        }
    }
    
    override suspend fun updateTournament(tournament: Tournament) {
        try {
            tournamentsCollection.document(tournament.id).set(tournament.toMap()).await()
            Log.d(TAG, "Tournament updated: ${tournament.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tournament", e)
            throw e
        }
    }
    
    override suspend fun deleteTournament(id: String) {
        try {
            tournamentsCollection.document(id).delete().await()
            Log.d(TAG, "Tournament deleted: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting tournament", e)
            throw e
        }
    }
    
    override fun observeTournaments(): Flow<List<Tournament>> = callbackFlow {
        val listener = tournamentsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing tournaments", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tournaments = snapshot?.documents?.mapNotNull { it.toTournament() } 
                    ?.sortedBy { it.date }
                    ?: emptyList()
                Log.d(TAG, "Snapshot received: ${tournaments.size} tournaments")
                trySend(tournaments)
            }
        awaitClose { listener.remove() }
    }
    
    override fun observeUpcomingTournaments(): Flow<List<Tournament>> = callbackFlow {
        val listener = tournamentsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing upcoming tournaments", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val today = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val tournaments = snapshot?.documents
                    ?.mapNotNull { it.toTournament() }
                    ?.filter { tournament ->
                        try {
                            val tournamentDate = dateFormat.parse(tournament.date)
                            tournamentDate != null && !tournamentDate.before(today.time)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    ?.sortedBy { it.date }
                    ?: emptyList()
                
                trySend(tournaments)
            }
        awaitClose { listener.remove() }
    }
    
    private fun Tournament.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "city" to city,
        "modality" to modality,
        "date" to date,
        "professorId" to professorId,
        "createdAt" to createdAt
    )
    
    private fun DocumentSnapshot.toTournament(): Tournament? {
        return try {
            Tournament(
                id = getString("id") ?: id,
                name = getString("name") ?: "",
                city = getString("city") ?: "",
                modality = getString("modality") ?: "",
                date = getString("date") ?: "",
                professorId = getString("professorId"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing tournament document", e)
            null
        }
    }
}
