package com.vincent.transfercloud.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vincent.transfercloud.ui.screens.FolderView
import com.vincent.transfercloud.ui.screens.HomeUI
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

class HomeScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey

	@Composable
	override fun Content() = HomeUI()
}

class FolderDetailView(val id: String) : Screen {
	override val key: ScreenKey = uniqueScreenKey

	@Composable
	override fun Content() = FolderView(id = id)
}