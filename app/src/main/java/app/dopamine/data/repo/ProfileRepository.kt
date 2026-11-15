package app.dopamine.data.repo

import app.dopamine.data.db.ProfileDao
import app.dopamine.data.db.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface ProfileState {
    data object Loading : ProfileState
    data object None : ProfileState
    data class Ready(val name: String) : ProfileState
}

class ProfileRepository(private val dao: ProfileDao) {
    val state: Flow<ProfileState> = dao.observe().map { p -> p?.let { ProfileState.Ready(it.name) } ?: ProfileState.None }

    suspend fun save(name: String) = dao.upsert(ProfileEntity(name = name, createdAt = System.currentTimeMillis()))
}
