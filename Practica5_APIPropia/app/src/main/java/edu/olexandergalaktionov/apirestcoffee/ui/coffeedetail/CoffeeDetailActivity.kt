package edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail

import android.os.Build
import android.os.Bundle
import android.text.Html
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
import edu.olexandergalaktionov.apirestcoffee.R
import edu.olexandergalaktionov.apirestcoffee.data.CoffeeRepository
import edu.olexandergalaktionov.apirestcoffee.databinding.ActivityCoffeeDetailBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments
import edu.olexandergalaktionov.apirestcoffee.utils.SessionManager
import edu.olexandergalaktionov.apirestcoffee.utils.checkConnection
import edu.olexandergalaktionov.apirestcoffee.utils.dataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Class CoffeeDetailActivity.kt
 *
 * Activity to display detailed information of a selected coffee and its comments.
 * Allows users to view details, refresh comments and add new comments.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class CoffeeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoffeeDetailBinding
    private var coffeeId = -1 // to prevent errors of coffeeId not being set
    private var token: String? = null

    // ViewModel with repository injected via SessionManager using dataStore
    private val viewModel: CoffeeViewModel by viewModels {
        CoffeeViewModelFactory(CoffeeRepository(SessionManager(dataStore)))
    }

    /**
     * Called when the activity is starting.
     * Sets up the view, loads coffee data and initializes observers.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCoffeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets for full screen layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get coffee ID from intent
        coffeeId = intent.getIntExtra("coffeeId", -1)
        if (coffeeId == -1) {
            finish()
            return // if coffeeId is not set, finish the activity
        }

        // Set up RecyclerView for displaying comments
        binding.recyclerComments.layoutManager = LinearLayoutManager(this)

        // Pull-to-refresh functionality
        binding.swipeRefresh.setOnRefreshListener {
            token?.let { t -> viewModel.loadComments(t, coffeeId) }
        }

        // Set comment button logic
        binding.btnAddComment.setOnClickListener { showAddCommentDialog() }

        // Load session token and then load coffee data
        lifecycleScope.launch {
            val session = SessionManager(dataStore).sessionFlow.first()
            token = session.first

            if (token != null) {
                viewModel.loadCoffeeDetail(token!!, coffeeId)
                viewModel.loadComments(token!!, coffeeId)
            }
        }

        // Observe changes in ViewModel and update UI
        observeViewModel()
    }

    /**
     * Observes data from ViewModel and updates the UI.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.coffeeDetail.collectLatest { detail ->
                detail?.let {
                    binding.tvCoffeeName.text = it.coffeeName
                    val htmlDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(it.coffeeDesc, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        Html.fromHtml(it.coffeeDesc)
                    }
                    binding.tvCoffeeDesc.text = htmlDesc
                }
            }
        }

        lifecycleScope.launch {
            viewModel.comments.collectLatest { comments ->
                binding.recyclerComments.adapter = CommentAdapter(comments)
            }
        }

        lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { refreshing ->
                binding.swipeRefresh.isRefreshing = refreshing
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { msg ->
                msg?.let {
                    Toast.makeText(this@CoffeeDetailActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Opens a dialog for the user to write and publish a comment.
     * Validates input and triggers postComment on ViewModel.
     */
    private fun showAddCommentDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_comment, null)
        val etComment = view.findViewById<EditText>(R.id.etComment)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuevo comentario")
            .setView(view)
            .setPositiveButton(getString(R.string.accept), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val commentText = etComment.text.toString().trim()

                if (commentText.isBlank()) {
                    Toast.makeText(this, getString(R.string.no_empty_comment), Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        if (!checkConnection(this@CoffeeDetailActivity)) {
                            Toast.makeText(this@CoffeeDetailActivity, "Sin conexión", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val user = SessionManager(dataStore).sessionFlow.first().second ?: "anonymous"
                        val comment = CoffeeComments(0, coffeeId, user, commentText)

                        token?.let { t ->
                            viewModel.postComment(
                                token = t,
                                comment = comment,
                                onSuccess = {
                                    Toast.makeText(this@CoffeeDetailActivity,
                                        getString(R.string.comment_posted), Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(this@CoffeeDetailActivity, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
