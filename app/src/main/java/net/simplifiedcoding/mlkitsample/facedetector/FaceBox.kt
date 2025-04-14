package net.simplifiedcoding.mlkitsample.facedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.face.Face

class FaceBox(
    overlay: FaceBoxOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private var boxColor: Int = Color.GREEN
) : FaceBoxOverlay.FaceBox(overlay) {

    private val paint = Paint().apply {
        color = boxColor
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    override fun draw(canvas: Canvas?) {
        val rect = getBoxRect(
            imageRectWidth = imageRect.width().toFloat(),
            imageRectHeight = imageRect.height().toFloat(),
            faceBoundingBox = face.boundingBox
        )
        canvas?.drawRect(rect, paint)
    }

    //    // Method to update the color dynamically
    fun updateColor(newColor: Int) {
        if (boxColor != newColor) {
            boxColor = newColor
            paint.color = newColor
            //invalidate() // Force a redraw of the face box with the new color
        }
    }
}
