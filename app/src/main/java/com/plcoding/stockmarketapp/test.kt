package com.plcoding.stockmarketapp

import com.plcoding.stockmarketapp.data.csv.CSVParser
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.mapper.toCompanyInfo
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyInfo(
        symbol: String,
        fetchFromRemote: Boolean,

        ): Resource<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())

        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        }
    }

    override suspend fun getCompanyListings(
        //данный флаг используется при выборе
        //грузить из базы или из Api
        fetchFromRemote: Boolean,
        query: String

    ): Flow<Resource<List<CompanyListing>>> {

        return flow {

//////////////DATABASE////////////////////////
            //загружаем данные из базы по поиску
            //emit является частью flow
            //получаем списко объектов List<CompanyListingEntity>
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)

            //в переменную data сохраняем уже список
            //приведенный к классу-модели List<CompanyListing>
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }))

            // чтобы просто не делать ненужный запрос к Api
            // проверяем сам список и запрос
            val isDbEmpty = localListings.isEmpty() && query.isBlank()

            //Если база не пустая то грузим из нее
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            //а загрузку из API делаем false
            if(shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            ///////////////API////////////////////////
            //первый раз всегда грузим из API
            //здесь идет непосредственный запрос к API
            val remoteListings = try {
                val response = api.getListings()
                //bytestream получаем для чтения CSV файла
                companyListingsParser.parse(response.byteStream())


            } catch(e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            // если все ок
            remoteListings?.let { listings ->
                // удаляем все из базы
                dao.clearCompanyListings()
                // вставляем в базу полученные данные
                // преобразуя их в класс-базу
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                // делаем поиск по базе согласно запросу
                // и получаем данные в виде класса-модели
                emit(Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }


    override suspend fun getIntradayInfo(symbol: String)
            : Resource<List<IntradayInfo>> {

        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)

        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }
    }




}







































