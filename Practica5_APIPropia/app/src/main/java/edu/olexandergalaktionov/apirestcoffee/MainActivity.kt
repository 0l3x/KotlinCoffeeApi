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
import androidx.recyclerview.widget.LinearLayoutManager

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

        // 1. Login automático (esto puede ser opcional)
        vm.login("ogalaktionov", "10755610")

        // 2. Observar el estado de login
        lifecycleScope.launch {
            vm.loginState.collect { state ->
                when (state) {
                    is LoginState.Idle -> Log.i("LOGIN", "Idle")
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${state.response.token}")
                        // Ya estamos logueados: ahora sí podemos obtener cafés
                        viewModel.fetchCoffees()
                    }
                    is LoginState.Error -> Log.e("LOGIN", "Error: ${state.message}")
                }
            }
        }

        // 3. Observar los datos de sesión (opcional)
        lifecycleScope.launch {
            vm.getSessionFlow().collect { (token, username) ->
                if (token != null) {
                    Log.i("SESSION", "Usuario autenticado: $username")
                } else {
                    Log.i("SESSION", "No hay usuario autenticado")
                }
            }
        }

        // 4. Observar cafés cargados desde ViewModel
        lifecycleScope.launchWhenStarted {
            viewModel.coffeeList.collectLatest { coffeeList ->
                coffeeList?.let {
                    Log.d("MainActivity", "Cafés mostrados en UI: $it")
                }
            }
        }

        // Inicializa el RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launchWhenStarted {
            viewModel.coffeeList.collectLatest { coffeeList ->
                coffeeList?.let {
                    val adapter = CoffeeAdapter(it)
                    binding.recyclerView.adapter = adapter
                    Log.d("MainActivity", "Cafés mostrados en UI: $it")
                }
            }
        }

    }
}
