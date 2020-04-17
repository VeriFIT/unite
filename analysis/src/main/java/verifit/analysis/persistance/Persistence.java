/**
 * Author: Ondrej Vasicek
 * VUT-login: xvasic25
 * VUT-email: xvasic25@stud.fit.vutbr.cz
 * 
 * This file is part of my Bachelor's thesis submitted for my IT degree
 * at the Brno University of Technology, Faculty of Information Technology.
 * 
 * This authors header is included only in the files I have personally modified
 * (hopefully did not miss any)
 */

package verifit.analysis.persistance;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.lyo.oslc.domains.auto.AutomationPlan;
import org.eclipse.lyo.oslc.domains.auto.AutomationRequest;
import org.eclipse.lyo.oslc.domains.auto.AutomationResult;
import org.eclipse.lyo.oslc.domains.auto.ParameterDefinition;
import org.eclipse.lyo.oslc.domains.auto.ParameterInstance;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.IResource;
import org.eclipse.lyo.oslc4j.provider.jena.JenaModelHelper;
import org.eclipse.lyo.store.ModelUnmarshallingException;
import org.eclipse.lyo.store.Store;
import org.eclipse.lyo.store.StoreAccessException;
import org.eclipse.lyo.store.StoreFactory;

import verifit.analysis.VeriFitAnalysisProperties;
import verifit.analysis.resources.SUT;
import verifit.analysis.resources.TextOut;

/**
 * A wrapper class for the Lyo Store.
 * @author od42
 *
 */
public class Persistence {

	Store store;
	
	/**
	 * Initializes the connection to a SPARQL triplestore. Tests whether the connection was successful and the data set exists.
	 * @param queryEndpoint		SPARQL query endpoint URL
	 * @param updateEndpoint	SPARQL update endpoint URL
	 * @throws IOException		When the connection failed, or the data set does not exist (or when something else went wrong)
	 */
	public Persistence (String queryEndpoint, String updateEndpoint) throws IOException
	{
		store = StoreFactory.sparql(queryEndpoint, updateEndpoint);
		
		try {
			// Check the triplestore connection. Dont care if the graph exists or not, will be created later if needed
			store.namedGraphExists(new URI(VeriFitAnalysisProperties.SPARQL_SERVER_NAMED_GRAPH_RESOURCES));
			
		} catch (URISyntaxException e) {
			// TODO should never be thrown (URI syntax)
			e.printStackTrace();
			
		} catch (Exception e) {
			String hint = "Is the triplestore running?";
			if (e.getMessage() == null)
				hint = "Are the endpoints correct? (server context, data set, endpoint)";
			
			throw new IOException("SPARQL triplestore initialization failed: " + e.getMessage()
				+ "\n  Current configuration:\n    - query endpoint: " + queryEndpoint + "\n    - update endpoint: " + updateEndpoint
				+ "\n\n  " + hint);
		}
	}
	

	/**
	 * Get a generic resource from the triplestore.
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resUri		URI of the resource to get
	 * @param clazz			Class of the resource to get
	 * @return				The requested resource
	 * @throws NoSuchElementException
	 * @throws StoreAccessException
	 */
	public <T extends IResource> T getResource(URI namedGraph, URI resUri, Class<T> clazz) throws NoSuchElementException, StoreAccessException
	{
		try {
			return store.getResource(namedGraph, resUri, clazz);

		} catch (NoSuchElementException e) {
			throw e;
		
		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Get all resources of a certain class from the triplestore
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param clazz			Class of the resources to get
	 * @return				A list of the requested resources
	 * @throws StoreAccessException
	 */
	public <T extends IResource> List<T> getResources(final URI namedGraph, final Class<T> clazz) throws StoreAccessException
	{
		try {
			return store.getResources(namedGraph, clazz);
		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Query for resources of a certain class. There are 3 options - query for all, query based on the where clause, and query based on search terms
	 * @param httpServletRequest	httpServletRequest of the query capability call
	 * @param namedGraph			URI of the named graph in the tripletore
	 * @param clazz					Class of the resources to get (added by me
	 * @param where					'where' clause passed by the request
	 * @param page					Page number offset passed by the request 
	 * @param limit					Resource return limit passed by the request
	 * @note oslc.where				See - https://tools.oasis-open.org/version-control/browse/wsvn/oslc-core/trunk/specs/oslc-query.html
	 * @note oslc.searchTerms		See - https://tools.oasis-open.org/version-control/browse/wsvn/oslc-core/trunk/specs/oslc-query.html)
	 * @return	List of the found resources
	 * @throws StoreAccessException
	 */
	public <T extends IResource> List<T> whereQuery(HttpServletRequest httpServletRequest, URI namedGraph, Class<T> clazz, String where, int page, int limit) throws StoreAccessException
	{
		try {
	    	if (where == null)
	    		where = "";
	    	
	    	String oslcPrefixes = httpServletRequest.getParameter("oslc.prefix");
	    	String searchTerms = httpServletRequest.getParameter("oslc.searchTerms");
	    	return store.getResources(namedGraph, clazz, oslcPrefixes, where, searchTerms, limit, page);
	    	
		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Insert new AutomationPlan into the triplestore. If the resource already exists then rewrite it.
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resToUpdate	Resources to update/insert
	 * @throws StoreAccessException
	 */
	public void updateResources(final URI namedGraph, final AutomationPlan... resToUpdate) throws StoreAccessException
	{
		try {
			for (AutomationPlan res : resToUpdate)
			{
				/*
				// update all inlined resources first TODO
				for (ParameterDefinition def : res.getParameterDefinition())
				{
					store.updateResources(namedGraph, def);
				}
				*/
				
				store.updateResources(namedGraph, res);
			}

		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Insert new AutomationRequest into the triplestore. If the resource already exists then rewrite it.
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resToUpdate	Resources to update/insert
	 * @throws StoreAccessException
	 */	
	public void updateResources(final URI namedGraph, final AutomationRequest... resToUpdate) throws StoreAccessException
	{
		try {
			for (AutomationRequest res : resToUpdate)
			{
				/*
				// update all inlined resources first TODO
				for (ParameterInstance input : res.getInputParameter())
				{
					store.updateResources(namedGraph, input);
				}
				*/
				
				store.updateResources(namedGraph, res);
			}

		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Insert new AutomationResults into the triplestore. If the resource already exists then rewrite it.
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resToUpdate	Resources to update/insert
	 * @throws StoreAccessException
	 */
	public void updateResources(final URI namedGraph, final AutomationResult... resToUpdate) throws StoreAccessException
	{
		try {
			for (AutomationResult res : resToUpdate)
			{
				/*
				// update all inlined resources first TODO
				for (ParameterInstance input : res.getInputParameter())
				{
					store.updateResources(namedGraph, input);
				}
				for (ParameterInstance output : res.getOutputParameter())
				{
					store.updateResources(namedGraph, output);
				}
				for (TextOut log : res.getContribution())
				{
					// encode the output log to be XML storable
					String plainOutput = log.getValue();
					String xmlEncoded = StringEscapeUtils.escapeXml(plainOutput); // TODO
					log.setValue(xmlEncoded);
					
					store.updateResources(namedGraph, log);
				}
				*/
				
				store.updateResources(namedGraph, res);
			}

		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	

	/**
	 * Insert new SUT into the triplestore. If the resource already exists then rewrite it.
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resToUpdate	Resources to update/insert
	 * @throws StoreAccessException
	 */
	public void updateResources(final URI namedGraph, final SUT... resToUpdate) throws StoreAccessException
	{
		try {
			for (SUT res : resToUpdate)
			{
				store.updateResources(namedGraph, res);
			}

		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
	
	/**
	 * Delete an AutomationResult from the triplestore. Will also delete all inlined resources and the connected AutomationRequest
	 * @param namedGraph	URI of the named graph in the tripletore
	 * @param resToDelete	Resources to delete
	 * @throws StoreAccessException
	 */
	public void deleteAutoResult(URI namedGraph, AutomationResult resToDelete) throws StoreAccessException
	{
		try {	
			ArrayList<URI> urisToDelete = new ArrayList<URI>();
			
			// delete the AutoResult itself
			urisToDelete.add(resToDelete.getAbout());
			
			// delete the AutoRequest tied with the result
			urisToDelete.add(resToDelete.getProducedByAutomationRequest().getValue());
			
			// delte all inlined resources (outputlog, input and output parameters)
			/*for (TextOut log : resToDelete.getContribution())
			{
				urisToDelete.add(log.getAbout());
			}

			for (ParameterInstance input : resToDelete.getInputParameter())
			{
				urisToDelete.add(input.getAbout());
			}

			for (ParameterInstance output : resToDelete.getOutputParameter())
			{
				urisToDelete.add(output.getAbout());
			}
			*/
			
			store.deleteResources(namedGraph, urisToDelete.toArray(new URI[]{}));
			
		} catch (Exception e) {
			throw new StoreAccessException(e.getMessage());
		}
	}
}