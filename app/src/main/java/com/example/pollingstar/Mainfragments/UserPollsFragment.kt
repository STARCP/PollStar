package com.example.pollingstar.Mainfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pollingstar.Options
import com.example.pollingstar.Question
import com.example.pollingstar.R
import com.example.pollingstar.adapters.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class UserPollsFragment : Fragment(), UserPoll {

    lateinit var db : DatabaseReference
    lateinit var ques : ArrayList<Question>
    lateinit var adapter : UserpollsAdapter
    lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_polls, container, false)
        val recyler = view.findViewById<RecyclerView>(R.id.userrecyler)

        db = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        (activity as AppCompatActivity).supportActionBar?.setTitle("My Polls")



        val pollsRef = db.child("polls")
        val usersRef = db.child("users")
        ques = ArrayList()

        pollsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    //ques.clear()
                    for(poll in snapshot.children) {
                        val uid = poll.child("createdby").getValue().toString()
                        //Log.i("mytag",uid.toString())
                        val options = ArrayList<Options>()
                        val answeredby = ArrayList<String>()
                        val votechoice = ArrayList<String>()
                        //answeredby.clear()
                        //votechoice.clear()
                        var votes = 0
                        val optionsRef = poll.child("options")
                        for (c in optionsRef.children) {
                            val value = c.child("value").getValue().toString()
                            val vote = c.child("votes").getValue().toString().toInt()
                            //Log.i("mytag", value)
                            val option1 = Options(c.key,value, vote)
                            options.add(option1)
                            votes += vote
                        }
                        var question = ""
                        val qu = poll.child("question").children
                        for(q in qu) {
                            question = q.getValue().toString()
                        }
                        //Log.i("mytag",question)
                        for (child in poll.child("answeredby").children) {
                            val option = child.child("userid").getValue().toString()
                            answeredby.add(option)
                            val choice = child.child("pollid").getValue().toString()
                            votechoice.add(choice)
                        }
                        val name = poll.child("name").getValue().toString()
                        val pollid = poll.child("pollid").getValue().toString()
                        //Toast.makeText(requireContext(), pollid, Toast.LENGTH_SHORT).show()
                        if(uid == mAuth.currentUser!!.uid)
                            ques.add(Question(pollid,name!!,question ,votes, uid, options,answeredby,votechoice))

                    }
                    adapter.arr = ques!!

                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        adapter = UserpollsAdapter(requireContext(), ques, this)
        recyler.adapter = adapter
        recyler.layoutManager = LinearLayoutManager(requireContext())


        return view
    }

    override fun redirect(poll: Question) {

                val bundle = Bundle()
                bundle.putParcelable("poll",poll)
                val fragment = DisplayFragment()
                Log.i("parcelable", poll.options[1].toString())
                fragment.arguments = bundle
                val tran = requireActivity().supportFragmentManager.beginTransaction()
                tran.replace(R.id.fragment, fragment)
                //tran.addToBackStack(null)
                tran.commit()
    }

    override fun delete(uid: String,ind : Int) {
        val pollref = db.child("polls").child(uid)
        pollref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.ref.removeValue()
                    adapter.arr.removeAt(ind)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}