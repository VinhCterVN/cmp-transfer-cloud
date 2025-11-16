package com.vincent.transfercloud.core.module

import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.koin.dsl.module

val appModule = module {
	single { AppViewModel(get()) }
	single { AppState() }
}