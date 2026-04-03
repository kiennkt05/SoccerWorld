package com.example.soccerworld.ui.fixture.detail.statistic

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.example.soccerworld.R
import com.example.soccerworld.base.BaseFragment
import com.example.soccerworld.databinding.FragmentStatisticBinding
import com.example.soccerworld.model.fixture.Fixture
import com.example.soccerworld.util.Constant

class StatisticFragment : BaseFragment<FragmentStatisticBinding, StatisticViewModel>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val fixtureIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS, Fixture::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS)
        }

        fixtureIds?.let {
            viewModel.getFixtureStatistics(it.fixtureId)
        }

        viewModel.fixtureStatic.observe(viewLifecycleOwner, Observer {
            it?.let {
                dataBinding.statistic = it
                dataBinding.pbSOGHome.max = it.shotsOnGoal.home.toInt()+it.shotsOnGoal.away.toInt()
                dataBinding.pbSOGAway.max = it.shotsOnGoal.home.toInt()+it.shotsOnGoal.away.toInt()
                dataBinding.pbSOGHome.progress = it.shotsOnGoal.home.toInt()
                dataBinding.pbSOGAway.progress = it.shotsOnGoal.away.toInt()

                dataBinding.pbOffGHome.max = it.shotsOffGoal.home.toInt()+it.shotsOffGoal.away.toInt()
                dataBinding.pbOffGAway.max = it.shotsOffGoal.home.toInt()+it.shotsOffGoal.away.toInt()
                dataBinding.pbOffGHome.progress = it.shotsOffGoal.home.toInt()
                dataBinding.pbOffGAway.progress = it.shotsOffGoal.away.toInt()

            }
        })

    }

    override fun getResourceLayout(): Int = R.layout.fragment_statistic

    override fun getViewModel(): Class<StatisticViewModel> = StatisticViewModel::class.java

}
