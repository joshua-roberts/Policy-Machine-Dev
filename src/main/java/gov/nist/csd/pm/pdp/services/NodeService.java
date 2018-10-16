package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pdp.analytics.PmAnalyticsEntry;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.InvalidEvrException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.*;

public class NodeService extends Service{

    private        AssignmentService assignmentService = new AssignmentService();
    private        AnalyticsService  analyticsService  = new AnalyticsService();

    public Set<Node> getNodes(String name, String type, Map<String, String> properties, String session, long process)
            throws InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException {
        Node user = getSessionUser(session);

        Set<Node> nodes = getNodes(name, type, properties);

        nodes.removeIf(node -> {
            try {
                if(node.getType().equals(NodeType.O) || node.getType().equals(NodeType.OA)) {
                    analyticsService.checkPermissions(user, process, node.getID(), ANY_OPERATIONS);
                }
                return false;
            }
            catch (Exception e) {
                return true;
            }
        });

        return nodes;
    }

    public Set<Node> getNodes(String name, String type, Map<String, String> properties)
            throws InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;

        HashSet<Node> results = new HashSet<>();
        HashSet<Node> nodes = getGraph().getNodes();
        for (Node node : nodes) {
            // if the name is not empty and the node's name does not match, continue
            if (name != null && !name.isEmpty() && !node.getName().equals(name)) {
                continue;
            }

            //if the types don't match continue
            if (!node.getType().equals(nodeType) && nodeType != null) {
                continue;
            }

            // iterate over the properties and if the current node does not have any of the given properties, continue
            boolean add = true;
            if (properties != null) {
                if (node.getProperties() == null) {
                    add = false;
                } else {
                    for (String key : properties.keySet()) {
                        String value = properties.get(key);
                        String checkValue = node.getProperties().get(key);
                        if (!(checkValue != null && checkValue.equals(value))) {
                            add = false;
                            break;
                        }
                    }
                }
            }

            if (add) {
                results.add(node);
            }
        }

        return results;
    }

    public Node getNode(String name, String type, Map<String, String> properties)
            throws InvalidNodeTypeException, UnexpectedNumberOfNodesException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Set<Node> nodes = getNodes(name, type, properties);

        if(nodes.size() != 1) {
            throw new UnexpectedNumberOfNodesException();
        }

        return nodes.iterator().next();
    }

    public Node createNodeIn(long baseID, String name, String type, Map<String, String> properties, String session, long process) throws DatabaseException, NodeNotFoundException, IOException, SQLException, InvalidPropertyException, ClassNotFoundException, NullNameException, NullTypeException, InvalidNodeTypeException, InvalidAssignmentException, NodeIDExistsException, NodeNameExistsException, ConfigurationException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, UnexpectedNumberOfNodesException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, MissingPermissionException, InvalidAssociationException, NodeNameExistsInNamespaceException {
        Node user = getSessionUser(session);

        //check parent node exists
        Node parentNode = getNode(baseID);

        //check parameters are not null
        if(name == null){
            throw new NullNameException();
        }
        if(type == null){
            throw new NullTypeException();
        }

        NodeType nt = NodeType.toNodeType(type);
        if(nt.equals(NodeType.O)) {
            analyticsService.checkPermissions(user, process, baseID, Constants.CREATE_OBJECT);
        } else if(nt.equals(NodeType.OA)) {
            analyticsService.checkPermissions(user, process, baseID, Constants.CREATE_OBJECT_ATTRIBUTE);
        }

        //create Node
        Node node = createNode(NEW_NODE_ID, name, type, properties);

        //create assignment
        try {
            assignmentService.createAssignment(node, parentNode);
        }
        catch (AssociationExistsException | InvalidAssociationException e) {
            deleteNode(node.getID());
            throw e;
        }

        return node;
    }

    /**
     * This method creates a new Policy Class in the NGAC graph.  Unlike other nodes, Policy Class nodes must have unique names.
     * @param name The name of the Policy Class
     * @param properties Any additional properties to add to the Policy Class
     * @param session The session ID of the user creating the Policy Class
     * @param process The ID of the process attempting to create a Policy Class
     * @return A Node object representing the Policy Class that was added to the graph
     */
    public Node createPolicy(String name, Map<String, String> properties, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NullNameException, NodeIDExistsException, ConfigurationException, NodeNotFoundException, AssignmentExistsException, InvalidNodeTypeException, PropertyNotFoundException, AssociationExistsException, NodeNameExistsException, InvalidAssignmentException, UnexpectedNumberOfNodesException, NullTypeException, InvalidAssociationException, InvalidKeySpecException, NoSuchAlgorithmException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, NodeNameExistsInNamespaceException, PolicyClassNameExistsException {
        Node user = getSessionUser(session);

        if (properties == null) {
            properties = new HashMap<>();
        }

        //check that the PC name does not exist
        Set<Node> nodes = getNodes(name, NodeType.PC.toString(), null);
        if (!nodes.isEmpty()) {
            throw new PolicyClassNameExistsException(name);
        }

        //create the pc node
        Node node = createNode(NEW_NODE_ID, name, NodeType.PC.toString(), properties);

        //create OA
        Node oaNode = createNodeIn(node.getID(), node.getName(), NodeType.OA.toString(), properties, session, process);

        //create UA
        Node uaNode = createNodeIn(node.getID(), node.getName() + " admin", NodeType.UA.toString(), properties, session, process);

        //assign U to UA
        new AssignmentService().createAssignment(user, uaNode);

        //create association
        new AssociationsService().createAssociation(uaNode.getID(), oaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));

        return node;
    }

    protected Node createNode(long id, String name, String type, Map<String, String> properties) throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException, InvalidNodeTypeException, InvalidKeySpecException, NoSuchAlgorithmException, NodeNameExistsInNamespaceException {
        if (properties == null) {
            properties = new HashMap<>();
        }

        // first check if this name and type already exists in the namespace
        HashMap<String, String> nsProp = new HashMap<>();
        nsProp.put(NAMESPACE_PROPERTY, properties.get(NAMESPACE_PROPERTY));

        Set<Node> nodes = getNodes(name, type, nsProp);
        if (!nodes.isEmpty()) {
            throw new NodeNameExistsInNamespaceException(nsProp.get(NAMESPACE_PROPERTY), name);
        }

        //create node in database
        NodeType nt = NodeType.toNodeType(type);

        Node newNode = getDaoManager().getNodesDAO().createNode(id, name, nt, properties);

        //add the node to the nodes
        getGraph().addNode(newNode);

        return newNode;
    }

    public Node getNode(long nodeID, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException, PropertyNotFoundException, ProhibitionNameExistsException, InvalidEvrException, ProhibitionDoesNotExistException, InvalidEntityException, NullNameException, ProhibitionResourceExistsException, InvalidNodeTypeException {
        Node user = getSessionUser(session);

        Node node = getNode(nodeID);

        if(node.getType().equals(NodeType.OA) || node.getType().equals(NodeType.O)) {
            //check user can access the node
            analyticsService.checkPermissions(user, process, nodeID, ANY_OPERATIONS);
        }

        return node;
    }

    private Node getNode(long nodeID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getGraph().getNode(nodeID);
        if(node == null){
            throw new NodeNotFoundException(nodeID);
        }

        return node;
    }


    public void deleteNode(long nodeID, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException {
        Node user = getSessionUser(session);

        //check node exists
        Node node = getNode(nodeID);

        if(node.getType().equals(NodeType.OA) || node.getType().equals(NodeType.O)) {
            //check user can delete the node
            analyticsService.checkPermissions(user, process, nodeID, DELETE_NODE);
        }

        deleteNode(nodeID);
    }

    protected void deleteNode(long nodeID) throws NodeNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check node exists
        getNode(nodeID);

        //delete node in db
        getDaoManager().getNodesDAO().deleteNode(nodeID);

        //delete node in database
        getGraph().deleteNode(nodeID);
    }

    public Map<String, String> getNodeProperties(long nodeID) throws NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //get node
        Node node = getNode(nodeID);

        return node.getProperties();
    }

    public String getNodeProperty(long nodeID, String key) throws NodeNotFoundException, PropertyNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //get node
        Node node = getNode(nodeID);

        //get node property
        return node.getProperty(key);
    }

    public HashSet<Node> getChildrenOfType(long nodeID, String childType) throws NodeNotFoundException,
            InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getNode(nodeID);

        HashSet<Node> children = getGraph().getChildren(node);
        HashSet<Node> retChildren = new HashSet<>(children);
        if(childType != null) {
            NodeType nt = NodeType.toNodeType(childType);
            for (Node n : children) {
                if (!n.getType().equals(nt)) {
                    retChildren.remove(n);
                }
            }
        }
        return retChildren;
    }

    public HashSet<Node> getNodeChildren(long nodeID, String childType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, NoUserParameterException, ConfigurationException, InvalidNodeTypeException {
        Node user = getSessionUser(session);

        Node node = getNode(nodeID);

        HashSet<Node> nodes = new HashSet<>();

        if(node.getType().equals(NodeType.PC)) {
            HashSet<Node> uas = getChildrenOfType(nodeID, NodeType.UA.toString());
            //add all uas because there are no associations on them
            nodes.addAll(uas);

            //get all oas user has access to
            List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(nodeID, user.getID());
            for(PmAnalyticsEntry entry : accessibleChildren) {
                if(childType == null || childType.equals(entry.getTarget().getType().toString())) {
                    nodes.add(entry.getTarget());
                }
            }
        } else if(node.getType().equals(NodeType.OA)) {
            //get all oas user has access to
            List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(nodeID, user.getID());
            for(PmAnalyticsEntry entry : accessibleChildren) {
                if(childType == null || childType.equals(entry.getTarget().getType().toString())) {
                    nodes.add(entry.getTarget());
                }
            }
        } else if(node.getType().equals(NodeType.UA)) {
            //add all children
            HashSet<Node> children = getChildrenOfType(node.getID(), null);
            nodes.addAll(children);
        }

        return nodes;
    }

    public void deleteNodeChildren(long nodeID, String childType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NodeNotFoundException, InvalidNodeTypeException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        Node user = getSessionUser(session);

        analyticsService.checkPermissions(user, process, nodeID, DEASSIGN_FROM);

        HashSet<Node> children = getChildrenOfType(nodeID, childType);
        for(Node node : children) {
            analyticsService.checkPermissions(user, process, node.getID(), DELETE_NODE);
            analyticsService.checkPermissions(user, process, node.getID(), DEASSIGN);
        }

        deleteNodeChildren(nodeID, childType);
    }

    public void deleteNodeChildren(long nodeID, String childType) throws NodeNotFoundException, InvalidNodeTypeException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        HashSet<Node> children = getChildrenOfType(nodeID, childType);
        for(Node node : children){
            //delete node in db
            getDaoManager().getNodesDAO().deleteNode(node.getID());

            //delete node in database
            getGraph().deleteNode(node.getID());
        }
    }

    public HashSet<Node> getParentsOfType(long nodeID, String parentType, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, InvalidNodeTypeException, NodeNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        Node user = getSessionUser(session);

        HashSet<Node> parents = getParentsOfType(nodeID, parentType);
        parents.removeIf(node -> {
            try {
                analyticsService.checkPermissions(user, process, node.getID(), ANY_OPERATIONS);
                return false;
            } catch(Exception e) {
                return true;
            }
        });

        return parents;
    }

    public HashSet<Node> getParentsOfType(long nodeID, String parentType) throws InvalidNodeTypeException,
            NodeNotFoundException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        Node node = getNode(nodeID);

        HashSet<Node> parents = getGraph().getParents(node);
        HashSet<Node> retParents = new HashSet<>(parents);
        if(parentType != null) {
            NodeType nt = NodeType.toNodeType(parentType);
            for (Node n : parents) {
                if (!n.getType().equals(nt)) {
                    retParents.remove(n);
                }
            }
        }
        return retParents;
    }
}
