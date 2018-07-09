package gov.nist.policyserver.resources;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.response.ApiResponse;
import gov.nist.policyserver.service.AnalyticsService;
import gov.nist.policyserver.service.NodeService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/analytics")
public class AnalyticsResource {
    private AnalyticsService analyticsService = new AnalyticsService();
    private NodeService      nodeService      = new NodeService();

    @Path("/{var1:target}/users/permissions")
    @GET
    public Response getUsersWithPermissions(@PathParam("var1") PathSegment targetPs,
                                            @QueryParam("permissions") String permissions,
                                            @QueryParam("session") String session,
                                            @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"));

        //Get all users' permissions on target
        List<PmAnalyticsEntry> usersPerms = analyticsService.getUsersPermissionsOn(targetNode.getId());

        //if there are permissions to check for, split the string and check
        if(permissions != null && permissions.length() > 0) {
            String[] permArr = permissions.split(",\\s*");

            List<PmAnalyticsEntry> usersWithPerms = new ArrayList<>();
            for(PmAnalyticsEntry entry : usersPerms) {
                if(entry.getOperations().containsAll(Arrays.asList(permArr))) {
                    usersWithPerms.add(entry);
                }
            }
;
            return new ApiResponse(usersWithPerms).toResponse();
        }

        return new ApiResponse(usersPerms).toResponse();
    }

    @Path("/{var1:target}/users/{username}/permissions")
    @GET
    public Response getUserPermissions(@PathParam("var1") PathSegment targetPs,
                                       @PathParam("username") String username,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"));

        //get the user node
        Node userNode = nodeService.getNode(username, NodeType.U.toString(), null);

        return new ApiResponse(analyticsService.getUserPermissionsOn(targetNode.getId(), userNode.getId()).getOperations()).toResponse();
    }

    @Path("/{var1:target}/users/{username}")
    @GET
    public Response checkUserHasPermissions(@PathParam("var1") PathSegment targetPs,
                                            @PathParam("username") String username,
                                            @QueryParam("permissions") String permissions,
                                            @QueryParam("session") String session,
                                            @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"));

        //get the user node
        Node userNode = nodeService.getNode(username, NodeType.U.toString(), null);

        //get user permissions
        PmAnalyticsEntry userPerms = analyticsService.getUserPermissionsOn(targetNode.getId(), userNode.getId());

        //get permissions to check, if empty check if the user has any permissions
        if(permissions != null) {
            String[] permArr = permissions.split(",\\s*");
            return new ApiResponse(userPerms.getOperations().containsAll(Arrays.asList(permArr))).toResponse();
        } else {
            return new ApiResponse(!userPerms.getOperations().isEmpty()).toResponse();
        }
    }

    @Path("/{username}/targets/permissions")
    @GET
    public Response getAccessibleNodes(@PathParam("username") String username,
                                       @QueryParam("permissions") String permissions,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //get user node
        Node userNode = nodeService.getNode(username, NodeType.U.toString(), null);

        //get all accessible nodes
        List<PmAnalyticsEntry> accessibleNodes = analyticsService.getAccessibleNodes(userNode.getId());

        if(permissions != null) {
            String[] permArr = permissions.split(",\\s*");
            List<PmAnalyticsEntry> entries = new ArrayList<>();
            for(PmAnalyticsEntry entry : accessibleNodes) {
                if(entry.getOperations().containsAll(Arrays.asList(permArr))) {
                    entries.add(entry);
                }
            }

            return new ApiResponse(entries).toResponse();
        } else {
            return new ApiResponse(accessibleNodes).toResponse();
        }
    }

    /**
     * Get POS
     * @param session
     * @param process
     * @return
     * @throws NodeNotFoundException
     * @throws NoUserParameterException
     * @throws SessionUserNotFoundException
     * @throws ConfigurationException
     * @throws SessionDoesNotExistException
     */
    @Path("/sessions")
    @GET
    public Response getAccessibleNodes(@QueryParam("session") String session,
                                       @QueryParam("process") long process) throws NodeNotFoundException, NoUserParameterException, SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {

        Node user = analyticsService.getSessionUser(session);


        return new ApiResponse(analyticsService.getAccessibleNodes(user.getId())).toResponse();
    }
}
