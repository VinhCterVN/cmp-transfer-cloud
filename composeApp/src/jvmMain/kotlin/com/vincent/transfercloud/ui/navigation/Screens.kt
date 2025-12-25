package com.vincent.transfercloud.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vincent.transfercloud.ui.screens.DirectTransferUI
import com.vincent.transfercloud.ui.screens.DirectTransferSendUI
import com.vincent.transfercloud.ui.screens.FolderUI
import com.vincent.transfercloud.ui.screens.ShareScreen
import com.vincent.transfercloud.ui.screens.TransferApp
import com.vincent.transfercloud.ui.screens.auth.AppGate

object AuthScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = AuthScreen

	@Composable
	override fun Content() = AppGate()
}

object AppScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = AppScreen

	@Composable
	override fun Content() = TransferApp()
}

class FolderDetailView(val id: String) : Screen {
	override val key: ScreenKey = uniqueScreenKey

	@Composable
	override fun Content() = FolderUI(id = id)
}

object ShareWithMe : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = ShareWithMe

	@Composable
	override fun Content() = ShareScreen()
}

object DirectTransferReceiveScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = DirectTransferReceiveScreen

	@Composable
	override fun Content() = DirectTransferUI()
}

object DirectTransferSendScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = DirectTransferSendScreen

	@Composable
	override fun Content() = DirectTransferSendUI()
}