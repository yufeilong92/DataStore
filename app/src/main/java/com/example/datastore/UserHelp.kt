package com.example.datastore

import android.content.Context
import androidx.datastore.dataStore
import com.example.datastore.User.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

val Context.dataStroe by dataStore(
    fileName = "user.pd",
    serializer = UserSerializer
)

object UserHelp {

    suspend fun read(context: Context): Flow<UserPreferences> {
        return context.dataStroe.data.catch {
            if (it is IOException){
                it.printStackTrace()
                emit(UserPreferences.getDefaultInstance())
            }else{
                throw  it
            }
        }
    }

    suspend fun write(context: Context, age: Int, name: String, phont: String) {
        context.dataStroe.updateData {
            it.toBuilder().setAge(age).setName(name).setPhone(phont).build()
        }
    }

}