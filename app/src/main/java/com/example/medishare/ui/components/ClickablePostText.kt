import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextDirection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClickablePostText(
    postText: String,
    onWordClick: (String) -> Unit
) {
    val words = postText.split(" ")

    val isHebrew = words.joinToString(" ").any { it in 'א'..'ת' }
    val displayedWords = if (isHebrew) words.reversed() else words
    Log.d("ClickablePostText", displayedWords.toString())
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        displayedWords.forEach { word ->
            Text(
                text = "$word ",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable {
                        val cleanWord = word.filter { it.isLetterOrDigit() }
                        onWordClick(cleanWord)
                    }
                    .padding(end = 4.dp)
            )
        }
    }
}


