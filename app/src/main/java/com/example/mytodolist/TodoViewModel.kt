package com.example.mytodolist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class TodoItem(
    val id: Long,
    val title: String,
    val time: String,
    val colorHex: Long,
    val completed: Boolean = false,
    val dayIndex: Int? = null,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val dueEpochDay: Long? = null,
    val repeat: RepeatPattern? = null,
    val soundIndex: Int? = null
)

enum class Filter { Boards, Active, Done }
enum class RepeatPattern { None, Daily, Weekdays, Weekends, Weekly, BiWeekly, Monthly, Yearly, Custom }

class TodoViewModel : ViewModel() {
    var query by mutableStateOf("")
        private set

    var isDark by mutableStateOf(true)
    var showProfilePicker by mutableStateOf(false)
    var showNotifications by mutableStateOf(false)
    var showCreate by mutableStateOf(false)
    var showDatePicker by mutableStateOf(false)
    var showTimePicker by mutableStateOf(false)
    var showRepeatSchedule by mutableStateOf(false)
    var showReminderSound by mutableStateOf(false)
    var showReminderSettings by mutableStateOf(false)

    var selectedDayIndex by mutableIntStateOf(2)
    var activeFilter by mutableStateOf(Filter.Active)
    var editingItemId by mutableStateOf<Long?>(null)
    var profileImageUri by mutableStateOf<String?>(null)
    var profileAnimalName by mutableStateOf<String?>(null)
    private val uploadedProfileUris = mutableStateListOf<String>()
    private var appCtx: android.content.Context? = null
    var pendingDueEpochDay by mutableStateOf<Long?>(null)
    var selectedCalendarEpochDay by mutableStateOf<Long?>(null)
    var selectedHour by mutableIntStateOf(0)
    var selectedMinute by mutableIntStateOf(0)
    var selectedSoundIndex by mutableIntStateOf(0)
    var selectedRepeatPattern by mutableStateOf(RepeatPattern.None)

    private val _items = mutableStateListOf<TodoItem>()
    val items: List<TodoItem> get() = _items

    fun attach(context: android.content.Context) {
        appCtx = context.applicationContext
        TaskDb.init(context)
        val loaded = TaskDb.getAll()
        _items.clear()
        _items.addAll(loaded)
        allCounter = _items.size
        val prefs = context.getSharedPreferences("profile_prefs", android.content.Context.MODE_PRIVATE)
        profileImageUri = prefs.getString("profileUri", null)
        profileAnimalName = prefs.getString("profileAnimal", null)
    }

    fun onQueryChange(value: String) { query = value }
    fun toggleDark() { isDark = !isDark }
    fun setFilter(f: Filter) { activeFilter = f }
    fun setDay(i: Int) { selectedDayIndex = i }
    fun openCreate() { showCreate = true }
    fun closeCreate() { showCreate = false }
    fun openProfile() { showProfilePicker = true }
    fun closeProfile() { showProfilePicker = false }
    fun openNotifications() { showNotifications = true }
    fun closeNotifications() { showNotifications = false }
    fun openDatePicker() { showDatePicker = true }
    fun closeDatePicker() { showDatePicker = false }
    fun openTimePicker() { showTimePicker = true }
    fun closeTimePicker() { showTimePicker = false }
    fun openRepeatSchedule() { showRepeatSchedule = true }
    fun closeRepeatSchedule() { showRepeatSchedule = false }
    fun openReminderSound() { showReminderSound = true }
    fun closeReminderSound() { showReminderSound = false }
    fun openReminderSettings() { showReminderSettings = true }
    fun closeReminderSettings() { showReminderSettings = false }
    fun openEdit(id: Long) { editingItemId = id }
    fun closeEdit() { editingItemId = null }
    fun setProfileUri(uri: String?) {
        profileImageUri = uri
        if (uri != null) uploadedProfileUris.add(uri)
        appCtx?.getSharedPreferences("profile_prefs", android.content.Context.MODE_PRIVATE)?.edit()?.putString("profileUri", uri)?.apply()
    }
    fun setProfileAnimal(name: String?) {
        profileAnimalName = name
        appCtx?.getSharedPreferences("profile_prefs", android.content.Context.MODE_PRIVATE)?.edit()?.putString("profileAnimal", name)?.apply()
    }
    fun updatePendingDueEpochDay(value: Long?) { pendingDueEpochDay = value }
    fun updateSelectedCalendarEpochDay(value: Long?) { selectedCalendarEpochDay = value }
    fun applyTime(h: Int, m: Int) { selectedHour = h; selectedMinute = m }
    fun setRepeat(pattern: RepeatPattern) { selectedRepeatPattern = pattern }
    fun setSound(index: Int) { selectedSoundIndex = index }

    fun clearProfileUploads() {
        profileImageUri = null
        uploadedProfileUris.clear()
    }

    fun addItem() {
        val t = query.trim()
        if (t.isEmpty()) return
        val id = if (_items.isEmpty()) 1L else (_items.maxOf { it.id } + 1L)
        val item = TodoItem(id, t, "1h", 0xFF82D4F4, completed = false, dayIndex = selectedDayIndex)
        _items.add(0, item)
        TaskDb.insert(item)
        allCounter += 1
        query = ""
    }

    fun addItemDetailed(title: String, time: String, colorHex: Long, dayIndex: Int?, reminderHour: Int?, dueEpochDay: Long? = null, reminderMinute: Int? = null, repeat: RepeatPattern? = null, soundIndex: Int? = null): TodoItem {
        val id = if (_items.isEmpty()) 1L else (_items.maxOf { it.id } + 1L)
        val item = TodoItem(id, title, time, colorHex, false, dayIndex, reminderHour, reminderMinute, dueEpochDay, repeat, soundIndex)
        _items.add(0, item)
        TaskDb.insert(item)
        allCounter += 1
        return item
    }

    fun toggle(id: Long) {
        val index = _items.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _items[index]
            val updated = item.copy(completed = !item.completed)
            _items[index] = updated
            TaskDb.updateCompleted(updated.id, updated.completed)
        }
    }

    fun update(updated: TodoItem) {
        val index = _items.indexOfFirst { it.id == updated.id }
        if (index != -1) { _items[index] = updated; TaskDb.update(updated) }
    }
    fun remove(id: Long) {
        val index = _items.indexOfFirst { it.id == id }
        if (index != -1) {
            _items.removeAt(index)
            TaskDb.delete(id)
            if (allCounter > 0) allCounter -= 1
        }
    }

    fun filtered(): List<TodoItem> {
        return items.filter { item ->
            val dayOk = selectedCalendarEpochDay?.let { sel -> item.dueEpochDay == sel } ?: (item.dayIndex == selectedDayIndex)
            val statusOk = when (activeFilter) {
                Filter.Done -> item.completed
                Filter.Active -> !item.completed
                Filter.Boards -> true
            }
            dayOk && statusOk
        }
    }

    fun upcomingCount(): Int {
        val today = java.time.LocalDate.now().toEpochDay()
        return items.count { t ->
            !t.completed && t.dueEpochDay?.let { d ->
                val diff = d - today
                diff in 0..2
            } ?: false
        }
    }

    fun upcomingTasks(): List<TodoItem> {
        val today = java.time.LocalDate.now().toEpochDay()
        return items.filter { t -> !t.completed && t.dueEpochDay?.let { d -> val diff = d - today; diff in 0..2 } ?: false }
    }

    var allCounter by mutableIntStateOf(0)
}
