package com.example.soccerworld.ui.home.topscorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentTopScorerBinding
import com.example.soccerworld.util.CustomSharedPreferences

class TopScorerFragment : BaseVMFragment<TopScorerViewModel>() {

    private var _binding: FragmentTopScorerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopScorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customPreferences = CustomSharedPreferences(requireContext())
        val leagueId = customPreferences.getCountryId()

        viewModel.getTopScorers(leagueId!!)
        viewModel.topScorerList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvTopScorers.layoutManager = LinearLayoutManager(this.context)
                binding.rvTopScorers.adapter = TopScorerAdapter(it)
            }
        })

        viewModel.loadingTopScorer.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it){
                    binding.rvTopScorers.visibility = View.GONE
                    binding.progressBarTopScorer.visibility = View.VISIBLE
                }else{
                    binding.progressBarTopScorer.visibility = View.GONE
                    binding.rvTopScorers.visibility = View.VISIBLE
                }
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getViewModel(): Class<TopScorerViewModel> = TopScorerViewModel::class.java

}
