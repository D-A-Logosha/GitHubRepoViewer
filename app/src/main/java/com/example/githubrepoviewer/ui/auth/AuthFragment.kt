package com.example.githubrepoviewer.ui.auth

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.githubrepoviewer.R
import com.example.githubrepoviewer.databinding.FragmentAuthBinding
import com.example.githubrepoviewer.ui.lifecycleLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private var binding: FragmentAuthBinding by lifecycleLazy()

    private val viewModel: AuthViewModel by viewModels()

    private val inputMethodManager by lazy {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isLogout = arguments?.getBoolean("isLogout") ?: false
        if (isLogout) {
            viewModel.logout()
        }
        else {
            viewModel.init()
        }
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            root.setFocusableInTouchMode(true)
            root.setOnClickListener {
                if (etTokenInput.hasFocus()) etTokenInput.clearFocus()
            }
            etTokenInput.nextFocusForwardId = btnSignIn.id
            btnSignIn.nextFocusForwardId = etTokenInput.id
            etTokenInput.clearFocus()
        }

        binding.etTokenInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            val token = binding.etTokenInput.text.toString()
            viewModel.onTokenChanged(token, hasFocus)
            if (hasFocus) binding.etTokenInput.showKeyboard()
            else binding.etTokenInput.hideKeyboard()
        }

        binding.etTokenInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val token = s.toString()
                viewModel.onTokenChanged(token, binding.etTokenInput.hasFocus())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSignIn.setOnClickListener {
            if (binding.etTokenInput.hasFocus()) binding.etTokenInput.clearFocus()
            viewModel.onSignButtonPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.token.observe(viewLifecycleOwner) {
            if (viewModel.token.value != binding.etTokenInput.text.toString())
                binding.etTokenInput.setText(viewModel.token.value)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (viewModel.state.value) {
                is AuthViewModel.State.Idle -> idleState()
                is AuthViewModel.State.TokenInput -> inputState((viewModel.state.value as AuthViewModel.State.TokenInput).isTooShort)
                is AuthViewModel.State.InvalidInput -> invalidInputState((viewModel.state.value as AuthViewModel.State.InvalidInput).reason)
                is AuthViewModel.State.Loading -> loadingState()
                null -> {}
            }
        }

        lifecycleScope.launch {
            viewModel.actions.collect { action ->
                when (action) {
                    is AuthViewModel.Action.ShowError -> {
                        val dialog =
                            AlertDialogFragment.newInstance(
                                getString(R.string.error),
                                action.message
                            )
                        dialog.show(childFragmentManager, "auth_alert_dialog")
                    }

                    is AuthViewModel.Action.RouteToMain -> {
                        findNavController().navigate(R.id.action_authFragment_to_repositoriesListFragment)
                    }
                }
            }
        }
    }

    private fun idleState() {
        binding.tvTokenLabel.visibility = View.INVISIBLE
        binding.etTokenInput.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
        binding.tvTokenError.visibility = View.INVISIBLE
        hideProgressSignIn()
    }

    private fun inputState(isTooShort: Boolean) {
        binding.tvTokenLabel.visibility = View.VISIBLE
        if (isTooShort) {
            val color = ContextCompat.getColor(requireContext(), R.color.secondary)
            binding.tvTokenLabel.setTextColor(color)
            binding.etTokenInput.backgroundTintList = ColorStateList.valueOf(color)
        } else {
            binding.tvTokenLabel.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white_hint)
            )
            binding.etTokenInput.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
        }
        binding.tvTokenError.visibility = View.INVISIBLE
        hideProgressSignIn()
    }

    private fun loadingState() {
        showProgressSignIn()
    }

    private fun invalidInputState(reason: String) {
        val color = ContextCompat.getColor(requireContext(), R.color.error)
        binding.tvTokenLabel.visibility = View.VISIBLE
        binding.tvTokenLabel.setTextColor(color)
        binding.etTokenInput.backgroundTintList =
            ColorStateList.valueOf(color)
        binding.tvTokenError.visibility = View.VISIBLE
        binding.tvTokenError.text =
            getString(
                R.string.invalid_token,
                reason.takeIf { it.isNotBlank() }?.let { ": $it" } ?: "")
        hideProgressSignIn()
    }

    private fun EditText.showKeyboard() {
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun EditText.hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }

    private fun showProgressSignIn() {
        binding.btnSignIn.setTextColor(binding.btnSignIn.currentTextColor and 0x00FFFFFF)
        binding.progressSignIn.visibility = View.VISIBLE
    }

    private fun hideProgressSignIn() {
        binding.btnSignIn.setTextColor(binding.btnSignIn.currentTextColor or (0xFF shl 24))
        binding.progressSignIn.visibility = View.INVISIBLE
    }

}
