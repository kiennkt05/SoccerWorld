package com.example.soccerworld.ui.home.topscorer

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.soccerworld.base.BaseViewModel
import com.example.soccerworld.data.local.FootballDatabase
import com.example.soccerworld.data.remote.ApiClient
import com.example.soccerworld.model.topscorer.TopScorerResponse
import com.example.soccerworld.model.topscorer.Topscorer
import com.example.soccerworld.util.CustomSharedPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class TopScorerViewModel(application: Application) : BaseViewModel(application) {

    private val apiClient =  ApiClient()
    private val disposable = CompositeDisposable()

    private var customPreferences = CustomSharedPreferences(getApplication())
    private var refreshTime = 24 * 60 * 60 * 1000 * 1000 * 1000L

    val topScorerList = MutableLiveData<List<Topscorer>>()
    val loadingTopScorer = MutableLiveData<Boolean>()

    fun getTopScorers(leagueId: Int){

        val updateTime = customPreferences.getTime()
        if (updateTime != null && updateTime != 0L && (System.nanoTime() - updateTime) < refreshTime) {
            val temp = arrayListOf(0)
            temp.add(542)
            temp.add(leagueId)
            if (temp[(temp.lastIndex)-1] == leagueId){
                getDataFromSQLite()
            }else{
                getTopScorersFromApi(leagueId)
            }
        } else {
            getTopScorersFromApi(leagueId)
        }
    }

    private fun getDataFromSQLite() {
        loadingTopScorer.value = true
        launch {
            val countries = FootballDatabase(getApplication()).footballDao().getTopscorer()
            showLayout(countries)
        }
    }

    fun getTopScorersFromApi(leagueId: Int){
        loadingTopScorer.value = true
        disposable.add(
            apiClient.getTopScorers(leagueId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TopScorerResponse>(){
                    override fun onSuccess(t: TopScorerResponse) {
                        saveDataInSQLite(t.api?.topscorers)
                    }
                    override fun onError(e: Throwable) {
                        loadingTopScorer.value = false
                    }
                })
        )
    }

    private fun showLayout(topscorerList: List<Topscorer>?) {
        topScorerList.value = topscorerList ?: emptyList()
        loadingTopScorer.value = false
    }

    private fun saveDataInSQLite(list: List<Topscorer>?) {
        launch {
            val dao = FootballDatabase(getApplication()).footballDao()
            dao.deleteTopscorer()
            list?.let {
                val listLong = dao.insertAllTopscorer(*it.toTypedArray())
                var i = 0
                while (i < it.size) {
                    it[i].playerId = listLong[i].toInt()
                    i = i + 1
                }
            }
            showLayout(list)
        }
        customPreferences.saveTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}