package com.example.data.local

import androidx.room.*
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isAdmin = 1 OR isSuperAdmin = 1 ORDER BY name ASC")
    fun getAdminUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isAdmin = 0 AND isSuperAdmin = 0 ORDER BY name ASC")
    fun getStorePickers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE teamName = :teamName ORDER BY name ASC")
    fun getUsersByTeam(teamName: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE employeeCode = :code OR id = :code LIMIT 1")
    suspend fun findUserByCode(code: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("UPDATE users SET lastLoginTimestamp = :timestamp WHERE id = :id")
    suspend fun recordUserLogin(id: String, timestamp: Long = System.currentTimeMillis())
}
