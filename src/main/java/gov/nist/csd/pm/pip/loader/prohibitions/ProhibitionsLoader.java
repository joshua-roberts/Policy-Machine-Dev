package gov.nist.csd.pm.pip.loader.prohibitions;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMProhibitionException;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.prohibitions.model.Prohibition;

import java.util.List;

public interface ProhibitionsLoader {

    /**
     * Load prohibitions from a data source into a List of Prohibition objects.
     * @return a list of the prohibitions loaded.
     * @throws PMDBException if there is an error loading the prohibitions from the data source.
     * @throws PMProhibitionException if there is an error converting data into the Prohibition model
     */
    List<Prohibition> loadProhibitions() throws PMException;
}
