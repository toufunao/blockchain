package coin;

import chain.Encrypt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey receiver;
    public float value;
    public byte[] signature;

    public List<TransactionInput> input=new ArrayList<>();
    public List<TransactionOutput> output=new ArrayList<>();

    private static int sequence=0;

    public Transaction(PublicKey sender, PublicKey receiver, float value,List<TransactionInput> input) {
        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
        this.input=input;
    }

    private String calculateHash(){
        sequence++;
        return Encrypt.sha256Encryption(Encrypt.getStringFromKey(sender)+Encrypt.getStringFromKey(receiver)+Float.toString(value)+sequence);
    }

    public void generateSignature(PrivateKey privateKey){
        String data=Encrypt.getStringFromKey(sender)+Encrypt.getStringFromKey(receiver)+Float.toString(value);
        signature=Encrypt.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature(){
        String data=Encrypt.getStringFromKey(sender)+Encrypt.getStringFromKey(receiver)+Float.toString(value);
        return Encrypt.verifyECDSASig(sender,data,signature);
    }

    public boolean processTransaction(){

        if (verifySignature()==false){
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
        for (TransactionInput i:input){
            i.UTXO=CoinTest.UTXOS.get(i.transactionOutputId);
        }

        if(getInputValue()<CoinTest.minimumTransaction){
            System.out.println("#Transaction inputs to small: "+getInputValue());
            return false;
        }

        float leftOver=getInputValue()-value;
        transactionId=calculateHash();
        output.add(new TransactionOutput(this.receiver,value,transactionId));
        output.add(new TransactionOutput(this.sender,leftOver,transactionId));

        for (TransactionOutput transactionOutput:output){
            CoinTest.UTXOS.put(transactionOutput.id,transactionOutput);
        }

        for (TransactionInput transactionInput:input){
            if (transactionInput.UTXO==null)
                continue;
            CoinTest.UTXOS.remove(transactionInput.UTXO.id);
        }
        return true;
    }

    public float getInputValue() {
        float total=0;
        for (TransactionInput transactionInput:input){
            if (transactionInput.UTXO==null)
                continue;
            total+=transactionInput.UTXO.value;
        }
        return total;
    }

    public float getOutputValue(){
        float total=0;
        for (TransactionOutput transactionOutput:output){
            total+=transactionOutput.value;
        }
        return total;
    }
}
