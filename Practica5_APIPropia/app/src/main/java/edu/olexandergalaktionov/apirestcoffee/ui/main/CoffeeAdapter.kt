package edu.olexandergalaktionov.apirestcoffee.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCoffeeBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList

/**
 * Class CoffeeAdapter.kt
 *
 * Adapter to display a list of coffees in a RecyclerView using ListAdapter.
 * Each item displays the name and number of comments.
 * Clicking on an item triggers the detail screen.
 *
 * @param onItemClick Callback function executed when an item is clicked.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class CoffeeAdapter(
    private val onItemClick: (CoffeeList) -> Unit
) : ListAdapter<CoffeeList, CoffeeAdapter.CoffeeViewHolder>(CoffeeDiffCallback()) {

    /**
     * ViewHolder class for coffee items.
     *
     * @param binding The view binding for the item layout.
     */
    class CoffeeViewHolder(private val binding: ItemCoffeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds coffee data to the UI components.
         *
         * @param coffee The coffee object to display.
         * @param onItemClick Callback to execute on item click.
         */
        fun bind(coffee: CoffeeList, onItemClick: (CoffeeList) -> Unit) {
            binding.tvCoffeeName.text = coffee.coffeeName ?: "Sin nombre"
            binding.tvComments.text = "Comentarios: ${coffee.comments}"

            binding.root.setOnClickListener {
                onItemClick(coffee)
            }
        }
    }

    /**
     * Inflates the layout and creates a ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val binding = ItemCoffeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoffeeViewHolder(binding)
    }

    /**
     * Binds coffee data to a ViewHolder.
     */
    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}

/**
 * DiffUtil callback for CoffeeList to optimize list updates.
 */
class CoffeeDiffCallback : DiffUtil.ItemCallback<CoffeeList>() {
    override fun areItemsTheSame(oldItem: CoffeeList, newItem: CoffeeList): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CoffeeList, newItem: CoffeeList): Boolean {
        return oldItem == newItem
    }
}
