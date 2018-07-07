package com.duzi.kotlinsample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.duzi.kotlinsample.api.model.GithubRepo
import com.duzi.kotlinsample.callback.ItemClickInterface
import kotlinx.android.synthetic.main.item_repository.view.*

/**
 * Created by KIM on 2018-07-06.
 */

class SearchAdapter(itemClickListener: ItemClickInterface): RecyclerView.Adapter<SearchAdapter.Holder>() {

    private var itemClickListener = itemClickListener
    private var items: MutableList<GithubRepo> = mutableListOf()

    override fun onBindViewHolder(holder: Holder, position: Int) {
        items[position].let { repo ->
            with(holder.itemView) {
                GlideApp.with(context)
                        .load(repo.owner.avatar_url)
                        .into(ivItemRepositoryProfile)

                tvItemRepositoryName.text = repo.name
                tvItemRepositoryLanguage.text = repo.language

                setOnClickListener { itemClickListener.itemClick(repo) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(parent)

    inner class Holder(parent: ViewGroup)
        : RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false))

    fun clearItems() {
        items = arrayListOf()
    }

    fun setItems(items: List<GithubRepo>) {
        this.items = items.toMutableList()
    }

}