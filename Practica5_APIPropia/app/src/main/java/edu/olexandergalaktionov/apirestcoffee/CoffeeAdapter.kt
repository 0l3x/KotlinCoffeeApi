package edu.olexandergalaktionov.apirestcoffee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.apirestcoffee.databinding.ItemCoffeeBinding
import edu.olexandergalaktionov.apirestcoffee.model.CoffeeList

class CoffeeAdapter(private val coffeeList: List<CoffeeList>) :
    RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    class CoffeeViewHolder(private val binding: ItemCoffeeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(coffee: CoffeeList) {
            binding.tvCoffeeName.text = coffee.coffeeName ?: "Sin nombre"
            binding.tvComments.text = "Comentarios: ${coffee.comments}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val binding = ItemCoffeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoffeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        holder.bind(coffeeList[position])
    }

    override fun getItemCount(): Int = coffeeList.size
}
