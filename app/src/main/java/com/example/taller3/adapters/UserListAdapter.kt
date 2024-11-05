package com.example.taller3.adapters

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.example.taller3.databinding.ActiveUserRowBinding
import com.example.taller3.databinding.ActivityActiveUsersBinding
import java.io.InputStream

class UserListAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags){
    private lateinit var binding : ActiveUserRowBinding

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        binding = ActiveUserRowBinding.inflate(LayoutInflater.from(context))
        return binding.activeUserRow
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val binding = ActiveUserRowBinding.bind(view!!)
        val name = cursor?.getString(1)
        //val photoUri = getContactPhotoUri(cursor?.getLong(0) ?: -1, context!!)

        /*
        ///binding.contactName.text=name
        i*f (photoUri != null) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(photoUri)
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imageView.setImageBitmap(bitmap)
        }*/
    }


    }