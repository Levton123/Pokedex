package com.example.pokedex.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pokedex.data.model.PokemonListItem
import com.example.pokedex.ui.state.PokemonListUiState
import com.example.pokedex.ui.viewmodel.PokemonListEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    uiState: PokemonListUiState,
    onEvent: (PokemonListEvent) -> Unit,
    onPokemonClick: (Int) -> Unit,
    onFavouritesClick: () -> Unit
) {
    // Получаем текущий query из state
    val currentQuery = when (uiState) {
        is PokemonListUiState.Success -> uiState.searchQuery
        else -> ""
    }

    var searchQuery by remember { mutableStateOf(currentQuery) }

    // Синхронизируем локальный query с state при изменении
    LaunchedEffect(currentQuery) {
        searchQuery = currentQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PokeDex") },
                actions = {
                    IconButton(onClick = onFavouritesClick) {
                        Icon(Icons.Default.Favorite, "Favourites")
                    }
                    IconButton(onClick = {
                        searchQuery = ""
                        onEvent(PokemonListEvent.Refresh)
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onEvent(PokemonListEvent.Search(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search Pokemon...") },
                singleLine = true
            )

            when (uiState) {
                is PokemonListUiState.Loading -> {
                    LoadingContent()
                }
                is PokemonListUiState.Success -> {
                    PokemonList(
                        pokemonList = uiState.pokemonList,
                        favourites = uiState.favourites,
                        onPokemonClick = onPokemonClick,
                        onFavouriteClick = { onEvent(PokemonListEvent.ToggleFavourite(it)) }
                    )
                }
                is PokemonListUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        onRetry = {
                            searchQuery = ""
                            onEvent(PokemonListEvent.Retry)
                        }
                    )
                }
                is PokemonListUiState.Empty -> {
                    EmptyContent()
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading Pokemon...")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error: $message",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No Pokemon found",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PokemonList(
    pokemonList: List<PokemonListItem>,
    favourites: Set<Int>,
    onPokemonClick: (Int) -> Unit,
    onFavouriteClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pokemonList) { pokemon ->
            PokemonListItem(
                pokemon = pokemon,
                isFavourite = favourites.contains(pokemon.id),
                onClick = { onPokemonClick(pokemon.id) },
                onFavouriteClick = { onFavouriteClick(pokemon.id) }
            )
        }
    }
}

@Composable
private fun PokemonListItem(
    pokemon: PokemonListItem,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onFavouriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.id}.png",
                contentDescription = pokemon.name,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onFavouriteClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favourite",
                    tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}