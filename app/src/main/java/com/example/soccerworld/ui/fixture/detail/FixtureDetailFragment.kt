package com.example.soccerworld.ui.fixture.detail

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.soccerworld.databinding.FragmentFixtureDetailBinding
import com.example.soccerworld.model.fixture.Fixture
import com.example.soccerworld.ui.fixture.detail.h2h.H2HFragment
import com.example.soccerworld.ui.fixture.detail.statistic.StatisticFragment
import com.example.soccerworld.util.Constant

class FixtureDetailFragment : Fragment() {

    private var _binding: FragmentFixtureDetailBinding? = null
    private val binding get() = _binding!!
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFixtureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS, Fixture::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS)
        }

        binding.tabLayoutFixture.setupWithViewPager(binding.viewPagerFixture)

        val adapter = childFragmentManager.let { BasePagerAdapter(it) }
        bundle.putParcelable(Constant.FIXTURE_TEAM_IDS, result)
        val h2hFragment = H2HFragment()
        val statisticFragment = StatisticFragment()
        h2hFragment.arguments = bundle
        statisticFragment.arguments = bundle
        adapter.apply {
            addFragment(h2hFragment, "H2H")
            addFragment(statisticFragment, "STATS")
        }

        binding.viewPagerFixture.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
