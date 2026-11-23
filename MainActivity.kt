package com.example.smartcctv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView

private data class Camera(
    val id: Long,
    val name: String,
    val brand: String,
    val streamUrl: String = "",
    val username: String = "",
    val password: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartCctvApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartCctvApp() {
    var cameras by remember {
        mutableStateOf(listOf(
            Camera(1, "Tapo C510W", "TP-Link Tapo C510W"),
            Camera(2, "CP-E25Q", "CP PLUS EzyKam+ CP-E25Q")
        ))
    }
    var selected by remember { mutableStateOf<Camera?>(null) }
    var editing by remember { mutableStateOf<Camera?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF70D6B3), secondary = Color(0xFF9AB7FF),
        background = Color(0xFF0B1117), surface = Color(0xFF151E27)
    )) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1117)) {
            when {
                editing != null -> CameraEditor(
                    camera = editing!!,
                    onCancel = { editing = null },
                    onSave = { updated ->
                        cameras = cameras.map { if (it.id == updated.id) updated else it }
                        selected = null
                        editing = null
                    }
                )
                selected != null -> CameraScreen(
                    camera = selected!!,
                    onBack = { selected = null },
                    onSettings = { editing = selected }
                )
                showAdd -> AddCameraScreen(
                    onCancel = { showAdd = false },
                    onAdd = { name, brand ->
                        val c = Camera(System.currentTimeMillis(), name, brand)
                        cameras = cameras + c
                        showAdd = false
                        editing = c
                    }
                )
                else -> Dashboard(
                    cameras = cameras,
                    onOpen = { selected = it },
                    onEdit = { editing = it },
                    onAdd = { showAdd = true }
                )
            }
        }
    }
}

@Composable
private fun Dashboard(cameras: List<Camera>, onOpen: (Camera) -> Unit, onEdit: (Camera) -> Unit, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SMART CCTV", color = Color(0xFF70D6B3), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Camera dashboard", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Your cameras in one place", color = Color.LightGray, fontSize = 13.sp)
            }
            FilledTonalIconButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add camera") }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MY CAMERAS", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${cameras.size} devices", color = Color(0xFF70D6B3), fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cameras, key = { it.id }) { camera ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151E27)), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.clickable { onOpen(camera) }.padding(12.dp)) {
                        if (camera.streamUrl.isNotBlank()) {
                            RtspPlayer(camera, Modifier.fillMaxWidth().height(125.dp))
                        } else {
                            Box(Modifier.fillMaxWidth().height(125.dp).background(Color(0xFF202D38), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF70D6B3), modifier = Modifier.size(30.dp))
                                    Text("SET UP LIVE STREAM", color = Color.LightGray, fontSize = 9.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(camera.name, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(camera.brand, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                        Spacer(Modifier.height(8.dp))
                        Text(if (camera.streamUrl.isBlank()) "Setup required" else "Stream configured", color = if (camera.streamUrl.isBlank()) Color(0xFFFFC46B) else Color(0xFF70D6B3), fontSize = 11.sp)
                        Text("Open camera  →", color = Color(0xFF9AB7FF), fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp).clickable { onEdit(camera) })
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Both configured RTSP feeds play on this dashboard at the same time. For remote access, use a secure VPN; do not expose RTSP ports directly to the internet.", color = Color.LightGray, fontSize = 11.sp)
    }
}

@Composable
private fun CameraScreen(camera: Camera, onBack: () -> Unit, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(camera.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(camera.brand, color = Color.LightGray, fontSize = 12.sp)
            }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Stream settings", tint = Color.White) }
        }
        Spacer(Modifier.height(14.dp))
        if (camera.streamUrl.isBlank()) {
            Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF151E27), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF70D6B3), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Camera stream not configured", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Add the camera's RTSP stream address in settings to start live view.", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onSettings) { Text("Configure camera") }
                }
            }
        } else {
            RtspPlayer(camera, Modifier.fillMaxWidth().weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text("For Tapo C510W, create a separate Camera Account in the Tapo app. RTSP credentials are not your TP-Link cloud login.", color = Color.LightGray, fontSize = 11.sp)
    }
}

private fun streamUriWithCredentials(url: String, username: String, password: String): android.net.Uri {
    val parsed = android.net.Uri.parse(url)
    if (username.isBlank() || parsed.encodedAuthority.isNullOrBlank() || parsed.encodedAuthority!!.contains("@")) return parsed
    val userInfo = "${android.net.Uri.encode(username)}:${android.net.Uri.encode(password)}@${parsed.encodedAuthority}"
    return parsed.buildUpon().encodedAuthority(userInfo).build()
}

@Composable
private fun RtspPlayer(camera: Camera, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var error by remember(camera.id, camera.streamUrl) { mutableStateOf<String?>(null) }
    val player = remember(camera.id, camera.streamUrl, camera.username, camera.password) {
        ExoPlayer.Builder(context).build().apply {
            val uri = streamUriWithCredentials(camera.streamUrl, camera.username, camera.password)
            val mediaSource = RtspMediaSource.Factory()
                .setForceUseRtpTcp(true)
                .createMediaSource(MediaItem.fromUri(uri))
            setMediaSource(mediaSource)
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlayerError(exception: androidx.media3.common.PlaybackException) {
                    error = exception.message ?: "Unable to play stream. Check address and credentials."
                }
            })
            prepare()
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    Column(modifier) {
        AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } }, modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black))
        error?.let { Text("Playback error: $it", color = Color(0xFFFF9B9B), fontSize = 12.sp) }
    }
}

@Composable
private fun AddCameraScreen(onCancel: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("TP-Link Tapo C500") }
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        Text("Add camera", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Camera name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Camera brand", color = Color.LightGray)
        listOf("TP-Link Tapo C510W", "CP PLUS EzyKam+ CP-E25Q", "Other RTSP camera").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(brand == option, { brand = option }); Text(option, color = Color.White) }
        }
        Spacer(Modifier.height(18.dp))
        Row { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Spacer(Modifier.width(10.dp)); Button(onClick = { onAdd(name.ifBlank { "Camera" }, brand) }) { Text("Add") } }
    }
}

@Composable
private fun CameraEditor(camera: Camera, onCancel: () -> Unit, onSave: (Camera) -> Unit) {
    var name by remember(camera.id) { mutableStateOf(camera.name) }
    var url by remember(camera.id) { mutableStateOf(camera.streamUrl) }
    var username by remember(camera.id) { mutableStateOf(camera.username) }
    var password by remember(camera.id) { mutableStateOf(camera.password) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Camera settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(camera.brand, color = Color.LightGray, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Camera name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(url, { url = it }, label = { Text("RTSP URL") }, placeholder = { Text("rtsp://192.168.1.50:554/stream1") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Camera username (if required)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Camera password (if required)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Security: use a VPN for remote access. This starter version keeps settings in app memory only; credentials are not saved after closing the app. Username and password are added to the RTSP connection when playback starts.", color = Color.LightGray, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Row { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Spacer(Modifier.width(10.dp)); Button(onClick = { onSave(camera.copy(name = name, streamUrl = url.trim(), username = username, password = password)) }) { Text("Save") } }
    }
}
