package projet.example.ma.soapclient.beans

/**
 * Enum représentant le type de compte
 */
enum class TypeCompte {
    COURANT,
    EPARGNE
}

/**
 * Data class représentant un compte bancaire
 * Utilise String pour dateCreation pour simplifier le parsing SOAP
 */
data class Compte(
    val id: Long? = null,
    val solde: Double = 0.0,
    val dateCreation: String? = null,  // Changé de Date à String
    val type: TypeCompte = TypeCompte.COURANT
)