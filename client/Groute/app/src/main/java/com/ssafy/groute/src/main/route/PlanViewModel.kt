package com.ssafy.groute.src.main.route

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.groute.src.dto.UserPlan
import com.ssafy.groute.src.service.UserPlanService
import kotlinx.coroutines.launch

private const val TAG = "PlanViewModel_Groute"
class PlanViewModel : ViewModel(){
    private val _planBestResponse = MutableLiveData<MutableList<UserPlan>>()

    val planBestList : LiveData<MutableList<UserPlan>>
        get() = _planBestResponse

    fun setPlanBestList(plan: MutableList<UserPlan>) = viewModelScope.launch {
        _planBestResponse.value = plan
    }
    suspend fun getPlanBestList(){
        val response = UserPlanService().getBestUserPlan()
        viewModelScope.launch { 
            var res = response.body()
            if(response.code() == 200){
                if(res!=null){
                    setPlanBestList(res)
                    Log.d(TAG, "getPlanBestList: ")
                }
            }else{
                Log.d(TAG, "getPlanBestList: ")
            }
        }
    }
}