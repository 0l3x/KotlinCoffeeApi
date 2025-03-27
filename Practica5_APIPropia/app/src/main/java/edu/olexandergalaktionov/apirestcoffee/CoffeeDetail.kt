package edu.olexandergalaktionov.apirestcoffee

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.olexandergalaktionov.apirestcoffee.data.Retrofit2Api
import edu.olexandergalaktionov.apirestcoffee.databinding.ActivityCoffeeDetailBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import edu.olexandergalaktionov.apirestcoffee.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CoffeeDetail : AppCompatActivity() {
    private lateinit var binding: ActivityCoffeeDetailBinding
    private var coffeeId = -1
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCoffeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        coffeeId = intent.getIntExtra("coffeeId", -1)
        if (coffeeId == -1) {
            finish()
            return
        }

        binding.recyclerComments.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val sessionManager = SessionManager(dataStore)
            token = sessionManager.sessionFlow.first().first

            if (token != null) {
                loadCoffeeDetail()
                loadComments()
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                loadComments()
            }
        }
    }

    private suspend fun loadCoffeeDetail() {
        try {
            val response = Retrofit2Api.getRetrofit2Api().getCoffeeById("Bearer $token", coffeeId)
            binding.tvCoffeeName.text = response.coffeeName

            val htmlDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(response.coffeeDesc, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(response.coffeeDesc)
            }
            binding.tvCoffeeDesc.text = htmlDesc

        } catch (e: Exception) {
            Log.e("DETAIL", "Error cargando detalle: ${e.message}")
            binding.tvCoffeeDesc.text = "No se pudo cargar el detalle"
        }
    }

    private suspend fun loadComments() {
        binding.swipeRefresh.isRefreshing = true

        try {
            val commentList: List<CoffeeComments> =
                Retrofit2Api.getRetrofit2Api().getComments("Bearer $token", coffeeId)

            val commentAdapter = CommentAdapter(commentList)
            binding.recyclerComments.adapter = commentAdapter

        } catch (e: Exception) {
            Log.e("DETAIL", "Error cargando comentarios: ${e.message}")
        } finally {
            binding.swipeRefresh.isRefreshing = false
        }
    }
}
