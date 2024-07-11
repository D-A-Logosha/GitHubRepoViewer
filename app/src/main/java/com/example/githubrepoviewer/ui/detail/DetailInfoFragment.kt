package com.example.githubrepoviewer.ui.detail

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
import com.example.githubrepoviewer.databinding.LayoutLoadingBinding
import com.example.githubrepoviewer.ui.lifecycleLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailInfoFragment : Fragment() {

    private var binding: FragmentContainerBinding by lifecycleLazy()

    private val loadingBinding by lifecycleLazy {
        LayoutLoadingBinding.inflate(layoutInflater, binding.contentContainer, true)
    }

    private val viewModel: RepositoryInfoViewModel by viewModels()

    private lateinit var repoName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repoId = arguments?.getString("repoId")?:""
        repoName = arguments?.getString("repoName")?:""
        Log.d("DetailInfoFragment", repoId)
        viewModel.loadRepository(repoId)
        setupToolbar()

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.contentContainer.apply {
                displayedChild = when (state) {
                    is RepositoryInfoViewModel.State.Loading -> {
                        indexOfChild(loadingBinding.root)
                    }

                    is RepositoryInfoViewModel.State.Error -> TODO()
                    is RepositoryInfoViewModel.State.Loaded -> TODO()
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
        binding.appBar.toolbar.title = repoName
        binding.appBar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    findNavController().navigate(R.id.action_detailInfoFragment_to_authFragment)
                    true
                }

                else -> {
                    throw IllegalStateException("Toolbar: Unknown menu item clicked")
                }
            }
        }
    }
}
