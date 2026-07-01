package com.example.hoof_care_02.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hoof_care_02.R
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.util.SessionManager

// Para carregar imagens da internet, você precisará de uma biblioteca como Glide ou Coil.
// Adicione a dependência no seu build.gradle.kts e descomente a linha de import.
// import com.bumptech.glide.Glide

class DogAdapter(
    private var dogList: List<Dog>,
    private val onVerPerfilClicked: (Dog) -> Unit,
    private val onSelecionarClicked: (Dog) -> Unit
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petPhoto: ImageView = itemView.findViewById(R.id.petPhoto)
        val petName: TextView = itemView.findViewById(R.id.petName)
        val petBreed: TextView = itemView.findViewById(R.id.petBreed)
        val petAge: TextView = itemView.findViewById(R.id.dogAge)
        // Referências para os novos botões
        val btnVerPerfil: Button = itemView.findViewById(R.id.btnVerPerfil)
        val btnSelecionar: Button = itemView.findViewById(R.id.btnSelecionar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_card, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogList[position]
        holder.petName.text = dog.name
        holder.petBreed.text = dog.breed.name
        holder.petAge.text = if (dog.age == 1) "1 ano" else "${dog.age} anos"

        // Configura o clique de cada botão
        holder.btnVerPerfil.setOnClickListener {
            // --- LOG DE DEBUG ADICIONADO ---
            val token = SessionManager.getAuthToken()
            Log.d("AUTH_CHECK", "Adapter: Clique em 'Ver Perfil'. Token encontrado? ${token != null}")
            // --------------------------------

            onVerPerfilClicked(dog)
        }
        holder.btnSelecionar.setOnClickListener {
            onSelecionarClicked(dog)
        }
    }

    override fun getItemCount(): Int {
        return dogList.size
    }

    // Função para atualizar a lista de cachorros no adapter
    fun updateData(newDogList: List<Dog>) {
        dogList = newDogList
        notifyDataSetChanged()
    }
}