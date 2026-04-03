package com.example.soccerworld.ui.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentTeamBinding
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.ui.team.TeamFragmentDirections

class TeamFragment : BaseVMFragment<TeamViewModel>() {

    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customPreferences = CustomSharedPreferences(requireContext().applicationContext)
        val leagueId = customPreferences.getCountryId()

        viewModel.getAllTeamsOfLeague(leagueId!!)
        viewModel.teamsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvTeamsList.layoutManager = LinearLayoutManager(this.context)
                binding.rvTeamsList.adapter = TeamAdapter(it){ team ->
                    Toast.makeText(context,"Tıklandı", Toast.LENGTH_SHORT).show()
                    val action = TeamFragmentDirections.actionTeamFragmentToTeamDetailFragment(team.teamId)
                    Navigation.findNavController(view).navigate(action)
                }
            }
        })

        viewModel.loadingTeamList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.rvTeamsList.visibility = View.GONE
                    binding.progressBarTeamsList.visibility = View.VISIBLE
                }else{
                    binding.rvTeamsList.visibility = View.VISIBLE
                    binding.progressBarTeamsList.visibility = View.GONE
                }
            }
        })

    }

    override fun getViewModel(): Class<TeamViewModel> = TeamViewModel::class.java

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
