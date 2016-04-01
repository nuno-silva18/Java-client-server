SDIS Serverless Distributed Backup

Work by Pedro Albano & Nuno Silva

----------------------------------
Compiling Instructions
----------------------------------
To compile both applications on unix/linux platforms,
there is a file named 'compile.sh' that will perform the required
shell commands in order to compile the class files.

Simply 'cd' into the application's directory and run:
$sh compile.sh

For compiling manually or on other platforms simply replicate
the 'javac' commands present in these files.

----------------------------------
Running Instructions
----------------------------------
To run both applications cd into their directory and run the following commands:

For the Peer Application:
$java sdis.Peer <peer_id> <mc_address> <mc_port> <mdb_address> <mdb_port> <mdr_address> <mdr_port>

For the Interface Application
$java sdis.TestApp [peer_address][:]<peer_id> <subprotocol> <operand1> [<operand2>]

In case of invalid or imcomplete inputs for the command line arguments the
applications will both respectively print their usage guides.
