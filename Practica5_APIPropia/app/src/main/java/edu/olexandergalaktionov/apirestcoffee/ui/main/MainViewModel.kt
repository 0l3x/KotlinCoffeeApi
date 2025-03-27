package edu.olexandergalaktionov.apirestcoffee.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.apirestcoffee.data.CoffeeRepository
import edu.olexandergalaktionov.apirestcoffee.model.LoginRequest
import edu.olexandergalaktionov.apirestcoffee.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Class MainViewModel.kt
 *
 * ViewModel for managing user login, logout and session state.
 * Handles asynchronous login and logout processes using a repository.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class MainViewModel(private val repository: CoffeeRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    /**
     * Public access to the current login state.
     */
    val loginState: StateFlow<LoginState>
        get() = _loginState

    /**
     * Authenticates the user by sending credentials to the server.
     * Updates login state according to the result.
     *
     * @param user Username.
     * @param password User password.
     */
    fun login(user: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(LoginRequest(user, password))
                _loginState.value = LoginState.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Clears the user session and resets the login state.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }
}

/**
 * Factory to create instances of MainViewModel with constructor parameters.
 */
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val repository: CoffeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
