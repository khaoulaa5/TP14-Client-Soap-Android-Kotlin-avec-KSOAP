package projet.example.ma.soapclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import projet.example.ma.soapclient.R
import projet.example.ma.soapclient.beans.Compte
import java.text.SimpleDateFormat
import java.util.*

class CompteAdapter : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {
    private var comptes = mutableListOf<Compte>()

    // Listeners pour gérer les clics sur Modifier et Supprimer
    var onEditClick: ((Compte) -> Unit)? = null
    var onDeleteClick: ((Compte) -> Unit)? = null

    /**
     * Met à jour la liste des comptes affichés.
     * @param newComptes Nouvelle liste de comptes.
     */
    fun updateComptes(newComptes: List<Compte>) {
        comptes.clear()
        comptes.addAll(newComptes)
        notifyDataSetChanged()
    }

    /**
     * Supprime un compte de la liste.
     * @param compte Compte à supprimer.
     */
    fun removeCompte(compte: Compte) {
        val position = comptes.indexOf(compte)
        if (position >= 0) {
            comptes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Crée une nouvelle vue pour chaque élément de la liste.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return CompteViewHolder(view)
    }

    /**
     * Lie un élément de données à une vue.
     */
    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        holder.bind(comptes[position])
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     */
    override fun getItemCount() = comptes.size

    /**
     * Classe ViewHolder pour gérer les vues individuelles.
     */
    inner class CompteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val id: TextView = view.findViewById(R.id.textId)
        private val solde: TextView = view.findViewById(R.id.textSolde)
        private val type: Chip = view.findViewById(R.id.textType)
        private val crDate: TextView = view.findViewById(R.id.textDate)
        private val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)

        /**
         * Associe un objet Compte aux vues.
         */
        fun bind(compte: Compte) {
            id.text = "Compte Numéro ${compte.id}"
            solde.text = "${compte.solde} DH"
            type.text = compte.type.name

            // ✅ CORRECTION : dateCreation est maintenant un String, pas une Date
            crDate.text = formatDate(compte.dateCreation)

            btnEdit.setOnClickListener { onEditClick?.invoke(compte) }
            btnDelete.setOnClickListener { onDeleteClick?.invoke(compte) }
        }

        /**
         * Formate la date reçue du serveur (qui est un String)
         */
        private fun formatDate(dateStr: String?): String {
            if (dateStr.isNullOrBlank()) {
                return "Date inconnue"
            }

            return try {
                // Formats de date possibles venant du serveur
                val inputFormats = listOf(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                )

                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Essayer de parser avec chaque format
                for (inputFormat in inputFormats) {
                    try {
                        val date = inputFormat.parse(dateStr)
                        if (date != null) {
                            return outputFormat.format(date)
                        }
                    } catch (e: Exception) {
                        // Continuer avec le format suivant
                    }
                }

                // Si aucun format ne fonctionne, retourner la date brute
                dateStr
            } catch (e: Exception) {
                // En cas d'erreur, retourner la date telle quelle
                dateStr
            }
        }
    }
}