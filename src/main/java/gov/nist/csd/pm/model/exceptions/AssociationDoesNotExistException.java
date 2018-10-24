package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class AssociationDoesNotExistException extends PMException {
    public AssociationDoesNotExistException(long uaId, long targetId) {
        super(ErrorCodes.ERR_ASSOCIATION_DOES_NOT_EXIST, String.format("An association between %d and %d does not exist.", uaId, targetId));
    }
}
