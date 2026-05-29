package com.example.inventra.data.repository

import com.example.inventra.core.network.SupabaseClientProvider
import com.example.inventra.data.remote.dto.ProfileDto
import com.example.inventra.domain.model.User
import com.example.inventra.domain.model.UserDivision
import com.example.inventra.domain.model.UserRole
import com.example.inventra.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl : AuthRepository {

    private val client = SupabaseClientProvider.client
    private val auth = client.auth
    private val db = client.postgrest

    override val currentUser: Flow<User?> = flow {
        emit(getCurrentUser())
    }

    override val isLoggedIn: Boolean
        get() = auth.currentSessionOrNull() != null

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = getCurrentUser()
                ?: return Result.failure(Exception("Gagal mengambil data user"))
            Result.success(user)
        } catch (e: Exception) {
            // Log error asli
            println("LOGIN ERROR: ${e.message}")
            println("LOGIN ERROR CLASS: ${e::class.simpleName}")
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        division: String
    ): Result<User> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                    put("role", "MEMBER")
                    put("division", division)
                }
            }
            val user = getCurrentUser()
                ?: return Result.failure(Exception("Gagal membuat akun"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendaftar: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val authUser = auth.currentUserOrNull() ?: return null
            val profile = db["profiles"]
                .select(columns = Columns.ALL) {
                    filter { eq("id", authUser.id) }
                    limit(1)
                }
                .decodeSingle<ProfileDto>()
            User(
                id = profile.id,
                name = profile.name,
                email = authUser.email ?: "",
                role = try { UserRole.valueOf(profile.role) } catch (e: Exception) { UserRole.MEMBER },
                division = try { UserDivision.valueOf(profile.division) } catch (e: Exception) { UserDivision.PUBDOK },
                studentId = profile.studentId,
                phone = profile.phone,
                avatarUrl = profile.avatarUrl,
                isActive = profile.isActive
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProfile(name: String, phone: String?): Result<User> {
        return try {
            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Tidak terautentikasi"))
            db["profiles"].update(
                mapOf("name" to name, "phone" to phone)
            ) {
                filter { eq("id", userId) }
            }
            val user = getCurrentUser()
                ?: return Result.failure(Exception("Gagal mengambil data"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}