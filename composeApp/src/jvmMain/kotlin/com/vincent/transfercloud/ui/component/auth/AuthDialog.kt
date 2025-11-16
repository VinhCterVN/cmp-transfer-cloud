package com.vincent.transfercloud.ui.component.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.koin.compose.koinInject

@Composable
fun AuthDialog(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val currentUser by appState.currentUser.collectAsState()
	val startDestination = AuthDestination.LOGIN
	var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

	var username: String by remember { mutableStateOf("") }
	var email: String by remember { mutableStateOf("") }
	var password: String by remember { mutableStateOf("") }

	if (currentUser == null) {
		Dialog(
			onDismissRequest = {},
		) {
			Column(
				Modifier.heightIn(min = 450.dp)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
					.padding(12.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceEvenly
			) {
				PrimaryTabRow(
					selectedTabIndex = selectedDestination,
					modifier = Modifier.padding(2.dp)
				) {
					AuthDestination.entries.forEachIndexed { index, destination ->
						Tab(
							selected = selectedDestination == index,
							onClick = {
								selectedDestination = index
							},
							text = {
								Text(
									text = destination.name.lowercase().replaceFirstChar { it.uppercase() },
									maxLines = 2,
									style = TitleLineBig,
									overflow = TextOverflow.Ellipsis
								)
							},
							modifier = Modifier.clip(RoundedCornerShape(12.dp))
						)
					}
				}

				when (AuthDestination.entries[selectedDestination]) {
					AuthDestination.LOGIN -> @Composable {
						Text("Login to your Account", style = HeadLineMedium)

						TextField(
							value = username,
							onValueChange = { username = it },
							shape = RoundedCornerShape(12.dp),
							placeholder = { Text("Username") },
							leadingIcon = { Icon(Icons.Default.Person, null) },
							maxLines = 1,
							colors = TextFieldDefaults.colors(
								focusedIndicatorColor = Color.Transparent,
								disabledIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
								errorIndicatorColor = Color.Transparent,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						)
						TextField(
							value = password,
							onValueChange = { password = it },
							shape = RoundedCornerShape(12.dp),
							placeholder = { Text("Password") },
							leadingIcon = { Icon(Icons.Default.Lock, null) },
							maxLines = 1,
							visualTransformation = PasswordVisualTransformation(),
							keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
							colors = TextFieldDefaults.colors(
								focusedIndicatorColor = Color.Transparent,
								disabledIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
								errorIndicatorColor = Color.Transparent,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						)

					}

					AuthDestination.REGISTER -> @Composable {
						Text("Register a new Account", style = HeadLineMedium)

						TextField(
							value = username,
							onValueChange = { username = it },
							shape = RoundedCornerShape(12.dp),
							placeholder = { Text("Your Username") },
							leadingIcon = { Icon(Icons.Default.Person, null) },
							maxLines = 1,
							colors = TextFieldDefaults.colors(
								focusedIndicatorColor = Color.Transparent,
								disabledIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
								errorIndicatorColor = Color.Transparent,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						)

						TextField(
							value = email,
							onValueChange = { email = it },
							shape = RoundedCornerShape(12.dp),
							placeholder = { Text("Your Email") },
							leadingIcon = { Icon(Icons.Default.Person, null) },
							maxLines = 1,
							colors = TextFieldDefaults.colors(
								focusedIndicatorColor = Color.Transparent,
								disabledIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
								errorIndicatorColor = Color.Transparent,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						)

						TextField(
							value = password,
							onValueChange = { password = it },
							shape = RoundedCornerShape(12.dp),
							placeholder = { Text("Password") },
							leadingIcon = { Icon(Icons.Default.Lock, null) },
							maxLines = 1,
							colors = TextFieldDefaults.colors(
								focusedIndicatorColor = Color.Transparent,
								disabledIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
								errorIndicatorColor = Color.Transparent,
								focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
							)
						)
					}
				}

				Row(
					modifier = Modifier.fillMaxWidth(0.75f),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					OutlinedButton(
						onClick = { },
						shape = RoundedCornerShape(8.dp)
					) {
						Text("Config")
					}

					ElevatedButton(
						onClick = {
							when (AuthDestination.entries[selectedDestination]) {
								AuthDestination.LOGIN -> {
									viewModel.login(username, password)
								}

								AuthDestination.REGISTER -> {
									viewModel.register(username, email, password)
								}}
						},
						shape = RoundedCornerShape(8.dp),
						modifier = Modifier.weight(1f),
						elevation = ButtonDefaults.buttonElevation(8.dp),
						colors = ButtonDefaults.buttonColors(
							containerColor = MaterialTheme.colorScheme.onSecondaryContainer
						)
					) {
						Text(if (selectedDestination == AuthDestination.LOGIN.ordinal) "Login" else "Register")
					}
				}
			}
		}
	}
}

enum class AuthDestination {
	LOGIN, REGISTER
}