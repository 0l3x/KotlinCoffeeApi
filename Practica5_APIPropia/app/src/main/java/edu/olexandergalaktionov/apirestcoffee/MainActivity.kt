package edu.olexandergalaktionov.apirestcoffee

import android.content.Intent
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

        // Login automático
        vm.login("ogalaktionov", "10755610")

        // Observa estado de login
        lifecycleScope.launch {
            vm.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${state.response.token}")
                        loadCoffees()
                    }
                    is LoginState.Error -> Log.e("LOGIN", "Error: ${state.message}")
                    else -> {}
                }
            }
        }

        // Observa sesión (opcional)
        lifecycleScope.launch {
            vm.getSessionFlow().collect { (token, username) ->
                if (token != null) {
                    Log.i("SESSION", "Usuario autenticado: $username")
                } else {
                    Log.i("SESSION", "No hay usuario autenticado")
                }
            }
        }

        // Inicializa RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Swipe para refrescar
        binding.swipeRefresh.setOnRefreshListener {
            loadCoffees()
        }
    }

    private fun loadCoffees() {
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launchWhenStarted {
            viewModel.fetchCoffees() // Este método es seguro, pero puedes moverlo aquí si prefieres
            viewModel.coffeeList.collectLatest { coffeeList ->
                coffeeList?.let {
                    val adapter = CoffeeAdapter(it) { selectedCoffee ->
                        val intent = Intent(this@MainActivity, CoffeeDetail::class.java)
                        intent.putExtra("coffeeId", selectedCoffee.id)
                        startActivity(intent)
                    }
                    binding.recyclerView.adapter = adapter
                    Log.d("MainActivity", "Cafés mostrados en UI: $it")
                }
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}

