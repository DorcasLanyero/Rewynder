package com.rewyndr.rewyndr.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.fragment.app.activityViewModels
import com.rewyndr.rewyndr.R
import com.rewyndr.rewyndr.databinding.FragmentColorPickerBinding
import com.rewyndr.rewyndr.interfaces.IColorPickerListener
import com.rewyndr.rewyndr.viewmodel.StepViewModel
import java.util.*
import kotlin.collections.ArrayList

class ColorPickerFragment : Fragment() {
    private var _binding: FragmentColorPickerBinding? = null
    private val binding get() = _binding!!

    private val observers = ArrayList<IColorPickerListener> ()

    private var selectedTagColor : String = "#ffffff"
    private var checkedView : AppCompatRadioButton? = null

    private val colorMap = mapOf(R.id.radio_white to "#ffffff",
                                 R.id.radio_black to "#000000",
                                 R.id.radio_red to "#ff0000",
                                 R.id.radio_orange to "#ffa500",
                                 R.id.radio_yellow to "#ffff00",
                                 R.id.radio_green to "#008000",
                                 R.id.radio_blue to "#0000ff",
                                 R.id.radio_indigo to "#4b0082")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorPickerBinding.inflate(inflater, container, false)
        val viewModel by activityViewModels<StepViewModel>()

        viewModel.selectedTagColor.observe(viewLifecycleOwner, {
            if(it == selectedTagColor)
                return@observe

            selectedTagColor = it

            for(key in colorMap.keys){
                val v = binding.root.findViewById<AppCompatRadioButton>(key)
                val isChecked = colorMap[key] == selectedTagColor.toUpperCase(Locale.ROOT)
                v.isChecked = isChecked

                if(isChecked)
                    checkedView = v
            }
        })

        val listener = View.OnClickListener {
            val rb = it as AppCompatRadioButton
            val checked = rb.isChecked

            if(checked && checkedView != null)
                checkedView!!.isChecked = false

            if(checked) {
                selectedTagColor = colorMap[it.id] ?: error("colors map: invalid view ID")
                checkedView = rb
            }

            notifyObservers()
            viewModel.setShowColorPicker(false)
        }

        binding.radioWhite.setOnClickListener(listener)
        binding.radioBlack.setOnClickListener(listener)
        binding.radioRed.setOnClickListener(listener)
        binding.radioOrange.setOnClickListener(listener)
        binding.radioYellow.setOnClickListener(listener)
        binding.radioGreen.setOnClickListener(listener)
        binding.radioBlue.setOnClickListener(listener)
        binding.radioIndigo.setOnClickListener(listener)

        binding.backButton.setOnClickListener {
            viewModel.setShowColorPicker(false)
        }

        registerObserver(viewModel)

        return binding.root
    }

    private fun notifyObservers(){
        for(observer in observers){
            observer.onColorUpdated(selectedTagColor)
        }
    }

    private fun registerObserver(obs: IColorPickerListener){
        observers.add(obs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}