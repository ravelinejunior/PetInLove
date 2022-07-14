package com.raveline.petinlove.domain.utils

import androidx.recyclerview.widget.DiffUtil

class ListDiffUtil<T>(
    private val oldList: List<T>?,
    private val newList: List<T>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = if (oldList?.isNotEmpty() == true) oldList.size else 0

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList?.get(oldItemPosition) === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList?.get(oldItemPosition) === newList[newItemPosition]
    }

}