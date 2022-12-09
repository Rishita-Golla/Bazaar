# Bazaar

Main branch - election-fault-tolerance

Running system

● Open the terminal
● Clone the repository :
https://github.com/Rishita-Golla/Bazaar/tree/election-fault-tolerance
● cd src
● Run the random network generation program.

Command:

Consider a configuration having 4 nodes. 2 buyers and 2 sellers.
● Ensure you are in the src directory. Run the Server side program for each seller node. Seller nodes: [1]
Open a new terminal.

cd src
javac Main.java
java Main 1 config{id}.txt networkConfig.txt salt

● Replicate this process for the Client side program which will bring the buyer nodes up. Buyer nodes: [2]
Open a new terminal.
cd src
javac Main.java
java Main 2 config{id}.txt networkConfig.txt salt
