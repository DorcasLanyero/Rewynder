package com.rewyndr.rewyndr.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.rewyndr.rewyndr.adapters.SafetyIconAdapter
import com.rewyndr.rewyndr.databinding.FragmentTagEditorBinding
import com.rewyndr.rewyndr.interfaces.ISafetyIconSelectionObserver
import com.rewyndr.rewyndr.viewmodel.StepViewModel

class TagEditorFragment : Fragment(), ISafetyIconSelectionObserver {
    private var _binding: FragmentTagEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StepViewModel

    private var selectedColor = "#ffffff"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagEditorBinding.inflate(inflater, container, false)
        viewModel = activityViewModels<StepViewModel>().value

        val iconAdapter = SafetyIconAdapter(requireContext(), null)
        iconAdapter.registerObserver(this)
        binding.tagSafetyIcons.adapter = iconAdapter

        viewModel.selectedTag.observe(viewLifecycleOwner, {

            binding.tagNameInput.setText(it?.name ?: "")
            binding.colorSwatch.setBackgroundColor(Color.parseColor(it?.boundaryColor ?: "#ffffff"))
            selectedColor = it?.boundaryColor ?: "#ffffff"

            viewModel.setTagIcon(it?.icon ?: "")
            viewModel.setTagText(it?.name ?: "")
            viewModel.setTagColor(it?.boundaryColor ?: "#ffffff")

            val adapter = SafetyIconAdapter(requireContext(), it?.icon)
            adapter.registerObserver(this)
            binding.tagSafetyIcons.adapter = adapter
        })

        viewModel.selectedTagColor.observe(viewLifecycleOwner, {
            binding.colorSwatch.setBackgroundColor(Color.parseColor(it))
        })

        viewModel.showColorPicker.observe(viewLifecycleOwner, {
            binding.saveTagButton.isEnabled = !it
        })

        binding.openColorPickerButton.setOnClickListener {
            viewModel.setShowColorPicker(true)
        }

        binding.tagSafetyIcons.layoutManager = GridLayoutManager(requireContext(), 4)

        binding.tagNameInput.addTextChangedListener {
            viewModel.setTagText(it.toString())
        }

        binding.saveTagButton.setOnClickListener { viewModel.attemptTagSave() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun selectionUpdated(selectedIcon: String){
        viewModel.setTagIcon(selectedIcon)
    }
}