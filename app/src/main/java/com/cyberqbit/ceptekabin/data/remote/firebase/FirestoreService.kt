package com.cyberqbit.ceptekabin.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveUser(userId: String, userData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<Map<String, Any>?> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addKiyaketToUser(userId: String, kiyaketId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("kiyaketler")
                .document(kiyaketId)
                .set(mapOf("id" to kiyaketId, "addedAt" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserKiyaketler(userId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("kiyaketler")
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { it.data }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addKombinToUser(userId: String, kombinId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("kombinler")
                .document(kombinId)
                .set(mapOf("id" to kombinId, "createdAt" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserKiyaket(userId: String, kiyaketId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("kiyaketler")
                .document(kiyaketId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
