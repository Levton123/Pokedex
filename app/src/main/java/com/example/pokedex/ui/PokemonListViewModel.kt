package com.example.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ui.state.PokemonListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    private val _favourites = MutableStateFlow<Set<Int>>(emptySet())
    val favourites: StateFlow<Set<Int>> = _favourites.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPokemonList()
    }

    fun onEvent(event: PokemonListEvent) {
        when (event) {
            is PokemonListEvent.Search -> performSearch(event.query)
            is PokemonListEvent.Retry -> loadPokemonList()
            is PokemonListEvent.Refresh -> loadPokemonList()
            is PokemonListEvent.ToggleFavourite -> toggleFavourite(event.pokemonId)
        }
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading

            repository.getPokemonList().fold(
                onSuccess = { pokemonList ->
                    if (pokemonList.isEmpty()) {
                        _uiState.value = PokemonListUiState.Empty
                    } else {
                        _uiState.value = PokemonListUiState.Success(
                            pokemonList = pokemonList,
                            favourites = _favourites.value
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = PokemonListUiState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(300) // Debounce

            // Если query пустой, загружаем полный список
            if (query.isBlank()) {
                repository.getPokemonList().fold(
                    onSuccess = { pokemonList ->
                        if (pokemonList.isEmpty()) {
                            _uiState.value = PokemonListUiState.Empty
                        } else {
                            _uiState.value = PokemonListUiState.Success(
                                pokemonList = pokemonList,
                                searchQuery = "",
                                favourites = _favourites.value
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = PokemonListUiState.Error(
                            error.message ?: "Failed to load Pokemon"
                        )
                    }
                )
            } else {
                // Иначе выполняем поиск
                repository.searchPokemon(query).fold(
                    onSuccess = { results ->
                        if (results.isEmpty()) {
                            _uiState.value = PokemonListUiState.Empty
                        } else {
                            _uiState.value = PokemonListUiState.Success(
                                pokemonList = results,
                                searchQuery = query,
                                favourites = _favourites.value
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = PokemonListUiState.Error(
                            error.message ?: "Search failed"
                        )
                    }
                )
            }
        }
    }

    private fun toggleFavourite(pokemonId: Int) {
        val currentFavourites = _favourites.value.toMutableSet()
        if (currentFavourites.contains(pokemonId)) {
            currentFavourites.remove(pokemonId)
        } else {
            currentFavourites.add(pokemonId)
        }
        _favourites.value = currentFavourites

        // Обновляем UI state с новыми favourites
        val currentState = _uiState.value
        if (currentState is PokemonListUiState.Success) {
            _uiState.value = currentState.copy(favourites = currentFavourites)
        }
    }

    fun isFavourite(pokemonId: Int): Boolean {
        return _favourites.value.contains(pokemonId)
    }
}

sealed interface PokemonListEvent {
    data class Search(val query: String) : PokemonListEvent
    data object Retry : PokemonListEvent
    data object Refresh : PokemonListEvent
    data class ToggleFavourite(val pokemonId: Int) : PokemonListEvent
}