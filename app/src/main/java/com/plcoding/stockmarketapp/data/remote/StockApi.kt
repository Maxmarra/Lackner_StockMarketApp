package com.plcoding.stockmarketapp.data.remote

import com.plcoding.stockmarketapp.data.remote.dto.CompanyInfoDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query


    interface StockApi {

        //берем данные из CSV файла
        //возвращаем именно ResponseBody от Retrofit, так как
        //нам будет нужен bytestream
        @GET("query?function=LISTING_STATUS")
        suspend fun getListings(
            @Query("apikey") apiKey: String = API_KEY
        ): ResponseBody

        //берем данные из CSV файла
        @GET("query?function=TIME_SERIES_INTRADAY&interval=60min&datatype=csv")
        suspend fun getIntradayInfo(
            @Query("symbol") symbol: String,
            @Query("apikey") apiKey: String = API_KEY
        ): ResponseBody

        //берем данные из JSON
        @GET("query?function=OVERVIEW")
        suspend fun getCompanyInfo(
            @Query("symbol") symbol: String,
            @Query("apikey") apiKey: String = API_KEY
        ): CompanyInfoDto

        companion object {
            const val API_KEY = "S5CZRECXCTSXZP2M"
            const val BASE_URL = "https://alphavantage.co"
        }
    }
