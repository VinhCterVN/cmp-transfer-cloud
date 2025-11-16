package com.vincent.transfercloud.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vincent.transfercloud.ui.component.FileLargePanel
import com.vincent.transfercloud.ui.screens.TransferApp
import com.vincent.transfercloud.ui.screens.auth.AuthPage

class FileView : Screen {
	override val key: ScreenKey = uniqueScreenKey

	@Composable
	override fun Content() = FileLargePanel()
}

object AuthScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = AuthScreen

	@Composable
	override fun Content() = AuthPage()
}

object AppScreen : Screen {
	override val key: ScreenKey = uniqueScreenKey
	private fun readResolve(): Any = AppScreen

	@Composable
	override fun Content() = TransferApp()
}