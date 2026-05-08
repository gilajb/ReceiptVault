package com.receiptvault.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.databinding.ItemReceiptBinding
import com.receiptvault.utils.toCurrencyString
import com.receiptvault.utils.toDisplayDate

class ReceiptAdapter(
    private val onItemClick: (Receipt) -> Unit
) : ListAdapter<Receipt, ReceiptAdapter.ReceiptViewHolder>(ReceiptDiffCallback()) {

    inner class ReceiptViewHolder(private val binding: ItemReceiptBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(receipt: Receipt) {
            binding.tvMerchant.text = receipt.merchant
            binding.tvDate.text = receipt.date.toDisplayDate()
            binding.tvAmount.text = receipt.amount.toCurrencyString()
            binding.tvCategory.text = receipt.category
            binding.syncIndicator.alpha = if (receipt.syncedToCloud) 1f else 0.3f

            binding.root.setOnClickListener { onItemClick(receipt) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemReceiptBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ReceiptDiffCallback : DiffUtil.ItemCallback<Receipt>() {
    override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt) = oldItem == newItem
}
