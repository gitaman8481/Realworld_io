package com.aman_ar.realworld_io.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData


class UsersViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        const val PREFS_KEY_AUTH_TOKEN = "auth_token"
        const val TAG = "API:USER"
    }

    val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(
            getApplication()
        )

    val currentUser: MutableLiveData<User> by lazy {
        with(prefs.getString(PREFS_KEY_AUTH_TOKEN, "")!!) {
            if (this.isNotEmpty()) {
                Log.d(TAG, "Token exists, will try to retrieve user")
                fetchUserByToken(this)
            }
        }

        object : MutableLiveData<User>() {
            override fun postValue(value: User?) {
                super.postValue(value)
                Log.d(TAG, "Saving user token")
                prefs.edit().putString(PREFS_KEY_AUTH_TOKEN, value?.token).apply()
            }
        }
    }

    fun fetchUserByToken(token: String) {
        ConduitClient.authToken = token
        ConduitClient.conduitApi.getCurrentUser().enqueue { t, response ->
            response?.body()?.let {
                currentUser.postValue(it.user)
            }
        }
    }

    fun loginUser(email: String, password: String) {
        ConduitClient.conduitApi.loginUser(
            UserLoginRequest(
                UserLoginRequest.User(email, password)
            )
        ).enqueue { t, response ->
            response?.body()?.let {
                currentUser.postValue(it.user)
            } ?: run {
                Log.e("API", "Error logging in user", t)
            }
        }
    }

    fun registerUser(email: String, username: String, password: String) {
        ConduitClient.conduitApi.registerUser(
            UserRegisterRequest(
                UserRegisterRequest.User(
                    email, password, username
                )
            )
        ).enqueue { t, response ->
            response?.body()?.let {
                currentUser.postValue(it.user)
            } ?: run {
                Log.e("API", "Error registering in user", t)
            }
        }
    }

    fun logoutUser() {
        currentUser.postValue(null)
    }
}
