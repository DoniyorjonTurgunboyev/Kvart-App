package uz.gita.kvartarena.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.local.SpinnersData
import uz.gita.kvartarena.model.*
import java.io.File

class FirebaseRemote private constructor() {
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val auth = Firebase.auth
    private val users = firebaseDatabase.getReference("Users")
    private val apartments = firebaseDatabase.getReference("Apartments")
    private val expenses = firebaseDatabase.getReference("Expenses")
    private val storage = EncryptedLocalStorage.getInstance()

    companion object {
        private var instance: FirebaseRemote? = null
        fun getInstance(): FirebaseRemote {
            if (instance == null) instance = FirebaseRemote()
            return instance!!
        }
    }

    fun updateUser(user: User) {
        val map = HashMap<String, Any>()
        map["name"] = user.name!!
        map["surname"] = user.surname!!
        map["telegram"] = user.telegram!!
        map["address2"] = user.address2!!
        users.child(storage.uid).updateChildren(map)
    }

    fun createUser(user: User) {
        users.child(auth.uid!!).setValue(user)
    }

    fun addExpense(kid: String, expense: Expense, members: List<Member>, callback1: () -> Unit) {
        expenses.child(kid).child(expense.timeStamp).setValue(expense).addOnSuccessListener {
            setMembersToExpense(kid, expense.timeStamp, members) {
                var c = 0
                setExpenseToMembers(expense.timeStamp, expense.type, expense.amount / members.size, members, expense.investor) {
                    c++
                    if (c == members.size) {
                        callback1.invoke()
                    }
                }
            }
        }
    }

    private fun setExpenseToMembers(time: String, type: String, amount: Int, members: List<Member>, investor: String, callback1: () -> Unit) {
        var spent = 0
        members.forEach {
            if (investor != it.uid) {
                spent += amount
                val map = HashMap<String, Any>()
                map["amount"] = -amount
                map["type"] = type
                users.child(it.uid).child("expenses").child(time).setValue(map).addOnSuccessListener {
                    callback1.invoke()
                }
            }
        }
        val imap = HashMap<String, Any>()
        imap["amount"] = spent
        imap["type"] = type
        users.child(investor).child("expenses").child(time).setValue(imap).addOnSuccessListener {
            callback1.invoke()
        }
    }

    private fun setMembersToExpense(kid: String, time: String, members: List<Member>, callback1: () -> Unit) {
        val map = HashMap<String, String>()
        members.forEach { member ->
            map[member.uid] = member.name
        }
        expenses.child(kid).child(time).child("members").setValue(map).addOnSuccessListener {
            callback1.invoke()
        }
    }

    fun getApart(kid: String, callback: (Apartment) -> Unit) {
        if (kid != "" && kid != "null") {
            apartments.child(kid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = snapshot.value as HashMap<*, *>
                    val apartment = Apartment()
                    apartment.address = map["address"].toString()
                    apartment.owner = map["ownername"].toString()
                    apartment.name = map["name"].toString()
                    apartment.ownerid = map["owner"].toString()
                    apartment.bio = map["bio"].toString()
                    apartment.uid = snapshot.key
                    callback.invoke(apartment)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    fun getUser(uid: String, callback: (User) -> Unit) {
        if (storage.uid == "") return
        val id = if (uid == "") auth.uid!! else uid
        users.child(id).get().addOnSuccessListener { snapshot ->
            val map = snapshot.value as HashMap<*, *>
            val user = User()
            user.birthday = map["birthday"].toString()
            user.address1 = map["address1"].toString()
            user.name = map["name"].toString()
            user.surname = map["surname"].toString()
            user.telegram = map["telegram"].toString()
            user.address2 = map["address2"].toString()
            user.kid = map["kid"].toString()
            user.number = map["number"].toString()
            storage.address = user.address2!!
            callback.invoke(user)
        }
    }

    fun getImage(uid: String): LiveData<String> = liveData {
        var id = uid
        if (uid == "") id = storage.uid
        val f = File(App.instance.getExternalFilesDir("image"), id)
        if (!f.exists()) {
            firebaseStorage.reference.child("images/").child(id)
                .getFile(f).addOnSuccessListener {

                }
        } else emit(f.absolutePath)
    }

    fun getImageCallback(uid: String, callback: (String) -> Unit) {
        var id = uid
        if (uid == "" || uid == "1") id = storage.uid
        val f = File(App.instance.getExternalFilesDir("image"), id)
        if (uid == "1") {
            firebaseStorage.reference.child("images/").child(id)
                .getFile(f).addOnSuccessListener {
                    callback.invoke(f.absolutePath)
                }
        } else {
            if (!f.exists()) {
                firebaseStorage.reference.child("images/").child(id)
                    .getFile(f).addOnSuccessListener {
                        callback.invoke(f.absolutePath)
                    }
            } else callback.invoke(f.absolutePath)
        }
    }

    fun addMemberToApartment(kid: String) {
        val update: MutableMap<String, Any> = HashMap()
        update[storage.uid] = App.user.name + " " + App.user.surname
        apartments.child(kid).child("members").updateChildren(update)
        users.child(storage.uid).child("kid").setValue(kid)
        App.user.kid = kid
    }

    fun getMembersByApartId(callback: (List<ItemUser>) -> Unit) {
        val list = ArrayList<ItemUser>()
        apartments.child(App.user.kid!!).child("members").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { user ->
                    val member = ItemUser(user.key!!, user.value.toString(), false)
                    list.add(member)
                }
                callback.invoke(list)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getExpenses(callback: (List<Type>) -> Unit) {
        val list = SpinnersData.getAll()
        expenses.child(App.user.kid!!).get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.childrenCount.toInt()
                var i = 0
                snapshot.children.forEach { snap ->
                    i++
                    list.filter {
                        it.type == snap.child("type").value.toString()
                    }[0].amount += Integer.parseInt(snap.child("amount").value.toString())
                    if (i == count)
                        callback.invoke(list)
                }
            }
            .addOnFailureListener {
                callback.invoke(list)
            }
    }

    fun checkUser(uid: String, callback1: (Boolean) -> Unit) {
        users.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback1.invoke(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback1.invoke(false)
            }
        })
    }

    fun getMemberExpanses(callback1: (List<ItemUserGenerate>) -> Unit) {
        val list = ArrayList<ItemUserGenerate>()
        apartments.child(App.user.kid!!).child("members").get()
            .addOnSuccessListener {
                val count = it.childrenCount.toInt()
                it.children.forEach { user ->
                    var name = ""
                    var negA = 0;
                    var posA = 0
                    users.child(user.key!!).get()
                        .addOnSuccessListener {
                            name = it.child("name").value.toString() + " " + it.child("surname").value.toString()
                            it.child("expenses").children.forEach {
                                val amount = Integer.parseInt(it.child("amount").value.toString())
                                if (amount < 0) negA += amount
                                else posA += amount
                            }
                            list.add(ItemUserGenerate(it.key!!, name, negA + posA, negA, posA))
                            if (count == list.size)
                                callback1.invoke(list)
                        }
                }
            }
    }
}