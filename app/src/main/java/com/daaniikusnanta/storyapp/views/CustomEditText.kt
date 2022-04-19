package com.daaniikusnanta.storyapp.views

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.daaniikusnanta.storyapp.R

class CustomEditText : AppCompatEditText {
    private var invalidInputMsg: String = "default"
    private var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private var type: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun init() {
        type = inputType - 1

        invalidInputMsg = when (type) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD -> this.context.getString(R.string.invalid_password)
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> this.context.getString(R.string.invalid_email)
            else -> this.context.getString(R.string.invalid_input)
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                error = when (type) {
                    InputType.TYPE_TEXT_VARIATION_PASSWORD -> {

                        if (length() < 6) {
                            invalidInputMsg
                        } else {
                            null
                        }
                    }
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                        if (!(text.toString().matches(emailPattern.toRegex()))) {
                            invalidInputMsg
                        } else {
                            null
                        }
                    }
                    else -> {
                        if (length() < 1) {
                            invalidInputMsg
                        } else {
                            null
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })
    }
}