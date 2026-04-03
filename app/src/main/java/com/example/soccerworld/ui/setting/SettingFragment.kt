package com.example.soccerworld.ui.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.example.soccerworld.R
import com.example.soccerworld.databinding.FragmentSettingBinding
import com.example.soccerworld.util.CustomSharedPreferences

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customPreferences = CustomSharedPreferences(requireContext().applicationContext)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            val radioButtonId = group.indexOfChild(radioButton)
            when(radioButton.id){
                R.id.rbEngland -> customPreferences.saveCountryId(524)
                R.id.rbGermany -> customPreferences.saveCountryId(754)
                R.id.rbItaly -> customPreferences.saveCountryId(891)
                R.id.rbSpain -> customPreferences.saveCountryId(775)
                R.id.rbFrance -> customPreferences.saveCountryId(525)
                R.id.rbTurkey -> customPreferences.saveCountryId(782)
                R.id.rbHolland -> customPreferences.saveCountryId(566)
                R.id.rbGreece -> customPreferences.saveCountryId(787)
                R.id.rbPortugal -> customPreferences.saveCountryId(766)
                R.id.rbRussia -> customPreferences.saveCountryId(511)
            }
            customPreferences.saveRbCountry(radioButtonId)
        }

        val radioButtonResult = customPreferences.getRbCountry()
        val countryIdResult = customPreferences.getCountryId()
        Toast.makeText(requireContext(), "League Selected $countryIdResult", Toast.LENGTH_SHORT).show()
        val selectedRadioButton = binding.radioGroup.getChildAt(radioButtonResult!!) as RadioButton
        selectedRadioButton.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
