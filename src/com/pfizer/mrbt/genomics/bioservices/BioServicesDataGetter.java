package com.pfizer.mrbt.genomics.bioservices;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.cbr.remote.BioServicesRemoteException;
import com.pfizer.tnb.api.server.util.BioServicesInitParams;
import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import com.pfizer.mrbt.genomics.tou.bioservices.GeneGoGenericRowData;
import com.pfizer.mrbt.genomics.tou.bioservices.GeneGoBioServConstants;


public class BioServicesDataGetter {
    
    private BioServicesInitParams initParams;
    private static BsServiceClientImpl bs = new BsServiceClientImpl();
    private static BioServicesDataGetter _instance ;

    
    public static synchronized  BioServicesDataGetter getInstance(){
        if(_instance == null){
            _instance = new BioServicesDataGetter();
        }
        return _instance;
    }
    private BioServicesDataGetter(){       
        initParams = new BioServicesInitParams();
        initParams.setBioServicesServer(GeneGoBioServConstants.SERVER_URL);
        initParams.setServer(GeneGoBioServConstants.HOST);
        initParams.setPort(GeneGoBioServConstants.PORT);
        bs.setInitParams(initParams);
    }
    
    private  List<GeneGoGenericRowData> getMapFromList(List<List<String>> dataList,List<String> colNames ){
        Map<String,String> dataMap = new HashMap<String,String>();
        ArrayList<GeneGoGenericRowData> rowList = new ArrayList<GeneGoGenericRowData>();
        for(List<String> list : dataList){
            for(int i = 0; i <colNames.size(); i++){
                dataMap.put(colNames.get(i),list.get(i));
            }
            GeneGoGenericRowData grd = new GeneGoGenericRowData(dataMap);
            rowList.add(grd);
        }
        return rowList;
    }
    private String addParametersToUrl(String url,Map<String,String> params){
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        int ampCount = params.keySet().size();
        for(Map.Entry<String,String> pairs : params.entrySet()){
            String pname = (String)pairs.getKey();
            String pvalue = (String) pairs.getValue();
            if(ampCount > 0){
               sb.append("&");
               ampCount--;
            }
            sb.append(pname+"="+pvalue);
        }
        return sb.toString();
    }
    public List<List<String>> getResultInStringForService(int serviceId,Map<String,String> params){
        
        String qString = GeneGoBioServConstants.SERVER_URL+ "service="+serviceId+"&SERVICE_RENDERID="+GeneGoBioServConstants.XML_RAW_RENDER_ID;
        qString = addParametersToUrl(qString, params);
        QueryResult qr = bs.getDataFromBS(
                qString, 
                serviceId,
                false,
                null);
        List<List<String>> dataList = qr.getData();
        return dataList;
        
    }
    public List<GeneGoGenericRowData> getResultsForService(int serviceID,Map<String,String> params) throws BioServicesRemoteException, IOException{
        
        String qString = GeneGoBioServConstants.SERVER_URL+"service="+serviceID+"&SERVICE_RENDERID="+GeneGoBioServConstants.XML_RAW_RENDER_ID;
        qString = addParametersToUrl(qString,params);
        QueryResult qr = bs.getDataFromBS(
                qString, 
                serviceID,
                false,
                null);
     System.out.println(" colNames : "+qr.getColumnNames());
//     if(qr.getColumnNames().size() == 0){
//         for(Map.Entry pairs : params.entrySet()){
//             System.out.println(" parameter : "+pairs.getKey()+" value : "+pairs.getValue());
//         }
//     }
     List<List<String>> dataList = qr.getData();
     for(List<String> list : dataList){
         System.out.println(" list contents : "+list.toString());
         
     }
     return getMapFromList(qr.getData(),qr.getColumnNames());
    }
    public List<GeneGoGenericRowData> getDataForMechanism(String mechanism) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        System.out.println(" Replaced mechanism : "+mechanism);
        params.put(GeneGoBioServConstants.MECH_PARAM,mechanism);
        return getResultsForService(GeneGoBioServConstants.MECH_SERVICE,params);   
//        return getResultsForService(590, params); //590 for testing parser
    }
    public List<GeneGoGenericRowData> getHgncForProtein(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.PROT_PARAM, id);
        return getResultsForService(GeneGoBioServConstants.PROT_SERVICE,params);
    }
    
    public List<GeneGoGenericRowData> getHgncForCmplxOrGrp(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.CMPLX_GRP_PARAM,id);
        return getResultsForService(GeneGoBioServConstants.CMPLX_GRP_SERVICE, params);
    }
    public List<GeneGoGenericRowData> getReactantsForReaction(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.REACT_PARAM,id);
        return getResultsForService(GeneGoBioServConstants.REACTANT_SERVICE, params);
    }
    public List<GeneGoGenericRowData> getProductsForReaction(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.REACT_PARAM, id);
        return getResultsForService(GeneGoBioServConstants.PRODUCT_SERVICE, params);
    }
    public List<GeneGoGenericRowData> getECforEnzyme(String id) throws BioServicesRemoteException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.ENZYME_ID_PARAM, id);
        return getResultsForService(GeneGoBioServConstants.ENZYME_SERVICE, params);
    }
    public List<GeneGoGenericRowData> getPubChemForCmpd(String source_id) throws BioServicesRemoteException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.CMPD_PARAM,source_id);
        return getResultsForService(GeneGoBioServConstants.CMPD_SERVICE, params);
    }
    public  List<GeneGoGenericRowData> getPubChemForIsomers(String source_id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.ISOMER_PARAM,source_id);
        return getResultsForService(GeneGoBioServConstants.ISOMER_SERVICE, params);
    }
    public   void shutdown(){
        bs.shutdown();
    }
    public  List<GeneGoGenericRowData> getDataForMechanismAndType(
            String mechanism, String type) throws BioServicesRemoteException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        System.out.println(" Replaced mechanism : "+mechanism);
        params.put(GeneGoBioServConstants.MECH_PARAM,mechanism);
        type = type.replaceAll(" ", "+");
        params.put(GeneGoBioServConstants.TYPE_TEST_SERV_PARAM,type);
        return getResultsForService(GeneGoBioServConstants.MECH_TYPE_TEST_SERVICE, params);
    }
    
    public  List<GeneGoGenericRowData> getDataForMechanismExclType(String mechanism,String type) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        System.out.println(" Replaced mechanism : "+mechanism);
        type = type.replaceAll(" ", "+");
        System.out.println(" Replaced type : "+type);
        params.put(GeneGoBioServConstants.MECH_PARAM,mechanism);
        params.put(GeneGoBioServConstants.TYPE_TEST_SERV_PARAM,type);
        return getResultsForService(GeneGoBioServConstants.MECH_EXCL_TYPE_SERVICE, params);
    }
    public  List<GeneGoGenericRowData> getDataForMechanismInclType(String mechanism,String type) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        type = type.replaceAll(" ", "+");
        params.put(GeneGoBioServConstants.MECH_PARAM,mechanism);
        params.put(GeneGoBioServConstants.TYPE_TEST_SERV_PARAM,type);
        return getResultsForService(GeneGoBioServConstants.MECH_INCL_TYPE_SERVICE,params);
    }
    public List<GeneGoGenericRowData> getRowLimitedMechData(String mechanism,String type,String start,String end) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        System.out.println(" Replaced mechanism : "+mechanism);
        type = type.replaceAll(" ", "+");
        System.out.println(" Replaced type : "+type);
        params.put(GeneGoBioServConstants.MECH_PARAM, mechanism);
        params.put(GeneGoBioServConstants.TYPE_TEST_SERV_PARAM, type);
        params.put(GeneGoBioServConstants.START_ROW,start);
        params.put(GeneGoBioServConstants.END_ROW,end);
        return getResultsForService(GeneGoBioServConstants.MECH_LIM_ROW, params);
    }
    public  List<GeneGoGenericRowData> getDataForIntxnId(String mechanism,String type,String intxn_id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        mechanism = mechanism.replaceAll(" ", "+");
        type = type.replaceAll(" ", "+");
        params.put(GeneGoBioServConstants.MECH_PARAM,mechanism);
        params.put(GeneGoBioServConstants.TYPE_TEST_SERV_PARAM,type);
        params.put(GeneGoBioServConstants.INTXN_ID,intxn_id);
        return getResultsForService(GeneGoBioServConstants.SPL_MECH_TEST, params);
    }
    //metabolic reactions parser related service
    public List<GeneGoGenericRowData> getMetabolicReactions() throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        return getResultsForService(GeneGoBioServConstants.METABOLIC_RXN_SERVICE, params);
    }
    //pathway maps related services
    public List<GeneGoGenericRowData> getPathwayMapsMetadata() throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        return getResultsForService(GeneGoBioServConstants.PATHWAY_METADATA_SERVICE,params);
    } 
    public List<GeneGoGenericRowData> getPathwayInteractions(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.PATHWAY_ID, id);
        return getResultsForService(GeneGoBioServConstants.PATHWAY_INTERACTION_SERVICE, params);
    } 
    public List<GeneGoGenericRowData> getPathwayReferenceMaps(String id) throws BioServicesRemoteException, IOException{
        Map<String,String> params = new HashMap<String,String>();
        params.put(GeneGoBioServConstants.PATHWAY_ID, id);
        return getResultsForService(GeneGoBioServConstants.PATHWAY_REFERENCE_SERVICE, params);
    }
}

