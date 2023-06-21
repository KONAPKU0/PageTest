package com.deluxe1.turnthepage.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.deluxe1.turnthepage.data.model.Book
import com.deluxe1.turnthepage.nextKey
import com.deluxe1.turnthepage.prevKey

class BookPagingSource(
    private val query: String,
    private val repo: BookRepository
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        return try {
            if (query.isEmpty()) return LoadResult.Page(data = emptyList(), null, null)
            val nextPageNumber = params.key ?: 1
            val response = repo.getBooks(query, nextPageNumber, params.loadSize)
            val newCount = response.items.size
            val total = response.totalItems
            val itemsBefore = nextPageNumber * params.loadSize
            val itemsAfter = total - (itemsBefore + newCount)
            Log.e("BookPagingSource","load  " +itemsAfter  + "  itemsBefore " + itemsBefore + "  total " + total + "  newCount " + newCount + "  nextPageNumber " + nextPageNumber + "  params.loadSize " + params.loadSize)
            if (itemsAfter>0){
                LoadResult.Page(
                    data = response.items,
                    prevKey = params.prevKey(),
                    nextKey = params.nextKey(response.totalItems),
                    itemsBefore = itemsBefore,
                    itemsAfter = itemsAfter,
                )
            }else{
                LoadResult.Page(
                    data = response.items,
                    prevKey = params.prevKey(),
                    nextKey = params.nextKey(response.totalItems),
                )
            }

        } catch (e: Exception) {
            Log.e("eee",e.stackTraceToString())
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int =
        ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2)
            .coerceAtLeast(0)

}
