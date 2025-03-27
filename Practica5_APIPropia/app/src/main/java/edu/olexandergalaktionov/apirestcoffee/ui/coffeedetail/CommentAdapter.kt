package edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCommentBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments

/**
 * Class CommentAdapter.kt
 *
 * Adapter to display a list of comments related to a coffee in a RecyclerView.
 * Binds each comment item to its corresponding view using ViewBinding.
 *
 * @author Olexandr Galaktionov Tsisar
 *
 * @param commentList List of CoffeeComments to display.
 */
class CommentAdapter(private val commentList: List<CoffeeComments>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    /**
     * ViewHolder class that binds a CoffeeComments object to a view.
     *
     * @param binding Binding object for the comment item layout.
     */
    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds the comment data to the UI components.
         *
         * @param comment The comment to bind.
         */
        fun bind(comment: CoffeeComments) {
            binding.tvUser.text = comment.user
            binding.tvComment.text = comment.comment
        }
    }

    /**
     * Inflates the comment item layout and returns the ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }
    /**
     * Binds the data at the given position to the ViewHolder.
     */
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentList[position])
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int = commentList.size
}
