package com.vincent.transfercloud.core.module

import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
	single { AppState() }
	single { SocketRepository(get()) }
	single { AppViewModel(get(), get()) }
	single { FolderViewModel(get(), get()) }
	viewModel { FileDetailVM(get(), get()) }
	viewModel { ShareDialogVM(get(), get()) }
	viewModel { ShareViewModel(get(), get()) }
	viewModel { SearchViewModel(get(), get()) }
	viewModel { DirectTransferSendVM(get()) }
	viewModel { DirectTransferReceiveVM(get()) }
}