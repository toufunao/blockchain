package chain;

import coin.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public List<Transaction> transactions=new ArrayList<>();
    public  String data;
    public long timeStamp;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp=new Date().getTime();
        this.hash=calculateHash();
    }

    public Block(String data, String previousHash) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculatedHash = Encrypt.sha256Encryption(previousHash + Long.toString(timeStamp)+Integer.toHexString(nonce) + merkleRoot);
        return calculatedHash;
    }

    public void mineBlock(int difficulty){
        merkleRoot=Encrypt.getMarketRoot(transactions);
        String target=Encrypt.getDifficultyString(difficulty);
        while (!hash.substring(0,difficulty).equals(target)){
            this.nonce++;
            hash=calculateHash();
        }
        System.out.println("block mined: "+hash);
    }

    public boolean addTransaction(Transaction transaction){
        if (transaction==null)
            return false;

        if (previousHash!="0"){
            if (!transaction.processTransaction()){
                System.out.println("Transaction failed to process.Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to block");
        return true;
    }
}
