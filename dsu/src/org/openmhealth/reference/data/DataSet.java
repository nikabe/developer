package org.openmhealth.reference.data;

import java.util.List;

import org.openmhealth.reference.domain.ColumnList;
import org.openmhealth.reference.domain.Data;

/**
 * The data associated with their schema as defined by the Open mHealth.
 *
 * @author John Jenkins
 */
public abstract class DataSet {
	/**
	 * The name of the DB document/table/whatever that contains the data.
	 */
	public static final String DATA_DB_NAME = "data";
	
	/**
	 * The instance of this MongoDataSet to use. 
	 */
	protected static DataSet instance;

	/**
	 * Default constructor. All access to the data set is static.
	 */
	public DataSet() {
		DataSet.instance = this;
	}
	
	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static DataSet getInstance() {
		return instance;
	}

	/**
	 * Reads the data from the system.
	 * 
	 * @param schemaId The unique identifier for the schema for the requested
	 * 				   data. This parameter is required.
	 * 
	 * @param version The version of the schema for the requested data. This
	 * 				  parameter is required.
	 * 
	 * @param owner The identifier of the user whose data is requested. This
	 * 				parameter is required.
	 * 
	 * @param columnList The list of columns within the data to return.
	 * 
	 * @param numToSkip The number of data points to skip.
	 * 
	 * @param numToReturn The number of data points to return.
	 * 
	 * @return A database cursor that references the applicable data.
	 */
	public abstract MultiValueResult<? extends Data> getData(
		final String owner,
		final String schemaId,
		final long version,
		final ColumnList columnList,
		final Long numToSkip,
		final Long numToReturn);
	
	/**
	 * Stores some data.
	 * 
	 * @param data
	 *        The data to store.
	 */
	public abstract void setData(final List<Data> data);
}