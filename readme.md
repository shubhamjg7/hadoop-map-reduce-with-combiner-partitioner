			
# Commands to run
			

### To copy standalone jar from HDFS to EMR FS:
`hadoop fs -copyToLocal /user/s3806186/Assignment1-0.0.1-SNAPSHOT.jar .`

### To run "Count words by lengths" using standalone jar:
`hadoop jar Assignment1-0.0.1-SNAPSHOT.jar com.rmit.student.s3806186.bigdata.Assignment1.WordByLength /user/s3806186/input/ /user/s3806186/output1 ./`

### To run "Count words by the first character" using standalone jar:
`hadoop jar Assignment1-0.0.1-SNAPSHOT.jar com.rmit.student.s3806186.bigdata.Assignment1.WordByVowelOrConsonant /user/s3806186/input/ /user/s3806186/output2 ./`

### To run "Count word with in-mapper combining" using standalone jar:
`hadoop jar Assignment1-0.0.1-SNAPSHOT.jar com.rmit.student.s3806186.bigdata.Assignment1.WordByCountInMapCombine /user/s3806186/input/ /user/s3806186/output3 ./`

### To run "Count word with partitioner" using standalone jar:
`hadoop jar Assignment1-0.0.1-SNAPSHOT.jar com.rmit.student.s3806186.bigdata.Assignment1.WordByLengthPartitioned /user/s3806186/input/ /user/s3806186/output4 ./`


# Performance  analysis

### Performance analysis using different number of nodes in EMR cluster and one :
	Input file used:
	s3a://commoncrawl/crawl-data/CC-MAIN-2018-17/segments/1524125936833.6/wet/CC-MAIN-20180419091546-20180419111546-00004.warc.wet.gz

	With 3 nodes:
	CPU_MILLISECONDS
		MAP		: 130080
		REDUCE	: 2860
		TOTAL	: 132940
	
	With 5 nodes:
	CPU_MILLISECONDS
		MAP		: 134780
		REDUCE	: 3050
		TOTAL	: 137830
	
	With 7 nodes:
	CPU_MILLISECONDS
		MAP		: 131091
		REDUCE	: 2986
		TOTAL	: 133824
		
### Explanation for the results:

For analysis the input file available at "s3a://commoncrawl/crawl-data/CC-MAIN-2018-17/segments/1524125936833.6/wet/CC-MAIN-20180419091546-20180419111546-00004.warc.wet.gz" was used. 
I extracted the file and used it as input for a map-reduce program which counts number of words of lengths between 1-4, 5-7, 8-10 and >11 in the file. 
The job was run three times using 3, 5 and 7 nodes and CPU_MILLISECONDS reading was recorded for each task run.

When comparing the CPU_MILLISECONDS with nodes 3, 5 and 7 the performance difference is not significantly different. 
With the file in consideration the number of map and reduce tasks created will be less. 
This is because the size of input file is 110 MB which is not big enough to utilize the power of all the added nodes. 
Also, the overhead of creating mapper and reducer tasks, identifying and delegating the created tasks to the available nodes and coordinating the entire execution is still present. 
Given the size of input file the execution time will be very small compared to the earlier mentioned overheads of running it using map-reduce approach. 
To harness the true power of Hadoop we need files which are in GB’s stored in HDFS in a distributed fashion across multiple nodes. 
Hadoop manages to reduce the execution by running map and reduce tasks parallelly across these nodes. 
Also, the nodes work on data chunks available locally to them so there is minimal data movement. 
Data movement is the most time-consuming task when processing huge files as the time taken to move them around using network infrastructure is slow. 
Hadoop manages to avoid this data movement by making sure that nodes process local data whenever possible.
