package com.cyberqbit.ceptekabin.data.remote.firebase

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1039347948230-83vugh16vq6iqo9ur948844f1kus9hqu.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    val currentUser = firebaseAuth.currentUser

    val signInIntent: Intent
        get() = googleSignInClient.signInIntent

    suspend fun signInWithGoogle(intent: Intent): SignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.await()

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                SignInResult.Success(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
            } else {
                SignInResult.Error("Kullanıcı bilgileri alınamadı")
            }
        } catch (e: ApiException) {
            SignInResult.Error("Google sign-in failed: ${e.message}")
        } catch (e: Exception) {
            SignInResult.Error("Authentication error: ${e.message}")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }

    fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null

    sealed class SignInResult {
        data class Success(
            val uid: String,
            val email: String,
            val displayName: String,
            val photoUrl: String?
        ) : SignInResult()

        data class Error(val message: String) : SignInResult()
    }
}
