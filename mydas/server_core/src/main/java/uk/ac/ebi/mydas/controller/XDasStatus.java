package uk.ac.ebi.mydas.controller;

/**
 * Created using IntelliJ IDEA.
 * Date: 29-May-2007
 * Time: 20:20:59
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Valid X-DAS-STATUS codes.
 */
public enum XDasStatus {
    
    STATUS_200_OK ("200"),
    STATUS_400_BAD_COMMAND ("400"),
    STATUS_401_BAD_DATA_SOURCE ("401"),
    STATUS_402_BAD_COMMAND_ARGUMENTS ("402"),
    STATUS_403_BAD_REFERENCE_OBJECT ("403"),
    STATUS_404_BAD_STYLESHEET ("404"),
    STATUS_405_COORDINATE_ERROR ("405"),
    STATUS_500_SERVER_ERROR ("500"),
    STATUS_501_UNIMPLEMENTED_FEATURE ("501");

    private String errorCode;

    private XDasStatus(String errorCode){
        this.errorCode = errorCode;
    }

    public String toString(){
        return errorCode;
    }
}
