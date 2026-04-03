package com.example.soccerworld.ui.team.team_detail.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentPlayerBinding
import com.example.soccerworld.util.Constant


class PlayerFragment : BaseVMFragment<PlayerViewModel>() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val playerTeamId = arguments?.getInt(Constant.TEAM_ID)
        Toast.makeText(context,"Team Id Player :  "+playerTeamId, Toast.LENGTH_SHORT).show()

        viewModel.getAllPlayersOfTeam(playerTeamId!!)
        viewModel.playerList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvPlayer.layoutManager = LinearLayoutManager(context)
                binding.rvPlayer.adapter = PlayerAdapter(it){
                    Toast.makeText(requireContext(), "Tıklandı player item ", Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.loadingPlayer.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.rvPlayer.visibility = View.GONE
                    binding.progressBarPlayer.visibility = View.VISIBLE
                }else{
                    binding.rvPlayer.visibility = View.VISIBLE
                    binding.progressBarPlayer.visibility = View.GONE
                }
            }
        })

    }

    override fun getViewModel(): Class<PlayerViewModel> = PlayerViewModel::class.java

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
