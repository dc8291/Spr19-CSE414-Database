import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

/**
 * Runs queries against a back-end database.
 * This class is responsible for searching for flights.
 */
public class QuerySearchOnly
{
  // `dbconn.properties` config file
  private String configFilename;

  // DB Connection
  protected Connection conn;

  // Map of Itineraries for Booking Function
  public HashMap<Integer, Itinerary> lastSearch = new HashMap<Integer, Itinerary>();

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  protected PreparedStatement checkFlightCapacityStatement;

  private static final String DIRECT_FLIGHTS = "SELECT TOP (?) fid,day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
          + "FROM Flights "
          + "WHERE origin_city = ? AND dest_city = ? AND day_of_month =  ? AND canceled = 0 "
          + "ORDER BY actual_time ASC, fid ASC";
  protected PreparedStatement directFlightStatement;

  private static final String INDIRECT_FLIGHTS = "SELECT TOP (?) "
          + "f1.fid as f1fid, f1.day_of_month as f1day_of_month, f1.carrier_id as f1carrier_id, f1.flight_num as f1flight_num,"
          + "f1.origin_city as f1origin_city, f1.dest_city as f1dest_city, f1.actual_time as f1actual_time, f1.capacity as f1capacity, f1.price as f1price,"
          + "f2.fid as f2fid, f2.day_of_month as f2day_of_month, f2.carrier_id as f2carrier_id, f2.flight_num as f2flight_num,"
          + "f2.origin_city as f2origin_city, f2.dest_city as f2dest_city, f2.actual_time as f2actual_time, f2.capacity as f2capacity, f2.price as f2price "
          + "FROM Flights f1, Flights f2 "
          + "WHERE f1.origin_city = ? AND f1.dest_city = f2.origin_city AND f2.dest_city = ? "
          + "AND f1.day_of_month = ? AND f1.day_of_month = f2.day_of_month AND f1.canceled = 0 AND f2.canceled = 0 "
          + "ORDER BY f1.actual_time + f2.actual_time ASC, f1.fid ASC, f2.fid ASC";
  protected PreparedStatement indirectFlightStatement;

  class Flight
  {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    @Override
    public String toString()
    {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }

  class Itinerary implements Comparable<Itinerary>
  {
    public Flight f1;
    public Flight f2;
    public int time;

    @Override
    public int compareTo(Itinerary x){
      return this.time - x.time;
    }
  }

  public QuerySearchOnly(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /** Open a connection to SQL Server in Microsoft Azure.  */
  public void openConnection() throws Exception
  {
    Properties configProps = new Properties();
    configProps.load(new FileInputStream(configFilename));

    String jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    String jSQLUrl = configProps.getProperty("flightservice.url");
    String jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    String jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

    /* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

    /* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement
    /* In the full Query class, you will also want to appropriately set the transaction's isolation level:
          conn.setTransactionIsolation(...)
       See Connection class's JavaDoc for details.
    */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);

    /* add here more prepare statements for all the other queries you need */
    /* . . . . . . */
    directFlightStatement = conn.prepareStatement(DIRECT_FLIGHTS);
    indirectFlightStatement = conn.prepareStatement(INDIRECT_FLIGHTS);
  }

  // Returns an ArrayList of direct flights with Flight class
  private ArrayList<Flight> directFlights(String originCity, String destinationCity, int dayOfMonth,
                                   int numberOfItineraries) throws SQLException
  {
    directFlightStatement.clearParameters();
    directFlightStatement.setInt(1, numberOfItineraries);
    directFlightStatement.setString(2, originCity);
    directFlightStatement.setString(3, destinationCity);
    directFlightStatement.setInt(4, dayOfMonth);
    ResultSet results = directFlightStatement.executeQuery();
    ArrayList<Flight> listOfFlights = new ArrayList<Flight>();
    while (results.next())
    {
      Flight temp = new Flight();
      temp.fid = results.getInt("fid");
      temp.dayOfMonth = results.getInt("day_of_month");
      temp.carrierId = results.getString("carrier_id");
      temp.flightNum = results.getString("flight_num");
      temp.originCity = results.getString("origin_city");
      temp.destCity = results.getString("dest_city");
      temp.time = results.getInt("actual_time");
      temp.capacity = results.getInt("capacity");
      temp.price = results.getInt("price");
      listOfFlights.add(temp);
    }
    results.close();
    return listOfFlights;
  }

  // Returns an ArrayList of indirect flights with Flight class
  // Each Itinerary consists of two Flights or two items in the ArrayList
  private ArrayList<Flight> indirectFlights(String originCity, String destinationCity, int dayOfMonth,
                                   int numberOfItineraries) throws SQLException
  {
    indirectFlightStatement.clearParameters();
    indirectFlightStatement.setInt(1, numberOfItineraries);
    indirectFlightStatement.setString(2, originCity);
    indirectFlightStatement.setString(3, destinationCity);
    indirectFlightStatement.setInt(4, dayOfMonth);
    ResultSet results = indirectFlightStatement.executeQuery();
    ArrayList<Flight> listOfFlights = new ArrayList<Flight>();
    while (results.next())
    {
      Flight temp = new Flight();
      temp.fid = results.getInt("f1fid");
      temp.dayOfMonth = results.getInt("f1day_of_month");
      temp.carrierId = results.getString("f1carrier_id");
      temp.flightNum = results.getString("f1flight_num");
      temp.originCity = results.getString("f1origin_city");
      temp.destCity = results.getString("f1dest_city");
      temp.time = results.getInt("f1actual_time");
      temp.capacity = results.getInt("f1capacity");
      temp.price = results.getInt("f1price");
      listOfFlights.add(temp);
      Flight temp2 = new Flight();
      temp2.fid = results.getInt("f2fid");
      temp2.dayOfMonth = results.getInt("f2day_of_month");
      temp2.carrierId = results.getString("f2carrier_id");
      temp2.flightNum = results.getString("f2flight_num");
      temp2.originCity = results.getString("f2origin_city");
      temp2.destCity = results.getString("f2dest_city");
      temp2.time = results.getInt("f2actual_time");
      temp2.capacity = results.getInt("f2capacity");
      temp2.price = results.getInt("f2price");
      listOfFlights.add(temp2);
    }
    results.close();
    return listOfFlights;
  }

  /*
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise it searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */

  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {
    // Please implement your own (safe) version that uses prepared statements rather than string concatenation.
    // You may use the `Flight` class (defined above).
    lastSearch.clear();
    StringBuffer sb = new StringBuffer();
    ArrayList<Flight> listOfFlights = new ArrayList<Flight>();
    ArrayList<Itinerary> listOfItinerary = new ArrayList<Itinerary>();
    try
    {
      listOfFlights = directFlights(originCity, destinationCity, dayOfMonth, numberOfItineraries);
      int counter = 0;
      for (int i = 0; i < listOfFlights.size(); i++)
      {
        Itinerary temp = new Itinerary();
        temp.f1 = listOfFlights.get(i);
        temp.f2 = null;
        temp.time = listOfFlights.get(i).time;
        listOfItinerary.add(temp);
        counter++;
      }
      int numberNeeded = numberOfItineraries - counter;
      if (!directFlight && numberNeeded > 0){
        listOfFlights = indirectFlights(originCity, destinationCity, dayOfMonth, numberNeeded);
        for (int i = 0; i < listOfFlights.size(); i+=2)
        {
          int totalTime = listOfFlights.get(i).time + listOfFlights.get(i + 1).time;
          Itinerary temp = new Itinerary();
          temp.f1 = listOfFlights.get(i);
          temp.f2 = listOfFlights.get(i+1);
          temp.time = totalTime;
          listOfItinerary.add(temp);
          counter++;
        }
      }
      if(counter == 0)
      {
        sb.append("No flights match your selection\n");
      } else {
        Collections.sort(listOfItinerary);
        for (int i = 0; i < listOfItinerary.size(); i++){
          Itinerary temp = listOfItinerary.get(i);
          lastSearch.put(i, temp);
          if (temp.f2 == null){
            sb.append("Itinerary " + i + ": 1 flight(s), " + temp.time + " minutes\n")
              .append(temp.f1.toString())
              .append('\n');
          } else {
            sb.append("Itinerary " + i + ": 2 flight(s), " + temp.time + " minutes\n")
                .append(temp.f1.toString())
                .append('\n')
                .append(temp.f2.toString())
                .append('\n');
          }
        }
      }
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: ").append(result_dayOfMonth)
                .append(" Carrier: ").append(result_carrierId)
                .append(" Number: ").append(result_flightNum)
                .append(" Origin: ").append(result_originCity)
                .append(" Destination: ").append(result_destCity)
                .append(" Duration: ").append(result_time)
                .append(" Capacity: ").append(result_capacity)
                .append(" Price: ").append(result_price)
                .append('\n');
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments.
   * You don't need to use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }
}
