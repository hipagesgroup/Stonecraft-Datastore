package com.stonecraft.datastore.parser;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.stonecraft.datastore.DbSchemaModel;
import com.stonecraft.datastore.DatabaseUtils;
import com.stonecraft.datastore.exceptions.SchemaParseException;
import com.stonecraft.datastore.utils.StringUtils;
import com.stonecraft.datastore.view.DatabaseColumn;
import com.stonecraft.datastore.view.DatabaseTable;
import com.stonecraft.datastore.view.SQLiteColumn;
import com.stonecraft.datastore.view.SQLiteTable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This class parses a Database XML file and returns the contents a
 * DbSchemaModel object. It uses reflection to create the tables and columns
 * specific to the database this schema is to be created on.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseParser extends AsyncTask<InputStream, Void, DbSchemaModel>{
	private static final String SCHEMA = "Schema";
	private static final String NAME = "Name";
	private static final String VERSION = "Version";
	private static final String TABLE = "Table";
	private static final String COLUMN = "Column";
	private static final String TYPE = "Type";
	private static final String PRIMARY = "Primary";
	private static final String AUTOINCREMENT = "AutoIncrement";
	private static final String NULLABLE = "Nullable";
	private static final String LENGTH = "length";
	private static final String URI = "uri";
	private static final String DEFAULT = "Default";

	private DbSchemaModel mySchema;
	private OnSchemaModelCreated myOnSchemaModelCreated;

	public DatabaseParser(OnSchemaModelCreated listener) {
		mySchema = new DbSchemaModel();
		myOnSchemaModelCreated = listener;
	}

	@Override
	protected DbSchemaModel doInBackground(InputStream... params) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(params[0], myHandler);
		} catch (MalformedURLException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("MalformedURLException " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("IOException " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("SAXException " + e);
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("ParserConfigurationException " + e);
			e.printStackTrace();
		}

		myOnSchemaModelCreated.OnSchemaModelCreated(mySchema);

		return mySchema;
	}

	public DbSchemaModel parse(String databaseLocation) {
		try {
			InputStream is = new FileInputStream(new File(databaseLocation));
			execute(is);
		} catch (IOException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("IOException " + e);
			e.printStackTrace();
		}
		return mySchema;
	}

	private DefaultHandler myHandler = new DefaultHandler() {
		private String myCurrentElement;
		private StringBuilder myTagTextBuilder;
		private String myCurrentBlock;

		private Map<String, String> myTableValues;
		private Map<String, String> myColumnValues;

		private List<DatabaseColumn> myCols = new ArrayList<DatabaseColumn>();

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			myCurrentElement = qName;
			myTagTextBuilder = new StringBuilder();
			if (myCurrentElement.equalsIgnoreCase(SCHEMA)) {
				myCurrentBlock = qName;
			} else if (myCurrentElement.equalsIgnoreCase(TABLE)) {
				myCurrentBlock = qName;
			} else if (myCurrentElement.equalsIgnoreCase(COLUMN)) {
				myCurrentBlock = qName;
			}

            //Starts a new table block
			if (myCurrentBlock.equalsIgnoreCase(TABLE)) {
				if (myTableValues == null || myCols == null) {
					myTableValues = new HashMap<String, String>();
					myCols = new ArrayList<DatabaseColumn>();
				}
				if(TABLE.equalsIgnoreCase(qName)) {
					myTableValues.put(URI, attributes.getValue(URI));
				}

            //Starts a new column block
			} else if (myCurrentBlock.equalsIgnoreCase(COLUMN)) {
				if (myColumnValues == null) {
					myColumnValues = new HashMap<String, String>();
				}

				if (myCurrentElement.equalsIgnoreCase(TYPE)) {
					myColumnValues.put(LENGTH, attributes.getValue(LENGTH));
				}
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
            String tagTextValue = myTagTextBuilder.toString().replace("(\\r|\\n|\\t)", "");
            if(tagTextValue.equals("CONTACTS")){
                String breakHere = "";
            }
			if (myCurrentBlock.equalsIgnoreCase(SCHEMA)) {
				if (myCurrentElement.equalsIgnoreCase(NAME)) {
					mySchema.setName(tagTextValue);
				} else if (myCurrentElement.equalsIgnoreCase(VERSION)) {
					mySchema.setVersion(Integer.parseInt(tagTextValue));
				}

			} else if (myCurrentBlock.equalsIgnoreCase(TABLE)
					&& !myCurrentBlock.equalsIgnoreCase(myCurrentElement)) {
				myTableValues.put(myCurrentElement, tagTextValue);
			} else if (myCurrentBlock.equalsIgnoreCase(COLUMN)
					&& !myCurrentBlock.equalsIgnoreCase(myCurrentElement)
					&& !TextUtils.isEmpty(myCurrentElement)) {
				myColumnValues.put(myCurrentElement, tagTextValue);
			}

			myCurrentElement = StringUtils.EmptyString;

			if (qName.equalsIgnoreCase(TABLE)) {
				if(TextUtils.isEmpty(myTableValues.get(URI))) {
					throw new SchemaParseException("There was no uri attribute in " +
							"the table element " + myTableValues.get(NAME));
				}

				mySchema.addTable(buildTable());
				myTableValues = new HashMap<String, String>();
				myCols = new ArrayList<DatabaseColumn>();

			} else if (qName.equalsIgnoreCase(COLUMN)) {
				myCols.add(buildCol());
				myColumnValues = new HashMap<String, String>();
			}
		}

		public void characters(char ch[], int start, int length)
				throws SAXException {

			if(!TextUtils.isEmpty(myCurrentElement)) {
				myTagTextBuilder.append(new String(ch, start, start + length));
			}
		}

		/**
		 * This method builds a Database table using reflection based on the
		 * Database table type that was passed into the constructor when
		 * creating an instance of this class
		 * 
		 * @return
		 */
		private DatabaseTable buildTable() {
			try {
				String name = myTableValues.get(NAME);
				String uri = myTableValues.get(URI);
				DatabaseTable table = new SQLiteTable(name, Uri.parse(uri));
				for (DatabaseColumn col : myCols) {
					table.addColumn(col);
				}
				return table;
			} catch (Exception e) {
				System.out.println("Failed to create Table " + e);
			}
			return null;
		}

		/**
		 * This method builds a Database column using reflection based on the
		 * Database column type that was passed into the constructor when
		 * creating an instance of this class
		 * 
		 * @return
		 */
		private DatabaseColumn buildCol() {
			DatabaseColumn dbColumn = null;
			String name = myColumnValues.get(NAME);
			int type = DatabaseUtils.getIntDatatype(StringUtils
					.getStringNotNull(myColumnValues.get(TYPE)));
			String lengthString = StringUtils
					.getStringNotNull(myColumnValues.get(LENGTH));
			int length = 0;
			if(TextUtils.isDigitsOnly(lengthString)) {
				length = Integer.parseInt(lengthString);
			}

			boolean primary = StringUtils.getStringNotNull(
					myColumnValues.get(PRIMARY)).equalsIgnoreCase(
					Boolean.TRUE.toString());
			boolean autoIncrement = StringUtils.getStringNotNull(
					myColumnValues.get(AUTOINCREMENT)).equalsIgnoreCase(
					Boolean.TRUE.toString());
			boolean nullable = StringUtils.getStringNotNull(
					myColumnValues.get(NULLABLE)).equalsIgnoreCase(
					Boolean.TRUE.toString());
			String defaultValue = myColumnValues.get(DEFAULT);

			dbColumn = new SQLiteColumn(name, type,
					length, primary, nullable, autoIncrement);
			if(!TextUtils.isEmpty(defaultValue)) {
				dbColumn.setDefaultValue(defaultValue);
			}

			return dbColumn;
		}
	};

	public interface OnSchemaModelCreated {
		void OnSchemaModelCreated(DbSchemaModel schema);
	}
}
