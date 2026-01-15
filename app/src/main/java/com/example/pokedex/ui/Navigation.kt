package com.example.pokedex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.data.api.RetrofitClient
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ui.screen.FavouritesScreen
import com.example.pokedex.ui.screen.PokemonDetailScreen
import com.example.pokedex.ui.screen.PokemonListScreen
import com.example.pokedex.ui.state.PokemonListUiState
import com.example.pokedex.ui.viewmodel.PokemonDetailEvent
import com.example.pokedex.ui.viewmodel.PokemonDetailViewModel
import com.example.pokedex.ui.viewmodel.PokemonListEvent
import com.example.pokedex.ui.viewmodel.PokemonListViewModel

sealed class Screen(val route: String) {
    data object PokemonList : Screen("pokemon_list")
    data object PokemonDetail : Screen("pokemon_detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "pokemon_detail/$pokemonId"
    }
    data object Favourites : Screen("favourites")
}

@Composable
fun PokemonNavigation(
    navController: NavHostController = rememberNavController()
) {
    val repository = PokemonRepository(RetrofitClient.pokemonApi)

    // Shared ViewModel для всех экранов - создаём на уровне NavHost
    val sharedListViewModel: PokemonListViewModel = viewModel(
        factory = PokemonListViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.PokemonList.route
    ) {
        composable(Screen.PokemonList.route) {
            val uiState by sharedListViewModel.uiState.collectAsState()

            // Проверяем, есть ли активный поиск, и сбрасываем его при возвращении
            LaunchedEffect(Unit) {
                val currentState = uiState
                if (currentState is PokemonListUiState.Success && currentState.searchQuery.isNotEmpty()) {
                    // Если был активен поиск, загружаем полный список
                    sharedListViewModel.onEvent(PokemonListEvent.Search(""))
                }
            }

            PokemonListScreen(
                uiState = uiState,
                onEvent = sharedListViewModel::onEvent,
                onPokemonClick = { pokemonId ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                },
                onFavouritesClick = {
                    navController.navigate(Screen.Favourites.route)
                }
            )
        }

        composable(
            route = Screen.PokemonDetail.route,
            arguments = listOf(
                navArgument("pokemonId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: return@composable

            val viewModel: PokemonDetailViewModel = viewModel(
                key = "detail_$pokemonId", // Уникальный ключ для каждого покемона
                factory = PokemonDetailViewModelFactory(repository, pokemonId)
            )

            val detailUiState by viewModel.uiState.collectAsState()
            val favourites by sharedListViewModel.favourites.collectAsState()

            // Синхронизируем состояние избранного при изменении
            LaunchedEffect(favourites) {
                viewModel.onEvent(
                    PokemonDetailEvent.SetFavourite(favourites.contains(pokemonId))
                )
            }

            PokemonDetailScreen(
                uiState = detailUiState,
                onEvent = { event ->
                    when (event) {
                        is PokemonDetailEvent.ToggleFavourite -> {
                            // Переключаем в shared ViewModel
                            sharedListViewModel.onEvent(
                                PokemonListEvent.ToggleFavourite(pokemonId)
                            )
                            // И в локальном для немедленного UI обновления
                            viewModel.onEvent(event)
                        }
                        else -> viewModel.onEvent(event)
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Favourites.route) {
            val favourites by sharedListViewModel.favourites.collectAsState()

            FavouritesScreen(
                favouriteIds = favourites,
                onPokemonClick = { pokemonId ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                },
                onRemoveFavourite = { pokemonId ->
                    sharedListViewModel.onEvent(
                        PokemonListEvent.ToggleFavourite(pokemonId)
                    )
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}