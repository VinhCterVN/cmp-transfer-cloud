package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ShareViewModel(
	appState: AppState,
	socketRepository: SocketRepository
) : BaseSocketViewModel(appState, socketRepository) {
	private val _shareData = MutableStateFlow<Map<TimeGroup, List<SharedUiItem>>>(emptyMap())
	val shareData = _shareData.asStateFlow()
	private val _uiState = MutableStateFlow<UIState>(UIState.Ready)
	val uiState = _uiState.asStateFlow()

	fun getSharedData() = viewModelScope.launch(Dispatchers.IO) {
		_uiState.emit(UIState.Loading)
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest(
				resource = "shared-with-me",
				ownerId = appState.currentUser.value!!.id,
			),
			onSuccess = { res ->
				val data = json.decodeFromString<GetSharedDataResponse>(res.data!!)
				val groupedData = groupSharedItems(data.folders, data.files)
				_shareData.emit(groupedData)
			},
			onError = { e ->
				_uiState.emit(UIState.Error(e))
				println("Error fetching shared data: $e")
			}
		).join()
		_uiState.emit(UIState.Ready)
	}

	fun groupSharedItems(
		folders: List<FolderOutputDto>,
		files: List<FileOutputDto>
	): Map<TimeGroup, List<SharedUiItem>> {
		val allItems = mutableListOf<SharedUiItem>()
		allItems.addAll(folders.map { SharedUiItem.FolderItem(it) })
		allItems.addAll(files.map { SharedUiItem.FileItem(it) })
		allItems.sortByDescending { it.sharedAt }
		return allItems
			.groupBy { getTimeGroup(it.sharedAt) }
			.toSortedMap(compareBy { it.ordinal })
	}

	fun getTimeGroup(isoString: String?): TimeGroup {
		if (isoString.isNullOrBlank()) return TimeGroup.OLDER
		return try {
			val instant = Instant.parse(isoString)
			val zoneId = ZoneId.systemDefault()
			val inputDate = instant.atZone(zoneId).toLocalDate()
			val today = LocalDate.now(zoneId)
			val daysDiff = ChronoUnit.DAYS.between(inputDate, today)
			when {
				daysDiff == 0L -> TimeGroup.TODAY
				daysDiff == 1L -> TimeGroup.YESTERDAY
				daysDiff < 7L -> TimeGroup.THIS_WEEK
				inputDate.year == today.year && inputDate.month == today.month -> TimeGroup.THIS_MONTH
				else -> TimeGroup.OLDER
			}
		} catch (e: Exception) {
			TimeGroup.OLDER
		}
	}

	fun reload() = getSharedData()
}

sealed interface SharedUiItem {
	val id: String
	val name: String
	val sharedAt: String?

	data class FolderItem(val data: FolderOutputDto) : SharedUiItem {
		override val id = data.id
		override val name = data.name
		override val sharedAt = data.sharedAt
	}

	data class FileItem(val data: FileOutputDto) : SharedUiItem {
		override val id = data.id
		override val name = data.name
		override val sharedAt = data.sharedAt
	}
}

enum class TimeGroup(val label: String) {
	TODAY("Today"),
	YESTERDAY("Yesterday"),
	THIS_WEEK("This week"),
	THIS_MONTH("This month"),
	OLDER("Older")
}