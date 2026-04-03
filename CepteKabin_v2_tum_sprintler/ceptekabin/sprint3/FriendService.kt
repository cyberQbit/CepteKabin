package com.cyberqbit.ceptekabin.data.remote.firebase

import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.KiyaketTur
import com.cyberqbit.ceptekabin.domain.model.Mevsim
import com.cyberqbit.ceptekabin.domain.model.UrunDurum
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class FriendRequest(
    val fromUid: String     = "",
    val fromName: String    = "",
    val fromEmail: String   = "",
    val toUid: String       = "",
    val status: String      = "pending",   // pending | accepted | rejected
    val createdAt: Long     = System.currentTimeMillis()
)

data class FriendProfile(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val kiyafetSayisi: Int
)

@Singleton
class FriendService @Inject constructor() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val me   get() = auth.currentUser

    // ── Arkadaş İsteği Gönder ─────────────────────────────────────────────────

    suspend fun sendFriendRequest(toUid: String): Result<Unit> = runCatching {
        val user = me ?: error("Giriş yapılmamış")
        val req  = hashMapOf(
            "fromUid"   to user.uid,
            "fromName"  to (user.displayName ?: ""),
            "fromEmail" to (user.email ?: ""),
            "toUid"     to toUid,
            "status"    to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("friendRequests").add(req).await()
        Unit
    }

    // ── Bekleyen İstekleri Getir ──────────────────────────────────────────────

    suspend fun getPendingRequests(): Result<List<FriendRequest>> = runCatching {
        val uid = me?.uid ?: error("Giriş yapılmamış")
        db.collection("friendRequests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(FriendRequest::class.java)?.copy()
            }
    }

    // ── İsteği Kabul Et ───────────────────────────────────────────────────────

    suspend fun acceptRequest(requestId: String, fromUid: String): Result<Unit> = runCatching {
        val uid = me?.uid ?: error("Giriş yapılmamış")
        val batch = db.batch()

        // İstek güncelle
        batch.update(db.collection("friendRequests").document(requestId), "status", "accepted")

        // Her iki kullanıcıya da arkadaş ekle
        batch.set(db.collection("users").document(uid).collection("friends").document(fromUid),
            hashMapOf("uid" to fromUid, "addedAt" to FieldValue.serverTimestamp()))
        batch.set(db.collection("users").document(fromUid).collection("friends").document(uid),
            hashMapOf("uid" to uid, "addedAt" to FieldValue.serverTimestamp()))

        batch.commit().await()
    }

    // ── Arkadaş Listesi ────────────────────────────────────────────────────────

    suspend fun getFriends(): Result<List<FriendProfile>> = runCatching {
        val uid = me?.uid ?: error("Giriş yapılmamış")
        val friendDocs = db.collection("users").document(uid)
            .collection("friends").get().await()
        friendDocs.documents.mapNotNull { doc ->
            val friendUid = doc.getString("uid") ?: return@mapNotNull null
            val profile   = db.collection("users").document(friendUid).get().await()
            val kiyafetSayisi = db.collection("users").document(friendUid)
                .collection("kiyaketler").get().await().size()
            FriendProfile(
                uid          = friendUid,
                displayName  = profile.getString("displayName") ?: "İsimsiz",
                photoUrl     = profile.getString("photoUrl"),
                kiyafetSayisi = kiyafetSayisi
            )
        }
    }

    // ── Arkadaşın Kıyafetleri (Salt Okunur) ──────────────────────────────────

    suspend fun getFriendKiyaketler(friendUid: String): Result<List<Kiyaket>> = runCatching {
        val uid = me?.uid ?: error("Giriş yapılmamış")

        // Güvenlik: arkadaş listesinde mi kontrol et
        val isFriend = db.collection("users").document(uid)
            .collection("friends").document(friendUid).get().await().exists()
        if (!isFriend) error("Bu kullanıcı arkadaşlarınız arasında değil.")

        db.collection("users").document(friendUid)
            .collection("kiyaketler").get().await()
            .documents.mapNotNull { doc ->
                try {
                    Kiyaket(
                        id       = doc.getLong("id") ?: 0L,
                        marka    = doc.getString("marka") ?: "",
                        model    = doc.getString("model"),
                        tur      = KiyaketTur.fromString(doc.getString("tur")),
                        beden    = doc.getString("beden") ?: "",
                        renk     = doc.getString("renk"),
                        mevsim   = Mevsim.fromString(doc.getString("mevsim")),
                        imageUrl = doc.getString("imageUrl"),
                        favori   = false   // Kendi favorimiz değil
                    )
                } catch (_: Exception) { null }
            }
    }

    // ── Kullanıcı UID'sini E-posta ile Ara ────────────────────────────────────

    suspend fun findUserByEmail(email: String): Result<FriendProfile?> = runCatching {
        val docs = db.collection("users")
            .whereEqualTo("email", email.trim().lowercase())
            .limit(1).get().await()
        if (docs.isEmpty) return@runCatching null
        val doc = docs.documents[0]
        FriendProfile(
            uid          = doc.id,
            displayName  = doc.getString("displayName") ?: "İsimsiz",
            photoUrl     = doc.getString("photoUrl"),
            kiyafetSayisi = 0
        )
    }
}
