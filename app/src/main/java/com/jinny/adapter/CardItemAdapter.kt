package com.jinny.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.R
import com.jinny.model.CardItem
import com.jinny.soloescape.databinding.ItemCardBinding

class CardItemAdapter : ListAdapter<CardItem, CardItemAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val viewBinding: ItemCardBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(cardItem: CardItem) {
            viewBinding.nameTextView.text = cardItem.name
            Log.d("ttt", "" + cardItem)
            Glide.with(viewBinding.profileImageView.rootView)
                .load(cardItem.imageUrl)
                .into(viewBinding.profileImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        // binding을 사용하지 않았더라면
        // val inflater = LayoutInflater.from(parent.context)
        // return ViewHolder(inflater.inflate(R.layout.item_card,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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