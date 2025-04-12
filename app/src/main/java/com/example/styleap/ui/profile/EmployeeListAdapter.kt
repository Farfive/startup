package com.example.styleap.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.styleap.databinding.ItemEmployeeBinding

class EmployeeListAdapter : ListAdapter<String, EmployeeListAdapter.EmployeeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = getItem(position)
        holder.bind(employee)
    }

    class EmployeeViewHolder(private val binding: ItemEmployeeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(employee: String) {
            binding.textViewEmployeeName.text = employee
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 