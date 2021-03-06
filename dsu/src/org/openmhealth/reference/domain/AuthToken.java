package org.openmhealth.reference.domain;

import java.util.UUID;

import org.openmhealth.reference.exception.OmhException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A user's authentication token.
 * </p>
 *
 * @author John Jenkins
 */
public class AuthToken implements OmhObject {
	/**
	 * The version of this class for serialization purposes.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The JSON key for the authentication token.
	 */
	public static final String JSON_KEY_TOKEN = "token";
	/**
	 * The JSON key for the time the token was granted. 
	 */
	public static final String JSON_KEY_GRANTED = "granted";
	/**
	 * The JSON key for the time the token expires.
	 */
	public static final String JSON_KEY_EXPIRES = "expires";
	/**
	 * The default duration of the authentication token.
	 */
	public static final Long AUTH_TOKEN_LIFETIME = 1000 * 60 * 30L;
	
	/**
	 * The authentication token.
	 */
	@JsonProperty(JSON_KEY_TOKEN)
	private final String token;
	/**
	 * The user-name of the user to whom the token applies.
	 */
	@JsonProperty(User.JSON_KEY_USERNAME)
	private final String username;
	/**
	 * The number of milliseconds since the epoch at which time the token was
	 * granted.
	 */
	@JsonProperty(JSON_KEY_GRANTED)
	private final long granted;
	/**
	 * The number of milliseconds since the epoch at which time the token will
	 * expire.
	 */
	@JsonProperty(JSON_KEY_EXPIRES)
	private final long expires;

	/**
	 * Creates an {@link AuthToken} object via injection from the data layer.
	 * 
	 * @param token The authentication token.
	 * 
	 * @param username The user's user-name.
	 * 
	 * @param granted The time when the token was granted.
	 * 
	 * @param expires The time when the token expires.
	 * 
	 * @throws OmhException The token and/or user-name are null.
	 */
	@JsonCreator
	public AuthToken(
		@JsonProperty(JSON_KEY_TOKEN) final String token,
		@JsonProperty(User.JSON_KEY_USERNAME) final String username,
		@JsonProperty(JSON_KEY_GRANTED) final long granted,
		@JsonProperty(JSON_KEY_EXPIRES) final long expires) 
		throws OmhException {
		
		if(token == null) {
			throw new OmhException("The authentication token is null.");
		}
		if(username == null) {
			throw new OmhException("The user-name is null.");
		}
		
		this.token = token;
		this.username = username;
		this.granted = granted;
		this.expires = expires;
	}
	
	/**
	 * Creates a new authentication token for a user.
	 * 
	 * @param user The authentication token for a user.
	 * 
	 * @throws OmhException The user is null.
	 */
	public AuthToken(final User user) throws OmhException {
		if(user == null) {
			throw new OmhException("The user is null.");
		}
		
		token = UUID.randomUUID().toString();
		username = user.getUsername();
		granted = System.currentTimeMillis();
		expires = granted + AUTH_TOKEN_LIFETIME;
	}
	
	/**
	 * Retrieves the authentication token.
	 * 
	 * @return The authentication token.
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Returns the user-name of the user associated with this authentication
	 * token.
	 * 
	 * @return The user-name of the user associated with this authentication
	 *         token.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns the number of milliseconds since the epoch when this token was
	 * granted.
	 * 
	 * @return The number of milliseconds since the epoch when this token was
	 *         granted.
	 */
	public long getGranted() {
		return granted;
	}

	/**
	 * Returns the number of milliseconds since the epoch when this token
	 * (will) expire(d).
	 * 
	 * @return The number of milliseconds since the epoch when this token
	 * 		   (will) expire(d).
	 */
	public long getExpires() {
		return expires;
	}
}