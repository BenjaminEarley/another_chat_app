package com.benjaminearley.chat.util

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.R

fun ViewGroup.inflate(layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

fun ViewGroup.inflater(): LayoutInflater =
    LayoutInflater.from(context)

val divider =
    DividerItemDecoration(
        ChatApplication.INSTANCE,
        LinearLayoutManager.VERTICAL
    ).apply {
        ContextCompat.getDrawable(
            ChatApplication.INSTANCE,
            R.drawable.divider_space
        )
            ?.let { setDrawable(it) }
    }

fun EditText.hideKeyboard() {
    val imm: InputMethodManager =
        ChatApplication.INSTANCE.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}