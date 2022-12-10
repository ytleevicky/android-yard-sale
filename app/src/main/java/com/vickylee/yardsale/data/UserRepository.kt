package com.vickylee.yardsale.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.HashMap
import kotlin.math.log

class UserRepository(private val context: Context) {
    //region Properties
    private val TAG = this.toString()
    private val db = Firebase.firestore
    private val COLLECTION_NAME = "users"
    private val FIELD_USER_NAME = "name"
    private val FIELD_USER_EMAIL = "email"
    private val FIELD_USER_PHONE = "phone"
    private val FIELD_USER_ADDRESS = "address"
    private val FIELD_USER_PASSWORD = "password"
    private val FIELD_USER_TYPE = "userType"

    private val SUB_COLLECTION_NAME = "items"
    private val FIELD_ITEM_NAME = "itemName"
    private val FIELD_ITEM_DESCRIPTION = "itemDescription"
    private val FIELD_ITEM_PRICE = "itemPrice"
    private val FIELD_ITEM_IS_AVAILABLE = "isItemAvailable"
    private val FIELD_ITEM_CREATION_TIME = "creation_time_ms"

    private lateinit var currentUser: User

    var user: MutableLiveData<User?> = MutableLiveData<User?>()
    var allItemsInUserAccount: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>()

    private val sharedPreference =
        context.getSharedPreferences("YARD_SALE_PREFS", Context.MODE_PRIVATE)
    private var editor = sharedPreference.edit()
    //endregion

    //region Functions
    fun addUserToDB(newUser: User) {
        try {
            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_USER_NAME] = newUser.name
            data[FIELD_USER_EMAIL] = newUser.email
            data[FIELD_USER_PASSWORD] = newUser.password
            data[FIELD_USER_PHONE] = newUser.phone
            data[FIELD_USER_ADDRESS] = newUser.address
            data[FIELD_USER_TYPE] = newUser.userType

            db.collection(COLLECTION_NAME).add(data).addOnSuccessListener { docRef ->
                Log.d(TAG, "addUserToDB: Document added with ID ${docRef.id}")
                editor.putString("USER_DOC_ID", docRef.id)
                editor.commit()

            }.addOnFailureListener {
                Log.e(TAG, "addUserToDB: $it")
            }

        } catch (ex: Exception) {
            Log.e(TAG, "addUserToDB: ${ex.toString()}")
        }
    }

    fun searchUserWithEmail(email: String) {
        try {
            db.collection(COLLECTION_NAME)
                .whereEqualTo(FIELD_USER_EMAIL, email)
                .addSnapshotListener(EventListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            TAG,
                            "searchUserWithEmail: Listening to collection documents FAILED ${error}"
                        )
                        return@EventListener
                    }

                    if (snapshot != null) {
                        Log.d(
                            TAG,
                            "searchUserWithEmail: Received the documents from collection ${snapshot}"
                        )

                        //process the received documents
                        //save the doc ID & user role to the SharedPreferences
                        for (documentChange in snapshot.documentChanges) {
                            editor.putString("USER_DOC_ID", documentChange.document.id)
                            editor.putString(
                                "USER_NAME",
                                documentChange.document.data.get(FIELD_USER_NAME) as String?
                            )
                            editor.putString(
                                "USER_TYPE",
                                documentChange.document.data.get(FIELD_USER_TYPE) as String?
                            )
                            Log.d(
                                TAG,
                                "searchUserWithEmail: User found: ${documentChange.document.id}",
                            )
                            editor.commit()
                        }
                    } else {
                        Log.e(TAG, "searchUserWithEmail: No Documents received from collection")
                    }
                })

        } catch (ex: Exception) {
            Log.e(TAG, "searchUserWithEmail: ${ex}")
        }
    }

    // Seller: Add item to account
    fun addItemToUserAccount(newItem: Item) {
        try {
            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_ITEM_NAME] = newItem.itemName
            data[FIELD_ITEM_DESCRIPTION] = newItem.itemDescription
            data[FIELD_ITEM_PRICE] = newItem.itemPrice
            data[FIELD_ITEM_IS_AVAILABLE] = newItem.isItemAvailable
            data[FIELD_ITEM_CREATION_TIME] = FieldValue.serverTimestamp()

            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!

            db.collection(COLLECTION_NAME).document(userDocumentID)
                .collection(SUB_COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addItemToUserAccount: Document added with ID ${docRef.id}")

                }.addOnFailureListener {
                    Log.e(TAG, "addItemToUserAccount: $it")
                }

        } catch (ex: Exception) {
            Log.e(TAG, "addUserToDB: ${ex.toString()}")
        }
    }

    // Retrieve a list of items that are created by the current seller
    fun getAllItemsInSellerAccount() {
        try {
            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!
            db.collection(COLLECTION_NAME).document(userDocumentID)
                .collection(SUB_COLLECTION_NAME)
                .orderBy(FIELD_ITEM_CREATION_TIME, Query.Direction.DESCENDING)
                .addSnapshotListener(EventListener { snapshot, error ->

                    if (error != null) {
                        Log.e(
                            TAG,
                            "getAllItemsInUserAccount: Listening to collection documents FAILED ${error}"
                        )
                        return@EventListener
                    }

                    if (snapshot != null) {
                        Log.d(
                            TAG,
                            "getAllItemsInSellerAccount: ${snapshot.size()} Received the documents from collection ${snapshot}"
                        )

                        val itemsArrayList: MutableList<Item> = ArrayList<Item>()

                        // process the received document
                        for (documentChange in snapshot.documentChanges) {
                            val currentItem: Item =
                                documentChange.document.toObject(Item::class.java)

                            when (documentChange.type) {
                                DocumentChange.Type.ADDED -> {
                                    itemsArrayList.add(currentItem)
                                }
                                DocumentChange.Type.MODIFIED -> {}
                                DocumentChange.Type.REMOVED -> {
                                    itemsArrayList.remove(currentItem)
                                }
                            }
                        }

                        Log.d(TAG, "getAllItemsInSellerAccount: ${itemsArrayList.toString()}")
                        allItemsInUserAccount.postValue(itemsArrayList)
                    } else {
                        Log.d(
                            TAG,
                            "getAllItemsInSellerAccount: No Document received from collection."
                        )
                    }
                })

        } catch (ex: Exception) {
            Log.e(TAG, "getAllItemsInUserAccount: ${ex}")
        }
    }
    //endregion

    // Get user details for profile
    fun getUserDetailsFromDB(userID: String) {
        try {

            val docRef = db.collection(COLLECTION_NAME).document(userID)
//            val currentUser: User = docRef.toObject
//            docRef?.get()?.addOnSuccessListener {
//                Log.d(TAG, "getUserDetailsFromDB: ${it.data}")
//                user.postValue(it.data.toObject)
//
//            }?.addOnFailureListener {
//                Log.e(TAG, "addUserToDB: $it")
//            }

            docRef.addSnapshotListener(EventListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getUserDetailsFromDB: Listening to collect documents FAILED $error")
                    return@EventListener
                }

                if (snapshot != null) {
                    val currentUser: User = snapshot.toObject(User::class.java)!!
                    if (currentUser != null) {
                        currentUser.id = snapshot.id
                        currentUser.email = snapshot.get(FIELD_USER_EMAIL).toString()
                        currentUser.name = snapshot.get(FIELD_USER_NAME).toString()
                        currentUser.phone = snapshot.get(FIELD_USER_PHONE).toString()
                        currentUser.password = snapshot.get(FIELD_USER_PASSWORD).toString()
                    }
                    user.postValue(currentUser)
                }

            })
        }
        catch (ex:Exception) {
            Log.e(TAG, "getUserDetailsFromDB: Couldn't find user", )
        }

    }
}