package com.example.githubrepoviewer.ui.repositories

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.databinding.RepositoryItemBinding
import com.example.githubrepoviewer.ui.providers.ResourcesProvider


interface RepositoryActionListener {
    fun selectRepository(checkRepo: Repo)
}

class RepositoryAdapter(
    private val actionListener: RepositoryActionListener,
    private val resourcesProvider: ResourcesProvider,
) :
    RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>(), View.OnClickListener {

    var repos: List<Repo> = emptyList()
        set(newValue) {
            val diffResult = DiffUtil.calculateDiff(RepositoryDiffCallback(field, newValue))
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onClick(v: View) {
        val checkRepo = v.tag as Repo
        actionListener.selectRepository(checkRepo)
    }

    override fun getItemCount(): Int = repos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RepositoryItemBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)

        return RepositoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repo = repos[position]
        holder.itemView.tag = repo
        holder.binding.apply {
            tvRepoName.text = repo.name
            tvRepoDescription.text = repo.description
            tvRepoLanguage.text = repo.language
            tvRepoLanguage.setTextColor(resourcesProvider.getGithubColorForLanguage(repo.language))
        }
    }

    class RepositoryViewHolder(val binding: RepositoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class RepositoryDiffCallback(
        private val oldList: List<Repo>,
        private val newList: List<Repo>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
