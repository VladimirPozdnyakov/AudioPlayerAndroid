package com.foxelectronic.audioplayer.ui.settings.sections

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R

/**
 * Секция "О приложении"
 */
@Composable
fun AboutSection(
    versionName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null
                )
            },
            headlineContent = { Text("AudioPlayer") },
            supportingContent = { Text(stringResource(R.string.version_format, versionName)) }
        )

        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/VladimirPozdnyakov/AudioPlayerAndroid")
                )
                context.startActivity(intent)
            },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = null
                )
            },
            headlineContent = { Text(stringResource(R.string.source_code)) },
            supportingContent = { Text("github.com/VladimirPozdnyakov/AudioPlayerAndroid") }
        )
    }
}
