package edu.olexandergalaktionov.apirestcoffee.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import edu.olexandergalaktionov.apirestcoffee.data.CoffeeRepository
import edu.olexandergalaktionov.apirestcoffee.R
import edu.olexandergalaktionov.apirestcoffee.databinding.ActivityMainBinding
import edu.olexandergalaktionov.apirestcoffee.model.LoginState
import edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail.CoffeeDetail
import edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail.CoffeeViewModel
import edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail.CoffeeViewModelFactory
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import edu.olexandergalaktionov.apirestcoffee.utils.checkConnection
import edu.olexandergalaktionov.apirestcoffee.utils.dataStore
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

    override fun onStart() {
        super.onStart()
        loadCoffees()
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

        // Verifica automáticamente si hay sesión activa al iniciar la actividad
        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            if (token == null) {
                showLoginDialog()
            } else {
                loadCoffees()
            }
        }

        lifecycleScope.launch {
            vm.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${state.response.token}")
                        updateToolbarMenu()
                        lifecycleScope.launch {
                            val token = SessionManager(dataStore).sessionFlow.first().first
                            if (token != null) {
                                loadCoffees()
                            }
                        }
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
                Log.i("SESSION", if (token != null) "Usuario autenticado: $username" else "No hay usuario autenticado")
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CoffeeAdapter(emptyList()) { }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                val token = SessionManager(dataStore).sessionFlow.first().first
                if (token == null) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@MainActivity, "No has iniciado sesión", Toast.LENGTH_SHORT).show()
                    showLoginDialog()
                } else {
                    loadCoffees()
                }
            }
        }

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
                        clearCoffees("Sesión cerrada correctamente.")
                    }
                    true
                }

                else -> false
            }
        }

        binding.mToolbar.setNavigationOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Acerca de")
                .setMessage("""
                    Autor: Olexandr Galaktionov Tsisar
                    Grupo: 2º DAM/DAW
                    Asignatura: Programación Multimedia y Dispositivos Móviles
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

            binding.swipeRefresh.isRefreshing = true

            // COMPROBAR CONEXIÓN
            if (!checkConnection(this@MainActivity)) {
                clearCoffees("Sin conexión a Internet. Verifica tu red.")
                return@launch
            }

            val sessionManager = SessionManager(dataStore)
            val token = sessionManager.sessionFlow.first().first

            if (token == null) {
                clearCoffees("No has iniciado sesión.")
                return@launch
            }

            try {
                viewModel.fetchCoffees()

                viewModel.coffeeList.collect { coffeeList ->
                    if (coffeeList != null) {
                        if (coffeeList.isEmpty()) {
                            clearCoffees("No hay cafés disponibles.")
                        } else {
                            val adapter = CoffeeAdapter(coffeeList) { selectedCoffee ->
                                val intent = Intent(this@MainActivity, CoffeeDetail::class.java)
                                intent.putExtra("coffeeId", selectedCoffee.id)
                                startActivity(intent)
                            }
                            binding.recyclerView.adapter = adapter
                            binding.tvEmpty.visibility = View.GONE
                        }

                        // Paramos el swipe refresh después de cargar los datos correctamente
                        binding.swipeRefresh.isRefreshing = false
                        return@collect // detenemos el collect después de la primera respuesta
                    }
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    sessionManager.clearSession()
                    vm.logout()
                    updateToolbarMenu()
                    clearCoffees("Sesión expirada. Por favor, inicia sesión de nuevo.")
                } else {
                    clearCoffees("Error al obtener cafés: ${e.message}")
                }
            } catch (e: Exception) {
                clearCoffees("Error inesperado: ${e.message}")
            } finally {
                // Por seguridad, aseguramos que se desactive el refresh si no se detuvo antes
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }


    private fun clearCoffees(message: String) {
        binding.recyclerView.adapter = CoffeeAdapter(emptyList()) {}
        binding.tvEmpty.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

                if (!checkConnection(this)) {
                    Toast.makeText(this, "Sin conexión a Internet. Verifica tu red.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                vm.login(username, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

