package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class ProhibitionNameExistsException extends PMException {
    public ProhibitionNameExistsException(String prohibitionName) {
        super(ErrorCodes.ERR_PROHIBITION_NAME_EXISTS, String.format("Prohibition with name '%s' already exists", prohibitionName));
    }
}