package com.vickylee.yardsale.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.HashMap

class UserRepository(private val context: Context) {
    private val TAG = this.toString()
    private val db = Firebase.firestore
    private val COLLECTION_NAME = "users"
    private val FIELD_USER_NAME = "name"
    private val FIELD_USER_EMAIL = "email"
    private val FIELD_PASSWORD = "password"
    private val FIELD_USER_TYPE = "userType"

    private val sharedPreference =
        context.getSharedPreferences("YARD_SALE_PREFS", Context.MODE_PRIVATE)
    private var editor = sharedPreference.edit()

    fun addUserToDB(newUser: User) {
        try {
            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_USER_NAME] = newUser.name
            data[FIELD_USER_EMAIL] = newUser.email
            data[FIELD_PASSWORD] = newUser.password
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
                            editor.putString("USER_NAME",
                                documentChange.document.data.get(FIELD_USER_NAME) as String?
                            )
                            editor.putString("USER_TYPE",
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

}