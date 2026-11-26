package com.valeton.editor.ui.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valeton.editor.BuildConfig
import com.valeton.editor.R
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun StudioAboutScreen(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier.fillMaxSize()
            .background(StudioPalette.Case)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("SOBRE", style = MaterialTheme.typography.titleLarge,
            color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(painterResource(R.drawable.app_icon_preview), contentDescription = null, modifier = Modifier.size(80.dp))
            Text("GP-200 Editor", style = MaterialTheme.typography.headlineSmall,
                color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
            Text("Editor independente para Android", color = StudioPalette.TextSecondary)
            Text("Versão ${BuildConfig.VERSION_NAME}", color = StudioPalette.Accent, fontFamily = FontFamily.Monospace)
        }
        Surface(
            color = StudioPalette.SurfaceRaised,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, StudioPalette.Accent.copy(alpha = 0.4f))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aplicativo não oficial da Valeton", color = StudioPalette.Accent, fontWeight = FontWeight.Bold)
                Text("Este é um projeto independente, sem vínculo, afiliação ou endosso da Valeton.",
                    color = StudioPalette.TextPrimary)
                Text("Valeton e GP-200 são marcas de seus respectivos titulares. Seus nomes são usados apenas para identificar o equipamento compatível.",
                    color = StudioPalette.TextSecondary)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CRÉDITOS", color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
            Text("Artes e trabalhos anteriores que contribuíram para este editor independente:",
                color = StudioPalette.TextSecondary)
            AboutLinkRow("GP200 Studio · Kabir Tamari") {
                uriHandler.openUri("https://github.com/kabir0st/gp200-studio")
            }
            AboutLinkRow("gp200editor / Preset Forge · phash") {
                uriHandler.openUri("https://github.com/phash/gp200editor")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CÓDIGO E LICENÇAS", color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
            Text("Código próprio sob GNU GPL versão 3. Fornecido sem garantia. Os componentes de terceiros mantêm seus avisos e condições de licença.",
                color = StudioPalette.TextSecondary)
            AboutLinkRow("Código-fonte, créditos e licenças") {
                uriHandler.openUri("https://github.com/marcosepifanio/gp200-android-editor")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PRIVACIDADE", color = StudioPalette.TextPrimary, fontWeight = FontWeight.Bold)
            Text("O app não acessa a internet nem coleta dados pessoais. Presets e preferências ficam somente neste aparelho.",
                color = StudioPalette.TextSecondary)
            AboutLinkRow("Política de privacidade") {
                uriHandler.openUri(PRIVACY_POLICY_URL)
            }
        }
    }
}

@Composable
private fun AboutLinkRow(label: String, onClick: () -> Unit) {
    Surface(
        color = StudioPalette.Surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, StudioPalette.BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = StudioPalette.Accent, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = StudioPalette.Accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

internal const val PRIVACY_POLICY_URL =
    "https://github.com/marcosepifanio/gp200-android-editor/blob/main/docs/privacy-policy.md"
