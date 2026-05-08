package com.receiptvault.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.receiptvault.R
import com.receiptvault.databinding.FragmentSignupBinding
import com.receiptvault.utils.gone
import com.receiptvault.utils.showToast
import com.receiptvault.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (password != confirm) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }
            viewModel.signUpWithEmail(email, password)
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSignup.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.gone()
                    findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                }
                is AuthState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSignup.isEnabled = true
                    showToast(state.message)
                    viewModel.resetState()
                }
                else -> {
                    binding.progressBar.gone()
                    binding.btnSignup.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
