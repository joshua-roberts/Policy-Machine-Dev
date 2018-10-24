package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NodeIDExistsException extends PMException {
    public NodeIDExistsException(long id, OldNode node) {
        super(ErrorCodes.ERR_NODE_ID_EXISTS, "A node already exists with ID " + id + ": name=" + node.getName() + ", type=" + node.getType());
    }
}
