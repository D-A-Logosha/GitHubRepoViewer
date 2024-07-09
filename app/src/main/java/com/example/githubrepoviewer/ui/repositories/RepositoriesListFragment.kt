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
import com.example.githubrepoviewer.databinding.FragmentContainerBinding
import com.example.githubrepoviewer.databinding.LayoutLoadingBinding
import com.example.githubrepoviewer.ui.lifecycleLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RepositoriesListFragment : Fragment() {

    private var binding: FragmentContainerBinding by lifecycleLazy()

    private val loadingBinding by lifecycleLazy {
        LayoutLoadingBinding.inflate(layoutInflater, binding.contentContainer, true)
    }

    private val viewModel: RepositoriesListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RepositoriesListViewModel.State.Loading -> binding.contentContainer.apply {
                    displayedChild = indexOfChild(loadingBinding.root)
                }

                RepositoriesListViewModel.State.Empty -> TODO()
                is RepositoriesListViewModel.State.Error -> TODO()
                is RepositoriesListViewModel.State.Loaded -> TODO()
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
