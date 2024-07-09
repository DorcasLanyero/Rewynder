package com.rewyndr.rewyndr.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rewyndr.rewyndr.R
import com.rewyndr.rewyndr.databinding.ActivityStepBinding
import com.rewyndr.rewyndr.interfaces.IRewyndrImageListener
import com.rewyndr.rewyndr.interfaces.IRewyndrImageProvider
import com.rewyndr.rewyndr.model.Image
import com.rewyndr.rewyndr.model.Tag
import com.rewyndr.rewyndr.utility.ToastUtility
import com.rewyndr.rewyndr.viewmodel.StepViewModel

class StepActivity : BaseActivity(), IRewyndrImageProvider {
    private lateinit var binding : ActivityStepBinding
    private lateinit var viewModel : StepViewModel
    private var state : StepViewModel.StepMode = StepViewModel.StepMode.View
    private var stepId : Int = 0
    private lateinit var image : Image
    private var selectedTag : Tag? = null

    private var colorPickerVisible = false
    private var zoomedFragmentVisible = false

    private lateinit var optionsMenu: Menu
    private var addTagMenuItem: MenuItem? = null
    private var editStepMenuItem: MenuItem? = null
    private var deleteStepMenuItem: MenuItem? = null
    private var saveTagMenuItem: MenuItem? = null
    private var editTagMenuItem: MenuItem? = null
    private var deleteTagMenuItem: MenuItem? = null
    private var addCommentMenuItem: MenuItem? = null

    private val imageListeners: MutableList<IRewyndrImageListener> = mutableListOf()

    private class StepViewModelProvider(val stepId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) : T {
            return modelClass.getConstructor(Int::class.java).newInstance(stepId)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepId = intent.getIntExtra("executionId", 0)

        val factory = StepViewModelProvider(stepId)
        viewModel = ViewModelProvider(this, factory).get(StepViewModel::class.java)

        binding = ActivityStepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        viewModel.title.observe(this, {
            supportActionBar?.title = it.toString()
        })
        viewModel.image.observe(this, { image = it })
        viewModel.description.observe(this, { binding.stepDescription.text = it.toString() })
        viewModel.state.observe(this, {
            if (state == it)
                return@observe

            hideKeyboard()

            state = it
            when (it) {
                StepViewModel.StepMode.Create -> {
                    displayTagCreationMenu()
                    binding.annotationsList.visibility = GONE
                    binding.tagEditor.visibility = VISIBLE
                }
                StepViewModel.StepMode.Selected -> {
                    displayTagSelectedMenu()
                }
                else -> {
                    displayDefaultMenu()
                    binding.annotationsList.visibility = VISIBLE
                    binding.tagEditor.visibility = GONE
                }
            }
        })
        viewModel.imageBitmap.observe(this, {
            if(it == null)
                return@observe

            for(listener in imageListeners){
                listener.setImageBitmap(it)
            }
        })

        viewModel.showInstructions.observe(this, {
            binding.addTagInstructionsContainer.visibility = if(it) LinearLayout.VISIBLE else LinearLayout.GONE
        })

        viewModel.instructionsResource.observe(this, {
            binding.addTagInstructions.setText(it)
        })

        viewModel.showColorPicker.observe(this, {
            binding.colorPickerContainer.visibility = if(it) VISIBLE else GONE
            colorPickerVisible = it
        })

        viewModel.showZoomedImage.observe(this, {
            binding.zoomImageFragment.visibility = if(it) VISIBLE else GONE
            zoomedFragmentVisible = it
        })

        viewModel.errorMessage.observe(this, {
            ToastUtility.popShort(this, it)
        })

        viewModel.selectedTag.observe(this, {
            selectedTag = it
        })

        // Intercept touches when the color picker is open to
        // prevent the tag editor from receiving them
        binding.colorPickerContainer.setOnTouchListener { _, _ ->
            true
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.refreshStep()
    }

    override fun onBackPressed() {
        hideKeyboard()
        if(zoomedFragmentVisible){
            viewModel.setShowZoomedImage(false)
            return
        }

        if(colorPickerVisible) {
            viewModel.setShowColorPicker(false)
            return
        }

        if (state != StepViewModel.StepMode.View) {
            viewModel.setStepMode(StepViewModel.StepMode.View)
            return
        }

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(colorPickerVisible && item!!.itemId != android.R.id.home)
            return false

        when (item!!.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_add_tag -> viewModel.setStepMode(StepViewModel.StepMode.Create)
            R.id.action_delete_tag -> { viewModel.deleteTag() }
            R.id.action_edit_tag -> viewModel.setStepMode(StepViewModel.StepMode.Create)
            R.id.action_save_tag -> viewModel.attemptTagSave()
            R.id.action_edit_step -> navigateToStepForm()
            R.id.action_delete_step -> presentStepDeletionDialog()
            R.id.action_add_comment -> navigateToCommentForm()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun presentStepDeletionDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.step_delete_dialog_message)
        builder.setTitle(R.string.step_delete_dialog_title)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            viewModel.deleteStep()
            onBackPressed()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.step_menu, menu)

        optionsMenu = menu
        this.addTagMenuItem = menu.findItem(R.id.action_add_tag)
        this.editStepMenuItem = menu.findItem(R.id.action_edit_step)
        this.deleteStepMenuItem = menu.findItem(R.id.action_delete_step)
        this.saveTagMenuItem = menu.findItem(R.id.action_save_tag)
        this.editTagMenuItem = menu.findItem(R.id.action_edit_tag)
        this.deleteTagMenuItem = menu.findItem(R.id.action_delete_tag)
        this.addCommentMenuItem = menu.findItem(R.id.action_add_comment)
        return super.onCreateOptionsMenu(menu)
    }

    private fun displayDefaultMenu(){
        addTagMenuItem?.isVisible = true
        addCommentMenuItem?.isVisible = true
        editStepMenuItem?.isVisible = true
        deleteStepMenuItem?.isVisible = true

        saveTagMenuItem?.isVisible = false
        editTagMenuItem?.isVisible = false
        deleteTagMenuItem?.isVisible = false
    }

    private fun displayTagSelectedMenu() {
        addCommentMenuItem?.isVisible = true
        editTagMenuItem?.isVisible = true
        deleteTagMenuItem?.isVisible = true

        deleteStepMenuItem?.isVisible = false
        saveTagMenuItem?.isVisible = false
        addTagMenuItem?.isVisible = false
        editStepMenuItem?.isVisible = false
    }

    private fun displayTagCreationMenu(){
        saveTagMenuItem?.isVisible = true

        addCommentMenuItem?.isVisible = false
        editTagMenuItem?.isVisible = false
        deleteTagMenuItem?.isVisible = false
        deleteStepMenuItem?.isVisible = false
        addTagMenuItem?.isVisible = false
        editStepMenuItem?.isVisible = false
    }

    private fun navigateToStepForm(){
        val editStepIntent = Intent(this, StepFormActivity::class.java)
        editStepIntent.putExtra("executionId", stepId)
        startActivity(editStepIntent)
    }

    private fun navigateToCommentForm(){
        val addCommentIntent = Intent(this, AnnotationFormActivity::class.java)
        addCommentIntent.putExtra("executionId", stepId)
        addCommentIntent.putExtra("imageId", image.id)
        if (state == StepViewModel.StepMode.Selected) {
            addCommentIntent.putExtra("tagId", selectedTag?.id ?: 0)
        }
        startActivity(addCommentIntent)
    }

    override fun registerListener(listener: IRewyndrImageListener){
        imageListeners.add(listener)
    }

    override fun setShowZoomedImage(showImage: Boolean) {
        viewModel.setShowZoomedImage(showImage)
    }
}