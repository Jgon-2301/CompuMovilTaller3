package com.example.taller3.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3.R
import com.example.taller3.databinding.ActiveUserRowBinding
import com.example.taller3.model.User
import com.bumptech.glide.Glide


class UserRecyclerAdapter(private val userList: List<User>, private val onLocationClick: (User) -> Unit)
    : RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ActiveUserRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ActiveUserRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.userName.text = "${user.name} ${user.lastName}"

        user.profileImageUrl?.let {
            Glide.with(holder.binding.profileImage.context)
                .load(it)
                .placeholder(R.drawable.pictures)
                .into(holder.binding.profileImage)
        } ?: run {
            holder.binding.profileImage.setImageResource(R.drawable.pictures) // Imagen predeterminada si no hay URL
        }

        holder.binding.botonSeguir.setOnClickListener {
            onLocationClick(user)
        }
    }

    override fun getItemCount(): Int = userList.size
}