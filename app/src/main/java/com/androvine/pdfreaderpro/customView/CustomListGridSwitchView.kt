package com.androvine.pdfreaderpro.customView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.androvine.pdfreaderpro.R

class CustomListGridSwitchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    enum class SwitchMode {
        LIST, GRID
    }

    companion object {
        private const val PREF_NAME = "CustomListGridSwitchViewPrefs"
        private const val KEY_MODE = "mode"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private var currentMode = SwitchMode.LIST

    private val defaultListDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_list)
    private val defaultGridDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_grid)

    private var listDrawable: Drawable? = defaultListDrawable
    private var gridDrawable: Drawable? = defaultGridDrawable

    var onModeChangedListener: ((SwitchMode) -> Unit)? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomListGridSwitchView)
        val mode =
            SwitchMode.values()[typedArray.getInt(R.styleable.CustomListGridSwitchView_setMode, 0)]
        val iconColor =
            typedArray.getColor(R.styleable.CustomListGridSwitchView_iconColor, Color.BLACK)
        listDrawable = typedArray.getDrawable(R.styleable.CustomListGridSwitchView_listIcon)
            ?: defaultListDrawable
        gridDrawable = typedArray.getDrawable(R.styleable.CustomListGridSwitchView_gridIcon)
            ?: defaultGridDrawable
        typedArray.recycle()

        setColorFilter(iconColor)
        setOnClickListener { toggleMode() }

        val savedMode = sharedPreferences.getInt(KEY_MODE, -1)
        if (savedMode != -1) {
            setMode(SwitchMode.values()[savedMode])
        } else {
            setMode(mode)
        }
    }

    private fun toggleMode() {
        currentMode = if (currentMode == SwitchMode.LIST) SwitchMode.GRID else SwitchMode.LIST
        setImageDrawable(if (currentMode == SwitchMode.LIST) listDrawable else gridDrawable)
        onModeChangedListener?.invoke(currentMode)
    }

    fun getCurrentMode(): SwitchMode = currentMode

    fun setIconColor(color: Int) = setColorFilter(color)

    fun setListIcon(drawable: Drawable) {
        listDrawable = drawable
        if (currentMode == SwitchMode.LIST) setImageDrawable(listDrawable)
    }

    fun setGridIcon(drawable: Drawable) {
        gridDrawable = drawable
        if (currentMode == SwitchMode.GRID) setImageDrawable(gridDrawable)
    }

    fun setMode(mode: SwitchMode) {
        currentMode = mode
        setImageDrawable(if (currentMode == SwitchMode.LIST) listDrawable else gridDrawable)
    }

    fun rememberState(shouldRemember: Boolean) {
        if (shouldRemember) {
            sharedPreferences.edit().putInt(KEY_MODE, currentMode.ordinal).apply()
        }
    }

    fun clearState() {
        sharedPreferences.edit().remove(KEY_MODE).apply()
    }
}
