package com.example.pokedex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ui.state.PokemonDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val repository: PokemonRepository,
    private val pokemonId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    init {
        loadPokemonDetail()
    }

    fun onEvent(event: PokemonDetailEvent) {
        when (event) {
            is PokemonDetailEvent.Retry -> loadPokemonDetail()
            is PokemonDetailEvent.ToggleFavourite -> {
                // Просто переключаем локальное состояние для UI
                val currentState = _uiState.value
                if (currentState is PokemonDetailUiState.Success) {
                    _uiState.value = currentState.copy(isFavourite = !currentState.isFavourite)
                }
            }
            is PokemonDetailEvent.SetFavourite -> {
                // Устанавливаем состояние из shared ViewModel
                val currentState = _uiState.value
                if (currentState is PokemonDetailUiState.Success) {
                    _uiState.value = currentState.copy(isFavourite = event.isFavourite)
                }
            }
        }
    }

    private fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading

            repository.getPokemonDetail(pokemonId).fold(
                onSuccess = { pokemon ->
                    _uiState.value = PokemonDetailUiState.Success(
                        pokemon = pokemon,
                        isFavourite = false // Будет обновлено из Navigation
                    )
                },
                onFailure = { error ->
                    _uiState.value = PokemonDetailUiState.Error(
                        error.message ?: "Failed to load Pokemon details"
                    )
                }
            )
        }
    }
}

sealed interface PokemonDetailEvent {
    data object Retry : PokemonDetailEvent
    data object ToggleFavourite : PokemonDetailEvent
    data class SetFavourite(val isFavourite: Boolean) : PokemonDetailEvent
}