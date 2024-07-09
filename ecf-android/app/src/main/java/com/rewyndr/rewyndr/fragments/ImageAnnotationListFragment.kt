package com.rewyndr.rewyndr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rewyndr.rewyndr.R
import com.rewyndr.rewyndr.adapters.AnnotationCardAdapter
import com.rewyndr.rewyndr.databinding.FragmentImageAnnotationListBinding
import com.rewyndr.rewyndr.model.Annotation
import com.rewyndr.rewyndr.storage.UserPrefsStore
import com.rewyndr.rewyndr.viewmodel.StepViewModel

class ImageAnnotationListFragment : Fragment() {
    private lateinit var userPrefsStore: UserPrefsStore

    private lateinit var annotationsAdapter: AnnotationCardAdapter

    private var _binding: FragmentImageAnnotationListBinding? = null
    private val binding get() = _binding!!

    private var annotations: List<Annotation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        userPrefsStore = UserPrefsStore(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageAnnotationListBinding.inflate(inflater, container, false)
        val viewModel by activityViewModels<StepViewModel>()

        annotationsAdapter = AnnotationCardAdapter(activity, annotations, userPrefsStore.getCurrentUser())
        binding.annotationsList.adapter = annotationsAdapter

        viewModel.annotations.observe(viewLifecycleOwner, {
            annotationsAdapter.setAnnotations(it)
            binding.annotationListHeading.text = String.format(getString(R.string.comments_count), annotationsAdapter.count)
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}