package com.pfizer.mrbt.genomics.tou.bioservices;

public class GeneGoBioServConstants {

    // to be used by GeneGoGenericRowData class
    //for mechanism -filtered GeneGo Data (version 2)
    public static final String INTERACTION_ID = "INTERACTIONID";
    public static final String SOURCE_NAME = "SOURCENAME";
    public static final String SOURCE_TYPE = "SRCTYPE";
    public static final String TARGET_NAME = "TARGETNAME";
    public static final String TARGET_TYPE = "TGTTYPE";
    public static final String SOURCE_ID = "SRCID";
    public static final String TARGET_ID  = "TGTID";
    public static final String MECH_TYPE = "MECHANISMTYPE";
    public static final String INTXN_TYPE = "INTERACTIONTYPE";
    public static final String ORGANISM = "ORGANISM";
    public static final String TAXON_ID = "TAXONID";
    public static final String PUBMED =  "PUBMEDID";
    public static final String METHOD_NAME = "METHODNAME";
    public static final String TISSUENAME = "TISSUENAME";
    public static final String SITEDESC = "SITEDESC";
    public static final String SITEOF = "SITEOF";
    public static final String DESC = "DESCRIPTION";
    //colNames for various db queries
    public static final String SWISSPROT_ID = "PROT";
    public static final String PUBCHEM_ID = "PUBCHEM_ID";
    public static final String HGNC_CODE = "HGNC_CODE";
    public static final String EC = "EC";
    //colNames for metabolic reaction query:
    public static final String REACTID = "REACTID";
    public static final String RXN_NAME = "RNAME";
    public static final String REVERS = "REVERS";
    public static final String CATALYST = "CATALYST";

    public static final String HOST = "bioservicesdev.pfizer.com";
    public static final int PORT = 443;
    public static final String SERVER_URL = "https://" + HOST + ":" + PORT + "/TouVis/user/DataServletFiltered?";
    public static final String XML_RAW_RENDER_ID = "7"; 
    //bioservice ids used by parser
    public static final int MECH_SERVICE = 636;
    public static final int PROT_SERVICE = 643;//581;
    public static final int CMPLX_GRP_SERVICE = 649; //582;
    public static final int REACTANT_SERVICE = 583;
    public static final int PRODUCT_SERVICE = 584;
    public static final int ENZYME_SERVICE = 619;
    public static final int CMPD_SERVICE = 586;
    public static final int ISOMER_SERVICE = 592;
    public static final int MECH_TYPE_TEST_SERVICE = 594;
    public static final int MECH_EXCL_TYPE_SERVICE = 595;
    public static final int MECH_INCL_TYPE_SERVICE = 618;
    public static final int METABOLIC_RXN_SERVICE = 620;
    //pathwaymaps related services
    public static final int PATHWAY_METADATA_SERVICE = 702;
    public static final int PATHWAY_INTERACTION_SERVICE = 703;
    public static final int PATHWAY_REFERENCE_SERVICE = 704;
    //pathway maps related parameters
    public static final String PATHWAY_ID = "PID";
    //new service for mechanisms that generate 300K+ results like transcription regulation; addtnl inputs start and end rows
    public static final int MECH_LIM_ROW = 644;//596; 
    public static final int SPL_MECH_TEST = 642;//599;
    //parameter names used in services
    public static final String MECH_PARAM = "MECHANISM";
    public static final String PROT_PARAM = "PROT_ID";
    public static final String CMPLX_GRP_PARAM = "COMP_ID";
    public static final String REACT_PARAM = "REACT_ID";
    public static final String ENZYME_ID_PARAM = "FUNC_ID";
    public static final String CMPD_PARAM = "COMP_ID";
    public static final String ISOMER_PARAM = "COMP_ID";
    public static final String TYPE_TEST_SERV_PARAM = "TYPE";
    // additional parameters for serv id 596
    public static final String START_ROW = "START";
    public static final String END_ROW = "END";
    public static final String INTXN_ID = "INTXN_ID";
    //pathway maps related column names
    public static final String PID = "IMID";
    public static final String MAPNAME = "MAPNAME";
    public static final String PATHWAY_DESC = "DESCRIPTION";
    public static final String PATHWAY_INTXN_ID = "INTERACTION_ID";
    public static final String PATHWAY_INTXN_SRC = "SOURCE_ID";
    public static final String PATHWAY_INTXN_TGT = "TARGET_ID";
    public static final String PATHWAY_SRC_TYPE = "SOURCE_TYPE";
    public static final String PATHWAY_TGT_TYPE = "TARGET_TYPE";
    public static final String PATHWAY_INTXN_EFFECT = "EFFECT";
    public static final String PATHWAY_INTXN_SPECIES = "SPECIES";
}
