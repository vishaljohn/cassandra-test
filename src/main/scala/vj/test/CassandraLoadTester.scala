package vj.test

import com.datastax.driver.core.policies.DefaultRetryPolicy
import com.datastax.driver.core.{Host, HostDistance, Cluster}
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement}
import com.datastax.driver.core.Session

import java.util.Date

/*
 * CREATE KEYSPACE testks WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};
 * USE testks;
 * CREATE TABLE table1 (key text, "count" counter, ts timestamp, PRIMARY KEY(key, ts)) WITH CLUSTERING ORDER BY (ts DESC); 
 * SELECT * FROM table1;
 * 
 */

object CassandraLoadTester {
  
  private val UpdateCountQuery = "UPDATE table1 SET count = count + ? WHERE key = ? AND ts = ?"

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println("Usage: CassandraLoadTester <numMessages> <numThreads>")
      System.exit(1)
    }
    val Array(numMessages, numThreads) = args.map(_.toInt)
    val builder = Cluster.builder().withPort(9042)
    builder.addContactPoint("localhost")
    builder.poolingOptions().setMaxConnectionsPerHost(HostDistance.LOCAL, 10)
    val cassandraSession = builder.build().connect("testks")
    val updateCountStatement = cassandraSession.prepare(UpdateCountQuery).setConsistencyLevel(ConsistencyLevel.ONE)
    
    val start = System.currentTimeMillis()
    println("Start timestamp " + start)

    val threads: Array[Thread] = new Array[Thread](numThreads)
    val msgsPerThread = numMessages/numThreads
    for (i <- 0 until numThreads) {
      threads(i) = new Thread(new Worker(cassandraSession, updateCountStatement, msgsPerThread))
      threads(i).start()
    }

    for (i <- 0 until numThreads) threads(i).join()

    val end = System.currentTimeMillis()
    println("End timestamp " + end)
    println("Total no. of messages pushed = " + numMessages)
    println("Time taken : " + (end - start) + " ms") 
    
  }

}

class Worker(cassandraSession: Session, statement: PreparedStatement, limit: Int) extends Runnable {
  
  override def run() {
    for(i <- 0 to limit) {
      val ts = System.currentTimeMillis()
      val secTs = getTimeBucket(ts, 1000)
      val boundUpdate = statement.bind(long2Long(1), "test:key", new Date(secTs))
      cassandraSession.executeAsync(boundUpdate)
    }
  }
  
  private def getTimeBucket(timestamp: Long, bucket: Long): Long = {
    timestamp - timestamp % bucket
  }
  
}