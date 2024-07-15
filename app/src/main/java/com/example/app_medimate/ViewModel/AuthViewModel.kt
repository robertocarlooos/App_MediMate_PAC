package com.example.app_medimate.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel:ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthSate>()
    val authState: LiveData<AuthSate> = _authState

    init {
        checkAuthState()
    }
    fun checkAuthState(){
       if (auth.currentUser ==null){
           _authState.value = AuthSate.UnAuthenticated
       } else {
           _authState.value = AuthSate.Authenticated
       }
    }
    fun login(email:String,password:String){
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthSate.Error("Email or Password is empty")
            return
        }
        _authState.value = AuthSate.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    _authState.value = AuthSate.Authenticated

                }else{
                    _authState.value = AuthSate.Error(task.exception?.message?: "An Error Occurred")

                }

            }
    }
    fun signup(email:String,password:String){
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthSate.Error("Email or Password is empty")
            return
        }
        _authState.value = AuthSate.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    _authState.value = AuthSate.Authenticated

                }else{
                    _authState.value = AuthSate.Error(task.exception?.message?: "An Error Occurred")

                }

            }
    }
    fun signout() {
        auth.signOut()
        _authState.value = AuthSate.UnAuthenticated
    }
}
sealed class AuthSate{
    object Authenticated : AuthSate()
    object UnAuthenticated : AuthSate()
    object Loading : AuthSate()
    data class Error(val message: String) : AuthSate()
}
