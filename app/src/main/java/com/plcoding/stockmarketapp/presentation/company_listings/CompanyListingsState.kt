package com.plcoding.stockmarketapp.presentation.company_listings

import com.plcoding.stockmarketapp.domain.model.CompanyListing

data class CompanyListingsState(

    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    //для swipe layout
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""
)
