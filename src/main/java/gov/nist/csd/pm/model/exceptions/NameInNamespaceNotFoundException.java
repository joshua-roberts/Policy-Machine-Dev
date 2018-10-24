package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NameInNamespaceNotFoundException extends PMException {
    public NameInNamespaceNotFoundException(String namespace, String nodeName, NodeType type) {
        super(ErrorCodes.ERR_NAME_IN_NAMESPACE_NOT_FOUND, String.format("OldNode with name '%s' and type %s does not " +
                "exist in namespace %s", nodeName, namespace, type.toString()));
    }
}
