package com.example.soccerworld.ui.home.leaguetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentLeagueTableBinding
import com.example.soccerworld.util.CustomSharedPreferences


class LeagueTableFragment : BaseVMFragment<LeagueTableViewModel>() {

    private var _binding: FragmentLeagueTableBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeagueTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customPreferences = CustomSharedPreferences(requireContext())
        val leagueId = customPreferences.getCountryId()
        Toast.makeText(requireContext(), " id : "+leagueId, Toast.LENGTH_SHORT).show()


        viewModel.getLeagueTable(leagueId!!)
        viewModel.leagueTable.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvLeagueTable.layoutManager = LinearLayoutManager(this.context)
                binding.rvLeagueTable.adapter = LeagueTableAdapter(it){
                    Toast.makeText(requireContext(), " tıklandı", Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.loadingLeagueTable.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.progressBarLeagueTable.visibility = View.VISIBLE
                    binding.rvLeagueTable.visibility = View.GONE
                }else{
                    binding.progressBarLeagueTable.visibility = View.GONE
                    binding.rvLeagueTable.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getViewModel(): Class<LeagueTableViewModel> = LeagueTableViewModel::class.java

}
