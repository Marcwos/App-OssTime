package com.example.osstime.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.osstime.presentation.screens.ClassInfo

@Composable
fun ClassSection(
    title: String,
    date: String,
    classes: List<ClassInfo>
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray
            )
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(classes) { classInfo ->
                ClassCard(info = classInfo)
            }
        }
    }
}
