package gov.nist.csd.pm.pap.loader.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.relationships.Assignment;
import gov.nist.csd.pm.common.model.graph.relationships.Association;

import java.util.HashSet;

/**
 * This interface provides methods needed to load a graph into memory from a database.
 */
public interface GraphLoader {
    
    /**
     * Get all of the Policy Classes in the graph.
     * @return A set of all the Policy Classes in the graph.
     * @throws PMException When there is an error loading the Policy Classes.
     */
    HashSet<Long> getPolicies() throws PMException;

    /**
     * Get all of the nodes in the graph.
     * @return The set of all nodes in the graph.
     * @throws PMException When there is an error loading the nodes.
     */
    HashSet<NodeContext> getNodes() throws PMException;

    /**
     * Get all of the assignments in the graph.
     * @return A set of all the assignments in the graph.
     * @throws PMException When there is an error loading the assignments.
     */
    HashSet<Assignment> getAssignments() throws PMException;

    /**
     * Get all of the associations in the graph.
     * @return A set of all the associations in the graph.
     * @throws PMException When there is an error loading the associations.
     */
    HashSet<Association> getAssociations() throws PMException;
}
