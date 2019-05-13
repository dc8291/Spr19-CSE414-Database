# CSE 414 Homework 6: Parallel Data Processing and Spark

**Objectives:**  To practice writing queries that are executed in a distributed manner. To learn about Spark and running distributed data processing in the cloud using AWS.

**Assigned date:** Friday, May 10, 2019.

**Due date:** Friday, May 24, 2019.

**What to turn in:**

A single Java file for entire assignment, `HW6.java` in the `submission` directory, along with the text outputs from AWS. A skeleton `HW6.java` has been provided for you in the starter-file. **Make sure you copy this over to `submission` before working on the assignment to avoid repo conflicts.** You should not change any of the method's signatures. You should not need to create any other class to complete this assignment, although you are free to do so.

**Resources:**

- [Spark programming guide](https://spark.apache.org/docs/2.4.0/rdd-programming-guide.html)
- [Spark Javadoc](https://spark.apache.org/docs/2.4.0/api/java/index.html)
- [Amazon web services EMR (Elastic MapReduce) documentation](https://aws.amazon.com/documentation/emr/)
- [Amazon S3 documentation](https://aws.amazon.com/documentation/s3/)
- [Small dataset for local testing](https://courses.cs.washington.edu/courses/cse344/17au/assets/flights_small.tar.gz) 
- Alvin Cheung's research group has been working on a tool called [Casper](https://courses.cs.washington.edu/courses/cse344/tools/casper/) that translates sequential Java to Hadoop/Spark. Feel free to use it to learn Spark syntax. This is entirely optional.

## Assignment Details

In this homework, you will be writing Spark and Spark SQL code, to be executed both locally on your machine and also using Amazon Web Services.

We will be using a similar flights dataset from HW2 and HW3. This time, however, we will be using the *entire* data dump from the [US Bereau of Transportation Statistics](https://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time), which consists of information about all domestic US flights from 1987 to 2011 or so.  

The data is stored in a columnar format called [Parquet](https://parquet.apache.org/) and is available publicly on Amazon [S3](https://aws.amazon.com/s3/). S3 is Amazon's cloud storage service. The data is around 4GB compressed (79GB uncompressed), and contains 162,212,419 rows. Hence you probably don't want to be crunching on this dataset using your laptop! Each row contains 109 different attributes (see `HW6.java` for details), although you will only need to use a few of them in this homework. Note that the attribute names are different from those in HW2/HW3, and are described at the end of your HW java file you will work in as a giant block comment.

To help you debug, we provide a subset of the data on [the course website](https://courses.cs.washington.edu/courses/cse344/17au/assets/flights_small.tar.gz). This dataset is a dump of all the flights that happened on November 4, 2010. Remember that this is in [Parquet](https://parquet.apache.org/) format, not CSV as in HW2 and HW3. **We STRONGLY encourage you to run your code using this small dataset locally before trying it out on AWS.** See instructions below.

Here is a rough outline on how to do the HW:

- Sign up for AWS
- Apply for credit for AWS
- Complete the HW locally
- Run your solutions on AWS Elastic MapReduce one at a time when you are fairly confident with your solutions

### A. Sign up on Amazon Web Services

Follow these steps to set up your Amazon Web Services account.

1. If you do not already have an Amazon account, go to [their website](http://aws.amazon.com/) and sign up. Note: Amazon will ask you for your credit card information during the setup process. *If you follow these instructions and do local testing first you should NOT need to spend any money.* Sign in to your AWS console, go to "Support -> Support center" in the navigation bar, and locate your *account number*.
2. To get $ to use Amazon AWS, you must apply for credits by going to their [education website](https://aws.amazon.com/education/awseducate/apply/). *You must use your UW email address, <your_uwid>@uw.edu, when registering for the credits, as they use this to verify your identity.* Leave the promo code blank, and enter your AWS account number on the next page. Make sure you *do not* check the starter account option on the final page as that has limited permissions which may cause problems.
3. After applying, you will have to wait to be approved. You should get an email when your application has been approved, which gives you a credit code. Make sure you check the spam folder. Once you have it, go to [the AWS website](http://aws.amazon.com/awscredits/) and apply the credit. We have no control over how long this can take, but was told it can range from seconds to days. Hence, it is crucial that you apply ASAP!

**IMPORTANT: If you exceed the credit you are given, Amazon will charge your credit card without warning. If you run AWS in any other way rather than how we instruct you to do so below, you must remember to manually terminate the AWS clusters when you are done. While the credit that you receive should be more than enough for this homework assignment, but you should still monitor your billing usage by going to [their billing website](https://console.aws.amazon.com/billing/home) and clicking on "Bills" (upper left).**

You should get $100 from AWS once your application is approved. The credits that you have left over after the assignment are for you to keep, but *if you exceeded the credits due to forgetting to turn off your clusters, mining bitcoins, etc. then you will be responsible for paying the extra bill.*

Now you are ready to run applications using Amazon cloud. But before you do that let's write some code and run it locally.

### B. Download Spark

Go to `https://spark.apache.org/downloads.html`. Choose Spark release 2.4.0, package type "Pre-built for Apache Hadoop 2.7 and later". Download the tgz file.

Unzip the tgz file somewhere in your computer. The rest of the document assumes you place the spark directory in `/usr/local`; update the links if not.

Here are [some hints](https://guendouz.wordpress.com/2017/07/18/how-to-install-apache-spark-on-windows-10) in case you like to install Spark on your own Windows machine.

### C. Run Code Locally

Use your favorite text editor/IDE and open `HW6.java` to check out the code. You have provided an example method, `warmup`, that you can run after compiling the code. To do this, add all the jar files that come with Spark to classpath as follows:

Compile:

```sh
javac -cp "/usr/local/spark-2.4.0-bin-hadoop2.7/jars/*" HW6.java
```

Run:

```sh
java -cp "/usr/local/spark-2.4.0-bin-hadoop2.7/jars/*:." HW6 <path to the flights data directory> <path to output directory where you want the output to be stored>
```

Some notes on modifying the above commands:

- Make sure that you use the fully-qualified directory path (starting from root, "/" on Mac, "C:/" on Windows) if you use some other path.
- Use ";" instead of ":" as the classpath separator on Windows

If you are using an IDE:

- For Eclipse, you can do the same thing by right clicking on the project name → "Build Path" → "Configure Build Path" → Libraries → "Add External JARs". To run you program with arguments, go to "Run" → "Run Configurations".
- For IntelliJ, go to File → Project Structure → Libraries → "+" to add the folder containing your Spark JARs.

You will likely get some warning messages such as `WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable`. You can safely ignore those.
  
The output will be stored in a file (called `part-XXXXX`) in the output directory you specified (the directory structure may depend on your platform). That is just a plain text file that you can open that file with any text editor. **You will get an error if the output directory already exists, so delete it before running your program.** Be sure you read through the code and understand what is going on.

### D. Run Code on Elastic Map Reduce (EMR)

We will use Amazon's [Elastic Map Reduce](https://aws.amazon.com/emr/) (EMR) to deploy our code on AWS. Follow these steps to do so after you have set up your account, received credits as mentioned above, and have tested your solution locally. *Read this carefully!*

1. Toggle the `SparkSession` initialization on line 32-36 of `HW6.java` to allow it to run on AWS. Then create a jar file containing your class files. On the home VM you can do this with:

    ```sh
    jar cf HW6.jar *.class
    ```

    This creates `HW6.jar` that includes all `.class` files inside your current directory.

2. Login to [S3](https://s3.console.aws.amazon.com/s3/home) and create a "bucket." S3 is Amazon's cloud storage service, and a bucket is similar to a folder. Give your bucket a meaningful name, and leave the settings as default. Upload the jar file that you created in Step 1 to that bucket by selecting that file from your local drive and click "Upload" once you have selected the file.

3. Login to [EMR](https://console.aws.amazon.com/elasticmapreduce/home?region=us-east-1). Make sure you select `US East (N. Virginia)` or `US East (Ohio)` on the upper right. This is because the full, public data file that we will use is stored there, so it will be faster to access from a machine located elsewhere in the world.

4. We will now launch a three node m4.large EMR cluster (default) with Spark to execute the code. m4.large is an "instance type" on AWS, which is analogous to some set of allocated resources on a server. Go to the **Create Cluster** – **Advanced Options** in the Amazon EMR console. Scroll down to the **Software Configuration** section to add Spark as an application. Select the proper options and make sure that your screen looks like this:

    <img src="https://courses.cs.washington.edu/courses/cse344/17au/assets/hw6-createCluster.png" width="700"/>

    If m4.large is not available (unlikely), choose another instance size with a similar name (m4.xlarge, m5.large, etc.). You can find out about other instances on the [AWS website](https://aws.amazon.com/ec2/instance-types/).

5. This is the big one! Next, scroll to the **Steps** section near the bottom of the page and select **Spark application** and click **Configure**. A "step" is a single Spark job to be executed. You can specify multiple Spark jobs to be executed one after another in a cluster. Add a Spark application step by filling in the textboxes so that your screen looks like this:

    <img src="https://courses.cs.washington.edu/courses/cse344/17au/assets/hw6-step.png" width="700"/>

    Again for copy and paste:

    ```txt
    --class HW6

    s3://us-east-1.elasticmapreduce.samples/flightdata/input
    ```

    The `--class` option under "Spark-submit options" tells Spark where your `main` method lives (in this case inside `HW6.class`).

    The "Application location" should just point to where your uploaded jar file is. You can use the folder button to navigate.

    Make sure you fill out the correct bucket names. There are two arguments listed (and separated by a space, as if you were running the program locally):

    - The first one specifies where the input data file directory is.
    - The second one specifies where you want the output to be written. EMR can only write to a bucket, but you can download the output from the bucket afterwards. Use the same bucket where you uploaded the HW6 jar file, but specify a new folder like `s3://<your bucket containing the jar file>/output`.

    Change **Action on failure** to **Terminate cluster** (or else you will need to terminate the cluster manually). Then click **Add**.

6. Back to the main screen, now check the **Auto-terminate cluster after the last step is completed** option at the bottom of the page, so the cluster will automatically shut down once your Spark application is finished. Click **Next**.

7. On the next screen you get to choose how many machines you want in your cluster. For this assignment **1 master instance and 2 core (i.e., worker) instances of m4.large should be enough**. You are free to add more or pick other types, but make sure you think about the price tag first... Grabbing 100 machines at once will probably drain your credit in a snap :(. Click **Next**.

8. Under **General Options** uncheck the **Termination protection** option. We recommend that you allow the default logging information in case you need to debug a failure. Click **Next**.

9. You can optionally create an [EC2 pair](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html) if you want to ssh into the machines you are about to create.

10. Click **Create cluster** once you are done and your cluster will start spinning up!

It will take a bit for AWS to both start the machines and run your Spark job. As a reference, it took 10 mins to run the `warmup` job on EMR using 1 master and 2 core nodes. You can monitor the status of your cluster on the EMR homepage.

Use the "Clone" cluster button to copy the settings into a new job when you run your actual HW problems. **Each time you want to run a new EMR job, make sure to recompile your solution and pack it into a jar for uploading into S3!**

**Make sure you terminate the cluster!** It should do so if you selected the options above. You should check this each time you look at the HW, just to make sure you don't get charged for leaving a cluster running.

You can now check the output by clicking on the bucket you have created on S3. Text written to standard output, if any (e.g., from `System.out.println`), are located in the `containers/application<ID>/container<ID>/stdout.gz`, if you have enabled logging when you launched the EMR cluster.

It's fine if you see warning (or even occasional error) messages in the logs. If your EMR job finishes successfully, you should see something similar to the below in the main EMR console screen:

<img src="https://courses.cs.washington.edu/courses/cse344/17au/assets/hw6-success.png" width="700"/>

#### Debugging AWS jobs

Debugging AWS jobs is not easy for beginners. Besides making sure your program works locally before running on AWS, here are some general tips:

- Make sure that you set the job details (i.e., options, arguments, bucket names, etc) correctly!
- Make sure you switched the two lines of code mentioned to run your job on AWS instead of locally.
- Make sure you replace your jar to test a new solution!
- **99% of cluster failures or errors are due to the first three points!**
- The easiest way to debug is to look at the output/logging files. Spark generates a lot of log files, the most useful ones are probably the `stderr.gz` files listed under `containers/application.../container/stderr.gz`. You will have one `container` folder per machine instance. So make sure you check all folders. You should also check out the log files that you have specified when you created the job in Step 8 above. You can also see the names of those files listed as "Log File" under "Steps":

    <img src="starter-code/debug.png" width="700"/>
- It is rare that your HW solution is fine but the cluster fails. This is usually due to AWS not being able to grab your machines due to the demand for the instance type saturating the supply available. If you can't find available instances in a region, try changing to a different *EC2 subnet*, like so:

    <img src="starter-code/subnet.png" width="700"/>
- Spark have a web UI that you can set up to check on job progress etc. You can check out [their webpage](http://docs.aws.amazon.com/emr/latest/ManagementGuide/emr-web-interfaces.html) for details. But these are more involved so you are probably better off to first try examining the logs.  Specifically, try the "Application History" tab and the dropdown.

#### IMPORTANT: Cleanup after completing the HW

Double check that the clusters you have created are all terminated.

S3 charges by [downloading/uploading data from/to the buckets](https://aws.amazon.com/s3/pricing/). So once you are done with the assignment you might want to delete all the buckets that you have created (in addition to shutting down any EMR clusters that you have created).

The amount you are allocated from Amazon should be more than enough to complete the assignment. And every year we have students forgetting to shut down their cluster/clean up their buckets and that can result in substantial charges that they need to pay out of pocket. So be warned!

### E. Problems

We have created empty method bodies for each of the questions below (`Q1`, `Q2`, etc). **Do NOT change any of the method signatures.** You are free to define extra methods and classes if you need to. Run all of the following problems on the full dataset.

Except for problem 1, you should use methods like `filter` and `mapToPair` from the [JavaRDD API](https://spark.apache.org/docs/2.4.0/api/java/index.html?org/apache/spark/api/java/JavaRDD.html). We recommend looking at `reduceByKey` and `toRDD` from [JavaPairRDD](https://spark.apache.org/docs/2.4.0/api/java/index.html?org/apache/spark/api/java/JavaPairRDD.html), and `toJavaRDD` from [JavaRDD](https://spark.apache.org/docs/2.4.0/api/java/org/apache/spark/rdd/RDD.html) classes to implement your solution. **Save the output from EMR to `q1.txt`, `q2.txt` etc and make sure you add those to your `submission` directory on gitlab**.

Going forward, remember that objects like `JavaRDD` are immutable like Java Strings. This means that doing something like `d.filter(...)` does not change `d` but rather returns the result of doing `filter(...)` on `d`.

1. Select all flights that leave from Seattle, WA, and return the destination city names. Only return each destination city name once. Implement this using the `Dataset` API. This should be trivial and is intended for you to learn about the `Dataset` API. You can either use the functional form (i.e., `join(...)`, `select(...)`) or write a SQL query using `SparkSession.sql`. Check the corresponding Spark Javadoc for the parameters and return values. Save the EMR output as `q1.txt` and add it to your repo. (10 points) [Result Size: 79 rows (50 rows on the small dataset), 10 mins on EMR]

    Hint: If you decide to write a SQL query, note that you should use single quotes inside your query for string literals (e.g., `'Seattle'`). Also, it does not matter what you name the output column as, since that information is not dumped to the output.

2. Implement the same query as above, but use the `RDD` API. We convert a `Dataset` to a `JavaRDD` by calling `javaRDD()` in the skeleton code. Save the EMR output as `q2.txt` and add it to your repo. (20 points) [Result Size: 79 rows (50 rows on the small dataset), 15 mins on EMR]

3. Find the number of non-cancelled flights per month that departs from each city, return the results in a RDD where the key is a pair (i.e., a `Tuple2` object), consisting of a `String` for the departing city name, and an `Integer` for the month. The value should be the number of non-cancelled flights. Save the EMR output as `q3.txt` and add it to your repo. (20 points) [Result Size: 4383 rows (281 rows on the small dataset), 17 mins on EMR] 

4. Find the name of the city that is connected to the most number of other cities with direct flights. Return the result as a pair that consists of a `String` for the city name, and an `Integer` for the *total number of flights* (not cities) to the other cities. Save the EMR output as `q4.txt` and add it to your repo. (25 points) [Result Size: 1 row, 19 mins on EMR]

    Hint: [check out this post](https://stackoverflow.com/questions/35389219/task-not-serializable-exception-in-spark-while-calling-javapairrdd-max) if you get a "Task Not Serializable exception in Spark" exception.

5. Compute the average delay from all departing flights for each city. Flights with NULL delay values (due to cancellation or otherwise) should not be counted. Return the results in a RDD where the key is a `String` for the city name, and the value is a `Double` for the average delay in minutes. Save the EMR output as `q5.txt` and add it to your repo. (25 points) [Result Size: 383 rows (281 rows on the small dataset), 17 mins on EMR]

## Submission Instructions

Turn in your `q1.txt`, `q2.txt`, ..., `HW6.java` and any other Java files that you created.

**Important**: To remind you, in order for your answers to be added to the git repo, you need to explicitly add each file:

```sh
git add HW6.java ...
```

**Again, just because your code has been committed on your local machine does not mean that it has been submitted -- it needs to be on GitLab!**

Use the same bash script `turnIn_Hw6.sh` in the root level directory of your repository that commits your changes, deletes any prior tag for the current lab, tags the current commit, and pushes the branch and tag to GitLab.

If you are using Linux or Mac OSX, you should be able to run the following:

```sh
./turnIn_Hw6.sh
```

Like previous assignments, make sure you check the results afterwards to make sure that your file(s) have been committed.
