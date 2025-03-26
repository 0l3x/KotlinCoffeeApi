package edu.olexandergalaktionov.apirestcoffee

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.olexandergalaktionov.apirestcoffee.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import edu.olexandergalaktionov.apirestcoffee.model.LoginState
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import edu.olexandergalaktionov.apirestcoffee.utils.dataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CoffeeViewModel by viewModels {
        CoffeeViewModelFactory(CoffeeRepository(SessionManager(dataStore)))
    }

    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(CoffeeRepository(SessionManager(dataStore)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vm.login("ogalaktionov", "10755610")

        // Observando el estado de autenticación
        lifecycleScope.launch {
            vm.loginState.collect {
                when (it) {
                    is LoginState.Idle -> Log.i("LOGIN", "Idle")
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${it.response.token}")
                    }
                    is LoginState.Error -> {
                        Log.e("LOGIN", "Error: ${it.message}")
                    }
                }
            }
        }

        // Obteniendo sesión guardada
        lifecycleScope.launch {
            vm.getSessionFlow().collect {
                if (it.first != null) {
                    Log.i("SESSION", "Usuario autenticado: ${it.second}")
                } else {
                    Log.i("SESSION", "No hay usuario autenticado")
                }
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.coffeeList.collectLatest { coffeeList ->
                coffeeList?.let {
                    Log.d("MainActivity", "Cafés mostrados en UI: $it")
                }
            }
        }

        // Llamar a la API para obtener cafés
        viewModel.fetchCoffees()

        // Logout
        vm.logout()
    }
}