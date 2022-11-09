package com.dse.license;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class LicenseGeneration {

    public String Decryption(String encodedString) {
        String decodedString = "";
        try {
            // Read file contains private key
            Path temp = Files.createTempFile("priKey", ".bin");
            Files.copy(LicenseGeneration.class.getResourceAsStream("/license/priKey.bin"), temp, StandardCopyOption.REPLACE_EXISTING);
            FileInputStream fis = new FileInputStream(temp.toFile());

            byte[] b = new byte[fis.available()];
            fis.read(b);
            fis.close();

            // Tạo private key
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = factory.generatePrivate(spec);
            // Giải mã dữ liệu
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, priKey);
            byte[] decryptOut = c.doFinal(Base64.getDecoder().decode(encodedString));
            decodedString = new String(decryptOut, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return decodedString;
    }
}
