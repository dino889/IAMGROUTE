package com.ssafy.groute.src.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssafy.groute.src.dto.User
import com.ssafy.groute.src.response.UserInfoResponse
import com.ssafy.groute.util.RetrofitCallback
import com.ssafy.groute.util.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserService_groute"
class UserService {
    fun login(user: User, callback: RetrofitCallback<User>) {
        RetrofitUtil.userService.login(user.id, user.password).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val res = response.body()
                if(response.code() == 200){
                    if (res != null) {
                        callback.onSuccess(response.code(), res)
                    }
                } else {
                    callback.onFailure(response.code())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                callback.onError(t)
            }
        })
    }


    /**
     * 서버로부터 입력된 id가 사용 중인지 확인한다.
     * @param inputId
     * @param callback
     */
    fun isUsedId(inputId: String, callback: RetrofitCallback<Boolean>) {
        RetrofitUtil.userService.isUsedId(inputId).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                val res = response.body()
                if(response.code() == 200) {
                    if (res != null) {
                        callback.onSuccess(response.code(), res)
                    }
                } else {
                    callback.onFailure(response.code())
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback.onError(t)
            }
        })
    }

    /**
     * 사용자 아이디에 해당하는 사용자 정보를 불러온다.
     * @param userId
     */
    fun getUserInfo(userId: String): LiveData<UserInfoResponse>{
        val responseLiveData : MutableLiveData<UserInfoResponse> = MutableLiveData()
        val userInfoRequest: Call<UserInfoResponse> = RetrofitUtil.userService.getUserInfo(userId)
        Log.d(TAG, "getUserInfo: $userId")
        userInfoRequest.enqueue(object : Callback<UserInfoResponse> {
            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                val res = response.body()
                if(response.code() == 200){
                    if (res != null) {
                        Log.d(TAG, "onResponse: $res")
                        responseLiveData.value = res
                    }
                } else {
                    Log.d(TAG, "onResponse: Error Code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                Log.d(TAG, t.message ?: "사용자 정보 받아오는 중 통신오류")
            }
        })
        return responseLiveData
    }
}