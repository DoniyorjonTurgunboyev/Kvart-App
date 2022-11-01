package uz.gita.kvartarena.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun ViewGroup.inflate(id: Int): View = LayoutInflater.from(context).inflate(id, this, false)