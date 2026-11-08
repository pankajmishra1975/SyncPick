package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val role: String, // Store Picker, Dispatch Director, Floor Supervisor, Platform Owner, etc.
    val teamName: String,
    val employeeCode: String,
    val pinCode: String,
    val isAdmin: Boolean = false,
    val isSuperAdmin: Boolean = false,
    val avatarInitials: String = "",
    val colorHex: Long = 0xFF2563EB,
    val isActive: Boolean = true,
    val lastLoginTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toPickerProfile(): PickerProfile {
        return PickerProfile(
            id = id,
            name = name,
            teamName = teamName,
            color = Color(colorHex),
            role = role,
            pinCode = pinCode,
            employeeCode = employeeCode,
            isAdmin = isAdmin,
            isSuperAdmin = isSuperAdmin
        )
    }

    companion object {
        fun fromPickerProfile(profile: PickerProfile, email: String? = null): UserEntity {
            return UserEntity(
                id = profile.id,
                name = profile.name,
                email = email ?: "${profile.id.replace("picker_", "p.").replace("admin_", "a.").replace("super_", "s.")}@sellershub.io",
                role = profile.role,
                teamName = profile.teamName,
                employeeCode = profile.employeeCode,
                pinCode = profile.pinCode,
                isAdmin = profile.isAdmin,
                isSuperAdmin = profile.isSuperAdmin,
                avatarInitials = profile.avatarInitials,
                colorHex = profile.color.value.toLong()
            )
        }
    }
}
