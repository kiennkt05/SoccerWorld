package com.example.soccerworld.ui.fixture.detail.h2h

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.databinding.FragmentH2HBinding
import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.model.fixture.Fixture
import com.example.soccerworld.util.Constant

class H2HFragment : BaseVMFragment<H2HViewModel>() {

    private var _binding: FragmentH2HBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentH2HBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val teamIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS, Fixture::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Constant.FIXTURE_TEAM_IDS)
        }

        teamIds?.let {
            viewModel.getAllH2hItems(it.homeTeam.teamId, it.awayTeam.teamId)
        }

        viewModel.h2hList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvDetailH2h.layoutManager = LinearLayoutManager(context)
                binding.rvDetailH2h.adapter = H2hAdapter(it)
            }
        })

        viewModel.loadingH2h.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.rvDetailH2h.visibility = View.GONE
                    binding.progressBarH2h.visibility = View.VISIBLE
                }else{
                    binding.rvDetailH2h.visibility = View.VISIBLE
                    binding.progressBarH2h.visibility = View.GONE
                }
            }
        })
    }

    override fun getViewModel(): Class<H2HViewModel> = H2HViewModel::class.java

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
