package chain;

import coin.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Encrypt {
    public static String sha256Encryption(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    stringBuffer.append('0');

                stringBuffer.append(hex);
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] applyECDSASig(PrivateKey privateKey,String input){
        Signature dsa;
        byte[] output=new byte[0];

        try {
            dsa=Signature.getInstance("ECDSA","BC");
            dsa.initSign(privateKey);
            byte[] strByte=input.getBytes();
            dsa.update(strByte);
            byte[] realSig=dsa.sign();
            output=realSig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static boolean verifyECDSASig(PublicKey publicKey,String data,byte[] signature){
        try {
            Signature ecdsaVerify=Signature.getInstance("ECDSA","BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMarketRoot(List<Transaction> transactions){
        int count=transactions.size();
        List<String> previousTreeLayer=new ArrayList<>();
        for (Transaction transaction:transactions){
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer=previousTreeLayer;
        while (count>1){
            treeLayer=new ArrayList<>();
            for (int i=1;i<previousTreeLayer.size();i++){
                treeLayer.add(sha256Encryption(previousTreeLayer.get(i-1)+previousTreeLayer.get(i)));
            }
            count=treeLayer.size();
            previousTreeLayer=treeLayer;
        }
        String merkleRoot=(treeLayer.size()==1)?treeLayer.get(0):"";
        return merkleRoot;
    }

    public static String getDifficultyString(int difficulty) {
        return Integer.toString(difficulty);
    }
}
