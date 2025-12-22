package com.vincent.transfercloud.core.module

import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import com.vincent.transfercloud.ui.viewModel.DirectTransferReceiveVM
import com.vincent.transfercloud.ui.viewModel.DirectTransferSendVM
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.ui.viewModel.SearchViewModel
import com.vincent.transfercloud.ui.viewModel.ShareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
	single { AppState() }
	single { AppViewModel(get()) }
	single { FolderViewModel(get()) }
	viewModel { ShareViewModel(get()) }
	viewModel { SearchViewModel(get()) }
	viewModel { DirectTransferReceiveVM(get()) }
	viewModel { DirectTransferSendVM(get()) }
}