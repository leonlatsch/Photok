package dev.leonlatsch.photok.model.repositories

import dev.leonlatsch.photok.model.database.dao.PasswordDao
import dev.leonlatsch.photok.model.database.entity.Password
import javax.inject.Inject

class PasswordRepository @Inject constructor(
    private val passwordDao: PasswordDao
) {
    suspend fun insert(password: Password) = passwordDao.insert(password)

    suspend fun delete(password: Password) = passwordDao.delete(password)

    suspend fun update(password: Password) = passwordDao.update(password)

    fun getPassword() = passwordDao.getPassword()
}