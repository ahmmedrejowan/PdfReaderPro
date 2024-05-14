package com.rejowan.pdfreaderpro.customView


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.rejowan.pdfreaderpro.R

class CustomListGridSwitchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    enum class SwitchMode {
        LIST, GRID
    }

    companion object {
        private const val PREF_NAME = "CustomListGridSwitchViewPrefs"
        private const val KEY_MODE = "mode"
        private const val KEY_SHOULD_REMEMBER = "should_remember"

    }

    private var shouldRemember: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOULD_REMEMBER, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SHOULD_REMEMBER, value).apply()

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private var currentMode = SwitchMode.LIST

    private val defaultListDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_list_custom_switch_view)
    private val defaultGridDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_grid_custom_switchview)

    private var listDrawable: Drawable? = defaultListDrawable
    private var gridDrawable: Drawable? = defaultGridDrawable

    private var onModeChangedListener: ((SwitchMode) -> Unit)? = null

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

        if (shouldRemember) {
            val savedMode = sharedPreferences.getInt(KEY_MODE, -1)
            if (savedMode != -1) {
                setMode(SwitchMode.values()[savedMode])
            } else {
                setMode(mode)
            }
        } else {
            setMode(mode)
        }
    }

    private fun toggleMode() {
        currentMode = if (currentMode == SwitchMode.LIST) SwitchMode.GRID else SwitchMode.LIST
        setImageDrawable(if (currentMode == SwitchMode.LIST) listDrawable else gridDrawable)
        onModeChangedListener?.invoke(currentMode)
        saveState()
    }

    fun getCurrentMode(): SwitchMode = currentMode

    fun setIconColor(color: Int) = setColorFilter(color)

    fun setListIcon(drawable: Drawable?) {
        listDrawable = drawable ?: defaultListDrawable
        if (currentMode == SwitchMode.LIST) setImageDrawable(listDrawable)
    }

    fun setGridIcon(drawable: Drawable?) {
        gridDrawable = drawable ?: defaultGridDrawable
        if (currentMode == SwitchMode.GRID) setImageDrawable(gridDrawable)
    }

    fun setMode(mode: SwitchMode) {
        currentMode = getSavedMode() ?: mode
        setImageDrawable(if (currentMode == SwitchMode.LIST) listDrawable else gridDrawable)
    }


    fun shouldRememberState(shouldRemember: Boolean) {
        this.shouldRemember = shouldRemember
        if (!shouldRemember) clearState() // Clears the saved mode if the state should not be remembered
    }

    private fun saveState() {
        if (shouldRemember) {
            sharedPreferences.edit().putInt(KEY_MODE, currentMode.ordinal).apply()
        }
    }

    fun clearState() {
        sharedPreferences.edit().remove(KEY_MODE).apply()
    }

    fun setListener(listener: ((SwitchMode) -> Unit)?) {
        onModeChangedListener = listener
    }

    fun getSavedMode(): SwitchMode? {
        val savedModeOrdinal = sharedPreferences.getInt(KEY_MODE, -1)
        if (savedModeOrdinal != -1) {
            return SwitchMode.values()[savedModeOrdinal]
        }
        return null
    }


}
