package com.jinny.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jinny.model.CardItem
import com.jinny.soloescape.databinding.ItemCardBinding
import com.jinny.soloescape.databinding.MatchListBinding

class MatchListAdapter : ListAdapter<CardItem, MatchListAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val viewBinding: MatchListBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(cardItem: CardItem) {
            viewBinding.userNameTextView.text = cardItem.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MatchListBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: MatchListAdapter.ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CardItem>() {
            override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem) =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem) =
                oldItem.userID == newItem.userID
        }
    }
}