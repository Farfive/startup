package com.example.styleap.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleap.databinding.FragmentCompanyProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompanyProfileFragment : Fragment() {

    private var _binding: FragmentCompanyProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CompanyProfileViewModel by viewModels()
    private lateinit var employeeAdapter: EmployeeListAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        
        // Get arguments if passed from registration or other screens
        arguments?.let { args ->
            args.getString("companyName")?.let { name ->
                viewModel.setCompanyName(name)
            }
            args.getString("companyType")?.let { type ->
                viewModel.setCompanyType(type)
            }
            args.getStringArrayList("employees")?.let { employees ->
                viewModel.setEmployees(employees)
            }
            args.getInt("points", 0).let { points ->
                viewModel.setPoints(points)
            }
        }
        
        setupObservers()
        loadCompanyData()
    }
    
    private fun setupRecyclerView() {
        employeeAdapter = EmployeeListAdapter()
        binding.recyclerViewEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.companyName.observe(viewLifecycleOwner, Observer { name ->
            binding.textViewCompanyName.text = name
        })
        
        viewModel.companyType.observe(viewLifecycleOwner, Observer { type ->
            binding.textViewCompanyType.text = type
        })
        
        viewModel.employees.observe(viewLifecycleOwner, Observer { employees ->
            employeeAdapter.submitList(employees)
        })
        
        viewModel.points.observe(viewLifecycleOwner, Observer { points ->
            binding.textViewCompanyPoints?.text = "$points points"
        })
    }
    
    private fun loadCompanyData() {
        // Load company data from repository
        viewModel.loadCompanyData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
