package com.yurii.foody.utils

interface OnBackPressed {
    /**
     * The function is called if back button is pressed.
     * @returns True if it was handled
     **/
    fun onBackPressed(): Boolean
}