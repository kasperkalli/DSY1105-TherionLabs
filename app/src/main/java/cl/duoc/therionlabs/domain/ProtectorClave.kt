package cl.duoc.therionlabs.domain

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object ProtectorClave {
    fun generarSal(): String =
        Base64.getEncoder().encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) })

    fun derivar(clave: String, sal: String): String {
        val parametros =
            PBEKeySpec(clave.toCharArray(), Base64.getDecoder().decode(sal), 120000, 256)
        return try {
            Base64.getEncoder()
                .encodeToString(
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(parametros)
                        .encoded
                )
        } finally {
            parametros.clearPassword()
        }
    }

    fun coincide(clave: String, sal: String, esperado: String) =
        MessageDigest.isEqual(
            Base64.getDecoder().decode(derivar(clave, sal)),
            Base64.getDecoder().decode(esperado),
        )
}
