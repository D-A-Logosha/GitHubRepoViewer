package com.example.githubrepoviewer.ui.repositories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.githubrepoviewer.R
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.databinding.FragmentContainerBinding
import com.example.githubrepoviewer.databinding.FragmentRepositoriesListBinding
import com.example.githubrepoviewer.databinding.LayoutConnectionErrorBinding
import com.example.githubrepoviewer.databinding.LayoutEmptyBinding
import com.example.githubrepoviewer.databinding.LayoutLoadingBinding
import com.example.githubrepoviewer.databinding.LayoutSomethingErrorBinding
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
    private val connectionErrorBinding by lifecycleLazy {
        LayoutConnectionErrorBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val somethingErrorBinding by lifecycleLazy {
        LayoutSomethingErrorBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val emptyBinding by lifecycleLazy {
        LayoutEmptyBinding.inflate(layoutInflater, binding.contentContainer, true)
    }

    private val viewModel: RepositoriesListViewModel by viewModels()

    private val adapter by lazy {
        RepositoryAdapter(object : RepositoryActionListener {
            override fun selectRepository(checkRepo: Repo) {
                Log.d("RepositoriesList", "$checkRepo")
                val bundle = Bundle().apply {
                    putString("repoId", checkRepo.id.toString())
                    putString("repoName", checkRepo.name)
                }
                findNavController().navigate(
                    R.id.action_repositoriesListFragment_to_detailInfoFragment, bundle
                )
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

                    is RepositoriesListViewModel.State.Error -> {
                        if (state.error == "UnknownHostException") {
                            connectionErrorBinding.button.setOnClickListener { viewModel.loadRepositories() }
                            indexOfChild(connectionErrorBinding.root)
                        } else {
                            somethingErrorBinding.button.setOnClickListener { viewModel.loadRepositories() }
                            somethingErrorBinding.tvMessage.text = state.error
                            indexOfChild(somethingErrorBinding.root)
                        }
                    }

                    RepositoriesListViewModel.State.Empty -> {
                        emptyBinding.button.setOnClickListener { viewModel.loadRepositories() }
                        indexOfChild(emptyBinding.root)
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        if (binding.appBar.toolbar.menu.size() == 0) {
            binding.appBar.toolbar.inflateMenu(R.menu.menu_appbar)
        }
        if (parentFragmentManager.backStackEntryCount == 0) {
            binding.appBar.toolbar.navigationIcon = null
        } else {
            val navController = findNavController()
            val appBarConfiguration = AppBarConfiguration(setOf(binding.appBar.toolbar.id))
            binding.appBar.toolbar.setupWithNavController(navController, appBarConfiguration)
        }
        binding.appBar.toolbar.title = getString(R.string.repositories)
        binding.appBar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    findNavController().navigate(R.id.action_repositoriesListFragment_to_authFragment)
                    true
                }

                else -> {
                    throw IllegalStateException("Toolbar: Unknown menu item clicked")
                }
            }
        }
    }
}
