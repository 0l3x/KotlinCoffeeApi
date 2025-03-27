package edu.olexandergalaktionov.apirestcoffee.ui.coffeedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCommentBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments

/**
 * Class CommentAdapter.kt
 *
 * Adapter to display a list of comments related to a coffee using ListAdapter.
 * Binds each comment item to its corresponding view using ViewBinding.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class CommentAdapter :
    ListAdapter<CoffeeComments, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    /**
     * ViewHolder class that binds a CoffeeComments object to a view.
     *
     * @param binding Binding object for the comment item layout.
     */
    class CommentViewHolder(private val binding: ItemCommentBinding) :
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
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil callback for CoffeeComments to optimize list updates.
 */
class CommentDiffCallback : DiffUtil.ItemCallback<CoffeeComments>() {
    override fun areItemsTheSame(oldItem: CoffeeComments, newItem: CoffeeComments): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CoffeeComments, newItem: CoffeeComments): Boolean {
        return oldItem == newItem
    }
}
