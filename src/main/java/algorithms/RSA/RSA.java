package main.java.algorithms.RSA;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
  private static final int KEY_SIZE = 2048;
  private static final SecureRandom random = new SecureRandom();

  private static PublicKey publicKey;
  private static PrivateKey privateKey;


  public static void generateKeyPair() {
    BigInteger p = BigInteger.probablePrime(KEY_SIZE / 2, random);
    BigInteger q = BigInteger.probablePrime(KEY_SIZE / 2, random);
    BigInteger n = p.multiply(q);
    BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    BigInteger e = BigInteger.valueOf(65537); // Valor comumente utilizado

    // Verificar se e é coprimo com φ(n)
    while (!phi.gcd(e).equals(BigInteger.ONE)) {
      e = e.add(BigInteger.ONE);
    }

    BigInteger d = e.modInverse(phi);

    publicKey = new PublicKey(n, e);
    privateKey = new PrivateKey(n, d);
  }

  public static void encryptFile(RandomAccessFile raf) throws IOException {
    generateKeyPair();
    // Ler os bytes do arquivo
    byte[] fileBytes = new byte[(int) raf.length()];
    raf.seek(0);
    raf.readFully(fileBytes);

    // Criptografar os bytes do arquivo
    BigInteger encryptedMessage = encrypt(fileBytes);

    // Salvar os bytes criptografados no arquivo
    raf.seek(0);
    raf.write(encryptedMessage.toByteArray());
  }

  public static void decryptFile(RandomAccessFile raf) throws IOException {
    raf.seek(0);
    // Ler os bytes criptografados do arquivo
    byte[] encryptedBytes = new byte[(int) raf.length()];
    raf.readFully(encryptedBytes);

    // Descriptografar os bytes criptografados
    byte[] decryptedBytes = decrypt(new BigInteger(encryptedBytes));

    // Salvar os bytes descriptografados no arquivo
    raf.seek(0);
    raf.write(decryptedBytes);
  }

  public static BigInteger encrypt(byte[] messageBytes) {
    BigInteger m = new BigInteger(messageBytes);

    // Cifrar c = m^e mod n
    return m.modPow(publicKey.getExponent(), publicKey.getModulus());
  }

  public static byte[] decrypt(BigInteger encryptedMessage) {
    // Decifrar m = c^d mod n
    BigInteger decrypted = encryptedMessage.modPow(privateKey.getExponent(), privateKey.getModulus());
    return decrypted.toByteArray();
  }

  private static class PublicKey {
    private final BigInteger modulus;
    private final BigInteger exponent;

    public PublicKey(BigInteger modulus, BigInteger exponent) {
      this.modulus = modulus;
      this.exponent = exponent;
    }

    public BigInteger getModulus() {
      return modulus;
    }

    public BigInteger getExponent() {
      return exponent;
    }
  }

  private static class PrivateKey {
    private final BigInteger modulus;
    private final BigInteger exponent;

    public PrivateKey(BigInteger modulus, BigInteger exponent) {
      this.modulus = modulus;
      this.exponent = exponent;
    }

    public BigInteger getModulus() {
      return modulus;
    }

    public BigInteger getExponent() {
      return exponent;
    }
  }
}
