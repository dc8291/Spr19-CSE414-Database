import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Collections;

public class HW6 {

  // the full input data file is at s3://us-east-1.elasticmapreduce.samples/flightdata/input

  /*
    You are free to change the contents of main as much as you want. We will be running a separate main for
     grading purposes.
   */
  public static void main(String[] args) {

    if (args.length < 2)
      throw new RuntimeException("Usage: HW6 <datafile location> <output location>");

    String dataFile = args[0];
    String output = args[1];

    // turn off logging except for error messages
    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR);
    Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR);

    // use this for running locally
    // SparkSession spark = SparkSession.builder().appName("HW6").config("spark.master", "local").getOrCreate();

    // use this for running on ec2
    // SparkSession spark = SparkSession.builder().appName("HW6").getOrCreate();

    // Dataset<Row> r = warmup(spark, dataFile);
    // r.javaRDD().repartition(1).saveAsTextFile(output);

    // uncomment each of the below to run your solutions

    /* Problem 1 */
    // Dataset<Row> r1 = Q1(spark, dataFile);

    // collect all outputs from different machines to a single partition, and write to the output
    // make sure the output location does not already exists (otherwise it will throw an error)
    // r1.javaRDD().repartition(1).saveAsTextFile(output);

    /* Problem 2 */
    // JavaRDD<Row> r2 = Q2(spark, dataFile);
    // r2.repartition(1).saveAsTextFile(output);

    /* Problem 3 */
    // JavaPairRDD<Tuple2<String, Integer>, Integer> r3 = Q3(spark, dataFile);
    // r3.repartition(1).saveAsTextFile(output);

    /* Problem 4 */
    // Tuple2<String, Integer> r4 = Q4(spark, dataFile);
    // spark.createDataset(Collections.singletonList(r4), Encoders.tuple(Encoders.STRING(), Encoders.INT()))
    //      .javaRDD().saveAsTextFile(output);

    /* Problem 5 */
    JavaPairRDD<String, Double> r5 = Q5(spark, dataFile);
    r5.repartition(1).saveAsTextFile(output);


    // this saves the results to an output file in parquet format
    // (useful if you want to generate a test dataset on an even smaller dataset)
    // r.repartition(1).write().parquet(output);

    // shut down
    spark.stop();
  }

  // offsets into each Row from the input data read
  public static final int MONTH = 2;
  public static final int ORIGIN_CITY_NAME = 15;
  public static final int DEST_CITY_NAME = 24;
  public static final int DEP_DELAY = 32;
  public static final int CANCELLED = 47;
  //
  // public static Dataset<Row> warmup (SparkSession spark, String dataFile) {
  //
  //   Dataset<Row> df = spark.read().parquet(dataFile);
  //
  //   // create a temporary table based on the data that we read
  //   df.createOrReplaceTempView("flights");
  //
  //   // run a SQL query
  //   Dataset<Row> r = spark.sql("SELECT f.origincityname FROM flights f LIMIT 10");
  //
  //   // this prints out the results
  //   r.show();
  //
  //   // this uses the RDD API to project a column from the read data and print out the results
  //   r.javaRDD()
  //    .map(t -> t.get(DEST_CITY_NAME))
  //    .foreach(t -> System.out.println(t));
  //
  //   return r;
  // }

  public static Dataset<Row> Q1 (SparkSession spark, String dataFile) {

    Dataset<Row> df = spark.read().parquet(dataFile);

    // your code here
    df.createOrReplaceTempView("flights");

    Dataset<Row> r = spark.sql("SELECT f.destcityname FROM flights f WHERE f.origincityname = 'Seattle, WA' GROUP BY f.destcityname");

    return r;
  }

  public static JavaRDD<Row> Q2 (SparkSession spark, String dataFile) {

    JavaRDD<Row> d = spark.read().parquet(dataFile).javaRDD();

    // your code here
    JavaRDD<Row> ans = d.filter(x -> x.get(ORIGIN_CITY_NAME).equals("Seattle, WA"))
                        .map(x -> RowFactory.create(x.get(DEST_CITY_NAME)))
                        .distinct();

    return ans;
  }

  public static JavaPairRDD<Tuple2<String, Integer>, Integer> Q3 (SparkSession spark, String dataFile) {

    JavaRDD<Row> d = spark.read().parquet(dataFile).javaRDD();

    // your code here
    /*
      1. Filter the intial RDD by not cancelled flights.
      2. Pair the flights by (<destcityname, month>, 1) // "1" indicates the number of occurrences
      3. Reduce the pair by key by adding up the values, hence the a+b.
         i.e. For every key, the values will be added up.
      4. The final result contains JavaPairRDD<Tuple2<destcityname, month>, #occurrences>
    */

    JavaPairRDD<Tuple2<String, Integer>, Integer> ans = d.filter(x -> x.getInt(CANCELLED) == 0)
                            .mapToPair(x -> new Tuple2<Tuple2<String, Integer>, Integer>(new Tuple2<String, Integer>(x.get(DEST_CITY_NAME).toString(), (x.getInt(MONTH))), 1))
                            .reduceByKey((a,b) -> a+b);

    return ans;
  }


  public static Tuple2<String, Integer> Q4 (SparkSession spark, String dataFile) {

    JavaRDD<Row> d = spark.read().parquet(dataFile).javaRDD();

    // your code here
     class q4Comparator implements
      Comparator<Tuple2 <String, Integer>>, Serializable {
        public int compare(Tuple2<String, Integer> x, Tuple2<String, Integer> y){
          return x._2 - y._2;
        }
      }
    /*
      Idea 1:
        1. Get a list of every origincity with distinct destcity
        2. Obtain the origincity with highest occurrences
        3. Filter initial RDD by the origincity
        4. count the total number of flights

      Idea 2:
        1. For each origincity get distinct destcity
        2. For each origincity, count the numbers of destcity
        3. Return origincity with max number
    */
    Tuple2<String, Integer> temp = d.map(x->RowFactory.create(x.get(ORIGIN_CITY_NAME).toString(), x.get(DEST_CITY_NAME).toString()))
                                     .distinct()
                                     .mapToPair(x -> new Tuple2<String, Integer>(x.get(0).toString(), 1))
                                     .reduceByKey((a,b)-> a+b)
                                     .max(new q4Comparator());

    long number = d.filter(x->x.get(ORIGIN_CITY_NAME).equals(temp._1))
                  .count();
    return new Tuple2<String, Integer>(temp._1, (int)number);
  }

  public static JavaPairRDD<String, Double> Q5 (SparkSession spark, String dataFile) {

    JavaRDD<Row> d = spark.read().parquet(dataFile).javaRDD();

    // your code here
    /*
      1. Filter out flights with NULL values
      2. Create PairRDD with <origincity, Tuple2<DEP_DELAY, 1>>
      3. reduceByKey (a,b) - > new Tuple2<Integer, Integer>(a._1 + b._1, a._2 + b._2)
      4. reMapToPair by creating new Tuples
    */

    JavaPairRDD<String, Tuple2<Integer, Integer>> temp = d.filter(x -> x.get(DEP_DELAY) != null)
                                                          .mapToPair(x-> new Tuple2<String, Tuple2<Integer, Integer>>(x.get(ORIGIN_CITY_NAME).toString(), new Tuple2<Integer, Integer>(x.getInt(DEP_DELAY), 1)))
                                                          .reduceByKey((a,b) -> new Tuple2<Integer, Integer>(a._1 + b._1, a._2 + b._2));
    JavaPairRDD<String, Double> ans = temp.mapToPair(x -> new Tuple2<String, Double>(x._1, x._2._1*1.0/x._2._2));
    return ans;
  }

  /* We list all the fields in the input data file for your reference
  root
 |-- year: integer (nullable = true)   // index 0
 |-- quarter: integer (nullable = true)
 |-- month: integer (nullable = true)
 |-- dayofmonth: integer (nullable = true)
 |-- dayofweek: integer (nullable = true)
 |-- flightdate: string (nullable = true)
 |-- uniquecarrier: string (nullable = true)
 |-- airlineid: integer (nullable = true)
 |-- carrier: string (nullable = true)
 |-- tailnum: string (nullable = true)
 |-- flightnum: integer (nullable = true)
 |-- originairportid: integer (nullable = true)
 |-- originairportseqid: integer (nullable = true)
 |-- origincitymarketid: integer (nullable = true)
 |-- origin: string (nullable = true)   // airport short name
 |-- origincityname: string (nullable = true) // e.g., Seattle, WA
 |-- originstate: string (nullable = true)
 |-- originstatefips: integer (nullable = true)
 |-- originstatename: string (nullable = true)
 |-- originwac: integer (nullable = true)
 |-- destairportid: integer (nullable = true)
 |-- destairportseqid: integer (nullable = true)
 |-- destcitymarketid: integer (nullable = true)
 |-- dest: string (nullable = true)
 |-- destcityname: string (nullable = true)
 |-- deststate: string (nullable = true)
 |-- deststatefips: integer (nullable = true)
 |-- deststatename: string (nullable = true)
 |-- destwac: integer (nullable = true)
 |-- crsdeptime: integer (nullable = true)
 |-- deptime: integer (nullable = true)
 |-- depdelay: integer (nullable = true)
 |-- depdelayminutes: integer (nullable = true)
 |-- depdel15: integer (nullable = true)
 |-- departuredelaygroups: integer (nullable = true)
 |-- deptimeblk: integer (nullable = true)
 |-- taxiout: integer (nullable = true)
 |-- wheelsoff: integer (nullable = true)
 |-- wheelson: integer (nullable = true)
 |-- taxiin: integer (nullable = true)
 |-- crsarrtime: integer (nullable = true)
 |-- arrtime: integer (nullable = true)
 |-- arrdelay: integer (nullable = true)
 |-- arrdelayminutes: integer (nullable = true)
 |-- arrdel15: integer (nullable = true)
 |-- arrivaldelaygroups: integer (nullable = true)
 |-- arrtimeblk: string (nullable = true)
 |-- cancelled: integer (nullable = true) Cancelled = 1
 |-- cancellationcode: integer (nullable = true)
 |-- diverted: integer (nullable = true)
 |-- crselapsedtime: integer (nullable = true)
 |-- actualelapsedtime: integer (nullable = true)
 |-- airtime: integer (nullable = true)
 |-- flights: integer (nullable = true)
 |-- distance: integer (nullable = true)
 |-- distancegroup: integer (nullable = true)
 |-- carrierdelay: integer (nullable = true)
 |-- weatherdelay: integer (nullable = true)
 |-- nasdelay: integer (nullable = true)
 |-- securitydelay: integer (nullable = true)
 |-- lateaircraftdelay: integer (nullable = true)
 |-- firstdeptime: integer (nullable = true)
 |-- totaladdgtime: integer (nullable = true)
 |-- longestaddgtime: integer (nullable = true)
 |-- divairportlandings: integer (nullable = true)
 |-- divreacheddest: integer (nullable = true)
 |-- divactualelapsedtime: integer (nullable = true)
 |-- divarrdelay: integer (nullable = true)
 |-- divdistance: integer (nullable = true)
 |-- div1airport: integer (nullable = true)
 |-- div1airportid: integer (nullable = true)
 |-- div1airportseqid: integer (nullable = true)
 |-- div1wheelson: integer (nullable = true)
 |-- div1totalgtime: integer (nullable = true)
 |-- div1longestgtime: integer (nullable = true)
 |-- div1wheelsoff: integer (nullable = true)
 |-- div1tailnum: integer (nullable = true)
 |-- div2airport: integer (nullable = true)
 |-- div2airportid: integer (nullable = true)
 |-- div2airportseqid: integer (nullable = true)
 |-- div2wheelson: integer (nullable = true)
 |-- div2totalgtime: integer (nullable = true)
 |-- div2longestgtime: integer (nullable = true)
 |-- div2wheelsoff: integer (nullable = true)
 |-- div2tailnum: integer (nullable = true)
 |-- div3airport: integer (nullable = true)
 |-- div3airportid: integer (nullable = true)
 |-- div3airportseqid: integer (nullable = true)
 |-- div3wheelson: integer (nullable = true)
 |-- div3totalgtime: integer (nullable = true)
 |-- div3longestgtime: integer (nullable = true)
 |-- div3wheelsoff: integer (nullable = true)
 |-- div3tailnum: integer (nullable = true)
 |-- div4airport: integer (nullable = true)
 |-- div4airportid: integer (nullable = true)
 |-- div4airportseqid: integer (nullable = true)
 |-- div4wheelson: integer (nullable = true)
 |-- div4totalgtime: integer (nullable = true)
 |-- div4longestgtime: integer (nullable = true)
 |-- div4wheelsoff: integer (nullable = true)
 |-- div4tailnum: integer (nullable = true)
 |-- div5airport: integer (nullable = true)
 |-- div5airportid: integer (nullable = true)
 |-- div5airportseqid: integer (nullable = true)
 |-- div5wheelson: integer (nullable = true)
 |-- div5totalgtime: integer (nullable = true)
 |-- div5longestgtime: integer (nullable = true)
 |-- div5wheelsoff: integer (nullable = true)
 |-- div5tailnum: integer (nullable = true)
   */
}
