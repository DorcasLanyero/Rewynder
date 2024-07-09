package com.rewyndr.rewyndr.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rewyndr.rewyndr.databinding.FragmentZoomedImageBinding
import com.rewyndr.rewyndr.interfaces.IRewyndrImageListener
import com.rewyndr.rewyndr.interfaces.IRewyndrImageProvider

class ZoomedImageFragment : Fragment(), IRewyndrImageListener {
    private lateinit var provider: IRewyndrImageProvider
    private var _binding: FragmentZoomedImageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentZoomedImageBinding.inflate(inflater, container, false)

        provider = requireActivity() as IRewyndrImageProvider

        provider.registerListener(this)

        binding.zoomIcon.setOnClickListener {
            provider.setShowZoomedImage(false)
            val zoomView = binding.image
            zoomView.setZoom(1.0f)
        }

        return binding.root
    }

    override fun setImageBitmap(imageBitmap: Bitmap) {
        binding.image.setImageBitmap(imageBitmap)
    }
}