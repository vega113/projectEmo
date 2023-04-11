package util

import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object JwtUtils {
  private val random = new SecureRandom()

  def generateSecret(): SecretKey = {
    val bytes = new Array[Byte](32)
    random.nextBytes(bytes)
    new SecretKeySpec(bytes, "HmacSHA256")
  }

  def encodeBase64(key: SecretKey): String = {
    Base64.getUrlEncoder.withoutPadding().encodeToString(key.getEncoded)
  }

  def main(args: Array[String]): Unit = {
    val secret = generateSecret()
    println(encodeBase64(secret))
  }
}
