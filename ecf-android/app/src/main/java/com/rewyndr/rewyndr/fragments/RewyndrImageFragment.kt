package com.rewyndr.rewyndr.fragments


import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.rewyndr.rewyndr.databinding.FragmentRewyndrImageBinding
import com.rewyndr.rewyndr.enums.RewyndrImageState
import com.rewyndr.rewyndr.interfaces.IRewyndrImageObserver
import com.rewyndr.rewyndr.model.Tag
import com.rewyndr.rewyndr.view.RewyndrImageView
import com.rewyndr.rewyndr.viewmodel.StepViewModel

class RewyndrImageFragment : Fragment(), IRewyndrImageObserver {
    private lateinit var viewModel: StepViewModel
    private var _binding: FragmentRewyndrImageBinding? = null
    private var selectedTag: Tag? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewyndrImageBinding.inflate(inflater, container, false)
        viewModel = activityViewModels<StepViewModel>().value

        viewModel.image.observe(viewLifecycleOwner, { binding.image.setImage(it) })
        viewModel.state.observe(viewLifecycleOwner, {
            when(it){
                StepViewModel.StepMode.Create -> {
                    transitionToTagCreation(binding.image)
                    binding.zoomIcon.visibility = GONE
                }
                StepViewModel.StepMode.Selected -> {
                    binding.image.setTaggingOperationMode(RewyndrImageView.TagOperationMode.view)
                    binding.zoomIcon.visibility = VISIBLE
                }
                else -> {
                    binding.image.setTaggingOperationMode(RewyndrImageView.TagOperationMode.view)
                    selectedTag = null
                    binding.zoomIcon.visibility = VISIBLE
                }
            }
        })

        binding.image.registerObserver(this)

        binding.zoomIcon.setOnClickListener {
            viewModel.setShowZoomedImage(true)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun tagUpdate(selectedTag : Tag?){
        if(this.selectedTag == selectedTag)
            return

        this.selectedTag = selectedTag

        var tag: Tag? = null
        if(this.selectedTag != null) {
            tag = Tag(selectedTag!!)
        }

        viewModel.setSelectedTag(tag)
    }

    override fun imageChanged(newBitmap : Bitmap) {
        viewModel.setImageBitmap(newBitmap)
    }

    override fun stateUpdate(state: RewyndrImageState) {
        viewModel.onImageStateUpdate(state)
    }

    override fun tagBoundsUpdated(list: List<Point>){
        viewModel.setCreatedTagBounds(list)
    }

    private fun transitionToTagCreation(image : RewyndrImageView){
        if(selectedTag != null)
            image.setTaggingOperationMode(RewyndrImageView.TagOperationMode.edit)
        else
            image.setTaggingOperationMode(RewyndrImageView.TagOperationMode.add)
    }
}