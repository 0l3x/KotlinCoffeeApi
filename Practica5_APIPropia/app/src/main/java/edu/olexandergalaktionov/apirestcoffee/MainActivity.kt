package edu.olexandergalaktionov.apirestcoffee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.olexandergalaktionov.apirestcoffee.databinding.ActivityMainBinding
import edu.olexandergalaktionov.apirestcoffee.model.LoginState
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import edu.olexandergalaktionov.apirestcoffee.utils.dataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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

        lifecycleScope.launch {
            vm.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${state.response.token}")
                        updateToolbarMenu()
                        loadCoffees()
                    }
                    is LoginState.Error -> {
                        Log.e("LOGIN", "Error: ${state.message}")
                        Toast.makeText(this@MainActivity, "Error de login: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            vm.getSessionFlow().collect { (token, username) ->
                if (token != null) {
                    Log.i("SESSION", "Usuario autenticado: $username")
                } else {
                    Log.i("SESSION", "No hay usuario autenticado")
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CoffeeAdapter(emptyList()) { } //

        binding.swipeRefresh.setOnRefreshListener {
            loadCoffees()
        }

        // Toolbar menú
        binding.mToolbar.inflateMenu(R.menu.main_menu)
        updateToolbarMenu()

        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_login -> {
                    showLoginDialog()
                    true
                }

                R.id.action_logout -> {
                    lifecycleScope.launch {
                        SessionManager(dataStore).clearSession()
                        vm.logout()
                        updateToolbarMenu()
                        clearCoffees()
                    }
                    true
                }

                else -> false
            }
        }

        // Acción del icono de navegación ("Acerca de")
        binding.mToolbar.setNavigationOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Acerca de")
                .setMessage(""" 
                            Autor: Olexander Galaktionov Tsisar
                            Grupo: 2º DAM/DAW
                            Asignatura: PMDM
                            Práctica: API REST Coffee
                            """.trimIndent())
                .setPositiveButton("Aceptar", null)
                .show()
        }

    }

    private fun updateToolbarMenu() {
        lifecycleScope.launch {
            val (token, _) = SessionManager(dataStore).sessionFlow.first()

            val menu = binding.mToolbar.menu
            menu.findItem(R.id.action_login)?.isVisible = token == null
            menu.findItem(R.id.action_logout)?.isVisible = token != null
        }
    }

    private fun loadCoffees() {
        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            if (token == null) {
                clearCoffees()
                return@launch
            }

            binding.swipeRefresh.isRefreshing = true

            viewModel.fetchCoffees()
            viewModel.coffeeList.collectLatest { coffeeList ->
                coffeeList?.let {
                    val adapter = CoffeeAdapter(it) { selectedCoffee ->
                        val intent = Intent(this@MainActivity, CoffeeDetail::class.java)
                        intent.putExtra("coffeeId", selectedCoffee.id)
                        startActivity(intent)
                    }
                    binding.recyclerView.adapter = adapter
                    binding.tvEmpty.visibility = View.GONE
                }
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun clearCoffees() {
        binding.recyclerView.adapter = CoffeeAdapter(emptyList()) {}
        binding.tvEmpty.visibility = View.VISIBLE
        Toast.makeText(this, "Lista de cafés perdida", Toast.LENGTH_SHORT).show()
    }

    private fun showLoginDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_login, null)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(this)
            .setTitle("Iniciar sesión")
            .setView(view)
            .setPositiveButton("Login") { _, _ ->
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                vm.login(username, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
