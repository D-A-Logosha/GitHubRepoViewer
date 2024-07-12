package com.example.githubrepoviewer.ui.detail

import android.net.Uri
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
import com.example.githubrepoviewer.databinding.FragmentContainerBinding
import com.example.githubrepoviewer.databinding.FragmentDetailInfoBinding
import com.example.githubrepoviewer.databinding.LayoutConnectionErrorBinding
import com.example.githubrepoviewer.databinding.LayoutLoadingBinding
import com.example.githubrepoviewer.databinding.LayoutSomethingErrorBinding
import com.example.githubrepoviewer.ui.lifecycleLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailInfoFragment : Fragment() {

    private var binding: FragmentContainerBinding by lifecycleLazy()

    private val loadingBinding by lifecycleLazy {
        LayoutLoadingBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val detailInfoBinding by lifecycleLazy {
        FragmentDetailInfoBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val connectionErrorBinding by lifecycleLazy {
        LayoutConnectionErrorBinding.inflate(layoutInflater, binding.contentContainer, true)
    }
    private val somethingErrorBinding by lifecycleLazy {
        LayoutSomethingErrorBinding.inflate(layoutInflater, binding.contentContainer, true)
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
        val repoId = arguments?.getString("repoId") ?: ""
        repoName = arguments?.getString("repoName") ?: ""
        Log.d("DetailInfoFragment", repoId)
        viewModel.loadRepository(repoId)
        setupToolbar()

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.contentContainer.apply {
                displayedChild = when (state) {
                    is RepositoryInfoViewModel.State.Loading -> {
                        indexOfChild(loadingBinding.root)
                    }

                    is RepositoryInfoViewModel.State.Error -> {
                        if (state.error == "UnknownHostException") {
                            connectionErrorBinding.button.setOnClickListener { viewModel.loadRepository(repoId) }
                            indexOfChild(connectionErrorBinding.root)
                        } else {
                            somethingErrorBinding.button.setOnClickListener { viewModel.loadRepository(repoId) }
                            somethingErrorBinding.tvMessage.text = state.error
                            indexOfChild(somethingErrorBinding.root)
                        }
                    }
                    is RepositoryInfoViewModel.State.Loaded -> {
                        val cleanedUrl = Uri.parse(state.githubRepo.htmlUrl).run {
                            "${host?.takeIf { it.startsWith("www.") }?.substring(4) ?: host}${path}"
                        }
                        detailInfoBinding.tvLink.text = cleanedUrl
                        state.githubRepo.htmlUrl
                        if (state.githubRepo.license?.isEmpty() != false) {
                            detailInfoBinding.groupLicense.visibility = View.GONE
                        } else {
                            detailInfoBinding.tvLicense.text = state.githubRepo.license
                            detailInfoBinding.groupLicense.visibility = View.VISIBLE
                        }
                        detailInfoBinding.tvStars.text = state.githubRepo.stargazersCount.toString()
                        detailInfoBinding.tvForks.text = state.githubRepo.forksCount.toString()
                        detailInfoBinding.tvWatchers.text =
                            state.githubRepo.watchersCount.toString()
                        detailInfoBinding.readmeContainer.apply {
                            displayedChild = indexOfChild(detailInfoBinding.pbReadme)
                        }
                        indexOfChild(detailInfoBinding.root)
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
