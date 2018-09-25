package com.duyp.architecture.clean.android.powergit.data.entities.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.duyp.architecture.clean.android.powergit.data.entities.converters.DateConverter
import java.util.*

@Entity(tableName = "User")
data class UserLocalData (

        @PrimaryKey
        var id: Long,

        var login: String? = null,

        var avatarUrl: String? = null,

        var gravatarId: String? = null,

        var url: String? = null,

        var htmlUrl: String? = null,

        var followersUrl: String? = null,

        var followingUrl: String? = null,

        var gistsUrl: String? = null,

        var starredUrl: String? = null,

        var subscriptionsUrl: String? = null,

        var organizationsUrl: String? = null,

        var reposUrl: String? = null,

        var eventsUrl: String? = null,

        var receivedEventsUrl: String? = null,

        var type: String? = null,

        var siteAdmin: Boolean? = null,

        var name: String? = null,

        var company: String? = null,

        var blog: String? = null,

        var location: String? = null,

        var email: String? = null,

        var hireable: Boolean? = null,

        var bio: String? = null,

        var publicRepos: Long = 0,

        var publicGists: Long = 0,

        var followers: Long = 0,

        var following: Long = 0,

        @TypeConverters(DateConverter::class)
        var createdAt: Date? = null,

        @TypeConverters(DateConverter::class)
        var updatedAt: Date? = null,

        var privateGists: Long = 0,

        var totalPrivateRepos: Long = 0,

        var ownedPrivateRepos: Long = 0,

        var diskUsage: Long = 0,

        var collaborators: Long = 0,

        var twoFactorAuthentication: Boolean? = null,

        //var plan: Plan? = null,

        var contributions: Int = 0
)