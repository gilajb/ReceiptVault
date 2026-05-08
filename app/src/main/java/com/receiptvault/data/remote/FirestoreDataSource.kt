package com.receiptvault.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.receiptvault.data.local.entities.Receipt
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    private val currentUid get() = auth.currentUser?.uid
        ?: throw IllegalStateException("User not authenticated")

    private fun receiptsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("receipts")

    suspend fun uploadReceipt(receipt: Receipt): String {
        val uid = currentUid
        val docRef = if (receipt.id != 0) {
            receiptsCollection(uid).document(receipt.id.toString())
        } else {
            receiptsCollection(uid).document()
        }

        val firestoreReceipt = hashMapOf(
            "id" to receipt.id,
            "merchant" to receipt.merchant,
            "amount" to receipt.amount,
            "date" to receipt.date,
            "category" to receipt.category,
            "imageUri" to receipt.imageUri,
            "notes" to receipt.notes,
            "syncedToCloud" to true
        )

        docRef.set(firestoreReceipt).await()
        return docRef.id
    }

    suspend fun uploadReceiptImage(receipt: Receipt): String {
        val uid = currentUid
        val localFile = File(receipt.imageUri)
        if (!localFile.exists()) return receipt.imageUri

        val storageRef = storage.reference
            .child("users/$uid/receipts/${receipt.id}_${System.currentTimeMillis()}.jpg")

        storageRef.putFile(android.net.Uri.fromFile(localFile)).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun deleteReceipt(receiptId: Int) {
        val uid = currentUid
        receiptsCollection(uid).document(receiptId.toString()).delete().await()
    }

    suspend fun getAllRemoteReceipts(): List<Receipt> {
        val uid = currentUid
        val snapshot = receiptsCollection(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                Receipt(
                    id = (doc.getLong("id") ?: 0).toInt(),
                    merchant = doc.getString("merchant") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getString("date") ?: "",
                    category = doc.getString("category") ?: "",
                    imageUri = doc.getString("imageUri") ?: "",
                    notes = doc.getString("notes"),
                    syncedToCloud = true
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun isUserLoggedIn() = auth.currentUser != null
}
