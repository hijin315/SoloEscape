package com.jinny.soloescape

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jinny.adapter.CardItemAdapter
import com.jinny.model.CardItem
import com.jinny.soloescape.databinding.ActivityLikeBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity(), CardStackListener {
    private var binding: ActivityLikeBinding? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    private val adapter = CardItemAdapter()
    private val cardItems = mutableListOf<CardItem>()
    private val manager by lazy {
        CardStackLayoutManager(this, this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userDB = Firebase.database.reference.child("Users")
        val currentUserDB = userDB.child(getCurrentUserID())
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("name").value == null) {
                    showNameInputPopUp()
                    return
                }
                // 유저정보 갱신하기!
                getUnSelectedUsers()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        initCardStackView()
        initSignOutBtn()
        initMatchListBtn()
    }

    private fun initSignOutBtn() {
        binding!!.signOutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initMatchListBtn() {
        binding!!.matchListBtn.setOnClickListener {
            startActivity(Intent(this, MatchListActivity::class.java))
        }
    }

    private fun initCardStackView() {
        binding!!.cardStackView.layoutManager = manager
        binding!!.cardStackView.adapter = adapter
    }

    private fun showNameInputPopUp() {
        // 동적으로 editText를 생성
        val editText = EditText(this)
        AlertDialog.Builder(this).setTitle("이름을 입력해주세요")
            .setView(editText).setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopUp()
                } else {
                    saveUserName(editText.text.toString())
                }


            }.setCancelable(false).show()
    }

    private fun saveUserName(name: String) {
        val userID = getCurrentUserID()
        val currentUserDB = userDB.child(userID)
        val user = mutableMapOf<String, Any>()
        user["userID"] = userID
        user["name"] = name
        currentUserDB.updateChildren(user)

        // 유저 정보 가져오기
        getUnSelectedUsers()

    }

    private fun getUnSelectedUsers() {
        userDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("userID").value != getCurrentUserID()
                    && snapshot.child("likeBy").child("like").hasChild(getCurrentUserID()).not()
                    && snapshot.child("likeBy").child("dislike")
                        .hasChild(getCurrentUserID()).not()
                ) {
                    val userID = snapshot.child("userID").value.toString()
                    var name = "이름없음"
                    if (snapshot.child("name").value != null) {
                        name = snapshot.child("name").value.toString()
                    }
                    cardItems.add(CardItem(userID, name))
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItems.find { it.userID == snapshot.key }?.let {
                    it.name = snapshot.child("name").value.toString()
                }
                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // 지워졌을때
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // 순서가 바꼈을때
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
    }

    private fun like() {
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst() // 옆으로 보내면 카드 없애기 ! 안하면 아래로 쌓임
        userDB.child(card.userID).child("likeBy").child("like").child(getCurrentUserID())
            .setValue(true)

        saveMatchIfOtherUserLikeME(card.userID)
        Toast.makeText(this, "${card.name}님을 Like 합니다", Toast.LENGTH_SHORT).show()
    }

    private fun dislike() {
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst() // 옆으로 보내면 카드 없애기 ! 안하면 아래로 쌓임
        userDB.child(card.userID).child("likeBy").child("dislike").child(getCurrentUserID())
            .setValue(true)
        Toast.makeText(this, "${card.name}님을 disLike 합니다", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchIfOtherUserLikeME(otherUserID: String) {
        val otherUserDB =
            userDB.child(getCurrentUserID()).child("likeBy").child("like").child(otherUserID)
        // true라면 상대방도 나를 좋아요 한 것!
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    userDB.child(getCurrentUserID())
                        .child("likeBy")
                        .child("match")
                        .child(otherUserID)
                        .setValue(true)

                    userDB.child(otherUserID).child("likeBy").child("match")
                        .child(getCurrentUserID()).setValue(true)

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> {
                like()
            }
            Direction.Left -> {
                dislike()
            }
            else -> {
            }
        }
    }

    override fun onCardRewound() {
    }

    override fun onCardCanceled() {
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardDisappeared(view: View?, position: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}