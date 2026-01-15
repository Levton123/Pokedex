package com.example.pokedex.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ui.viewmodel.PokemonDetailViewModel
import com.example.pokedex.ui.viewmodel.PokemonListViewModel

class PokemonListViewModelFactory(
    private val repository: PokemonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonListViewModel::class.java)) {
            return PokemonListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PokemonDetailViewModelFactory(
    private val repository: PokemonRepository,
    private val pokemonId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonDetailViewModel::class.java)) {
            return PokemonDetailViewModel(repository, pokemonId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}