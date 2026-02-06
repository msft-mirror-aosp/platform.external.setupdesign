package com.google.android.setupdesign.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.setupdesign.R

class SelectableCardView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  CardView(context, attrs, defStyleAttr) {

  private var externalOnClickListener: OnClickListener? = null
  private var originalIcon: Drawable? = null
  private val iconView: ImageView? = findViewById(R.id.sud_items_icon)

  init {
    originalIcon = cardIcon
    skipClickSelection = true
  }

  override fun setCardIcon(icon: Drawable?) {
    super.setCardIcon(icon)
    originalIcon = icon
    if (isSelected) {
      updateUi(true)
    }
  }

  override fun onClick(v: View) {
    setCardSelected(!isSelected)
    super.onClick(v)
  }

  fun isCardSelected(): Boolean = isSelected

  fun setCardSelected(selected: Boolean) {
    if (isSelected != selected) {
      isSelected = selected
      updateUi(selected)
    }
  }

  private fun updateUi(selected: Boolean) {
    iconView?.let {
      if (selected) {
        it.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sud_ic_check_mark))
        contentDescription =
          context.getString(
            com.google.android.setupdesign.strings.R.string.sud_card_view_check_mark_icon_label
          )
      } else {
        it.setImageDrawable(originalIcon)
        contentDescription = null
      }
      it.isSelected = selected
    }

    titleView?.isSelected = selected
  }
}
