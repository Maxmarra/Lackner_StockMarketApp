package com.plcoding.stockmarketapp.domain.repository

import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    //методы выглядят также как и в StockApi
    //только без api_key, так как он уже стоит по умолчанию
    //плюс мы возвращаем здесь не Response
    //а уже результат ОБРАБОТКИ (ПАРСИНГА) этого Response
    // т.е. непостредственный List<IntradayInfo>
        suspend fun getIntradayInfo(
            symbol: String
        ): Resource<List<IntradayInfo>>

        suspend fun getCompanyInfo(
            symbol: String
        ): Resource<CompanyInfo>
}
