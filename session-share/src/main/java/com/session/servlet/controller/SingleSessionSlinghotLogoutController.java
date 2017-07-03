package com.session.servlet.controller;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Created by Akshat on 11/8/16.
 */
public class SingleSessionSlinghotLogoutController extends AbstractController {

    private static final Logger logger = Logger.getLogger(SingleSessionSlinghotLogoutController.class);
    protected ConnectorService connectorService;
    public static final String REDIRECT_URL_PARAMETER = "redirectURL";
    public static final String REDIRECT_URL_PARAMETER_QUERY_KEY = "redirectURLQueryKey";
    public static final String REDIRECT_URL_PARAMETER_QUERY_VALUE = "redirectURLQueryValue";


    /**
     * @param connectorService   the ConnectorService to set
     */
    public void setConnectorService(ConnectorService connectorService)
    {
        this.connectorService = connectorService;
    }


    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
    	System.out.println("SingleSessionSlinghotLogoutController: Akshat");
        try
        {
           HttpSession session = request.getSession(false);
           if (session != null)
           {
               // retrieve the current user ID from the session
               String userId = (String)session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
               
               if (userId != null)
               {
                  // get the ticket from the Alfresco connector
                  Connector connector = connectorService.getConnector(AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID, userId, session);
                  String ticket = connector.getConnectorSession().getParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET);
                  
                  if (ticket != null)
                  {
                      // if we found a ticket, then expire it via REST API - not all auth will have a ticket i.e. SSO
                      Response res = connector.call("/api/login/ticket/" + ticket, new ConnectorContext(HttpMethod.DELETE));
                      if (logger.isDebugEnabled())
                          logger.debug("Expired ticket: " + ticket + " user: " + userId + " - status: " + res.getStatus().getCode());
                  }
                  
                  StringBuilderWriter buf = new StringBuilderWriter(512);
                  JSONWriter writer = new JSONWriter(buf);
                  writer.startObject();

                  writer.writeValue("userName", userId);
                  writer.endObject();
                  
                  Connector conn = FrameworkUtil.getConnector(session, userId, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
                  ConnectorContext postConnectorContext = new ConnectorContext(HttpMethod.POST);
                  postConnectorContext.setContentType("application/json");
                  Response res2 = conn.call("/api/session/status", postConnectorContext,
                          new ByteArrayInputStream(buf.toString().getBytes()));
                  if (Status.STATUS_OK != res2.getStatus().getCode())
                  {
                      throw new Exception("Error while login: " + res2.getStatus().getMessage());
                  }
               }
           }
        }
        finally
        {
        	AuthenticationUtil.logout(request, response);
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            // Check for a redirect URL - this should only be used when login is not required...
            String redirectURL = request.getParameter(REDIRECT_URL_PARAMETER);
            if (redirectURL != null)
            {
                String[] keys = request.getParameterValues(REDIRECT_URL_PARAMETER_QUERY_KEY);
                String[] values = request.getParameterValues(REDIRECT_URL_PARAMETER_QUERY_VALUE);
                
                if (keys != null && 
                    values != null && 
                    keys.length > 0 && 
                    keys.length == values.length)
                {
                    for (int i=0; i<keys.length; i++)
                    {
                        String delim = (i == 0) ? "?" : "&";
                        redirectURL = redirectURL + delim + keys[i] + "=" + values[i];
                    }
                }
                response.setHeader("Location", redirectURL);
            }
            else
            {
                // redirect to the root of the website
                response.setHeader("Location", request.getContextPath());
            }
            
            return null;
        }
    }
}
