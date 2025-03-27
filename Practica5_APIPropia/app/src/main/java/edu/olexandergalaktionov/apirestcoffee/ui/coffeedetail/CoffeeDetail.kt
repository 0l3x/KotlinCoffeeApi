package edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.EditText
import android.widget.Toast
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
import androidx.appcompat.app.AlertDialog
import edu.olexandergalaktionov.apirestcoffee.R

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

        binding.btnAddComment.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_add_comment, null)
            val etComment = view.findViewById<EditText>(R.id.etComment)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Nuevo comentario")
                .setView(view)
                .setPositiveButton("Publicar", null) // <- Ojo, null para sobreescribir luego
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val comment = etComment.text.toString().trim()

                    if (comment.isBlank()) {
                        Toast.makeText(this, "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        lifecycleScope.launch {
                            val sessionManager = SessionManager(dataStore)
                            val user = sessionManager.sessionFlow.first().second ?: "anon"
                            postComment(user, comment)
                        }
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
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

    private fun postComment(user: String, comment: String) {
        lifecycleScope.launch {
            val sessionManager = SessionManager(dataStore)
            val token = sessionManager.sessionFlow.first().first
            val coffeeId = intent.getIntExtra("coffeeId", -1)

            if (token != null && coffeeId != -1) {
                try {
                    val newComment = CoffeeComments(id = 0, idCoffee = coffeeId, user = user, comment = comment)
                    val response = Retrofit2Api.getRetrofit2Api().postComment("Bearer $token", newComment)
                    Log.d("POST_COMMENT", "Code: ${response.code()}, Body: ${response.body()}, Error: ${response.errorBody()?.string()}")
                    if (response.isSuccessful) {
                        Toast.makeText(this@CoffeeDetail, "Comentario publicado", Toast.LENGTH_SHORT).show()
                        loadComments() // <-- actualiza comentarios
                        Log.d("POST_COMMENT", "Response: ${response.code()} - ${response.body()}")
                    } else {
                        Toast.makeText(this@CoffeeDetail, "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CoffeeDetail, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
