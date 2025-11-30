package com.example.mytodolist.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytodolist.TodoViewModel
import com.example.mytodolist.TodoItem
import com.example.mytodolist.R
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(vm: TodoViewModel) {
    val bg = if (vm.isDark) Color(0xFF121820) else Color(0xFFF7F8FA)
    val cardBg = if (vm.isDark) Color(0xFF1A2230) else Color.White
    val accentBlue = Color(0xFF2E7CF6)
    val chipBlue = if (vm.isDark) Color(0xFF2A4B70) else Color(0xFFE9EEF9)
    val textPrimary = if (vm.isDark) Color(0xFF95A4B8) else Color(0xFF76839A)
    val textMain = if (vm.isDark) Color.White else Color(0xFF1E2A3A)

    val date = LocalDate.now()
    val dow = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val dateStr = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
    val total = vm.items.size.coerceAtLeast(1)
    val done = vm.items.count { it.completed }
    val percent = (done.toFloat() / total.toFloat() * 100).toInt()
    val hour = java.time.LocalTime.now().hour
    val greetSecond = when {
        hour < 12 -> "Morning"
        hour < 19 -> "Afternoon"
        else -> "Night"
    }
    LaunchedEffect(Unit) {
        val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        val currentShort = LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
        val currentIdx = days.indexOf(currentShort)
        if (currentIdx >= 0) vm.setDay(currentIdx)
    }
    val appCtx = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        vm.attach(appCtx)
    }

    val pressed = remember { mutableStateOf(false) }
    val rotation = androidx.compose.animation.core.animateFloatAsState(targetValue = if (pressed.value) 360f else 0f, label = "fab-rot").value
    val scale = androidx.compose.animation.core.animateFloatAsState(targetValue = if (pressed.value) 1.08f else 1f, label = "fab-scale").value

    Scaffold(
        containerColor = bg,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.openCreate() },
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .graphicsLayer(rotationZ = rotation, scaleX = scale, scaleY = scale)
                    .pointerInput(Unit) { detectTapGestures(onPress = { pressed.value = true; tryAwaitRelease(); pressed.value = false }) }
            ) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = cardBg,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = if (vm.isDark) Color(0xFF2A3442) else Color(0xFFEAF2FF)) {
                            Box(modifier = Modifier.size(56.dp).clickable { vm.openProfile() }, contentAlignment = Alignment.Center) {
                                val context = LocalContext.current
                                val uriStr = vm.profileImageUri
                                if (uriStr != null) {
                                    val uri = Uri.parse(uriStr)
                                    val bmp = remember(uriStr) {
                                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                                    }
                                    if (bmp != null) Image(bitmap = bmp, contentDescription = null, modifier = Modifier.size(56.dp)) else Text("🖼️", fontSize = 24.sp)
                                } else {
                                    Text("🖼️", fontSize = 24.sp)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = CircleShape, color = if (vm.isDark) Color(0xFF2A3442) else Color(0xFFEAF2FF)) {
                                Box(modifier = Modifier.size(44.dp).clickable { vm.openNotifications() }, contentAlignment = Alignment.Center) { Text("🔔", fontSize = 18.sp, color = if (vm.isDark) Color.White else Color(0xFF2E7CF6)) }
                            }
                            Surface(shape = CircleShape, color = if (vm.isDark) Color(0xFF2A3442) else Color(0xFFEAF2FF)) {
                                Box(modifier = Modifier.size(44.dp).clickable { vm.toggleDark() }, contentAlignment = Alignment.Center) { Text(if (vm.isDark) "🌙" else "☀️", fontSize = 18.sp, color = if (vm.isDark) Color.White else Color(0xFF2E7CF6)) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Good", fontSize = 36.sp, color = accentBlue, fontWeight = FontWeight.Bold)
                    Text(text = greetSecond, fontSize = 36.sp, color = accentBlue, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Today's ${dow}", color = textMain, fontSize = 14.sp)
                            Text(text = dateStr, color = textPrimary, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "${percent}% Done", color = textMain, fontSize = 14.sp)
                            Text(text = "Completed Tasks", color = textPrimary, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val allSelected = vm.activeFilter == com.example.mytodolist.Filter.Boards
                        val activeSelected = vm.activeFilter == com.example.mytodolist.Filter.Active
                        val doneSelected = vm.activeFilter == com.example.mytodolist.Filter.Done
                        Surface(shape = RoundedCornerShape(20.dp), color = if (allSelected) accentBlue else chipBlue, modifier = Modifier.clickable { vm.setFilter(com.example.mytodolist.Filter.Boards) }) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = accentBlue) { Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) { Text(vm.allCounter.toString(), color = Color.White, fontSize = 12.sp) } }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "All", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = if (activeSelected) accentBlue else chipBlue, modifier = Modifier.clickable { vm.setFilter(com.example.mytodolist.Filter.Active) }) { Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text(text = "Active", color = Color.White) } }
                        Surface(shape = RoundedCornerShape(20.dp), color = if (doneSelected) accentBlue else chipBlue, modifier = Modifier.clickable { vm.setFilter(com.example.mytodolist.Filter.Done) }) { Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text(text = "Done", color = Color.White) } }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                        val currentShort = LocalDate.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
                        val currentIdx = days.indexOf(currentShort)
                        days.forEachIndexed { i, d ->
                            val isPast = i < currentIdx
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if (!isPast) Modifier.clickable { vm.setDay(i) } else Modifier) {
                                val baseColor = if (i == vm.selectedDayIndex) textMain else textPrimary
                                Text(text = d, color = if (isPast) baseColor.copy(alpha = 0.4f) else baseColor)
                                if (i == vm.selectedDayIndex) { Box(modifier = Modifier.width(24.dp).height(2.dp).background(if (isPast) accentBlue.copy(alpha = 0.3f) else accentBlue)) } else { Spacer(modifier = Modifier.height(2.dp)) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    vm.filtered().forEach { t ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(t.colorHex)), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().clickable { vm.openEdit(t.id) }) {
                            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Checkbox(checked = t.completed, onCheckedChange = { vm.toggle(t.id) })
                                    Text(text = t.title, color = Color(0xFF203A24), fontSize = 20.sp)
                                }
                                Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.6f)) { Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) { Text(if (t.completed) "100%" else t.time, color = Color(0xFF203A24)) } }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (vm.showCreate) { CreateTaskModal(vm) }
    if (vm.showProfilePicker) { ProfilePickerDialog(vm = vm, onClose = { vm.closeProfile() }) }
    if (vm.showNotifications) { NotificationPanelDialog(onClose = { vm.closeNotifications() }, items = vm.upcomingTasks(), dark = vm.isDark, onTaskClick = { }) }
    if (vm.showDatePicker) { DatePickerModal(onClose = { vm.closeDatePicker() }, onDone = { sel -> vm.updatePendingDueEpochDay(sel); vm.updateSelectedCalendarEpochDay(sel); vm.closeDatePicker() }, dark = vm.isDark) }
    if (vm.showTimePicker) { TimePickerModal(onClose = { vm.closeTimePicker() }, onDone = { h, m -> vm.applyTime(h, m); vm.closeTimePicker() }, initHour = vm.selectedHour, initMinute = vm.selectedMinute, dark = vm.isDark) }
    if (vm.showRepeatSchedule) { RepeatScheduleModal(onClose = { vm.closeRepeatSchedule() }, onDone = { vm.closeRepeatSchedule() }, setPattern = { vm.setRepeat(it) }, openSound = { vm.openReminderSound() }, dark = vm.isDark) }
    if (vm.showReminderSound) { ReminderSoundModal(onClose = { vm.closeReminderSound() }, onDone = { vm.closeReminderSound() }, setSound = { vm.setSound(it) }, dark = vm.isDark) }
    if (vm.editingItemId != null) { EditTaskModal(vm) }

    BackHandler(enabled = true) {
        when {
            vm.showCreate -> vm.closeCreate()
            vm.showProfilePicker -> vm.closeProfile()
            vm.showNotifications -> vm.closeNotifications()
            vm.showDatePicker -> vm.closeDatePicker()
            vm.showTimePicker -> vm.closeTimePicker()
            vm.showRepeatSchedule -> vm.closeRepeatSchedule()
            vm.showReminderSound -> vm.closeReminderSound()
            vm.showReminderSettings -> vm.closeReminderSettings()
            vm.editingItemId != null -> vm.closeEdit()
            else -> {}
        }
    }

    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(vm.showNotifications) {
        if (vm.showNotifications) {
            vm.upcomingTasks().forEachIndexed { idx, t ->
                val granted = if (android.os.Build.VERSION.SDK_INT >= 33) androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED else true
                if (granted) {
                    val builder = androidx.core.app.NotificationCompat.Builder(context, "todo_channel")
                        .setContentTitle(t.title)
                        .setContentText((t.time.ifBlank { "Task" }) + " due soon")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setAutoCancel(true)
                    try {
                        androidx.core.app.NotificationManagerCompat.from(context).notify((t.id.toInt() shl 4) + idx, builder.build())
                    } catch (_: SecurityException) { }
                }
            }
        }
    }
}

@Composable
private fun EditTaskModal(vm: TodoViewModel) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (vm.isDark) Color(0xFF1A2230) else Color.White
    val textPrimary = if (vm.isDark) Color(0xFF95A4B8) else Color(0xFF76839A)
    val id = vm.editingItemId ?: return
    val item = vm.items.firstOrNull { it.id == id } ?: return
    var title = remember { mutableStateOf(item.title) }
    var time = remember { mutableStateOf(item.time) }
    var colorHex = remember { mutableStateOf(item.colorHex) }
    var dayIndex = remember { mutableStateOf<Int?>(item.dayIndex) }
    var reminderHour = remember { mutableStateOf<Int?>(item.reminderHour) }
    var completed = remember { mutableStateOf(item.completed) }
    val colorOptions = listOf(0xFF82D4F4, 0xFFFFD966, 0xFFFF9999, 0xFF90EE90, 0xFFDDA0DD, 0xFFFFB366)
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = (0..23).toList()

    Box(modifier = Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Edit Task", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Task Title", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    androidx.compose.material3.OutlinedTextField(value = title.value, onValueChange = { title.value = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        cursorColor = Color(0xFF2E7CF6),
                        focusedContainerColor = panelBg,
                        unfocusedContainerColor = panelBg,
                        focusedBorderColor = Color(0xFF2E7CF6),
                        unfocusedBorderColor = Color(0xFF95A4B8)
                    ))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Duration", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    androidx.compose.material3.OutlinedTextField(value = time.value, onValueChange = { time.value = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        cursorColor = Color(0xFF2E7CF6),
                        focusedContainerColor = panelBg,
                        unfocusedContainerColor = panelBg,
                        focusedBorderColor = Color(0xFF2E7CF6),
                        unfocusedBorderColor = Color(0xFF95A4B8)
                    ))
                }
                    
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Reminder Time (Hour)", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hours.size) { idx ->
                            val h = hours[idx]
                            val selected = reminderHour.value == h
                            Surface(shape = RoundedCornerShape(10.dp), color = if (selected) Color(0xFF2E7CF6) else Color(0xFF2A4B70), modifier = Modifier.clickable { reminderHour.value = h }) { Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text(String.format("%02d:00", h), color = Color.White) } }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Card Color", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            val selected = colorHex.value == c
                            Surface(color = Color(c), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp).clickable { colorHex.value = c }) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { if (selected) Box(Modifier.size(26.dp).background(Color.Transparent, CircleShape)) } }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFF2A4B70)) { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Text("🔔", color = Color.White) } }
                        Spacer(Modifier.width(12.dp))
                        Column { Text("Reminder", color = Color.White); Text(if (vm.pendingDueEpochDay != null) "Yes" else "No", color = textPrimary) }
                    }
                    TextButton(onClick = { vm.openReminderSettings() }) { Text("SET", color = Color(0xFF5AA2FF)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Mark as Completed", color = Color(0xFF95A4B8))
                    androidx.compose.material3.Switch(checked = completed.value, onCheckedChange = { completed.value = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { vm.remove(id); vm.closeEdit() }) { Text("DELETE", color = Color(0xFFFF6B6B)) }
                    TextButton(onClick = { vm.closeEdit() }) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    TextButton(onClick = {
                        val updated = item.copy(title = title.value.ifBlank { item.title }, time = time.value.ifBlank { item.time }, colorHex = colorHex.value, dayIndex = dayIndex.value, reminderHour = reminderHour.value, completed = completed.value)
                        vm.update(updated)
                        vm.closeEdit()
                    }) { Text("SAVE", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun TodoRow(item: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = item.completed, onCheckedChange = { onToggle() })
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).alpha(if (item.completed) 0.6f else 1f))
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun CreateTaskModal(vm: TodoViewModel) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (vm.isDark) Color(0xFF1A2230) else Color.White
    val accentBlue = Color(0xFF2E7CF6)
    val textPrimary = if (vm.isDark) Color(0xFF95A4B8) else Color(0xFF76839A)
    val appContext = LocalContext.current
    val panelRect = remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    val modalAlpha = androidx.compose.animation.core.animateFloatAsState(targetValue = if (closing) 0f else 1f, label = "create-alpha").value
    val modalScale = androidx.compose.animation.core.animateFloatAsState(targetValue = if (closing) 0.95f else 1f, label = "create-scale").value
    var title = remember { mutableStateOf("") }
    var colorHex = remember { mutableStateOf(0xFF82D4F4) }
    var reminderHour = remember { mutableStateOf<Int?>(vm.selectedHour) }
    var reminderMinute = remember { mutableStateOf<Int?>(vm.selectedMinute) }
    val colorOptions = listOf(0xFF82D4F4, 0xFFFFD966, 0xFFFF9999, 0xFF90EE90, 0xFFDDA0DD, 0xFFFFB366)
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = (0..23).toList()

    Box(modifier = Modifier.fillMaxSize().background(overlay).pointerInput(panelRect.value) { detectTapGestures(onTap = { pos -> if (panelRect.value?.contains(pos) == false) { closing = true; scope.launch { kotlinx.coroutines.delay(140); vm.closeCreate() } } }) }) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp).onGloballyPositioned { panelRect.value = it.boundsInRoot() }.graphicsLayer(alpha = modalAlpha, scaleX = modalScale, scaleY = modalScale)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Create New Task", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Task Title", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    androidx.compose.material3.OutlinedTextField(value = title.value, onValueChange = { title.value = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (vm.isDark) Color.White else Color.Black,
                        cursorColor = accentBlue,
                        focusedContainerColor = panelBg,
                        unfocusedContainerColor = panelBg,
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = textPrimary
                    ))
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Card Color", color = Color(0xFF95A4B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            val selected = colorHex.value == c
                            Surface(color = Color(c), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp).clickable { colorHex.value = c }) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (selected) Text("✓", color = Color.White)
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = if (vm.isDark) Color(0xFF2A4B70) else Color(0xFFE9EEF9)) { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Text("🕒", color = if (vm.isDark) Color.White else Color(0xFF1E2A3A)) } }
                        Spacer(Modifier.width(12.dp))
                        Column { Text("Time", color = if (vm.isDark) Color.White else Color(0xFF1E2A3A)); Text(String.format("%02d:%02d", vm.selectedHour, vm.selectedMinute), color = textPrimary) }
                    }
                    TextButton(onClick = { vm.openTimePicker() }) { Text("SET", color = Color(0xFF5AA2FF)) }
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = if (vm.isDark) Color(0xFF2A4B70) else Color(0xFFE9EEF9)) { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Text("🔁", color = if (vm.isDark) Color.White else Color(0xFF1E2A3A)) } }
                        Spacer(Modifier.width(12.dp))
                        Column { Text("Repeat", color = if (vm.isDark) Color.White else Color(0xFF1E2A3A)); Text(vm.selectedRepeatPattern.name, color = textPrimary) }
                    }
                    TextButton(onClick = { vm.openRepeatSchedule() }) { Text("SET", color = Color(0xFF5AA2FF)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { vm.closeCreate() }) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { vm.openDatePicker() }) { Text("Select Date", color = Color(0xFF5AA2FF)) }
                    val createPressed = remember { mutableStateOf(false) }
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2E7CF6), modifier = Modifier
                        .graphicsLayer(scaleX = if (createPressed.value) 0.98f else 1f, scaleY = if (createPressed.value) 0.98f else 1f)
                        .clickable {
                            createPressed.value = true
                            val ttl = title.value.ifBlank { "New Task" }
                            val dur = String.format("%02d:%02d", vm.selectedHour, vm.selectedMinute)
                            val created = vm.addItemDetailed(ttl, dur, colorHex.value, vm.selectedDayIndex, vm.selectedHour, vm.pendingDueEpochDay, vm.selectedMinute, vm.selectedRepeatPattern, vm.selectedSoundIndex)
                            com.example.mytodolist.ReminderScheduler.scheduleExact(appContext, created)
                            vm.closeCreate()
                            createPressed.value = false
                        }) {
                        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text("Create", color = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerModal(onClose: () -> Unit, onDone: (Int, Int) -> Unit, initHour: Int, initMinute: Int, dark: Boolean) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (dark) Color(0xFF1A2230) else Color.White
    val hourList = (0..23).toList()
    val minuteList = (0..59).toList()
    var hSel = remember { mutableStateOf(initHour.coerceIn(0, 23)) }
    var mSel = remember { mutableStateOf(initMinute.coerceIn(0, 59)) }
    Box(Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Select Time", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.align(Alignment.Center)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour", color = if (dark) Color(0xFF95A4B8) else Color(0xFF76839A))
                            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(200.dp)) {
                                items(hourList.size) { idx ->
                                    val sel = hSel.value == idx
                                    Surface(shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF2E7CF6) else if (dark) Color(0xFF2A4B70) else Color(0xFFE9EEF9), modifier = Modifier.clickable { hSel.value = idx }) {
                                        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { Text(String.format("%02d", idx), color = if (dark) Color.White else Color(0xFF1E2A3A)) }
                                    }
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute", color = if (dark) Color(0xFF95A4B8) else Color(0xFF76839A))
                            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(200.dp)) {
                                items(minuteList.size) { idx ->
                                    val sel = mSel.value == idx
                                    Surface(shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF2E7CF6) else if (dark) Color(0xFF2A4B70) else Color(0xFFE9EEF9), modifier = Modifier.clickable { mSel.value = idx }) {
                                        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { Text(String.format("%02d", idx), color = if (dark) Color.White else Color(0xFF1E2A3A)) }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onClose) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    TextButton(onClick = { onDone(hSel.value, mSel.value) }) { Text("DONE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}

@Composable
private fun RepeatScheduleModal(onClose: () -> Unit, onDone: () -> Unit, setPattern: (com.example.mytodolist.RepeatPattern) -> Unit, openSound: () -> Unit, dark: Boolean) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (dark) Color(0xFF1A2230) else Color.White
    val patterns = listOf(
        com.example.mytodolist.RepeatPattern.Daily,
        com.example.mytodolist.RepeatPattern.Weekdays,
        com.example.mytodolist.RepeatPattern.Weekends,
        com.example.mytodolist.RepeatPattern.Weekly,
        com.example.mytodolist.RepeatPattern.BiWeekly,
        com.example.mytodolist.RepeatPattern.Monthly,
        com.example.mytodolist.RepeatPattern.Yearly,
        com.example.mytodolist.RepeatPattern.Custom
    )
    Box(Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Repeat Schedule", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(300.dp)) {
                    val rows = patterns.chunked(2)
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { p ->
                                Surface(shape = RoundedCornerShape(16.dp), color = if (dark) Color(0xFF2A4B70) else Color(0xFFE9EEF9), modifier = Modifier.weight(1f).clickable { setPattern(p) }) {
                                    Box(Modifier.padding(16.dp)) { Text(p.name, color = if (dark) Color.White else Color(0xFF1E2A3A)) }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2E7CF6), modifier = Modifier.clickable { openSound() }) { Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text("Reminder Sound", color = Color.White) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onClose) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    TextButton(onClick = onDone) { Text("DONE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}

@Composable
private fun ReminderSoundModal(onClose: () -> Unit, onDone: () -> Unit, setSound: (Int) -> Unit, dark: Boolean) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (dark) Color(0xFF1A2230) else Color.White
    val context = LocalContext.current
    val tones = listOf(
        "Gentle Bell","Morning Birds","Wind Chimes","Soft Piano","Ocean Waves","Zen Gong","Happy Bells","Calm Beat","Forest","Rain"
    )
    var current = remember { mutableStateOf(-1) }
    Box(Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Reminder Sound", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(320.dp)) {
                    items(tones.size) { idx ->
                        val title = tones[idx]
                        Surface(shape = RoundedCornerShape(16.dp), color = if (dark) Color(0xFF2A4B70) else Color(0xFFE9EEF9), modifier = Modifier.clickable {
                            current.value = idx
                            setSound(idx)
                            try {
                                val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100)
                                tg.startTone(android.media.ToneGenerator.TONE_PROP_PROMPT, 300)
                            } catch (_: Exception) { }
                        }) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column { Text(title, color = if (dark) Color.White else Color(0xFF1E2A3A)); Text("Tap to preview", color = Color(0xFF95A4B8)) }
                                Text(if (current.value == idx) "✓" else "▶", color = if (dark) Color.White else Color(0xFF1E2A3A))
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onClose) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    TextButton(onClick = onDone) { Text("DONE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}

@Composable
private fun ProfilePickerDialog(vm: TodoViewModel, onClose: () -> Unit) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (vm.isDark) Color(0xFF1A2230) else Color.White
    val colors = listOf(0xFFFFE5E5, 0xFFE5F3FF, 0xFFFFF5E5, 0xFFF0E5FF, 0xFFE5FFEB, 0xFFFFEBE5, 0xFFE5D5C5, 0xFFE5F5FF)
    val context = LocalContext.current
    val selectedBitmap = remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    val panelRect = remember { mutableStateOf<Rect?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        vm.setProfileUri(uri?.toString())
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                selectedBitmap.value = bmp?.asImageBitmap()
            }
        }
    }
    Box(Modifier.fillMaxSize().background(overlay).pointerInput(panelRect.value) { detectTapGestures(onTap = { pos -> if (panelRect.value?.contains(pos) == false) onClose() }) }) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp).onGloballyPositioned { panelRect.value = it.boundsInRoot() }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Choose Profile Photo", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFF5AA2FF)) {
                        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                            if (selectedBitmap.value != null) {
                                Image(bitmap = selectedBitmap.value!!, contentDescription = null, modifier = Modifier.size(96.dp))
                            } else {
                                val selName = vm.profileAnimalName
                                val resId = selName?.let { context.resources.getIdentifier(it, "drawable", context.packageName) } ?: 0
                                if (resId != 0) {
                                    Image(painter = painterResource(id = resId), contentDescription = null, modifier = Modifier.size(96.dp))
                                } else {
                                    val emoji = when (selName) {
                                        "emoji_boy_light" -> "👦🏻"
                                        "emoji_boy_dark" -> "👦🏿"
                                        "emoji_girl_light" -> "👧🏻"
                                        "emoji_girl_dark" -> "👧🏿"
                                        else -> "🐰"
                                    }
                                    Text(emoji, fontSize = 36.sp)
                                }
                            }
                        }
                    }
                }
                // animal avatars removed intentionally
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    listOf("emoji_boy_light", "emoji_boy_dark", "emoji_girl_light", "emoji_girl_dark").forEach { key ->
                        val selected = vm.profileAnimalName == key
                        val pressed = remember { mutableStateOf(false) }
                        Surface(shape = RoundedCornerShape(16.dp), color = if (vm.isDark) Color(0xFF2A3442) else Color(0xFFEAF2FF), modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer(scaleX = if (pressed.value) 0.97f else 1f, scaleY = if (pressed.value) 0.97f else 1f)
                            .pointerInput(Unit) { detectTapGestures(onPress = { pressed.value = true; tryAwaitRelease(); pressed.value = false; vm.setProfileAnimal(key) }) }
                            .border(width = if (selected) 2.dp else 0.dp, color = Color(0xFF2E7CF6), shape = RoundedCornerShape(16.dp))) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                val emoji = when (key) {
                                    "emoji_boy_light" -> "👦🏻"
                                    "emoji_boy_dark" -> "👦🏿"
                                    "emoji_girl_light" -> "👧🏻"
                                    "emoji_girl_dark" -> "👧🏿"
                                    else -> "🙂"
                                }
                                Text(emoji, fontSize = 32.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2E7CF6), modifier = Modifier.clickable { launcher.launch("image/*") }) {
                        Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text("Upload custom photo", color = Color.White) }
                    }
                    // keep only default avatars and user upload; no extra removal button
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onClose) { Text("DONE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationPanelDialog(onClose: () -> Unit, items: List<TodoItem>, dark: Boolean, onTaskClick: (TodoItem) -> Unit) {
    val overlay = Color.Black.copy(alpha = 0.7f)
    val panelBg = if (dark) Color(0xFF1A2230) else Color.White
    Box(Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Upcoming Tasks", color = Color(0xFF5AA2FF), fontSize = 22.sp)
                items.forEach { t ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(t.colorHex)), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t.title, color = Color(0xFF203A24))
                            Text(t.time, color = Color(0xFF203A24))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onClose) { Text("CLOSE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}

@Composable
private fun DatePickerModal(onClose: () -> Unit, onDone: (Long?) -> Unit, dark: Boolean) {
    val overlay = Color.Black.copy(alpha = 0.5f)
    val panelBg = if (dark) Color(0xFF1A2230) else Color.White
    val monthNames = listOf("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER")
    val weekDays = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    val now = java.time.LocalDate.now()
    val currentMonth = remember { mutableStateOf(now.monthValue - 1) }
    val currentYear = remember { mutableStateOf(now.year) }
    val selected = remember { mutableStateOf(now.dayOfMonth) }

    fun daysInMonth(month: Int, year: Int): Int = java.time.YearMonth.of(year, month + 1).lengthOfMonth()
    fun firstDay(month: Int, year: Int): Int = java.time.LocalDate.of(year, month + 1, 1).dayOfWeek.value % 7

    val year = currentYear.value
    val totalDays = daysInMonth(currentMonth.value, year)
    val start = firstDay(currentMonth.value, year)
    val grid = mutableListOf<Int?>()
    repeat(start) { grid.add(null) }
    for (d in 1..totalDays) grid.add(d)
    while (grid.size % 7 != 0) grid.add(null)

    Box(Modifier.fillMaxSize().background(overlay)) {
        Surface(color = panelBg, shape = RoundedCornerShape(28.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("<", color = Color(0xFF5AA2FF), modifier = Modifier.clickable {
                        if (currentMonth.value == 0) { currentMonth.value = 11; currentYear.value = currentYear.value - 1 } else { currentMonth.value = currentMonth.value - 1 }
                    })
                    Text("${monthNames[currentMonth.value]} $year", color = Color(0xFF5AA2FF), fontSize = 16.sp)
                    Text(">", color = Color(0xFF5AA2FF), modifier = Modifier.clickable {
                        if (currentMonth.value == 11) { currentMonth.value = 0; currentYear.value = currentYear.value + 1 } else { currentMonth.value = currentMonth.value + 1 }
                    })
                    Spacer(Modifier.width(16.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2E7CF6), modifier = Modifier.clickable {
                        currentMonth.value = now.monthValue - 1
                        currentYear.value = now.year
                        selected.value = now.dayOfMonth
                    }) { Box(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) { Text("Today", color = Color.White) } }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { weekDays.forEach { Text(it, color = if (dark) Color(0xFF95A4B8) else Color(0xFF76839A)) } }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until (grid.size / 7)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0 until 7) {
                                val day = grid[row * 7 + col]
                                val cellDate = day?.let { java.time.LocalDate.of(year, currentMonth.value + 1, it) }
                                val past = cellDate?.isBefore(java.time.LocalDate.now()) == true
                                Surface(shape = CircleShape, color = if (day != null && day == selected.value) Color(0xFF2E7CF6) else Color.Transparent, modifier = Modifier.size(40.dp).alpha(if (past) 0.4f else 1f).clickable(enabled = day != null && !past) { selected.value = day!! }) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(day?.toString() ?: "", color = if (dark) Color.White else Color(0xFF1E2A3A)) }
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onClose) { Text("CANCEL", color = Color(0xFF5AA2FF)) }
                    TextButton(onClick = { val epoch = java.time.LocalDate.of(year, currentMonth.value + 1, selected.value).toEpochDay(); onDone(epoch) }) { Text("DONE", color = Color(0xFF5AA2FF)) }
                }
            }
        }
    }
}
