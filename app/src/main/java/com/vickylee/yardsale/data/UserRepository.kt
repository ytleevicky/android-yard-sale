package com.vickylee.yardsale.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import java.util.HashMap

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
    private val FIELD_USER_FAV_ITEMS = "favItems"
    private val FIELD_PROFILE_PIC = "profilePic"

    private val SUB_COLLECTION_NAME = "items"
    private val FIELD_ITEM_NAME = "itemName"
    private val FIELD_ITEM_DESCRIPTION = "itemDescription"
    private val FIELD_ITEM_PRICE = "itemPrice"
    private val FIELD_ITEM_IS_AVAILABLE = "isItemAvailable"
    private val FIELD_ITEM_CREATION_TIME = "creation_time_ms"
    private val FIELD_ITEM_PIC = "itemPic"

    private lateinit var currentUser: User

    var user: MutableLiveData<User?> = MutableLiveData<User?>()
    var item: MutableLiveData<Item?> = MutableLiveData<Item?>()
    var allItemsInUserAccount: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>()
    var allItemsForBuyer: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>()

    private val sharedPreference =
        context.getSharedPreferences("YARD_SALE_PREFS", Context.MODE_PRIVATE)
    private var editor = sharedPreference.edit()
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
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
            data[FIELD_USER_FAV_ITEMS] = arrayListOf<String>()
            data[FIELD_PROFILE_PIC] = newUser.profilePic

            db.collection(COLLECTION_NAME).add(data).addOnSuccessListener { docRef ->
                Log.d(TAG, "addUserToDB: Document added with ID ${docRef.id}")
                editor.putString("USER_DOC_ID", docRef.id)
                editor.putStringSet("USER_FAV_ITEMS", mutableSetOf())
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

                            // fav item list
                            val fav =
                                documentChange.document.data.get(FIELD_USER_FAV_ITEMS) as ArrayList<String>?
                            editor.putStringSet("USER_FAV_ITEMS", fav?.toMutableSet())

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
    fun addItemToUserAccount(
        itemName: String,
        itemDescription: String,
        itemPrice: Double,
        itemPic: String
    ) {
        try {
//            storageRef = FirebaseStorage.getInstance().reference.child("ItemImages")
//            firebaseFirestore = FirebaseFirestore.getInstance()
//            storageRef = storageRef.child(System.currentTimeMillis().toString())
//
//            itemPic?.let { it1 ->
//                storageRef.putFile(it1).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        storageRef.downloadUrl.addOnSuccessListener { uri ->
//
//                            val data: MutableMap<String, Any> = HashMap()
//
//                            data[FIELD_ITEM_NAME] = itemName
//                            data[FIELD_ITEM_DESCRIPTION] = itemDescription
//                            data[FIELD_ITEM_PRICE] = itemPrice
//                            data[FIELD_ITEM_IS_AVAILABLE] = true
//                            data[FIELD_ITEM_CREATION_TIME] = FieldValue.serverTimestamp()
//                            data[FIELD_ITEM_PIC] = uri.toString()
//
//                            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!
//
//                            db.collection(COLLECTION_NAME).document(userDocumentID)
//                                .collection(SUB_COLLECTION_NAME)
//                                .add(data)
//                                .addOnSuccessListener { docRef ->
//                                    Log.d(
//                                        TAG,
//                                        "addItemToUserAccount: Document added with ID ${docRef.id}"
//                                    )
//
//                                }.addOnFailureListener {
//                                    Log.e(TAG, "addItemToUserAccount: $it")
//                                }
//                        }
//                    }
//                }
//            }

            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_ITEM_NAME] = itemName
            data[FIELD_ITEM_DESCRIPTION] = itemDescription
            data[FIELD_ITEM_PRICE] = itemPrice
            data[FIELD_ITEM_IS_AVAILABLE] = true
            data[FIELD_ITEM_CREATION_TIME] = FieldValue.serverTimestamp()
            data[FIELD_ITEM_PIC] = itemPic

            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!

            db.collection(COLLECTION_NAME).document(userDocumentID)
                .collection(SUB_COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener { docRef ->
                    Log.d(
                        TAG,
                        "addItemToUserAccount: Document added with ID ${docRef.id}"
                    )

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

                            currentItem.itemID = documentChange.document.id
                            currentItem.sellerID = userDocumentID
                            currentItem.itemName =
                                documentChange.document.get(FIELD_ITEM_NAME).toString()
                            currentItem.itemPrice =
                                documentChange.document.get(FIELD_ITEM_PRICE).toString().toDouble()
                            currentItem.itemDescription =
                                documentChange.document.get(FIELD_ITEM_DESCRIPTION).toString()
                            currentItem.isItemAvailable =
                                documentChange.document.get(FIELD_ITEM_IS_AVAILABLE).toString()
                                    .toBoolean()
                            currentItem.itemPic =
                                documentChange.document.get(FIELD_ITEM_PIC).toString()

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

    // get all seller items list
    fun getAllSellerItems() {
        try {
            db.collection(COLLECTION_NAME)
                .whereEqualTo(FIELD_USER_TYPE, "Seller")
                .addSnapshotListener(EventListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            TAG,
                            "getAllSellerItems: Listening to collection documents FAILED ${error}",
                        )
                        return@EventListener
                    }

                    if (snapshot != null) {
                        Log.d(
                            TAG,
                            "getAllSellerItems: recieved items ${snapshot.size()} Received the documents from collection ${snapshot}"
                        )
                        val itemsArrayList: MutableList<Item> = ArrayList<Item>()
                        for (documentChange in snapshot.documentChanges) {
                            val currentUser: User =
                                documentChange.document.toObject(User::class.java)
                            currentUser.id = documentChange.document.id
                            Log.d(TAG, "getAllSellerItems: $currentUser")
                            db.collection(COLLECTION_NAME).document(currentUser.id)
                                .collection(SUB_COLLECTION_NAME)
                                .orderBy(FIELD_ITEM_CREATION_TIME, Query.Direction.DESCENDING)
                                .addSnapshotListener(EventListener { items, error ->
                                    if (error != null) {
                                        Log.e(
                                            TAG,
                                            "getAllSellerItems: Listening to collection documents FAILED ${error}",
                                        )
                                        return@EventListener
                                    }

                                    if (items != null) {
                                        Log.d(
                                            TAG,
                                            "getAllSellerItems: recieved items ${items.size()} Received the documents from collection ${items}"
                                        )
                                        for (documentChange in items.documentChanges) {
                                            val currentItem: Item =
                                                documentChange.document.toObject(Item::class.java)
                                            currentItem.sellerID = currentUser.id
                                            currentItem.itemID = documentChange.document.id
                                            currentItem.itemName =
                                                documentChange.document.get(FIELD_ITEM_NAME)
                                                    .toString()
                                            currentItem.itemPrice =
                                                documentChange.document.get(FIELD_ITEM_PRICE)
                                                    .toString().toDouble()
                                            currentItem.itemDescription =
                                                documentChange.document.get(FIELD_ITEM_DESCRIPTION)
                                                    .toString()
                                            currentItem.isItemAvailable =
                                                documentChange.document.get(FIELD_ITEM_IS_AVAILABLE)
                                                    .toString().toBoolean()
                                            currentItem.itemPic =
                                                documentChange.document.get(FIELD_ITEM_PIC)
                                                    .toString()

                                            Log.d(TAG, "getAllSellerItems: $currentItem")
                                            itemsArrayList.add(currentItem)
                                        }
                                        allItemsForBuyer.postValue(itemsArrayList)
                                    }

                                })


                        }
                    }

                })
        } catch (ex: Exception) {
            Log.e(TAG, "getAllSellerItems: $ex")
        }
    }

    // Get user details for profile
    fun getUserDetailsFromDB(userID: String) {
        try {
            val docRef = db.collection(COLLECTION_NAME).document(userID)

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
                        currentUser.profilePic = snapshot.get(FIELD_PROFILE_PIC).toString()
                    }
                    user.postValue(currentUser)
                }
            })
        } catch (ex: Exception) {
            Log.e(TAG, "getUserDetailsFromDB: Couldn't find user")
        }

    }

    // Update user details
    fun updateUserDetails(userID: String, phone: String, address: String, imageUrl: String) {
        try {
            Log.d(TAG, "updateUserDetails: Starting update")
            db.collection(COLLECTION_NAME).document(userID)
                .update(
                    FIELD_USER_PHONE,
                    phone,
                    FIELD_USER_ADDRESS,
                    address,
                    FIELD_PROFILE_PIC,
                    imageUrl
                )
                .addOnSuccessListener {
                    Log.d(TAG, "updateUserDetails: Updated successfully")
                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateUserDetails: Update Failed")
                }

        } catch (ex: Exception) {
            Log.e(TAG, "updateUserDetails: Update failed")
        }
    }

    // Change password
    fun changePassword(userID: String, newPassword: String) {
        try {
            db.collection(COLLECTION_NAME).document(userID)
                .update(FIELD_USER_PASSWORD, newPassword)
                .addOnSuccessListener {
                    Log.d(TAG, "updateUserDetails: Updated successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateUserDetails: Update Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "updateUserDetails: Update failed")
        }
    }

    // Delete Item from list
    fun deleteItem(userID: String, itemID: String) {
        try {
            db.collection(COLLECTION_NAME).document(userID)
                .collection(SUB_COLLECTION_NAME)
                .document(itemID)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteItem: deleted successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "deleteItem: delete Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "deleteItem: delete failed")
        }
    }

    // Update item availability status
    fun updateItemAvailability(userID: String, itemID: String, itemStatus: Boolean) {
        try {
            db.collection(COLLECTION_NAME).document(userID)
                .collection(SUB_COLLECTION_NAME).document(itemID)
                .update(FIELD_ITEM_IS_AVAILABLE, itemStatus)
                .addOnSuccessListener {
                    Log.d(TAG, "updateItemAvailability: Updated successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateItemAvailability: Update Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "updateItemAvailability: Update failed")
        }
    }

    fun getItemDetails(userID: String, itemID: String) {
        try {
            val docRef = db.collection(COLLECTION_NAME).document(userID)
                .collection(SUB_COLLECTION_NAME).document(itemID)

            docRef.addSnapshotListener(EventListener { snapshot, error ->
                Log.d(TAG, "getItemDetails: $snapshot")
                if (error != null) {
                    Log.e(TAG, "getItemDetails: Listening to collect documents FAILED $error")
                    return@EventListener
                }

                if (snapshot != null) {
                    val currentItem: Item? = snapshot.toObject(Item::class.java)
                    if (currentItem != null) {
                        Log.d(TAG, "getItemDetails: $snapshot")
                        currentItem.itemID = snapshot.id
                        currentItem.sellerID = userID
                        currentItem.itemName = snapshot.get(FIELD_ITEM_NAME).toString()
                        currentItem.itemPrice = snapshot.get(FIELD_ITEM_PRICE).toString().toDouble()
                        currentItem.itemDescription = snapshot.get(FIELD_ITEM_DESCRIPTION).toString()
                        currentItem.itemPic = snapshot.get(FIELD_ITEM_PIC).toString()
                        currentItem.isItemAvailable = snapshot.get(FIELD_ITEM_IS_AVAILABLE).toString().toBoolean()
                    }
                    item.postValue(currentItem)
                }
            })
        } catch (ex: Exception) {
            Log.e(TAG, "getItemDetails: Couldn't find user")
        }
    }

    // Update item by seller
    fun updateItem(
        userID: String,
        itemID: String,
        itemName: String,
        itemDescription: String,
        itemPrice: Double,
        itemPic: String
    ) {
        try {
            db.collection(COLLECTION_NAME).document(userID)
                .collection(SUB_COLLECTION_NAME).document(itemID)
                .update(
                    FIELD_ITEM_NAME,
                    itemName,
                    FIELD_ITEM_PRICE,
                    itemPrice,
                    FIELD_ITEM_DESCRIPTION,
                    itemDescription,
                    FIELD_ITEM_PIC,
                    itemPic
                )
                .addOnSuccessListener {
                    Log.d(TAG, "updateItem: Updated successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateItem: Update Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "updateItem: Update failed")
        }
    }

    fun addItemToFavorites(itemID: String) {
        try {
            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!
            val userEmail = sharedPreference.getString("USER_EMAIL", "")!!

            db.collection(COLLECTION_NAME).document(userDocumentID)
                .update(FIELD_USER_FAV_ITEMS, FieldValue.arrayUnion(itemID))
                .addOnSuccessListener {
                    Log.d(TAG, "addItemToFavorites: Updated successfully")

                    // to retrieve the latest fav list
                    searchUserWithEmail(userEmail)
                }
                .addOnFailureListener {
                    Log.e(TAG, "addItemToFavorites: Update Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "addItemToFavorites: Update failed")
        }
    }

    fun removeItemFromFavorites(removeItemID: String) {
        try {
            val userDocumentID = sharedPreference.getString("USER_DOC_ID", "")!!

            db.collection(COLLECTION_NAME).document(userDocumentID)
                .update(FIELD_USER_FAV_ITEMS, FieldValue.arrayRemove(removeItemID))
                .addOnSuccessListener {
                    Log.d(TAG, "removeItemFromFavorites: removeItem successfully")
                }
                .addOnFailureListener {
                    Log.e(TAG, "removeItemFromFavorites: removeItem Failed")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "removeItemFromFavorites: removeItem failed")
        }
    }
    //endregion
}