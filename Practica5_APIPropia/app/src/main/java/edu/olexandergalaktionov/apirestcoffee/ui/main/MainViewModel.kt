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

class MainViewModel(private val repository: CoffeeRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState>
        get() = _loginState

    // Función para iniciar sesión y obtener el token.
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

    // Función para cerrar sesión.
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }

    // Función obtener para el Flow de la sesión.
    fun getSessionFlow() = repository.getSessionFlow()
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val repository: CoffeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
