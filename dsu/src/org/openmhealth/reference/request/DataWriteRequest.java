package org.openmhealth.reference.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmhealth.reference.data.AuthTokenBin;
import org.openmhealth.reference.data.DataSet;
import org.openmhealth.reference.data.MultiValueResult;
import org.openmhealth.reference.data.Registry;
import org.openmhealth.reference.data.UserBin;
import org.openmhealth.reference.domain.AuthToken;
import org.openmhealth.reference.domain.Data;
import org.openmhealth.reference.domain.MetaData;
import org.openmhealth.reference.domain.Schema;
import org.openmhealth.reference.domain.User;
import org.openmhealth.reference.exception.InvalidAuthenticationException;
import org.openmhealth.reference.exception.OmhException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>
 * Stores the given data.
 * </p>
 *
 * @author John Jenkins
 */
public class DataWriteRequest extends Request {
	/**
	 * The JSON factory that is used to create the parser that will be used to
	 * parse the data.
	 */
	private static final JsonFactory JSON_FACTORY = new MappingJsonFactory();
	
	/**
	 * The authentication token for the requesting user.
	 */
	private final String authToken;
	/**
	 * The ID of the schema from which the data was generated.
	 */
	private final String schemaId;
	/**
	 * The version of the schema from which the data was generated.
	 */
	private final long version;
	/**
	 * The data to validate and store.
	 */
	private final String data;
	
	/**
	 * Creates a request to store some data.
	 * 
	 * @param authToken
	 *        The requesting user's authentication token.
	 * 
	 * @param schemaId
	 *        The ID of the schema which should be used to validate the data.
	 * 
	 * @param version
	 *        The version of the schema which should be used to validate the
	 *        data.
	 * 
	 * @param data
	 *        The data to validate and store.
	 * 
	 * @throws OmhException
	 *         A parameter was invalid.
	 */
	public DataWriteRequest(
		final String authToken,
		final String schemaId,
		final long version,
		final String data)		
		throws OmhException {
		
		super(0L, 1L);
		
		if(authToken == null) {
			throw
				new InvalidAuthenticationException(
					"The authentication token is missing.");
		}
		if(schemaId == null) {
			throw new OmhException("The schema ID is missing.");
		}
		if(data == null) {
			throw new OmhException("The data is missing.");
		}
		
		this.authToken = authToken;
		this.schemaId = schemaId;
		this.version = version;
		this.data = data;
	}

	/**
	 * Validates the data and, if valid, stores it.
	 */
	@Override
	public void service() throws OmhException {
		// First, short-circuit if this request has already been serviced.
		if(isServiced()) {
			return;
		}
		else {
			setServiced();
		}
		
		// Check to be sure the schema is known.
		MultiValueResult<? extends Schema> schemas = 
			Registry.getInstance().getSchemas(schemaId, version, 0, 1);
		if(schemas.count() == 0) {
			throw
				new OmhException(
					"The schema ID, '" +
						schemaId +
						"', and version, '" +
						version +
						"', pair is unknown.");
		}
		Schema schema = schemas.iterator().next();
		
		// Get the authentication token object based on the parameterized
		// authentication token.
		AuthToken tokenObject = AuthTokenBin.getInstance().getUser(authToken);
		if(tokenObject == null) {
			throw new OmhException("The token is unknown.");
		}
		
		// Get the user to which the token belongs.
		User requestingUser = 
			UserBin.getInstance().getUser(tokenObject.getUsername());
		if(requestingUser == null) {
			throw new OmhException("The user no longer exists.");
		}
		
		// Parse the data.
		JsonNode dataNode;
		try {
			dataNode =
				JSON_FACTORY
					.createJsonParser(data).readValueAs(JsonNode.class);
		}
		catch(JsonParseException e) {
			throw new OmhException("The data was not well-formed JSON.", e);
		}
		catch(JsonProcessingException e) {
			throw new OmhException("The data was not well-formed JSON.", e);
		}
		catch(IOException e) {
			throw new OmhException("The data could not be read.", e);
		}
		
		// Make sure it is a JSON array.
		if(! (dataNode instanceof ArrayNode)) {
			throw new OmhException("The data was not a JSON array.");
		}
		ArrayNode dataArray = (ArrayNode) dataNode;
		
		// Get the number of data points.
		int numDataPoints = dataArray.size();
		
		// Create the result list of data points.
		List<Data> dataPoints = new ArrayList<Data>(numDataPoints);
		
		// Create a new ObjectMapper that will be used to convert the meta-data
		// node into a MetaData object.
		ObjectMapper mapper = new ObjectMapper();
		
		// For each element in the array, be sure it is a JSON object that
		// represents a valid data point for this schema.
		for(int i = 0; i < numDataPoints; i++) {
			// Get the current data point.
			JsonNode dataPoint = dataArray.get(i);
			
			// Validate that it is a JSON object.
			if(! (dataPoint instanceof ObjectNode)) {
				throw
					new OmhException(
						"A data point was not a JSON object: " + i);
			}
			ObjectNode dataObject = (ObjectNode) dataPoint;
			
			// Attempt to get the meta-data;
			MetaData metaData = null;
			JsonNode metaDataNode = dataObject.get(Data.JSON_KEY_METADATA);
			if(metaDataNode != null) {
				metaData = mapper.convertValue(metaDataNode, MetaData.class);
			}
			
			// Attempt to get the schema data.
			JsonNode schemaData = dataObject.get(Data.JSON_KEY_DATA);
			
			// Create and add the point to the set of data.
			dataPoints
				.add(
					schema.validateData(
						requestingUser.getUsername(),
						metaData,
						schemaData));
		}
		
		// Store the data.
		DataSet.getInstance().setData(dataPoints);
	}
}