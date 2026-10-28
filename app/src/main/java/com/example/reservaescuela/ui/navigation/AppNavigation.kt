package com.example.reservaescuela.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.screens.DocumentosScreen
import com.example.reservaescuela.ui.screens.HomeScreen
import com.example.reservaescuela.ui.screens.LoginScreen
import com.example.reservaescuela.ui.screens.LogoutScreen
import com.example.reservaescuela.ui.screens.MisReservasScreen
import com.example.reservaescuela.ui.screens.PerfilScreen
import com.example.reservaescuela.ui.screens.RegisterScreen
import com.example.reservaescuela.ui.screens.ReservarScreen
import com.example.reservaescuela.ui.screens.WelcomeScreen
import kotlinx.coroutines.launch

object Routes {
    const val WELCOME = "bienvenida"
    const val LOGIN = "login"
    const val REGISTER = "registro"

    const val HOME = "inicio"
    const val RESERVAR = "reservar"
    const val MIS_RESERVAS = "mis_reservas"
    const val DOCUMENTOS = "documentos"
    const val PERFIL = "perfil"
    const val LOGOUT = "cerrar_sesion"
}

private class MenuItem(val route: String, val label: String, val icon: ImageVector)

private val inicio = MenuItem(Routes.HOME, "Inicio", Icons.Default.Home)
private val reservar = MenuItem(Routes.RESERVAR, "Reservar", Icons.Default.Add)
private val misReservas = MenuItem(Routes.MIS_RESERVAS, "Mis reservas", Icons.Default.DateRange)
private val documentos = MenuItem(Routes.DOCUMENTOS, "Documentos", Icons.AutoMirrored.Filled.List)
private val perfil = MenuItem(Routes.PERFIL, "Editar mis datos", Icons.Default.Edit)
private val cerrarSesion = MenuItem(Routes.LOGOUT, "Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp)

private val bottomItems = listOf(inicio, reservar, misReservas, documentos)

private val drawerItems = listOf(inicio, reservar, misReservas, documentos, perfil, cerrarSesion)

private fun titleFor(route: String): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.RESERVAR -> "Nueva reserva"
    Routes.MIS_RESERVAS -> "Mis reservas"
    Routes.DOCUMENTOS -> "Documentos"
    Routes.PERFIL -> "Editar mis datos"
    Routes.LOGOUT -> "Cerrar sesión"
    else -> "Reservas Marcela Paz"
}

// decide si mostrar las pantallas de acceso o la app con sesion iniciada
@Composable
fun AppNavigation(vm: AppViewModel = viewModel()) {
    Crossfade(targetState = vm.currentUser != null, label = "sesion") { logged ->
        if (logged) MainNav(vm) else AuthNav(vm)
    }
}

// ---------- antes de iniciar sesion ----------

@Composable
fun AuthNav(vm: AppViewModel) {
    val nav = rememberNavController()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = nav,
            startDestination = Routes.WELCOME,
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 } }
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin = { nav.navigate(Routes.LOGIN) },
                    onRegister = { nav.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onGoRegister = { nav.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onGoLogin = { nav.navigate(Routes.LOGIN) }
                )
            }
        }
    }
}

// ---------- con sesion iniciada: menu, barra superior e inferior ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route ?: Routes.HOME

    fun go(target: String) {
        nav.navigate(target) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
        }
    }

    // mensaje corto abajo de la pantalla
    val show: (String) -> Unit = { message ->
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    vm.currentUser?.name ?: "",
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    vm.currentUser?.email ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = route == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            go(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Text(titleFor(route)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { go(Routes.LOGOUT) }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = { go(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(tween(250)) },
                exitTransition = { fadeOut(tween(150)) }
            ) {
                composable(Routes.HOME) { HomeScreen(vm, onNavigate = { go(it) }) }
                composable(Routes.RESERVAR) {
                    ReservarScreen(vm, show, onSent = { go(Routes.MIS_RESERVAS) })
                }
                composable(Routes.MIS_RESERVAS) { MisReservasScreen(vm, show) }
                composable(Routes.DOCUMENTOS) { DocumentosScreen(vm, show) }
                composable(Routes.PERFIL) { PerfilScreen(vm, show) }
                composable(Routes.LOGOUT) {
                    LogoutScreen(onCancel = { go(Routes.HOME) }, onConfirm = { vm.logout() })
                }
            }
        }
    }
}
