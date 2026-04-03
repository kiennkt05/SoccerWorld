package com.example.soccerworld.ui.team.team_detail.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soccerworld.base.BaseVMFragment
import com.example.soccerworld.databinding.FragmentTransferBinding
import com.example.soccerworld.util.Constant

class TransferFragment : BaseVMFragment<TransferViewModel>() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transferTeamId = arguments?.getInt(Constant.TEAM_ID)
        Toast.makeText(context,"Team Id Transfer :  "+transferTeamId, Toast.LENGTH_SHORT).show()

        viewModel.getAllTransfersOfTeam(transferTeamId!!)
        viewModel.transferList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.rvTransfer.layoutManager = LinearLayoutManager(context)
                binding.rvTransfer.adapter = TransferAdapter(it)
            }
        })

    }

    override fun getViewModel(): Class<TransferViewModel> = TransferViewModel::class.java

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
