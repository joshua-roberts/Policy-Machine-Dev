package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class PropertyNotFoundException extends PMException {
    public PropertyNotFoundException(long nodeId, String propKey) {
        super(ErrorCodes.ERR_PROPERTY_NOT_FOUND, String.format("Node with ID = %d does not have a property with key '%s'", nodeId, propKey));
    }
}
