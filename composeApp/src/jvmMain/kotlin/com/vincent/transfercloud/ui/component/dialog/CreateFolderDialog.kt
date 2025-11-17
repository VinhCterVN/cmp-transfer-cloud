package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.MessageStyle
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderDialog(
	appState: AppState = koinInject<AppState>(),
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val currentFolder by appState.currentFolder.collectAsState()
	val dialogShow by appState.isCreatingFolder.collectAsState()
	var folderName by remember { mutableStateOf("") }
	val scaffoldState = LocalBottomSheetScaffoldState.current

	if (dialogShow)
		Dialog(onDismissRequest = { appState.isCreatingFolder.value = false }) {
			Card(
				Modifier.fillMaxWidth(0.8f),
				shape = RoundedCornerShape(12.dp),
				elevation = CardDefaults.cardElevation(8.dp),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
			) {
				Column(
					Modifier.fillMaxWidth().padding(20.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Text("New Folder...", style = TitleLineLarge, modifier = Modifier.align(Alignment.Start))

					OutlinedTextField(
						value = folderName,
						onValueChange = { folderName = it },
						label = { Text("Folder Name") },
						textStyle = MessageStyle,
						modifier = Modifier.fillMaxWidth()
					)

					Row(
						modifier = Modifier.align(Alignment.End),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						TextButton({
							appState.isCreatingFolder.value = false
						}) {
							Text("Close", style = TitleLineLarge.copy(fontSize = 16.sp))
						}
						Button(onClick = {
							(scope.launch {
								val res = viewModel.createFolder(folderName, currentFolder)
								scaffoldState.snackbarHostState.showSnackbar(
									res,
									withDismissAction = true,
									duration = SnackbarDuration.Short
								)
							})
						}) {
							Text("Create", style = TitleLineLarge.copy(fontSize = 16.sp))
						}
					}
				}
			}
		}
}