# AOS_Project_2
Implementation of Maekawa Distributed Mutual Exclussion Protocol

Source code file: Maekawa.java
Test code file: MutexTester.java

Launcher script for Source code: launcher.sh
command: ./launcher.sh <configFileName.txt> <NetID>

Clean-up script to kill all java jobs: cleanup.sh
command: ./cleanup.sh <configFileName.txt> <NetID>

Testing script file: testMutex.sh
command: ./testMutex.sh <configFileName.txt>

Steps to execute:

1. Run Launcher script

2. Run cleanup script once execution of source code is finished

3. All nodes will generate log-<nodeId>.out files.

4. Run testing script to evaluate is MutEx is achieved which uses all log files generated.

****Source code is self explainatory through all javadoc comments **** 

***For more details about requirements please read project2.pdf ***
 
