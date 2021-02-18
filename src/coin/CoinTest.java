package coin;

import chain.Block;
import chain.Encrypt;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoinTest {
    public static List<Block> blockChain=new ArrayList<>();
    public static HashMap<String,TransactionOutput> UTXOS=new HashMap<>();
    public static int difficulty=3;
    public static float minimumTransaction=0.1f;


    public static Wallet buyer;
    public static Wallet seller;

    public static Transaction genesisTransaction;
    public static void main(String[] args){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        buyer=new Wallet();
        seller=new Wallet();
        Wallet coinBase=new Wallet();

        genesisTransaction=new Transaction(coinBase.publicKey, buyer.publicKey,100f,null);
        genesisTransaction.generateSignature(coinBase.privateKey);
        genesisTransaction.transactionId="0";
        genesisTransaction.output.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value,genesisTransaction.transactionId));
        UTXOS.put(genesisTransaction.output.get(0).id,genesisTransaction.output.get(0));

        System.out.println("creating and mining genesis block...");
        Block genesis=new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        Block block1=new Block(genesis.hash);
        System.out.println("\nbuyer's balance is: "+buyer.getBalance());
        System.out.println("\nbuyer ->(40)-> seller");
        block1.addTransaction(buyer.sendFunds(seller.publicKey,40f));
        addBlock(block1);

        Block block2 = new Block(block1.hash);
        System.out.println("\nbuyer Attempting to send more funds (1000) than it has...");
        block2.addTransaction(buyer.sendFunds(seller.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nbuyer's balance is: " + buyer.getBalance());
        System.out.println("seller's balance is: " + seller.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nseller is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(seller.sendFunds( buyer.publicKey, 20));
        System.out.println("\nbuyer's balance is: " + buyer.getBalance());
        System.out.println("seller's balance is: " + seller.getBalance());

        isChainValid();



    }

    private static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.output.get(0).id, genesisTransaction.output.get(0));

        //loop through blockchain to check hashes:
        for(int i=1; i < blockChain.size(); i++) {

            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputValue() != currentTransaction.getOutputValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.input) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.output) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.output.get(0).receiver != currentTransaction.receiver) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.output.get(1).receiver != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    private static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }
}
