package com.ideas2it.aes256;

import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class used to perform AES encryption and decryption.
 */
public class AES256 extends CordovaPlugin {

    private static final String ENCRYPT = "encrypt";
    private static final String DECRYPT = "decrypt";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    private static final int PBKDF2_ITERATION_COUNT = 1001;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String PBKDF2_SALT = "hY0wTq6xwc6ni01G";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            String secureKey = args.getString(0);
            String iv = args.getString(1);
            String value = args.getString(2);
            if (ENCRYPT.equalsIgnoreCase(action)) {
                callbackContext.success(encrypt(secureKey, value, iv));
                return true;
            } else if (DECRYPT.equalsIgnoreCase(action)) {
                callbackContext.success(decrypt(secureKey, value, iv));
                return true;
            } else {
                callbackContext.error("Invalid method call");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error occurred while performing " + action + " : " + e.getMessage());
            callbackContext.error("Error occurred while performing " + action);
        }
        return false;
    }

    /**
     * <p>
     * To perform the AES256 encryption
     * </p>
     *
     * @param secureKey A 32 bytes string, which will used as input key for AES256 encryption
     * @param value     A string which will be encrypted
     * @param iv        A 16 bytes string, which will used as initial vector for AES256 encryption
     * @return AES Encrypted string
     * @throws Exception
     */
    private String encrypt(String secureKey, String value, String iv) throws Exception {
        byte[] pbkdf2SecuredKey = generatePBKDF2(secureKey.toCharArray(), PBKDF2_SALT.getBytes("UTF-8"),
                PBKDF2_ITERATION_COUNT, PBKDF2_KEY_LENGTH);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
        SecretKeySpec secretKeySpec = new SecretKeySpec(pbkdf2SecuredKey, "AES");

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encrypted = cipher.doFinal(value.getBytes());

        return Base64.encodeToString(encrypted, Base64.DEFAULT);

    }

    /**
     * <p>
     * To perform the AES256 decryption
     * </p>
     *
     * @param secureKey A 32 bytes string, which will used as input key for AES256 decryption
     * @param value     A 16 bytes string, which will used as initial vector for AES256 decryption
     * @param iv        An AES256 encrypted data which will be decrypted
     * @return AES Decrypted string
     * @throws Exception
     */
    private String decrypt(String secureKey, String value, String iv) throws Exception {
        byte[] pbkdf2SecuredKey = generatePBKDF2(secureKey.toCharArray(), PBKDF2_SALT.getBytes("UTF-8"),
                PBKDF2_ITERATION_COUNT, PBKDF2_KEY_LENGTH);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
        SecretKeySpec secretKeySpec = new SecretKeySpec(pbkdf2SecuredKey, "AES");

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] original = cipher.doFinal(Base64.decode(value, Base64.DEFAULT));

        return new String(original);
    }

    /**
     * @param password       The password
     * @param salt           The salt
     * @param iterationCount The iteration count
     * @param keyLength      The length of the derived key.
     * @return PBKDF2 secured key
     * @throws Exception
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/javax/crypto/spec/PBEKeySpec.html">
     * https://docs.oracle.com/javase/8/docs/api/javax/crypto/spec/PBEKeySpec.html</a>
     */
    public static byte[] generatePBKDF2(char[] password, byte[] salt, int iterationCount,
                                        int keyLength) throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        KeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey.getEncoded();
    }
}