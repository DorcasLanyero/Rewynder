package com.rewyndr.rewyndr.viewmodel

import android.graphics.Bitmap
import android.graphics.Point
import androidx.lifecycle.*
import com.rewyndr.rewyndr.api.RequestStatus
import com.rewyndr.rewyndr.api.StepRepository
import com.rewyndr.rewyndr.api.TagRepository
import com.rewyndr.rewyndr.enums.RewyndrImageState
import com.rewyndr.rewyndr.interfaces.IColorPickerListener
import com.rewyndr.rewyndr.model.*
import com.rewyndr.rewyndr.model.Annotation
import com.rewyndr.rewyndr.R
import kotlinx.coroutines.launch

class StepViewModel(private val stepId: Int) : ViewModel(), IColorPickerListener {
    val title : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val image : MutableLiveData<Image> by lazy { MutableLiveData<Image>() }
    val description : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val instructionsResource : MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val showInstructions : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val updating : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val annotations : MutableLiveData<List<Annotation>> by lazy { MutableLiveData<List<Annotation>>() }
    val state : MutableLiveData<StepMode> by lazy { MutableLiveData<StepMode>() }
    val selectedTag : MutableLiveData<Tag?> by lazy { MutableLiveData<Tag?>() }
    val imageBitmap : MutableLiveData<Bitmap?> by lazy { MutableLiveData<Bitmap?>() }
    val selectedTagColor : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val showColorPicker : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val showZoomedImage : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val errorMessage : MutableLiveData<String> by lazy { MutableLiveData<String>() }

    private val stepRepository : StepRepository by lazy { StepRepository() }
    private val tagRepository : TagRepository by lazy { TagRepository() }

    private var step: Step? = null
    private var createdTagBounds: List<Point>? = null
    private var createdTagInformation: TagFormInformation = TagFormInformation()

    init{
        state.value = StepMode.View

        showColorPicker.value = false
        showZoomedImage.value = false

        refreshStep()
    }

    fun refreshStep(){
        viewModelScope.launch {
            updating.value = true
            val stepData = stepRepository.getStep(stepId)

            step = stepData.data
            update(step)

            updating.value = false
        }
    }

    private fun update(step: Step?){
        if(step == null)
            return

        title.value = getTitle()
        image.value = step.image
        annotations.value = step.image!!.getAllAnnotations()
        description.value = step.description
    }

    fun attemptTagSave(){
        if(state.value != StepMode.Create) {
            errorMessage.value = "Invalid view state!"
            return
        }

        if(createdTagBounds == null || createdTagBounds!!.isEmpty()) {
            errorMessage.value = "No tag position specified"
            return
        }

        if(createdTagInformation.name.isEmpty()){
            errorMessage.value = "Tag must have a name"
            return
        }

        val newTag = Tag(createdTagBounds!!, createdTagInformation, step!!)

        if(selectedTag.value != null)
            newTag.id = selectedTag.value!!.id

        viewModelScope.launch {
            updating.value = true
            if(newTag.id == 0)
                tagRepository.createTag(newTag)
            else
                tagRepository.updateTag(newTag)

            refreshStep()

            createdTagInformation = TagFormInformation()
            selectedTag.value = null

            updating.value = false
        }

        setStepMode(StepMode.View)
    }

    fun deleteTag() : Boolean{
        if(selectedTag.value != null){
            var success = false
            viewModelScope.launch {
                updating.value = true
                val response = tagRepository.deleteTag(selectedTag.value!!.id)

                success = response.status == RequestStatus.SUCCESS

                if(!success)
                    errorMessage.value = response.message
                else {
                    setSelectedTag(null)

                    refreshStep()
                }
                updating.value = false
            }
            return success
        }
        errorMessage.value = "No tag selected!"
        return false
    }

    fun deleteStep() {
        viewModelScope.launch {
            updating.value = true
            stepRepository.deleteStep(step!!.id)

            updating.value = false
        }
    }

    fun setStepMode(mode: StepMode){
        if(state.value == mode)
            return

        state.value = mode
        title.value = getTitle()
    }

    fun setSelectedTag(tag: Tag?){
        if(selectedTag.value == tag)
            return

        selectedTag.value = tag

        if(tag == null) {
            setSelectedTagColor("#ffffff")
            setStepMode(StepMode.View)
        } else {
            setSelectedTagColor(tag.boundaryColor ?: "#ffffff")
            setStepMode(StepMode.Selected)
        }

        if(tag?.annotations != null){
            annotations.value = tag.annotations
        }
        else
            annotations.value = step!!.image!!.getAllAnnotations()

        title.value = getTitle()
    }

    fun setImageBitmap(bitmap: Bitmap){
        if(imageBitmap.value != bitmap)
            imageBitmap.value = bitmap
    }

    private fun setSelectedTagColor(color: String){
        if(selectedTagColor.value != color)
            selectedTagColor.value = color
    }

    fun setShowZoomedImage(showZoomedImage: Boolean){
        if(this.showZoomedImage.value != showZoomedImage)
            this.showZoomedImage.value = showZoomedImage
    }

    fun setShowColorPicker(showColorPicker: Boolean){
        if(this.showColorPicker.value != showColorPicker)
            this.showColorPicker.value = showColorPicker
    }

    fun setCreatedTagBounds(list: List<Point>){
        createdTagBounds = list
    }

    override fun onColorUpdated(newColor: String){
        createdTagInformation.color = newColor
        setSelectedTagColor(newColor)
    }

    fun onImageStateUpdate(state: RewyndrImageState){
        when(state){
            RewyndrImageState.NEW -> {
                instructionsResource.value = R.string.new_tag_instructions
                showInstructions.value = true
            }
            RewyndrImageState.EDIT_BOX -> {
                instructionsResource.value = R.string.box_tag_instructions
                showInstructions.value = true
            }
            RewyndrImageState.EDIT_SMART -> {
                instructionsResource.value = R.string.smart_tag_instructions
                showInstructions.value = true
            }
            RewyndrImageState.NONE -> showInstructions.value = false
            else -> showInstructions.value = false
        }
    }

    fun setTagText(text: String){
        createdTagInformation.name = text
    }

    fun setTagIcon(icon: String) {
        createdTagInformation.icon = icon
    }

    fun setTagColor(color: String) {
        selectedTagColor.value = color
        createdTagInformation.color = color
    }

    private fun getTitle() : String {
        if(step == null)
            return ""

        return if(state.value == StepMode.Selected)
            selectedTag.value!!.name!!
        else if(state.value == StepMode.Create)
            if(selectedTag.value == null) "Add Tag" else "Edit Tag"
        else
            if(selectedTag.value == null) step!!.name else selectedTag.value!!.name ?: "Unnamed Tag"
    }

    enum class StepMode {
        View,
        Create,
        Selected
    }
}