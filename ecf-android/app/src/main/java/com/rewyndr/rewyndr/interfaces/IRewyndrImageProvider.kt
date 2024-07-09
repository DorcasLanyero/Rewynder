package com.rewyndr.rewyndr.interfaces

interface IRewyndrImageProvider {
    fun registerListener(listener: IRewyndrImageListener)

    fun setShowZoomedImage(showImage : Boolean)
}