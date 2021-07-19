package by.shyshaliaksey.webproject.model.connection;

import static by.shyshaliaksey.webproject.model.connection.DatabasePropertiesReader.CONNECTION_POOL_DEFAULT_SIZE;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionPool {

	private static final Logger logger = LogManager.getRootLogger();
	private static ConnectionPool instance;
	private static final AtomicBoolean isConnectionPoolCreated = new AtomicBoolean(false);
	private BlockingQueue<ConnectionProxy> freeConnections;
	private BlockingQueue<ConnectionProxy> occupiedConnections;

	private ConnectionPool() {
		occupiedConnections = new LinkedBlockingQueue<>();
		freeConnections = new LinkedBlockingQueue<>();
		for (int i = 0; i < CONNECTION_POOL_DEFAULT_SIZE; i++) {
			try {
				Connection connection = ConnectionFactory.createConnection();
				ConnectionProxy connectionProxy = new ConnectionProxy(connection);
				freeConnections.put(connectionProxy);
			} catch (SQLException e) {
				logger.log(Level.ERROR, "Connection creation error: {}", e.getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if (freeConnections.isEmpty()) {
			logger.log(Level.FATAL, "Can not create ConnectionPool: empty");
			throw new RuntimeException("Can not create ConnectionPool:");
		} else if (freeConnections.size() == CONNECTION_POOL_DEFAULT_SIZE) {
			logger.log(Level.INFO, "ConnectionPool successfully created");
		} else if (freeConnections.size() < CONNECTION_POOL_DEFAULT_SIZE) {
			logger.log(Level.WARN, "ConnectionPool successfully created, default size: {}, current size: {}",
					CONNECTION_POOL_DEFAULT_SIZE, freeConnections.size());
		}

	}

	public static ConnectionPool getInstance() {
		while (instance == null) {
			if (isConnectionPoolCreated.compareAndSet(false, true)) {
				instance = new ConnectionPool();
			}
		}
		return instance;
	}

	// TODO what to do with interrupt? call getFreeConnection again from catch? use
	// optional? or throw new InterruptedException?
	public ConnectionProxy getConnection() {
		ConnectionProxy connection = null;
		try {
			connection = freeConnections.take();
			occupiedConnections.put(connection);

		} catch (InterruptedException e) {
			// TODO need to create lock?
			Thread.currentThread().interrupt();
		}
		return connection;
	}

	boolean releaseConnection(Connection connection) {
		boolean result = true;
		if (!(connection instanceof ConnectionProxy)) {
			logger.log(Level.ERROR, "Unboxed connection is detected: {}", connection);
			result = false;
		}
		occupiedConnections.remove(connection);
		try {
			freeConnections.put((ConnectionProxy) connection);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return result;
	}

	// TODO close only freeConnections or do something with rollback
	public void destroyConnectionPool() {
		while (!freeConnections.isEmpty()) {
			try {
				freeConnections.take().reallyClose();
			} catch (SQLException e) {
				logger.log(Level.WARN, "Connection is not deleted: {}", e.getMessage());
			} catch (InterruptedException e) {
				logger.log(Level.WARN, "Connection is not deleted: {}", e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		while (!occupiedConnections.isEmpty()) {
			try {
				ConnectionProxy connection = occupiedConnections.take();
				connection.rollback();
				connection.reallyClose();
			} catch (SQLException e) {
				logger.log(Level.WARN, "Connection is not deleted: {}", e.getMessage());
			} catch (InterruptedException e) {
				logger.log(Level.WARN, "Connection is not deleted: {}", e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		deregisterDrivers();
	}

	private void deregisterDrivers() {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
				logger.log(Level.ERROR, "Error occured while deregistering driver {}: {}", driver, e.getMessage());
			}
		}
	}

}
