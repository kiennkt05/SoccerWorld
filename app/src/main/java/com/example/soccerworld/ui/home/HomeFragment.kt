package com.example.soccerworld.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.example.soccerworld.databinding.FragmentHomeBinding
import com.example.soccerworld.ui.home.leaguetable.LeagueTableFragment
import com.example.soccerworld.ui.home.topscorer.TopScorerFragment
import com.example.soccerworld.util.CustomSharedPreferences


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var customPreferences = CustomSharedPreferences(requireContext())

        val a = customPreferences.getCountryId()

        Toast.makeText(requireContext(), " id : "+a, Toast.LENGTH_SHORT).show()
        setupUI()

    }

    private fun setupUI(){
        setupViewpager()
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setupViewpager() {
        val adapter = childFragmentManager.let { ViewPagerAdapter(it) }
        adapter.apply {
            addFragment(LeagueTableFragment(), "League Table")
            addFragment(TopScorerFragment(), "Top Scorers")
        }
        binding.viewPager.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
