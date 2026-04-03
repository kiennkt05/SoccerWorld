package com.example.soccerworld.ui.team.team_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

import com.example.soccerworld.databinding.FragmentTeamDetailBinding
import com.example.soccerworld.ui.team.team_detail.player.PlayerFragment
import com.example.soccerworld.ui.team.team_detail.transfer.TransferFragment
import com.example.soccerworld.util.Constant

class TeamDetailFragment : Fragment() {

    private var _binding: FragmentTeamDetailBinding? = null
    private val binding get() = _binding!!
    private val args: TeamDetailFragmentArgs by navArgs()
    private var teamId = 0
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamId = args.teamId
        setupIU()
    }

    private fun setupIU(){
        setupViewPager()
        binding.tabLayoutTeam.setupWithViewPager(binding.viewPagerTeam)
    }

    private fun setupViewPager(){
        val adapter = childFragmentManager.let { TeamDetailPagerAdapter(it) }

        bundle.putInt(Constant.TEAM_ID, teamId)

        val playerFragment = PlayerFragment()
        val transferFragment = TransferFragment()
        playerFragment.arguments = bundle
        transferFragment.arguments = bundle

        adapter.apply {
            addFragment(playerFragment, "Players")
            addFragment(transferFragment, "Transfer")
        }
        binding.viewPagerTeam.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
