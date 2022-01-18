package com.ssafy.groute.src.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssafy.groute.R
import com.ssafy.groute.databinding.FragmentSignBinding
import com.ssafy.groute.src.service.UserService
import com.ssafy.groute.util.RetrofitCallback
import java.util.regex.Pattern

private const val TAG = "SignFragment_싸피"
class SignFragment : Fragment() {
    private lateinit var binding: FragmentSignBinding
    private var isAvailableId = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }


    /**
     * TextInputEditText listener 등록
     */
    private fun initListeners() {

        binding.joinEditId.addTextChangedListener(TextFieldValidation(binding.joinEditId))
        binding.joinEditPw.addTextChangedListener(TextFieldValidation(binding.joinEditPw))
        binding.joinEditNick.addTextChangedListener(TextFieldValidation(binding.joinEditNick))
        binding.joinEditPhone.addTextChangedListener(TextFieldValidation(binding.joinEditPhone))
//        binding.joinEditEmail.addTextChangedListener(TextFieldValidation(binding.joinEditEmail))
    }

    /**
     * 입력된 id 유효성 검사 및 id 중복 체크
     * 유효성 검사 통과하면 id 중복 확인
     * @return id 중복 통과 시 true 반환
     */
    private fun validatedId() : Boolean {
        val inputId = binding.joinEditId.text.toString()

        if (inputId.trim().isEmpty()) {   // 값이 비어있으면
            binding.joinTilId.error = "Required Field"
            binding.joinEditId.requestFocus()
            return false
        } else if(!Pattern.matches("^[가-힣ㄱ-ㅎa-zA-Z0-9._-]{4,20}\$", inputId)) {
            binding.joinTilId.error = "아이디 형식을 확인해주세요.(특수 문자 제외)"
            binding.joinEditId.requestFocus()
            return false
        } else {
            binding.joinTilId.isErrorEnabled = false
            Log.d(TAG, "validatedId: id = $inputId")
            UserService().isUsedId(inputId, isUsedIdCallBack())
            return isAvailableId
        }
    }

    /**
     * 입력된 password 유효성 검사
     * @return 유효성 검사 통과 시 true 반환
     */
    private fun validatedPw() : Boolean {
        val inputPw = binding.joinEditPw.text.toString()

        if(inputPw.trim().isEmpty()){   // 값이 비어있으면
            binding.joinTilPw.error = "Required Field"
            binding.joinEditPw.requestFocus()
            return false
        } else if(!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,50}.\$", inputPw)) {
            binding.joinTilPw.error = "비밀번호 형식을 확인해주세요."
            binding.joinEditPw.requestFocus()
            return false
        }
        else {
            binding.joinTilPw.isErrorEnabled = false
            return true
        }
    }

    /**
     * 입력된 phone number 유효성 검사
     * @return 유효성 검사 통과 시 true 반환
     */
    private fun validatedPhone() : Boolean{
        val inputPhone = binding.joinEditPhone.text.toString()

        if(inputPhone.trim().isEmpty()){
            binding.joinTilPhone.error = "Required Field"
            binding.joinEditPhone.requestFocus()
            return false
        } else if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", inputPhone)) {
            binding.joinTilPhone.error = "휴대폰 번호 형식을 확인해주세요."
            binding.joinEditPhone.requestFocus()
            return false
        }
        else {
            binding.joinTilPhone.error = null
            return true
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignFragment().apply {

            }
    }


    inner class TextFieldValidation(private val view: View): TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            when(view.id){
                R.id.join_edit_id -> {
                    validatedId()
                }
                R.id.join_edit_pw -> {
                    validatedPw()
                }
                R.id.join_edit_phone -> {
                    validatedPhone()
                }
//                R.id.join_edit_nickname -> {
//                    validatedNickname()
//                }
            }
        }
    }

    /**
     * id 중복 확인 callback
     */
    inner class isUsedIdCallBack : RetrofitCallback<Boolean> {
        override fun onError(t: Throwable) {
            Log.d(TAG, "onError: ")
        }

        override fun onSuccess(code: Int, responseData: Boolean) {
            Log.d(TAG, "onSuccess: $responseData")  // true : 중복 O <-> false : 중복 X, 사용 가능
            if(responseData) {   // true - DB 내에 중복되는 ID가 있으면
                binding.joinTilId.error = "이미 존재하는 아이디입니다."
                binding.joinEditId.requestFocus()
                isAvailableId = false
            } else {// DB 내에 중복되는 ID가 없으면
                binding.joinTilId.error = null
                isAvailableId = true
            }
        }

        override fun onFailure(code: Int) {
            Log.d(TAG, "onFailure: ")
        }
    }
}