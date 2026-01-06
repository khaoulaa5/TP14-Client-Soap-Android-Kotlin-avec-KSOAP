package projet.example.ma.soapclient.ws

import android.util.Log
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import projet.example.ma.soapclient.beans.Compte
import projet.example.ma.soapclient.beans.TypeCompte

class Service {
    companion object {
        private const val TAG = "SoapService"

        // Configuration SOAP - VÉRIFIEZ CES VALEURS !
        private const val NAMESPACE = "http://ws.tp13_web_service_soap.example.com/"
        private const val URL = "http://10.0.2.2:8082/services/ws"

        // Noms des méthodes SOAP
        private const val METHOD_GET_COMPTES = "getComptes"
        private const val METHOD_CREATE_COMPTE = "createCompte"
        private const val METHOD_DELETE_COMPTE = "deleteCompte"
    }

    /**
     * Récupère tous les comptes
     */
    fun getComptes(): List<Compte> {
        val comptes = mutableListOf<Compte>()

        return try {
            val request = SoapObject(NAMESPACE, METHOD_GET_COMPTES)

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.dotNet = false
            envelope.setOutputSoapObject(request)

            val transport = HttpTransportSE(URL)
            transport.debug = true

            Log.d(TAG, "=== Appel getComptes ===")
            Log.d(TAG, "URL: $URL")
            Log.d(TAG, "Namespace: $NAMESPACE")

            transport.call("", envelope)

            Log.d(TAG, "Request XML: ${transport.requestDump}")
            Log.d(TAG, "Response XML: ${transport.responseDump}")

            val response = envelope.bodyIn as? SoapObject

            if (response != null) {
                Log.d(TAG, "Response count: ${response.propertyCount}")

                for (i in 0 until response.propertyCount) {
                    try {
                        val soapCompte = response.getProperty(i) as? SoapObject
                        if (soapCompte != null) {
                            val compte = parseCompteFromSoap(soapCompte)
                            comptes.add(compte)
                            Log.d(TAG, "Compte parsé: $compte")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur parsing compte $i: ${e.message}", e)
                    }
                }
            }

            comptes
        } catch (e: Exception) {
            Log.e(TAG, "Erreur getComptes: ${e.message}", e)
            Log.e(TAG, "Stack trace:", e)
            emptyList()
        }
    }

    /**
     * Crée un nouveau compte
     */
    fun createCompte(solde: Double, type: TypeCompte): Boolean {
        return try {
            val request = SoapObject(NAMESPACE, METHOD_CREATE_COMPTE)

            // Convertir en String pour éviter les problèmes de sérialisation
            request.addProperty("solde", solde.toString())
            request.addProperty("type", type.name)

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.dotNet = false
            envelope.setOutputSoapObject(request)

            // Enregistrer les types primitifs pour la sérialisation
            envelope.addMapping(NAMESPACE, "double", Double::class.java)

            val transport = HttpTransportSE(URL)
            transport.debug = true

            Log.d(TAG, "=== Appel createCompte ===")
            Log.d(TAG, "Solde: $solde, Type: ${type.name}")

            transport.call("", envelope)

            Log.d(TAG, "Request XML: ${transport.requestDump}")
            Log.d(TAG, "Response XML: ${transport.responseDump}")
            Log.d(TAG, "Compte créé avec succès")

            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur createCompte: ${e.message}", e)
            Log.e(TAG, "Stack trace complète:", e)
            false
        }
    }

    /**
     * Supprime un compte par ID
     */
    fun deleteCompte(id: Long): Boolean {
        return try {
            val request = SoapObject(NAMESPACE, METHOD_DELETE_COMPTE)
            request.addProperty("id", id)

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.dotNet = false
            envelope.setOutputSoapObject(request)

            val transport = HttpTransportSE(URL)
            transport.debug = true

            Log.d(TAG, "=== Appel deleteCompte ===")
            Log.d(TAG, "ID: $id")

            transport.call("", envelope)

            Log.d(TAG, "Request XML: ${transport.requestDump}")
            Log.d(TAG, "Response XML: ${transport.responseDump}")
            Log.d(TAG, "Compte supprimé avec succès")

            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur deleteCompte: ${e.message}", e)
            false
        }
    }

    /**
     * Parse un SoapObject en Compte
     */
    private fun parseCompteFromSoap(soapCompte: SoapObject): Compte {
        val id = getPropertySafely(soapCompte, "id")?.toLongOrNull()
        val solde = getPropertySafely(soapCompte, "solde")?.toDoubleOrNull() ?: 0.0
        val dateCreation = getPropertySafely(soapCompte, "dateCreation")
        val typeStr = getPropertySafely(soapCompte, "type") ?: "COURANT"

        val type = try {
            TypeCompte.valueOf(typeStr)
        } catch (e: Exception) {
            Log.w(TAG, "Type invalide: $typeStr, utilisation de COURANT par défaut")
            TypeCompte.COURANT
        }

        return Compte(
            id = id,
            solde = solde,
            dateCreation = dateCreation,
            type = type
        )
    }

    /**
     * Récupère une propriété de manière sécurisée
     */
    private fun getPropertySafely(soapObject: SoapObject, propertyName: String): String? {
        return try {
            val prop = soapObject.getProperty(propertyName)
            prop?.toString()
        } catch (e: Exception) {
            Log.w(TAG, "Propriété '$propertyName' non trouvée")
            null
        }
    }
}

// Extension pour faciliter l'extraction de propriétés
fun SoapObject.getPropertySafelyAsString(name: String): String? {
    return try {
        getProperty(name)?.toString()
    } catch (e: Exception) {
        null
    }
}