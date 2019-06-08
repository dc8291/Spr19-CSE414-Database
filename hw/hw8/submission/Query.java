import java.sql.*;
import java.util.*;

public class Query extends QuerySearchOnly {

	// Logged In User
	private String username = null; // customer username is unique

	// transactions
	private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
	protected PreparedStatement beginTransactionStatement;

	private static final String COMMIT_SQL = "COMMIT TRANSACTION";
	protected PreparedStatement commitTransactionStatement;

	private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
	protected PreparedStatement rollbackTransactionStatement;

	private static final String CLEAR_TABLES_SQL = "DELETE FROM Reservations; DELETE FROM Users; DELETE FROM Itineraries;";
	protected PreparedStatement clearTablesStatement;

	private static final String LOGIN_SQL = "SELECT * FROM Users WHERE username = ?";
	protected PreparedStatement loginStatement;

	private static final String CREATE_USER_SQL = "INSERT INTO Users VALUES ((?),(?),(?))";
	protected PreparedStatement createUserStatement;

	private static final String INSERT_RESERVATION_SQL = "SET IDENTITY_INSERT Reservations ON;"
																											+"INSERT Reservations(reservationID, paid, username, fid1, fid2) VALUES (1, 0, ?, ?, ?);"
																											+"SET IDENTITY_INSERT Reservations OFF;";
	protected PreparedStatement insertReservationStatement;

	private static final String CHECK_RESERVATION_SQL = "SELECT * FROM Reservations WHERE username = ?";
	protected PreparedStatement checkReservationStatement;

	private static final String FLIGHT_INFO_SQL = "SELECT * FROM Flights WHERE fid = ?";
	protected PreparedStatement flightInfoStatement;

	private static final String MAX_NUM_RESERVATION_SQL = "SELECT TOP 1 * FROM Reservations ORDER BY reservationId desc";
	protected PreparedStatement maxNumReservationStatement;

	private static final String CHECK_USER_INFO_SQL = "SELECT * FROM Users WHERE username = ?";
	protected PreparedStatement checkUserInfoStatement;

	private static final String UPDATE_RESERVATION_SQL = "UPDATE Reservations SET paid = 1 WHERE reservationId = ?";
	protected PreparedStatement updateReservationStatement;

	private static final String UPDATE_USERS_SQL = "UPDATE Users SET balance = ? WHERE username = ?";
	protected PreparedStatement updateUsersStatement;

	public Query(String configFilename) {
		super(configFilename);
	}

	/**
	 * Clear the data in any custom tables created. Do not drop any tables and do not
	 * clear the flights table. You should clear any tables you use to store reservations
	 * and reset the next reservation ID to be 1.
	 */
	public void clearTables ()
	{
		// your code here
		try {
			clearTablesStatement.executeUpdate();
		} catch (SQLException e) {e.printStackTrace();}
	}


	/**
	 * prepare all the SQL statements in this method.
	 * "preparing" a statement is almost like compiling it.
	 * Note that the parameters (with ?) are still not filled in
	 */
	@Override
	public void prepareStatements() throws Exception
	{
		super.prepareStatements();
		beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
		commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
		rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

		/* add here more prepare statements for all the other queries you need */
		/* . . . . . . */
		clearTablesStatement = conn.prepareStatement(CLEAR_TABLES_SQL);
		loginStatement = conn.prepareStatement(LOGIN_SQL);
		createUserStatement = conn.prepareStatement(CREATE_USER_SQL);
		insertReservationStatement = conn.prepareStatement(INSERT_RESERVATION_SQL);
		checkReservationStatement = conn.prepareStatement(CHECK_RESERVATION_SQL);
		flightInfoStatement = conn.prepareStatement(FLIGHT_INFO_SQL);
		maxNumReservationStatement = conn.prepareStatement(MAX_NUM_RESERVATION_SQL);
		checkUserInfoStatement = conn.prepareStatement(CHECK_USER_INFO_SQL);
		updateReservationStatement = conn.prepareStatement(UPDATE_RESERVATION_SQL);
		updateUsersStatement = conn.prepareStatement(UPDATE_USERS_SQL);
	}


	/**
	 * Takes a user's username and password and attempts to log the user in.
	 *
	 * @return If someone has already logged in, then return "User already logged in\n"
	 * For all other errors, return "Login failed\n".
	 *
	 * Otherwise, return "Logged in as [username]\n".
	 */
	public String transaction_login(String username, String password)
	{
		try {
			if (this.username != null){
				return "User already logged in\n";
			}
			loginStatement.clearParameters();
			loginStatement.setString(1, username);
			ResultSet result = loginStatement.executeQuery();
			boolean exist = result.next();
			if (!exist){
				return "Login failed\n";
			}
			String tempPW = result.getString("password");
			result.close();
			if (tempPW.equals(password)){
				this.username = username;
				return "Logged in as " + this.username + "\n";
			}
		} catch (SQLException e) {e.printStackTrace();}
		return "Login failed\n";
	}

	/**
	 * Implement the create user function.
	 *
	 * @param username new user's username. User names are unique the system.
	 * @param password new user's password.
	 * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
	 *
	 * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
	 */
	public String transaction_createCustomer (String username, String password, int initAmount)
	{
		try {
			beginTransaction();
			loginStatement.clearParameters();
			loginStatement.setString(1, username);
			ResultSet result = loginStatement.executeQuery();
			boolean exist = result.next();
			if (!exist && initAmount >= 0){
				result.close();
				createUserStatement.clearParameters();
				createUserStatement.setString(1, username);
				createUserStatement.setString(2, password);
				createUserStatement.setInt(3, initAmount);
				createUserStatement.executeUpdate();
				commitTransaction();
				return "Created user " + username + "\n";
			}
			result.close();
			rollbackTransaction();
		} catch (SQLException e) {
			transaction_createCustomer(username, password, initAmount);
		}
		return "Failed to create user\n";
	}

	/**
	 * Implements the book itinerary function.
	 *
	 * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
	 *
	 * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
	 * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
	 * If the user already has a reservation on the same day as the one that they are trying to book now, then return
	 * "You cannot book two flights in the same day\n".
	 * For all other errors, return "Booking failed\n".
	 *
	 * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
	 * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
	 * successful reservation is made by any user in the system.
	 */
	public String transaction_book(int itineraryId)
	{
		if (this.username == null) {
			return "Cannot book reservations, not logged in\n";
		} else if (!lastSearch.containsKey(itineraryId)){
			return "No such itinerary " + itineraryId + "\n";
		} else {
			try
			{
				// Checking if the itinerary overlaps with previous reservation
				// 1. Obtain info about chosen itinerary
				// 2. Obtain ReservationInfo
				// 3. Go through every reservation to check if they match
				int chosenFid1 = lastSearch.get(itineraryId).f1.fid;

				flightInfoStatement.clearParameters();
				flightInfoStatement.setInt(1, chosenFid1);
				ResultSet flightInfoResult = flightInfoStatement.executeQuery();
				flightInfoResult.next();
				int itineraryDay = flightInfoResult.getInt("day_of_month");
				int itineraryMonth = flightInfoResult.getInt("month_id");
				flightInfoResult.close();

				checkReservationStatement.clearParameters();
				checkReservationStatement.setString(1, this.username);
				ResultSet reservationResult = checkReservationStatement.executeQuery();
				while (reservationResult.next()){
					int reservationFid = reservationResult.getInt("fid1");
					flightInfoStatement.clearParameters();
					flightInfoStatement.setInt(1, reservationFid);
					ResultSet reservationInfo = flightInfoStatement.executeQuery();
					int reservationDay = reservationInfo.getInt("day_of_month");
					int reservationMonth = reservationInfo.getInt("month_id");
					reservationInfo.close();
					if(itineraryDay == reservationDay && itineraryMonth == reservationMonth){
						rollbackTransaction();
						return "You cannot book two flights in the same day\n";
					}
				}
				int chosenFid2 = -1;
				if (lastSearch.get(itineraryId).f2 != null){
					chosenFid2 = lastSearch.get(itineraryId).f2.fid;
				}
				reservationResult.close();
				// Insert into reservation
				try
				{
					beginTransaction();
					insertReservationStatement.clearParameters();
					insertReservationStatement.setString(1, this.username);
					insertReservationStatement.setInt(2, chosenFid1);
					insertReservationStatement.setInt(3, chosenFid2);
					insertReservationStatement.executeUpdate();

					// Max Number
					ResultSet maxResult = maxNumReservationStatement.executeQuery();
					maxResult.next();
					int maxNum = maxResult.getInt("reservationId");
					maxResult.close();

					commitTransaction();
					return "Booked flight(s), reservation ID: " + maxNum + "\n";
				} catch (SQLException e) {
					rollbackTransaction();
				}
			} catch (SQLException e) {
				transaction_book(itineraryId);
			}
		}


		return "Booking failed\n";
	}

	/**
	 * Implements the pay function.
	 *
	 * @param reservationId the reservation to pay for.
	 *
	 * @return If no user has logged in, then return "Cannot pay, not logged in\n"
	 * If the reservation is not found / not under the logged in user's name, then return
	 * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
	 * If the user does not have enough money in their account, then return
	 * "User has only [balance] in account but itinerary costs [cost]\n"
	 * For all other errors, return "Failed to pay for reservation [reservationId]\n"
	 *
	 * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
	 * where [balance] is the remaining balance in the user's account.
	 */
	public String transaction_pay (int reservationId)
	{
		if (this.username == null){
			return "Cannot pay, not logged in\n";
		}

		try {
			beginTransaction();
			// Check through all user reservations
			checkReservationStatement.clearParameters();
			checkReservationStatement.setString(1, this.username);
			ResultSet reservationResult = checkReservationStatement.executeQuery();
			while(reservationResult.next()){
				int tempReservation = reservationResult.getInt("reservationId");
				if (tempReservation == reservationId){
					int paid = reservationResult.getInt("paid");

					// If it's paid, return error
					if (paid == 1){
						rollbackTransaction();
						return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
					}

					// Calculate Itinerary cost
					int fid1 = reservationResult.getInt("fid1");
					int fid2 = reservationResult.getInt("fid2");
					flightInfoStatement.clearParameters();
					flightInfoStatement.setInt(1, fid1);
					ResultSet flightInfo = flightInfoStatement.executeQuery();
					flightInfo.next();
					int fid1Cost = flightInfo.getInt("price");
					int fid2Cost = 0;
					flightInfo.close();
					if (fid2 > 0) {
						flightInfoStatement.clearParameters();
						flightInfoStatement.setInt(1, fid2);
						ResultSet flightInfo2 = flightInfoStatement.executeQuery();
						flightInfo2.next();
						fid2Cost = flightInfo2.getInt("price");
						flightInfo2.close();
					}

					// Check User balance
					checkUserInfoStatement.clearParameters();
					checkUserInfoStatement.setString(1, this.username);
					ResultSet userInfoResult = checkUserInfoStatement.executeQuery();
					userInfoResult.next();
					int userBalance = userInfoResult.getInt("balance");
					userInfoResult.close();
					int totalCost = fid1Cost + fid2Cost;

					//Return error if user does not have enough balance
					if (totalCost > userBalance){
						rollbackTransaction();
				 		return "User has only " + userBalance + " in account but itinerary costs " + totalCost + "\n";
					}

					//update both reservation and user
					updateReservationStatement.clearParameters();
					updateReservationStatement.setInt(1, reservationId);
					updateReservationStatement.executeUpdate();
					int remainingBalance = userBalance - totalCost;

					updateUsersStatement.clearParameters();
					updateUsersStatement.setInt(1, remainingBalance);
					updateUsersStatement.setString(2, this.username);
					updateUsersStatement.executeUpdate();

					commitTransaction();
					return "Paid reservation: "+ reservationId +" remaining balance: "+ remainingBalance+"\n";
				}
			}
			return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
		} catch (SQLException e){
			e.printStackTrace();
		}
		return "Failed to pay for reservation " + reservationId + "\n";
	}

	/**
	 * Implements the reservations function.
	 *
	 * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
	 * If the user has no reservations, then return "No reservations found\n"
	 * For all other errors, return "Failed to retrieve reservations\n"
	 *
	 * Otherwise return the reservations in the following format:
	 *
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * ...
	 *
	 * Each flight should be printed using the same format as in the {@code Flight} class.
	 *
	 * @see Flight#toString()
	 */
	public String transaction_reservations()
	{
		if (this.username == null) {
			return "Cannot view reservations, not logged in\n";
		}
		try
		{
			beginTransaction();
			checkReservationStatement.clearParameters();
			checkReservationStatement.setString(1, this.username);
			ResultSet userReservations = checkReservationStatement.executeQuery();
			if (!userReservations.next()){
				rollbackTransaction();
				return "No reservations found\n";
			} else {
				StringBuffer sb = new StringBuffer();
				do {
					int fid1 = userReservations.getInt("fid1");
					int fid2 = userReservations.getInt("fid2");
					int reservationNumber = userReservations.getInt("reservationId");
					int paidReservation = userReservations.getInt("paid");
					ArrayList<Flight> listOfFlights = new ArrayList<Flight>();
					Flight fid1Flight = flight_info(fid1);
					listOfFlights.add(fid1Flight);
					if (fid2 > 0){
						Flight fid2Flight = flight_info(fid2);
						listOfFlights.add(fid2Flight);
					}
					if (paidReservation == 1){
						sb.append("Reservation " + reservationNumber + " paid: true:\n");
					} else {
						sb.append("Reservation " + reservationNumber + " paid: false:\n");
					}
					for(Flight f : listOfFlights){
						sb.append(f.toString() + "\n");
					}
				} while (userReservations.next());
				userReservations.close();
				commitTransaction();
				return sb.toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Failed to retrieve reservations\n";
	}

	// Returns the Flight class described in QuerySearchOnly class
	private Flight flight_info(int fid) throws SQLException
	{
		flightInfoStatement.clearParameters();
		flightInfoStatement.setInt(1, fid);
		ResultSet results = flightInfoStatement.executeQuery();
		results.next();
		Flight tempFlight = new Flight();
		tempFlight.fid = results.getInt("fid");
		tempFlight.dayOfMonth = results.getInt("day_of_month");
		tempFlight.carrierId = results.getString("carrier_id");
		tempFlight.flightNum = results.getString("flight_num");
		tempFlight.originCity = results.getString("origin_city");
		tempFlight.destCity = results.getString("dest_city");
		tempFlight.time = results.getInt("actual_time");
		tempFlight.capacity = results.getInt("capacity");
		tempFlight.price = results.getInt("price");
		results.close();
		return tempFlight;
	}
	/**
	 * Implements the cancel operation.
	 *
	 * @param reservationId the reservation ID to cancel
	 *
	 * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
	 * For all other errors, return "Failed to cancel reservation [reservationId]"
	 *
	 * If successful, return "Canceled reservation [reservationId]"
	 *
	 * Even though a reservation has been canceled, its ID should not be reused by the system.
	 */
	public String transaction_cancel(int reservationId)
	{
		// only implement this if you are interested in earning extra credit for the HW!
		return "Failed to cancel reservation " + reservationId;
	}


	/* some utility functions below */

	public void beginTransaction() throws SQLException
	{
		conn.setAutoCommit(false);
		beginTransactionStatement.executeUpdate();
	}

	public void commitTransaction() throws SQLException
	{
		commitTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}

	public void rollbackTransaction() throws SQLException
	{
		rollbackTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}
}
