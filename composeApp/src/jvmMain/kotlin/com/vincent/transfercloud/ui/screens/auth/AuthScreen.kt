package com.vincent.transfercloud.ui.screens.auth

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.vincent.transfercloud.ui.navigation.AppScreen
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.undraw_login

@Composable
fun AuthPage(
	appState: AppState = koinInject<AppState>(),
	viewModel: AuthViewModel = AuthViewModel(appState)
) {
	val scope = rememberCoroutineScope()
	val windowState = koinInject<WindowState>()
	val currentUser by appState.currentUser.collectAsState()
	val startDestination = AuthDestination.LOGIN
	var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
	var username: String by remember { mutableStateOf("") }
	var email: String by remember { mutableStateOf("") }
	var password: String by remember { mutableStateOf("") }

	if (currentUser != null) Navigator(AppScreen) {
		SlideTransition(
			it, animationSpec = tween(
				durationMillis = 500,
				delayMillis = 100
			)
		)
	}
	else {
		Box(
			Modifier.fillMaxSize().background(
				brush = Brush.linearGradient(
					colors = listOf(
						MaterialTheme.colorScheme.onPrimaryContainer,
						MaterialTheme.colorScheme.onSecondaryContainer,
					),
					start = Offset.Zero,
					end = Offset.Infinite
				)
			), contentAlignment = Alignment.Center
		) {
			Card(
				shape = RoundedCornerShape(12.dp),
				elevation = CardDefaults.cardElevation(4.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface
				),
				modifier = Modifier.fillMaxSize(0.75f).wrapContentSize()
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					if (windowState.size.width > 900.dp)
						Box(
							Modifier.sizeIn(maxHeight = 400.dp, maxWidth = 400.dp).fillMaxWidth().aspectRatio(1f),
							contentAlignment = Alignment.Center
						) {
							Image(
								painter = painterResource(Res.drawable.undraw_login), contentDescription = null
							)
						}

					Column(
						Modifier.heightIn(min = 400.dp)
							.padding(horizontal = 12.dp).weight(1f),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.SpaceEvenly
					) {
						PrimaryTabRow(
							selectedTabIndex = selectedDestination, modifier = Modifier.padding(2.dp)
						) {
							AuthDestination.entries.forEachIndexed { index, destination ->
								Tab(
									selected = selectedDestination == index, onClick = {
										selectedDestination = index
									}, text = {
										Text(
											text = destination.name.lowercase().replaceFirstChar { it.uppercase() },
											maxLines = 2,
											style = TitleLineBig,
											overflow = TextOverflow.Ellipsis
										)
									}, modifier = Modifier.clip(RoundedCornerShape(12.dp))
								)
							}
						}

						when (AuthDestination.entries[selectedDestination]) {
							AuthDestination.LOGIN -> @Composable {
								Text("Login to your Account ", style = HeadLineMedium)
								OutlinedTextField(
									value = email,
									onValueChange = { email = it },
									shape = RoundedCornerShape(12.dp),
									label = { Text("Enter your email") },
									leadingIcon = { Icon(Icons.Default.Person, null) },
									modifier = Modifier.fillMaxWidth(0.75f),
									maxLines = 1,
								)

								OutlinedTextField(
									value = password,
									onValueChange = { password = it },
									shape = RoundedCornerShape(12.dp),
									label = { Text("Password") },
									leadingIcon = { Icon(Icons.Default.Lock, null) },
									maxLines = 1,
									modifier = Modifier.fillMaxWidth(0.75f),
									visualTransformation = PasswordVisualTransformation(),
									keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
								)

							}

							AuthDestination.REGISTER -> @Composable {
								Text("Register a new Account", style = HeadLineMedium)

								OutlinedTextField(
									value = username,
									onValueChange = { username = it },
									shape = RoundedCornerShape(12.dp),
									label = { Text("Your Full Name") },
									leadingIcon = { Icon(Icons.Default.Person, null) },
									maxLines = 1,
									modifier = Modifier.fillMaxWidth(0.75f),
								)

								OutlinedTextField(
									value = email,
									onValueChange = { email = it },
									shape = RoundedCornerShape(12.dp),
									label = { Text("Your Email") },
									leadingIcon = { Icon(Icons.Default.Person, null) },
									maxLines = 1,
									modifier = Modifier.fillMaxWidth(0.75f),
								)

								OutlinedTextField(
									value = password,
									onValueChange = { password = it },
									shape = RoundedCornerShape(12.dp),
									label = { Text("Password") },
									leadingIcon = { Icon(Icons.Default.Lock, null) },
									maxLines = 1,
									modifier = Modifier.fillMaxWidth(0.75f),
								)
							}
						}

						Row(
							modifier = Modifier.fillMaxWidth(0.75f),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							OutlinedButton(
								onClick = { }, shape = RoundedCornerShape(8.dp)
							) {
								Text("Config")
							}

							ElevatedButton(
								onClick = {
									when (AuthDestination.entries[selectedDestination]) {
										AuthDestination.LOGIN -> {
											scope.launch {
												viewModel.login(email, password)
											}
										}

										AuthDestination.REGISTER -> {
											scope.launch {
												viewModel.register(username, email, password)
											}
										}
									}
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
	}
}

enum class AuthDestination {
	LOGIN, REGISTER
}