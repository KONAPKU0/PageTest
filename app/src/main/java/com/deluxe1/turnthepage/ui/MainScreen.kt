package com.deluxe1.turnthepage.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.deluxe1.turnthepage.R
import com.deluxe1.turnthepage.data.model.Book
import com.deluxe1.turnthepage.ui.composable.BookItem
import com.deluxe1.turnthepage.ui.theme.LightGray
import com.deluxe1.turnthepage.ui.theme.TurnThePageTheme
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun MainScreen(books: LazyPagingItems<Book>, modifier: Modifier = Modifier) {
    Log.e("aaaaa","MainScreen"+books.loadState.refresh)
    when (books.loadState.refresh) {
        LoadState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.something_went_wrong))
            }
        }
        else -> {
            Log.e("aaaaa","books.itemCount  "+books.itemCount)
            LazyColumn(modifier = modifier) {

                itemsIndexed(books) { index, item ->
                    Log.e("aaaaa","itemsIndexed  "+index)
                    BookItem(
                        book = item?:Book(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(getBackgroundForIndex(index))
                            .padding(vertical = 15.dp)
                            .placeholder(item == null)
                    )

                }
                pagingLoadStateItem(
                    loadState = books.loadState.append,
                    keySuffix = "append",
                    loading = {  },
                    error = {
                        BookItem(
                            book = Book(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(getBackgroundForIndex(0))
                                .padding(vertical = 15.dp)
                                .clickable {  }
                        )
                    },
                )
            }
        }
    }
}



fun getBackgroundForIndex(index: Int) =
    if (index % 2 == 0) LightGray
    else Color.White

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TurnThePageTheme {
        MainScreen(
            flowOf(
                PagingData.from(
                    listOf(
                        Book()
                    )
                )
            ).collectAsLazyPagingItems()
        )
    }
}

fun LazyListScope.pagingLoadStateItem(
    loadState: LoadState,
    keySuffix: String? = null,
    loading: (@Composable LazyItemScope.() -> Unit)? = null,
    error: (@Composable LazyItemScope.(LoadState.Error) -> Unit)? = null,
) {
    if (loading != null && loadState == LoadState.Loading) {
        item(
            key = keySuffix?.let { "loadingItem_$it" },
            content = loading,
        )
    }
    Log.e("aaaaa","pagingLoadStateItempagingLoadStateItem")
    if (error != null && loadState is LoadState.Error) {
        Log.e("aaaaa","LoadState.Error")
        item(
            key = keySuffix?.let { "errorItem_$it" },
            content = { error(loadState)},
        )
    }
}



fun <K : Any, V : Any> ViewModel.pager(
    config: PagingConfig = PagingConfig(pageSize = 10),
    initialKey: K? = null,
    loadData: suspend (PagingSource.LoadParams<K>) -> PagingSource.LoadResult<K, V>
): Flow<PagingData<V>> {
    val baseConfig = PagingConfig(
        config.pageSize,
        initialLoadSize = config.initialLoadSize,
        prefetchDistance = config.prefetchDistance,
        maxSize = config.maxSize,
        enablePlaceholders = config.enablePlaceholders
    )
    return Pager(
        config = baseConfig,
        initialKey = initialKey
    ) {
        object : PagingSource<K, V>() {
            override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
                return loadData.invoke(params)
            }

            override fun getRefreshKey(state: PagingState<K, V>): K? {
                return initialKey
            }

        }
    }.flow.cachedIn(viewModelScope)
}




