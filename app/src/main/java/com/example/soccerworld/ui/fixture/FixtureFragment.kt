package com.example.soccerworld.ui.fixture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.R
import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentFixtureBinding
import com.example.soccerworld.util.Constant
import com.example.soccerworld.util.CustomSharedPreferences


class FixtureFragment : BaseVMFragment<FixtureViewModel>() {

    private var _binding: FragmentFixtureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFixtureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customPreferences = CustomSharedPreferences(requireContext())
        val leagueId = customPreferences.getCountryId()

        viewModel.getAllFixtureOfLeague(leagueId!!)
        viewModel.fixtureList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvFixture.layoutManager = LinearLayoutManager(context)
                binding.rvFixture.adapter = FixtureAdapter(it){ fixtureId ->
                    val bundle = bundleOf(Constant.FIXTURE_TEAM_IDS to fixtureId)
                    Navigation.findNavController(view).navigate(R.id.action_fixtureFragment_to_fixtureDetailFragment, bundle)
                }
            }
        })

        viewModel.loadinFixture.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.rvFixture.visibility = View.GONE
                    binding.progressFixture.visibility = View.VISIBLE
                }else{
                    binding.rvFixture.visibility = View.VISIBLE
                    binding.progressFixture.visibility = View.GONE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getViewModel(): Class<FixtureViewModel> = FixtureViewModel::class.java

}
