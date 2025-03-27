package edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.apirestcoffee.data.CoffeeRepository
import edu.olexandergalaktionov.apirestcoffee.data.Retrofit2Api
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeId
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * ViewModel to manage coffee data, including list, details, comments, and posting.
 * Communicates with the repository and exposes state flows to the UI.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class CoffeeViewModel(private val repository: CoffeeRepository) : ViewModel() {
    // List of all coffees fetched from the API
    private val _coffeeList = MutableStateFlow<List<CoffeeList>?>(null)
    val coffeeList: StateFlow<List<CoffeeList>?> = _coffeeList

    // Detailed data of a selected coffee (aka description)
    private val _coffeeDetail = MutableStateFlow<CoffeeId?>(null)
    val coffeeDetail: StateFlow<CoffeeId?> = _coffeeDetail

    // Comments related to the selected coffee
    private val _comments = MutableStateFlow<List<CoffeeComments>>(emptyList())
    val comments: StateFlow<List<CoffeeComments>> = _comments

    // Tracks refresh status to control SwipeRefreshLayout in the UI
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // Holds error messages to be displayed in the UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // API interface instance
    private val api = Retrofit2Api.getRetrofit2Api()

    /**
     * Fetches the complete list of coffees from the API using the repository.
     * Handles session expiration and updates the observable state.
     */
    fun fetchCoffees() {
        viewModelScope.launch {
            try {
                val coffeeData = repository.getAllCoffees()
                _coffeeList.value = coffeeData
                Log.d("CoffeeViewModel", "Cafés obtenidos: $coffeeData")
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    Log.e("CoffeeViewModel", "Token inválido. Cerrando sesión.")
                    repository.logout() // limpia sessionManager
                    _coffeeList.value = emptyList() // dispara el cambio en UI
                } else {
                    Log.e("CoffeeViewModel", "Error HTTP: ${e.code()} - ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("CoffeeViewModel", "Error al obtener los cafés: ${e.message}", e)
            }
        }
    }

    /**
     * Fetches detailed information of a coffee by its ID.
     *
     * @param token Authorization token.
     * @param coffeeId ID of the coffee to load.
     */
    fun loadCoffeeDetail(token: String, coffeeId: Int) {
        viewModelScope.launch {
            try {
                val result = api.getCoffeeById("Bearer $token", coffeeId)
                _coffeeDetail.value = result
            } catch (e: Exception) {
                Log.e("CoffeeDetailVM", "Error loading detail: ${e.message}")
                _coffeeDetail.value = null
            }
        }
    }

    /**
     * Fetches the comments of a specific coffee and updates the state.
     * Displays an error message in case of failure and manages refresh status.
     *
     * @param token Authorization token.
     * @param coffeeId ID of the coffee for which to load comments.
     */
    fun loadComments(token: String, coffeeId: Int) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null

            try {
                val result = api.getComments("Bearer $token", coffeeId)
                _comments.value = result
            } catch (e: Exception) {
                Log.e("CoffeeDetailVM", "Error loading comments: ${e.message}")
                _errorMessage.value = "No se pueden cargar comentarios nuevos."
                _comments.value = emptyList()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Sends a new comment to the API and triggers a refresh of comments on success.
     *
     * @param token Authorization token.
     * @param comment Comment object to be posted.
     * @param onSuccess Callback triggered if the comment is posted successfully.
     * @param onError Callback triggered if the request fails.
     */
    fun postComment(
        token: String,
        comment: CoffeeComments,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response: Response<CoffeeComments> = api.postComment("Bearer $token", comment)
                if (response.isSuccessful) {
                    onSuccess()
                    loadComments(token, comment.idCoffee)
                } else {
                    onError("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

}

/**
 * Factory class for creating instances of CoffeeViewModel with a custom repository.
 *
 * @param repository Instance of CoffeeRepository used in the ViewModel.
 */
@Suppress("UNCHECKED_CAST")
class CoffeeViewModelFactory(private val repository: CoffeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CoffeeViewModel(repository) as T
    }
}