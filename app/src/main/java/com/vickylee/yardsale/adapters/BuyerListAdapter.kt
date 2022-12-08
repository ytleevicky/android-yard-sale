package com.vickylee.yardsale.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vickylee.yardsale.data.Item
import com.vickylee.yardsale.data.OnItemClickListener
import com.vickylee.yardsale.databinding.ItemBinding
import java.text.SimpleDateFormat

class BuyerListAdapter(
    private val context: Context,
    private val itemList: ArrayList<Item>,
    private val clickListener: OnItemClickListener
) : RecyclerView.Adapter<BuyerListAdapter.ItemViewHolder>() {

    // bind the data with the view
    class ItemViewHolder(var binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: Item, clickListener: OnItemClickListener) {

            binding.tvItemName.setText(currentItem.itemName)
            binding.tvItemPrice.setText("$ ${currentItem.itemPrice}")

            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy").format(currentItem.creationTimestamp.toDate())
            binding.tvPostedDate.setText(formattedDate.toString())

            if (currentItem.isItemAvailable) {
                binding.tvItemStatus.setText("Available")
            } else {
                binding.tvItemStatus.setText("Sold")
            }

            itemView.setOnClickListener {
                clickListener.onItemClicked(currentItem)
            }
        }
    }

    // creates the appearance of view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    // binds the data with view
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(
            itemList.get(position),
            clickListener
        )
    }

    // identifies total number of items
    override fun getItemCount(): Int {
        return itemList.size
    }

}