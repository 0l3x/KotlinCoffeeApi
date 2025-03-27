package edu.olexandergalaktionov.apirestcoffee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCommentBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeComments

class CommentAdapter(private val commentList: List<CoffeeComments>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CoffeeComments) {
            binding.tvUser.text = comment.user
            binding.tvComment.text = comment.comment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentList[position])
    }

    override fun getItemCount(): Int = commentList.size
}
