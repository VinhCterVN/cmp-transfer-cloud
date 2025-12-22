package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
	appState: AppState
) : BaseSocketViewModel(appState) {
	private val _query: MutableStateFlow<String> = MutableStateFlow("")
	val query = _query.asStateFlow()
	private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Ready)
	val uiState = _uiState.asStateFlow()

	fun setQuery(query: String) {
		_query.value = query
	}

	fun clearQuery() = viewModelScope.launch {
		_query.emit("")
	}

	fun handleSearch(query: String) {

	}
}