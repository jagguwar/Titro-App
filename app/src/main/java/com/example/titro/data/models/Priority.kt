package com.example.titro.data.models

import androidx.compose.ui.graphics.Color
import com.example.titro.ui.theme.HighPriorityColor
import com.example.titro.ui.theme.LowPriorityColor
import com.example.titro.ui.theme.MediumPriorityColor
import com.example.titro.ui.theme.NonePriorityColor

enum class Priority(val color: Color) {

    HIGH(HighPriorityColor),
    MEDIUM(MediumPriorityColor),
    LOW(LowPriorityColor),
    NONE(NonePriorityColor)

}