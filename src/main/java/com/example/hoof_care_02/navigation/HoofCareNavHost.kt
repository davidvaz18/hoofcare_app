package com.example.hoof_care_02.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hoof_care_02.ui.screens.*

/**
 * NavHost único do app.
 */
@Composable
fun HoofCareNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = HoofCareDestinations.SPLASH,
        modifier = modifier
    ) {
        composable(HoofCareDestinations.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(HoofCareDestinations.LOGIN) {
                        popUpTo(HoofCareDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(HoofCareDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HoofCareDestinations.PAGINA_HOME) {
                        popUpTo(HoofCareDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(HoofCareDestinations.SIGN_IN)
                }
            )
        }

        composable(HoofCareDestinations.SIGN_IN) {
            SignInScreen(
                onSignUpSuccess = {
                    navController.navigate(HoofCareDestinations.USER_PROFILE_01)
                }
            )
        }

        composable(HoofCareDestinations.PAGINA_HOME) {
            HomeScreen(
                onNavigateToPets = {
                    navController.navigate(HoofCareDestinations.ADICAO_PET)
                },
                onNavigateToLembretes = {
                    navController.navigate(HoofCareDestinations.LEMBRETES)
                },
                onNavigateToSettings = {
                    navController.navigate(HoofCareDestinations.CONFIGURACOES)
                },
                onNavigateToProfile = {
                    navController.navigate(HoofCareDestinations.USER_PROFILE_01)
                },
                onNavigateToClinicas = {
                    navController.navigate(HoofCareDestinations.CLINICAS)
                }
            )
        }

        composable(HoofCareDestinations.ADICAO_PET) {
            DogListScreen(
                onNavigateToAddPet = {
                    navController.navigate(HoofCareDestinations.CADASTRO_PET)
                },
                onNavigateToProfile = { petId ->
                    navController.navigate(HoofCareDestinations.perfilPetRoute(petId))
                },
                onNavigateHome = {
                    navController.navigate(HoofCareDestinations.PAGINA_HOME) {
                        popUpTo(HoofCareDestinations.PAGINA_HOME) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HoofCareDestinations.CADASTRO_PET) {
            AddPetScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(HoofCareDestinations.LEMBRETES) {
            RemindersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = HoofCareDestinations.PERFIL_PET,
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetProfileScreen(
                petId = petId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(HoofCareDestinations.CONFIGURACOES) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(HoofCareDestinations.CLINICAS) {
            ClinicasScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // --- FLUXO DE PERFIL ---
        composable(HoofCareDestinations.USER_PROFILE_01) {
            UserProfileScreen01(
                onNext = { navController.navigate(HoofCareDestinations.USER_PROFILE_02) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(HoofCareDestinations.USER_PROFILE_02) {
            UserProfileScreen02(
                onNext = { navController.navigate(HoofCareDestinations.USER_PROFILE_03) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(HoofCareDestinations.USER_PROFILE_03) {
            UserProfileScreen03(
                onNext = { navController.navigate(HoofCareDestinations.USER_PROFILE_04) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(HoofCareDestinations.USER_PROFILE_04) {
            UserProfileScreen04(
                onFinish = {
                    navController.navigate(HoofCareDestinations.PAGINA_HOME) {
                        popUpTo(HoofCareDestinations.USER_PROFILE_01) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
