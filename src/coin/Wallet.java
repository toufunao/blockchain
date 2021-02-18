package coin;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;       //私人签名
    public PublicKey publicKey;         //钱包地址

    public Map<String,TransactionOutput> UTXOs=new HashMap<>();

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen=KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec=new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec,random);

            KeyPair keyPair=keyGen.generateKeyPair();
            this.privateKey=keyPair.getPrivate();
            this.publicKey=keyPair.getPublic();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public float getBalance(){
        float total=0;
        for (Map.Entry<String,TransactionOutput> item:CoinTest.UTXOS.entrySet()){
            TransactionOutput UTXO=item.getValue();
            if (UTXO.isMine(publicKey)){
                UTXOs.put(UTXO.id,UTXO);
                total+=UTXO.value;
            }
        }
        return total;
    }


    public Transaction sendFunds(PublicKey _receiver,float value){
        if (getBalance()<value){
            System.out.println("#Not enough funds to send transaction. Transaction discarded.");
            return null;
        }
        List<TransactionInput> inputs=new ArrayList<>();
        float total=0;
        for (Map.Entry<String,TransactionOutput> item:UTXOs.entrySet()){
            TransactionOutput UTXO=item.getValue();
            total+=UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total>value)
                break;
        }
        Transaction transaction=new Transaction(publicKey,_receiver,value,inputs);
        transaction.generateSignature(privateKey);
        for (TransactionInput transactionInput:inputs){
            UTXOs.remove(transactionInput.transactionOutputId);
        }
        return transaction;
    }
}
