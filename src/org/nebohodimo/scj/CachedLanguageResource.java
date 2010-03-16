package org.nebohodimo.scj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * LanguageResource which caches all query results in an SQL database
 * 
 * @author Alexey Kotlyarov <koterpillar@gmail.com>
 */
public class CachedLanguageResource implements LanguageResource {

	private LanguageResource resource_;
	private int resourceIndex_;
	private Connection connection_;
	private PreparedStatement selectOccurencesStatement;
	private PreparedStatement insertOccurencesStatement;
	private PreparedStatement updateOccurencesStatement;
	private PreparedStatement selectRequestStatement;
	private PreparedStatement insertRequestStatement;
	private PreparedStatement selectAllRequestsStatement;
	private PreparedStatement deleteRequestStatement;
	private boolean requestForFailed_;

	public CachedLanguageResource(LanguageResource resource, int resourceIndex,
			Connection connection) throws SQLException {
		resource_ = resource;
		resourceIndex_ = resourceIndex;
		connection_ = connection;
		selectOccurencesStatement = connection_
				.prepareStatement("select * from search where (query=?) and (searcher=?)");
		insertOccurencesStatement = connection_
				.prepareStatement("insert into search(query, hits, context, obtained, searcher)"
						+ " values(?, ?, ?, ?, ?)");
		updateOccurencesStatement = connection_
				.prepareStatement("update search set hits=?, context=?, obtained=?"
						+ " where query=? and searcher=?");

		selectRequestStatement = connection_
				.prepareStatement("select * from search_request where query=? and searcher=?");
		insertRequestStatement = connection_
				.prepareStatement("insert into search_request(query, searcher) values(?, ?)");
		selectAllRequestsStatement = connection_
				.prepareStatement("select * from search_request where searcher=?");
		deleteRequestStatement = connection_
				.prepareStatement("delete from search_request where query=? and searcher=?");

		selectOccurencesStatement.setInt(2, resourceIndex_);
		insertOccurencesStatement.setInt(5, resourceIndex_);
		updateOccurencesStatement.setInt(5, resourceIndex_);

		selectRequestStatement.setInt(2, resourceIndex_);
		insertRequestStatement.setInt(2, resourceIndex_);
		selectAllRequestsStatement.setInt(1, resourceIndex_);
		deleteRequestStatement.setInt(2, resourceIndex_);
	}

	public boolean getRequestForFailed() {
		return requestForFailed_;
	}

	public void setRequestForFailed(boolean value) {
		requestForFailed_ = value;
	}

	private String serializeContext(Occurences context) {
		StringBuilder builder = new StringBuilder();
		for (List<Grapheme> c : context) {
			for (Grapheme g : c) {
				builder.append(g.getGrapheme());
				for (String property : g.getPropertyNames()) {
					builder.append('\t');
					builder.append(property);
					builder.append('\t');
					builder.append(g.getProperty(property).toString());
				}
				builder.append('\n');
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private Occurences deserializeContext(long hitCount, String occurences) {
		Occurences result = new Occurences(hitCount);
		BufferedReader r = new BufferedReader(new StringReader(occurences));
		try {
			String line = r.readLine();
			while (line != null && line.length() > 0) {
				List<Grapheme> c = new ArrayList<Grapheme>();
				while (line != null && line.length() > 0) {
					String[] parts = line.split("\t");
					DefaultGrapheme g = new DefaultGrapheme(parts[0]);
					for (int i = 1; i < parts.length - 1; i += 2) {
						String property = parts[i];
						String value = parts[i + 1];
						try {
							Integer intValue = Integer.valueOf(value);
							g.setProperty(property, intValue);
						} catch (NumberFormatException ex) {
							g.setProperty(property, value);
						}
					}
					c.add(g);
					line = r.readLine();
				}
				result.add(c);
				line = r.readLine();
			}
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return result;
	}

	private boolean isOld(Date time) {
		// two months
		// return new Date().getTime() - time.getTime() >
		// 1000 * 60 * 60 * 24 * 30 * 2;
		return false;
	}

	private String joinWords(Collection<String> words) {
		char delimiter = ' ';
		if (words == null || words.isEmpty()) {
			return "";
		}
		Iterator<String> iter = words.iterator();
		StringBuffer buffer = new StringBuffer(iter.next().toString());
		while (iter.hasNext()) {
			buffer.append(delimiter).append(iter.next().toString());
		}
		return buffer.toString();
	}

	private List<String> splitQuery(String query) {
		ArrayList<String> result = new ArrayList<String>();
		for (String word : query.split(" ")) {
			result.add(word);
		}
		return result;
	}

	public boolean isResultCached(List<String> words) throws IOException {
		String wordsStr = joinWords(words);
		ResultSet resultSet = null;
		try {
			selectOccurencesStatement.setString(1, wordsStr);
			resultSet = selectOccurencesStatement.executeQuery();
			boolean hasResult = resultSet.next();
			return hasResult;
		} catch (SQLException ex) {
			throw new IOException(ex);
		} finally {
			try {
				if (resultSet != null) // && !resultSet.isClosed())
				{
					resultSet.close();
				}
			} catch (SQLException ex) {
				throw new IOException(ex);
			}
		}

	}

	public Occurences findOccurences(Collection<String> words)
			throws IOException {
		return findOccurences(words, true);
	}

	public Occurences findOccurences(Collection<String> words,
			boolean useTransaction) throws IOException {
		ResultSet resultSet = null;
		String wordsStr = joinWords(words);
		Occurences result;
		try {
			connection_.setAutoCommit(false);
			selectOccurencesStatement.setString(1, wordsStr);
			try {
				resultSet = selectOccurencesStatement.executeQuery();
				if (resultSet.next()) {
					Date obtained = resultSet.getDate("obtained");
					if (!isOld(obtained)) {
						result = deserializeContext(resultSet.getLong("hits"),
								resultSet.getString("context"));
					} else {
						// Old value, replace it
						result = resource_.findOccurences(words);
						updateOccurencesStatement.setString(4, wordsStr);
						updateOccurencesStatement.setLong(1, result
								.getHitCount());
						updateOccurencesStatement.setString(2,
								serializeContext(result));
						updateOccurencesStatement.setDate(3, new java.sql.Date(
								new Date().getTime()));
						updateOccurencesStatement.execute();
					}
				} else {
					// Calculate new value and insert it
					result = resource_.findOccurences(words);
					insertOccurencesStatement.setString(1, wordsStr);
					insertOccurencesStatement.setLong(2, result.getHitCount());
					insertOccurencesStatement.setString(3,
							serializeContext(result));
					insertOccurencesStatement.setDate(4, new java.sql.Date(
							new Date().getTime()));
					insertOccurencesStatement.execute();
				}
				resultSet.close();
			} catch (IOException ex) {
				connection_.rollback();
				connection_.setAutoCommit(true);
				if (requestForFailed_) {
					// Request failed, but we want it. Let's add a request
					// First, check if already requested
					selectRequestStatement.setString(1, wordsStr);
					resultSet = selectRequestStatement.executeQuery();
					if (!resultSet.next()) {
						// Insert new request
						insertRequestStatement.setString(1, wordsStr);
						insertRequestStatement.execute();
					}
					resultSet.close();
				}
				// There is still no result
				throw new IOException(ex);
			}
			connection_.commit();
			connection_.setAutoCommit(true);
		} catch (SQLException ex) {
			throw new IOException(ex);
		}
		return result;
	}

	public void retryFailedRequests() throws SQLException, IOException {
		ResultSet failedRequests = selectAllRequestsStatement.executeQuery();
		while (failedRequests.next()) {
			String wordsStr = failedRequests.getString("query");
			List<String> words = splitQuery(wordsStr);
			connection_.setAutoCommit(false);
			try {
				findOccurences(words, false);
				deleteRequestStatement.setString(1, wordsStr);
				deleteRequestStatement.execute();
			} catch (IOException ex) {
				connection_.rollback();
				throw ex;
			}
			connection_.commit();
			connection_.setAutoCommit(true);
		}
	}
}
