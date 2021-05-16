package com.jinny.soloescape

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jinny.adapter.MatchListAdapter
import com.jinny.model.CardItem
import com.jinny.soloescape.databinding.ActivityMatchlistBinding

class MatchListActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    private val adapter = MatchListAdapter()
    private val cardItems = mutableListOf<CardItem>()

    private var binding: ActivityMatchlistBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchlistBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userDB = Firebase.database.reference.child("Users")
        initMatchListRecyclerView()
        getMatchList()
    }

    private fun getMatchList(){
        // 매치 리스트 가져오기
        val matchedDB = userDB.child(getCurrentUserID()).child("likeBy").child("match")
        matchedDB.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(snapshot.key?.isNotEmpty() == true){
                    // 키가 존재 한다면
                    getUserByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

    }
    private fun getUserByKey(userID:String){
        userDB.child(userID).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItems.add(CardItem(userID,snapshot.child("name").value.toString()))
                adapter.submitList(cardItems)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun initMatchListRecyclerView() {
        binding!!.matchListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding!!.matchListRecyclerView.adapter = adapter
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}