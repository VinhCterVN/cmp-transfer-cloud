package com.vincent.transfercloud.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.vincent.transfercloud.ui.component.FileLargePanel
import com.vincent.transfercloud.ui.component.FileDetailsView

class FileView : Screen {
		@Composable
		override fun Content() {
			FileLargePanel()
		}
	}

	data class MailDetails(val emailId: String) : Screen {
		@Composable
		override fun Content() {
			FileDetailsView(emailId)
		}
	}