package com.example.githubrepoviewer.ui.repositories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.githubrepoviewer.R
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.databinding.FragmentContainerBinding
import com.example.githubrepoviewer.databinding.FragmentRepositoriesListBinding
import com.example.githubrepoviewer.databinding.LayoutLoadingBinding
import com.example.githubrepoviewer.ui.lifecycleLazy
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RepositoriesListFragment : Fragment() {

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

    private var binding: FragmentContainerBinding by lifecycleLazy()

    private val loadingBinding by lifecycleLazy {
        LayoutLoadingBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val repositoriesListBinding by lifecycleLazy {
        FragmentRepositoriesListBinding.inflate(layoutInflater, binding.contentContainer, true)
    }

    private val viewModel: RepositoriesListViewModel by viewModels()

    private val adapter by lazy {
        RepositoryAdapter(object : RepositoryActionListener {
            override fun selectRepository(checkRepo: Repo) {
                Log.d("RepositoriesList", "$checkRepo")
            }
        }, resourcesProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        viewModel.loadRepositories()
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.contentContainer.apply {
                displayedChild = when (state) {
                    is RepositoriesListViewModel.State.Loading -> {
                        indexOfChild(loadingBinding.root)
                    }

                    is RepositoriesListViewModel.State.Loaded -> {
                        repositoriesListBinding.rvRepositories.adapter = adapter
                        adapter.repos = state.repos
                        indexOfChild(repositoriesListBinding.root)
                    }

                    is RepositoriesListViewModel.State.Error -> TODO()
                    RepositoriesListViewModel.State.Empty -> TODO()


                }
            }
        }
    }

    private fun setupToolbar() {
        binding.appBar.toolbar.title = getString(R.string.repositories)

        binding.appBar.toolbar.inflateMenu(R.menu.menu_appbar)

        binding.appBar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    findNavController().navigate(R.id.action_repositoriesListFragment_to_authFragment)
                    true
                }

                else -> {
                    Log.d("Toolbar", "Unknown menu item clicked")
                    false
                }
            }
        }
    }
}
