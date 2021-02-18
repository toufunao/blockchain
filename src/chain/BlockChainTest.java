package chain;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class BlockChainTest {
    public static List<Block> blockchain=new ArrayList<>();
    public static int difficulty=5;
    public static void main(String[] args){
        blockchain.add(new Block("first block","0"));
        System.out.println("mining block 1: >>>>>>>>>>>>>>>>>>>>>");
        blockchain.get(0).mineBlock(difficulty);

        blockchain.add(new Block("second block",blockchain.get(blockchain.size()-1).hash));
        System.out.println("mining block 2: >>>>>>>>>>>>>>>>>>>>>");
        blockchain.get(1).mineBlock(difficulty);


        blockchain.add(new Block("third block",blockchain.get(blockchain.size()-1).hash));
        System.out.println("mining block 3: >>>>>>>>>>>>>>>>>>>>>");
        blockchain.get(2).mineBlock(difficulty);

        System.out.println("\nblockchain is valid: "+isValid());

        String blockChain=new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nthe blockchain is: ");
        System.out.println(blockChain);
    }

    public static boolean isValid(){
        Block previousBlock;
        Block currentBlock;
        String hashTarget=new String(new char[difficulty]).replace('\n','0');

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock=blockchain.get(i);
            previousBlock=blockchain.get(i-1);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("current does not equal "+ i);
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("previous does not equal "+i);
                return false;
            }
            if (!currentBlock.hash.substring(0,difficulty).equals(hashTarget)){
                System.out.println("the block has been mined ");
                return false;
            }
        }
        return true;
    }
}
