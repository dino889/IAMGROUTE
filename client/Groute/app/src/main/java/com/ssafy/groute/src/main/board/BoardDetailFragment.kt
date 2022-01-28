package com.ssafy.groute.src.main.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.groute.R
import com.ssafy.groute.config.BaseFragment
import com.ssafy.groute.databinding.FragmentBoardDetailBinding
import com.ssafy.groute.src.dto.BoardDetail
import com.ssafy.groute.src.main.MainActivity
import com.ssafy.groute.src.main.MainViewModel
import com.ssafy.groute.src.main.board.BoardFragment.Companion.BOARD_FREE_TYPE
import com.ssafy.groute.src.main.board.BoardFragment.Companion.BOARD_QUESTION_TYPE
import com.ssafy.groute.util.BoardViewModel

private const val TAG = "BoardDetailF_Groute"
class BoardDetailFragment : BaseFragment<FragmentBoardDetailBinding>(FragmentBoardDetailBinding::bind, R.layout.fragment_board_detail) {
//    private lateinit var binding: FragmentBoardDetailBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var boardRecyclerAdapter:BoardRecyclerviewAdapter
    private var boardDetailList = mutableListOf<BoardDetail>()

    private var boardId = -1
    private var boardDetailId = -1
    private var viewModel: MainViewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.hideMainProfileBar(true)
        arguments?.let {
            boardId = it.getInt("boardId", -1)
            Log.d(TAG, "onCreate: $boardId")
        }
        mainActivity.hideBottomNav(true)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding= FragmentBoardDetailBinding.inflate(layoutInflater,container,false)
//        return binding.root
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        if(boardId == BOARD_FREE_TYPE) {
            binding.boardDetailBoardNameTv.text = "자유게시판"
        } else if(boardId == BOARD_QUESTION_TYPE) {
            binding.boardDetailBoardNameTv.text = "질문게시판"
        }
        initAdapter()
        //그냥 글쓰기
        binding.boardDetailBtnWrite.setOnClickListener {
            mainActivity.moveFragment(8,"boardId",boardId)
        }

        binding.backBtn.setOnClickListener {
            Log.d(TAG, "onViewCreated: CLICK")
            mainActivity.supportFragmentManager.beginTransaction().remove(this).commit()
            mainActivity.supportFragmentManager.popBackStack()
        }

    }
    fun initViewModel(id : Int){
        val boardViewModel = ViewModelProvider(this).get(BoardViewModel::class.java)

        if(id == 1){
            boardViewModel.boardFreeList.observe(viewLifecycleOwner, Observer {
                if(it != null){
                    boardRecyclerAdapter.setBoardList(it)
                    boardRecyclerAdapter.notifyDataSetChanged()
                }
            })
            boardRecyclerAdapter.setItemClickListener(object:BoardRecyclerviewAdapter.ItemClickListener{
                override fun onClick(view: View, position: Int, name: String) {
                    mainActivity.moveFragment(6,"boardDetailId", boardViewModel.boardFreeList.value!!.get(position).id)
                }
            })
            boardRecyclerAdapter.setModifyClickListener(object : BoardRecyclerviewAdapter.ItemModifyListener{
                override fun onClick(position: Int) {
                    mainActivity.moveFragment(8,"boardDetailId",boardViewModel.boardFreeList.value!!.get(position).id)
                }

            })

        }else if(id == 2){
            boardViewModel.boardQuestionList.observe(viewLifecycleOwner, Observer {
                if(it != null){
                    boardRecyclerAdapter.setBoardList(it)
                    boardRecyclerAdapter.notifyDataSetChanged()
                }
            })
            boardRecyclerAdapter.setItemClickListener(object:BoardRecyclerviewAdapter.ItemClickListener{
                override fun onClick(view: View, position: Int, name: String) {
                    mainActivity.moveFragment(6,"boardDetailId", boardViewModel.boardQuestionList.value!!.get(position).id)
                }
            })
            boardRecyclerAdapter.setModifyClickListener(object : BoardRecyclerviewAdapter.ItemModifyListener{
                override fun onClick(position: Int) {
                    mainActivity.moveFragment(8,"boardDetailId",boardViewModel.boardQuestionList.value!!.get(position).id)
                }

            })
        }

    }
    fun initAdapter(){

        binding.boardDetailRvListitem.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        boardRecyclerAdapter = BoardRecyclerviewAdapter(viewLifecycleOwner, boardDetailList, boardId, requireContext())
        binding.boardDetailRvListitem.adapter = boardRecyclerAdapter

        initViewModel(boardId)

    }
    fun refreshFragment(){
        val ft:FragmentTransaction = requireFragmentManager().beginTransaction()
        ft.detach(this).attach(this).commit()
    }
    companion object {
        @JvmStatic
        fun newInstance(key: String, value: Int) =
            BoardDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(key, value)
                }
            }
    }
    override fun onResume() {
        super.onResume()
        initAdapter()
        refreshFragment()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }
}