package com.imp.impandroidclient.app_state.repos.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class CreatorSignUpInfo(
    @SerializedName("first_name")       var firstName: String? = null,
    @SerializedName("last_name")        var lastName: String? = null,
    @SerializedName("gender")           var gender: String? = null,
    @SerializedName("date_of_birth")    var dateOfBirth: String? = null,
    @SerializedName("email")            var emailAddress: String? = null,
    @SerializedName("phone_number")     var phoneNumber: String? = null,
    @SerializedName("username")         var username: String? = null,
    @SerializedName("password")         var password: String? = null,
    @SerializedName("location")         var location: String? = null
)

data class CreatorAccount(
    @SerializedName("id")                   var id: Int,
    @SerializedName("first_name")           var firstName: String? = null,
    @SerializedName("last_name")            var lastName: String? = null,
    @SerializedName("gender")               var gender: String? = null,
    @SerializedName("phone_number")         var phoneNumber: String? = null,
    @SerializedName("location")             var location: String? = null,
    @SerializedName("date_of_birth")        var dateOfBirth: String? = null,
    @SerializedName("user")                 var user: User
)

data class User(
    @SerializedName("id")                   var id: Int,
    @SerializedName("username")             var username: String,
    @SerializedName("avatar")               var avatar: String?
)

data class Conversation(
    @SerializedName("id")                   val id: Int,
    @SerializedName("post_submission")      val submissionId: Int,
    @SerializedName("creator")              val creatorId: Int,
    @SerializedName("last_modified")        val lastModified: Date
)

data class Message(
    @SerializedName("id")                   var id: Int,
    @SerializedName("time")                 val time: Date,
    @SerializedName("state")                val state: String,
    @SerializedName("message")              val message: String,
    @SerializedName("sender")               val sender: String,
    @SerializedName("conversation")         val conversationId: Int
)

data class SocketMessageEvent(
    @SerializedName("event")                val event: String,
    @SerializedName("message")              val message: Message
)
