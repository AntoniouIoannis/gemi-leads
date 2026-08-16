package com.example.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.BusinessProfile
import com.example.data.model.FirestoreBusinessProfile
import com.example.data.model.FirestoreUser
import com.example.data.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Clean data model representing the authenticated session state.
 */
data class AuthSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isAnonymous: Boolean = false,
    val isEmailVerified: Boolean = false
)

/**
 * Authentication and User/Business Profile link result.
 */
data class AuthResult(
    val session: AuthSession,
    val user: User,
    val businessProfile: BusinessProfile?
)

/**
 * Interface defining authentication operations and account lifecycle.
 */
interface IAuthRepository {
    val authStateFlow: Flow<FirebaseUser?>
    val currentSessionFlow: Flow<AuthSession?>
    val currentUserWithProfileFlow: Flow<UserAccountWithBusinessProfile?>

    fun isUserSignedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentFirebaseUser(): FirebaseUser?

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String = "",
        initialProfile: BusinessProfile? = null
    ): Result<AuthResult>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<AuthResult>

    suspend fun signInWithGoogleCredential(
        idToken: String,
        initialProfile: BusinessProfile? = null
    ): Result<AuthResult>

    suspend fun launchGoogleSignIn(
        context: Context,
        serverClientId: String,
        initialProfile: BusinessProfile? = null
    ): Result<AuthResult>

    suspend fun signOut(context: Context? = null)
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateDisplayName(displayName: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>

    suspend fun syncLocalDbAfterAuth(userProfileDao: UserProfileDao): Result<UserProfileEntity?>
}

/**
 * Production implementation of Firebase Authentication integrated with Firestore 'users' & 'business_profiles'.
 */
class FirebaseAuthRepository(
    firebaseAuthIn: FirebaseAuth? = null,
    private val userProfileRepo: IFirestoreUserProfileRepository = FirestoreUserProfileRepository()
) : IAuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        firebaseAuthIn ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }

    /**
     * Real-time stream of Firebase Authentication state changes.
     */
    override val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth!!.addAuthStateListener(authStateListener)
        // Emit initial value
        trySend(firebaseAuth!!.currentUser)
        awaitClose { firebaseAuth!!.removeAuthStateListener(authStateListener) }
    }.distinctUntilChanged()

    /**
     * Reactive stream of current session details.
     */
    override val currentSessionFlow: Flow<AuthSession?> = authStateFlow.distinctUntilChanged().flatMapLatest { fbUser ->
        if (fbUser != null) {
            flowOf(
                AuthSession(
                    uid = fbUser.uid,
                    email = fbUser.email.orEmpty(),
                    displayName = fbUser.displayName.orEmpty(),
                    photoUrl = fbUser.photoUrl?.toString(),
                    isAnonymous = fbUser.isAnonymous,
                    isEmailVerified = fbUser.isEmailVerified
                )
            )
        } else {
            flowOf(null)
        }
    }

    /**
     * Combined reactive stream of User document and linked BusinessProfile from Firestore.
     */
    override val currentUserWithProfileFlow: Flow<UserAccountWithBusinessProfile?> = authStateFlow.flatMapLatest { fbUser ->
        if (fbUser != null) {
            userProfileRepo.getUserWithBusinessProfileFlow(fbUser.uid)
        } else {
            flowOf(null)
        }
    }

    override fun isUserSignedIn(): Boolean = firebaseAuth?.currentUser != null

    override fun getCurrentUserId(): String? = firebaseAuth?.currentUser?.uid

    override fun getCurrentFirebaseUser(): FirebaseUser? = firebaseAuth?.currentUser

    /**
     * Signs up a new subscriber with Email & Password.
     * Automatically provisions:
     * 1. Firebase Auth user account.
     * 2. 'users/{userId}' document.
     * 3. 'business_profiles/{userId}' document.
     */
    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        initialProfile: BusinessProfile?
    ): Result<AuthResult> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            val authResult = firebaseAuth!!.createUserWithEmailAndPassword(email.trim(), password).await()
            val fbUser = authResult.user ?: throw IllegalStateException("Firebase user creation returned null")

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                fbUser.updateProfile(profileUpdates).await()
            }

            // Provision Firestore 'users' and 'business_profiles' documents linked by {userId}
            val accountResult = userProfileRepo.initializeNewUserAccount(
                userId = fbUser.uid,
                email = email.trim(),
                displayName = displayName.ifBlank { email.substringBefore("@") },
                photoUrl = fbUser.photoUrl?.toString(),
                initialProfile = initialProfile ?: FirestoreBusinessProfile(
                    userId = fbUser.uid,
                    companyName = displayName.ifBlank { "Η Επιχείρησή μου" }
                )
            )

            val account = accountResult.getOrThrow()
            val session = AuthSession(
                uid = fbUser.uid,
                email = fbUser.email.orEmpty(),
                displayName = displayName.ifBlank { fbUser.displayName.orEmpty() },
                photoUrl = fbUser.photoUrl?.toString(),
                isAnonymous = fbUser.isAnonymous,
                isEmailVerified = fbUser.isEmailVerified
            )

            Result.success(
                AuthResult(
                    session = session,
                    user = account.user,
                    businessProfile = account.businessProfile
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs in an existing user with Email & Password.
     * Updates 'lastLoginAt' timestamp and retrieves the linked 'business_profiles' document.
     */
    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<AuthResult> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            val authResult = firebaseAuth!!.signInWithEmailAndPassword(email.trim(), password).await()
            val fbUser = authResult.user ?: throw IllegalStateException("Firebase sign in returned null")

            // Update user last login timestamp in Firestore
            userProfileRepo.updateLastLogin(fbUser.uid)

            // Resolve existing User document and linked BusinessProfile
            var userAccount = userProfileRepo.fetchUserWithBusinessProfile(fbUser.uid).getOrNull()

            // If user doc doesn't exist yet, initialize it
            if (userAccount == null || userAccount.user.userId.isBlank()) {
                val initResult = userProfileRepo.initializeNewUserAccount(
                    userId = fbUser.uid,
                    email = fbUser.email.orEmpty(),
                    displayName = fbUser.displayName.orEmpty().ifBlank { fbUser.email?.substringBefore("@").orEmpty() },
                    photoUrl = fbUser.photoUrl?.toString(),
                    initialProfile = FirestoreBusinessProfile(userId = fbUser.uid)
                )
                userAccount = initResult.getOrNull()
            }

            val finalUser = userAccount?.user ?: FirestoreUser(
                userId = fbUser.uid,
                email = fbUser.email.orEmpty(),
                displayName = fbUser.displayName.orEmpty(),
                lastLoginAt = getCurrentIsoTimestamp()
            )

            val session = AuthSession(
                uid = fbUser.uid,
                email = fbUser.email.orEmpty(),
                displayName = fbUser.displayName.orEmpty(),
                photoUrl = fbUser.photoUrl?.toString(),
                isAnonymous = fbUser.isAnonymous,
                isEmailVerified = fbUser.isEmailVerified
            )

            Result.success(
                AuthResult(
                    session = session,
                    user = finalUser,
                    businessProfile = userAccount?.businessProfile
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs in with a Google ID Token (via Credential Manager or Google Sign-In SDK).
     */
    override suspend fun signInWithGoogleCredential(
        idToken: String,
        initialProfile: BusinessProfile?
    ): Result<AuthResult> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth!!.signInWithCredential(credential).await()
            val fbUser = authResult.user ?: throw IllegalStateException("Firebase Google Sign-In returned null")

            // Fetch or provision joined User & Business Profile
            var accountWithProfile = userProfileRepo.fetchUserWithBusinessProfile(fbUser.uid).getOrNull()

            if (accountWithProfile == null || accountWithProfile.businessProfile == null) {
                val initResult = userProfileRepo.initializeNewUserAccount(
                    userId = fbUser.uid,
                    email = fbUser.email.orEmpty(),
                    displayName = fbUser.displayName.orEmpty().ifBlank { "Επιχειρηματίας" },
                    photoUrl = fbUser.photoUrl?.toString(),
                    initialProfile = initialProfile ?: FirestoreBusinessProfile(
                        userId = fbUser.uid,
                        companyName = fbUser.displayName.orEmpty().ifBlank { "Η Επιχείρησή μου" }
                    )
                )
                accountWithProfile = initResult.getOrNull()
            } else {
                userProfileRepo.updateLastLogin(fbUser.uid)
            }

            val session = AuthSession(
                uid = fbUser.uid,
                email = fbUser.email.orEmpty(),
                displayName = fbUser.displayName.orEmpty(),
                photoUrl = fbUser.photoUrl?.toString(),
                isAnonymous = fbUser.isAnonymous,
                isEmailVerified = fbUser.isEmailVerified
            )

            val finalUser = accountWithProfile?.user ?: FirestoreUser(
                userId = fbUser.uid,
                email = fbUser.email.orEmpty(),
                displayName = fbUser.displayName.orEmpty(),
                photoUrl = fbUser.photoUrl?.toString(),
                lastLoginAt = getCurrentIsoTimestamp()
            )

            Result.success(
                AuthResult(
                    session = session,
                    user = finalUser,
                    businessProfile = accountWithProfile?.businessProfile
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Modern Jetpack CredentialManager Google Sign-In integration.
     */
    override suspend fun launchGoogleSignIn(
        context: Context,
        serverClientId: String,
        initialProfile: BusinessProfile?
    ): Result<AuthResult> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                signInWithGoogleCredential(idToken, initialProfile)
            } else {
                Result.failure(IllegalArgumentException("Μη αναμενόμενος τύπος διαπιστευτηρίων Google"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Η σύνδεση μέσω Google ακυρώθηκε από τον χρήστη"))
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Σφάλμα σύνδεσης Google: ${e.message}"))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception("Σφάλμα ανάλυσης διαπιστευτηρίου: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user and clears CredentialManager credentials if context is provided.
     */
    override suspend fun signOut(context: Context?) {
        try {
            firebaseAuth?.signOut()
            if (context != null) {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        } catch (e: Exception) {
            // Ignore clean-up errors
        }
    }

    /**
     * Sends a password reset email via Firebase Auth.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            firebaseAuth!!.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates user's display name in Firebase Auth and Firestore.
     */
    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            val fbUser = firebaseAuth!!.currentUser ?: throw IllegalStateException("Χρήστης μη συνδεδεμένος")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            fbUser.updateProfile(profileUpdates).await()

            val currentDoc = userProfileRepo.fetchUser(fbUser.uid).getOrNull()
            if (currentDoc != null) {
                userProfileRepo.createOrUpdateUser(currentDoc.copy(displayName = displayName))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes user account from Firebase Auth and cleans up linked 'users' and 'business_profiles' documents.
     */
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            if (firebaseAuth == null) return Result.failure(Exception("FirebaseAuth not initialized"))
            val fbUser = firebaseAuth!!.currentUser ?: throw IllegalStateException("Χρήστης μη συνδεδεμένος")
            val uid = fbUser.uid

            // Clean up Firestore documents first
            userProfileRepo.deleteUser(uid)

            // Delete Firebase Auth account
            fbUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Synchronizes the authenticated user's remote 'business_profiles' into local Room storage.
     */
    override suspend fun syncLocalDbAfterAuth(userProfileDao: UserProfileDao): Result<UserProfileEntity?> {
        val uid = getCurrentUserId() ?: return Result.success(null)
        return userProfileRepo.syncRemoteProfileToLocalDb(uid, userProfileDao)
    }
}
