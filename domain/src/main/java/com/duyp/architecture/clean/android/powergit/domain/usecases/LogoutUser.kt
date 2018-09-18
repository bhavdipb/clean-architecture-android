package com.duyp.architecture.clean.android.powergit.domain.usecases

import com.duyp.architecture.clean.android.powergit.domain.repositories.UserRepository

class LogoutUser(private val mUserRepository: UserRepository) {

    fun logoutCurrentUser() = mUserRepository.getLastLoggedInUsername()
            .flatMapCompletable { mUserRepository.logout(it) }
}