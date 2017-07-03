package com.session.servlet.controller;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.web.site.servlet.SlingshotLoginController;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.uri.UriUtils;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Akshat on 27/6/16.
 */
public class SingleSessionSlingshotLoginController extends SlingshotLoginController {

	protected ConnectorService connectorService;
	/**
     * @param connectorService   the ConnectorService to set
     */
    public void setConnectorService(ConnectorService connectorService)
    {
        this.connectorService = connectorService;
    }
    private static final Logger logger = Logger.getLogger(SingleSessionSlingshotLoginController.class);

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
    	System.out.println("SingleSessionSlingshotLoginController: Akshat");
    	request.setCharacterEncoding("UTF-8");
        
        String username = (String) request.getParameter(PARAM_USERNAME);
        Connector conn = FrameworkUtil.getConnector( AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
        ConnectorContext c = new ConnectorContext(HttpMethod.GET);
        c.setContentType("application/json");
        Response res2 = conn.call("/api/session/status?userName=" + URLEncoder.encode(username), c);
        if (Status.STATUS_OK == res2.getStatus().getCode())
        {
            // Assuming we get a successful response then we need to parse the response as JSON and then
            // retrieve the group data from it...
            //
            // Step 1: Get a String of the response...
            String resStr = res2.getResponse();

            // Step 2: Parse the JSON...
            org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
            Object userData = jp.parse(resStr.toString());

            // Step 3: Iterate through the JSON object
            if (userData instanceof org.json.simple.JSONObject)
            {
                JSONObject logInInfo = (JSONObject) userData;
                if( logInInfo.containsKey("isloggedIn") && logInInfo.get("isloggedIn") != null && "yes".equals(logInInfo.get("isloggedIn")) ){
                    throw new Exception("Already logged in some where.");
                	//onFailure(request, response);
                }
            }
        }
        return super.handleRequestInternal(request, response);
    }

    protected void beforeSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        try
        {
        	final HttpSession session = request.getSession();
            
            // Get the authenticated user name and use it to retrieve all of the groups that the user is a member of...
            String username = (String)request.getParameter(PARAM_USERNAME);
            if (username == null)
            {
                username = (String)session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
            }
            
            if (username != null && session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS) == null)
            {
                Connector conn = FrameworkUtil.getConnector(session, username, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
                ConnectorContext c = new ConnectorContext(HttpMethod.GET);
                c.setContentType("application/json");
                Response res = conn.call("/api/people/" + URLEncoder.encode(username) + "?groups=true", c);
                if (Status.STATUS_OK == res.getStatus().getCode())
                {
                    // Assuming we get a successful response then we need to parse the response as JSON and then
                    // retrieve the group data from it...
                    // 
                    // Step 1: Get a String of the response...
                    String resStr = res.getResponse();
                    
                    // Step 2: Parse the JSON...
                    JSONParser jp = new JSONParser();
                    Object userData = jp.parse(resStr.toString());
    
                    // Step 3: Iterate through the JSON object getting all the groups that the user is a member of...
                    StringBuilder groups = new StringBuilder(512);
                    if (userData instanceof JSONObject)
                    {
                        Object groupsArray = ((JSONObject) userData).get("groups");
                        if (groupsArray instanceof org.json.simple.JSONArray)
                        {
                            for (Object groupData: (org.json.simple.JSONArray)groupsArray)
                            {
                                if (groupData instanceof JSONObject)
                                {
                                    Object groupName = ((JSONObject) groupData).get("itemName");
                                    if (groupName != null)
                                    {
                                        groups.append(groupName.toString()).append(',');
                                    }
                                }
                            }
                        }
                    }
                    
                    // Step 4: Trim off any trailing commas...
                    if (groups.length() != 0)
                    {
                        groups.delete(groups.length() - 1, groups.length());
                    }
                    
                    // Step 5: Store the groups on the session...
                    session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, groups.toString());
                }
                else
                {
                    session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, "");
                }
                
                
                Connector connector = connectorService.getConnector(AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID, username, session);
                String ticket = connector.getConnectorSession().getParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET);
                
                StringBuilderWriter buf = new StringBuilderWriter(512);
                JSONWriter writer = new JSONWriter(buf);
                
                writer.startObject();

                writer.writeValue("userName", username);
                if (ticket != null)
                {
                    writer.writeValue("ticket", ticket);
                }
                
                writer.endObject();
                

                System.out.println("userName:"+username);
                System.out.println("ticket:"+ticket);
                ConnectorContext postConnectorContext = new ConnectorContext(HttpMethod.POST);
                postConnectorContext.setContentType("application/json");
                Response res2 = conn.call("/api/session/status", postConnectorContext,
                        new ByteArrayInputStream(buf.toString().getBytes()));
                if (Status.STATUS_OK != res2.getStatus().getCode())
                {
                    throw new Exception("Error while login: " + res2.getStatus().getMessage());
                }
                if (Status.STATUS_OK == res2.getStatus().getCode())
                {
                    // Assuming we get a successful response then we need to parse the response as JSON and then
                    // retrieve the group data from it...
                    //
                    // Step 1: Get a String of the response...
                    String resStr = res2.getResponse();

                    // Step 2: Parse the JSON...
                    org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
                    System.out.println(resStr.toString());
                    Object userData = jp.parse(resStr.toString());

                    // Step 3: Iterate through the JSON object getting all the groups that the user is a member of...
                    StringBuilder groups = new StringBuilder(512);
                    if (userData instanceof org.json.simple.JSONObject)
                    {
                        JSONObject loginInfo = (JSONObject) userData;
                        if( loginInfo.containsKey("isSuccess") && loginInfo.get("isloggedIn") != null && "yes".equals(loginInfo.get("isloggedIn"))){
                        	
                            //session.setAttribute("_bluvalt_user_id", userInfo.get("userId"));
                        }else{
                        	
                        }
                    }
                }
            }
        }
        catch (ConnectorServiceException e1)
        {
            throw new Exception("Error creating remote connector to request user group data.");
        }
    }
}
