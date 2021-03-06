package org.openmhealth.reference.data.mongodb;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.openmhealth.reference.data.UserBin;
import org.openmhealth.reference.data.mongodb.domain.MongoUser;
import org.openmhealth.reference.domain.User;
import org.openmhealth.reference.exception.OmhException;

import com.mongodb.QueryBuilder;

/**
 * <p>
 * The collection of users.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoUserBin extends UserBin {
	/**
	 * Default constructor. All access to the user bin is static.
	 */
	protected MongoUserBin() {
		// Do nothing.
	}

	/**
	 * Retrieves the {@link User} object from a user-name.
	 * 
	 * @param username
	 *        The desired user's user-name.
	 * 
	 * @return A {@link User} object for the user or null if the user does not
	 *         exist.
	 * 
	 * @throws OmhException
	 *         The user-name is null or multiple users have the same user-name.
	 */
	@Override
	public User getUser(final String username) throws OmhException {
		// Validate the parameter.
		if(username == null) {
			throw new OmhException("The username is null.");
		}
		
		// Get the authentication token collection.
		JacksonDBCollection<MongoUser, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoDao.getInstance()
						.getDb()
						.getCollection(DB_NAME_USER_BIN),
					MongoUser.class);
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query
		queryBuilder.and(MongoUser.JSON_KEY_USERNAME).is(username);
		
		// Execute query.
		DBCursor<MongoUser> result = collection.find(queryBuilder.get());
		
		// If multiple authentication tokens were returned, that is a violation
		// of the system.
		if(result.count() > 1) {
			throw
				new OmhException(
					"Multiple users exist with the same username: " +
						username);
		}
		
		// If no tokens were returned, then return null.
		if(result.count() == 0) {
			return null;
		}
		else {
			return result.next();
		}
	}
}