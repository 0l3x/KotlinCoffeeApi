package edu.olexandergalaktionov.apirestcoffee

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoffeeViewModel(private val repository: CoffeeRepository) : ViewModel() {

    private val _coffeeList = MutableStateFlow<List<CoffeeList>?>(null)
    val coffeeList: StateFlow<List<CoffeeList>?> = _coffeeList

    /**
     * Obtiene la lista de cafés y la imprime en Logcat.
     */
    fun fetchCoffees() {
        viewModelScope.launch {
            try {
                val coffeeData = repository.getAllCoffees()
                _coffeeList.value = coffeeData

                // Imprimir en la consola de Logcat
                Log.d("CoffeeViewModel", "Cafés obtenidos: $coffeeData")

            } catch (e: Exception) {
                Log.e("CoffeeViewModel", "Error al obtener los cafés: ${e.message}", e)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class CoffeeViewModelFactory(private val repository: CoffeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CoffeeViewModel(repository) as T
    }
}