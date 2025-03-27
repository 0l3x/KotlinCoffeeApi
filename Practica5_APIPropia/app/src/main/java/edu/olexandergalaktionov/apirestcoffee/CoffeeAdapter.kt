package edu.olexandergalaktionov.apirestcoffee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCoffeeBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList

/**
 * Class CoffeeAdapter.kt
 * Adapter to display a list of coffees in a RecyclerView.
 * Each item is clickable to trigger a detail view.
 *
 * @param coffeeList List of CoffeeList objects to display.
 * @param onItemClick Callback invoked when an item is clicked.
 */
class CoffeeAdapter(
    private val coffeeList: List<CoffeeList>,
    private val onItemClick: (CoffeeList) -> Unit
) : RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    class CoffeeViewHolder(private val binding: ItemCoffeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coffee: CoffeeList, onItemClick: (CoffeeList) -> Unit) {
            binding.tvCoffeeName.text = coffee.coffeeName ?: "Sin nombre"
            binding.tvComments.text = "Comentarios: ${coffee.comments}"

            // Evento de clic
            binding.root.setOnClickListener {
                onItemClick(coffee)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val binding = ItemCoffeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoffeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        holder.bind(coffeeList[position], onItemClick)
    }

    override fun getItemCount(): Int = coffeeList.size
}
