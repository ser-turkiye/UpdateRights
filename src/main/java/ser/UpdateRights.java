//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ser;

import ser.Constants.*;
import com.ser.blueline.*;
import com.ser.blueline.metaDataComponents.*;
import de.ser.doxis4.agentserver.UnifiedAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class UpdateRights extends UnifiedAgent {
    Logger log = LogManager.getLogger(this.getClass().getName());
    ISession ses = null;
    IDocumentServer srv = null;
    IUser owner = null;
    String prjCode = "";
    String mainCompSName = "";
    String compShortName = "";
    String ownerCompSName = "";
    boolean isExternal = false;

    public UpdateRights() {
    }

    protected Object execute() {
        this.log.info("Initiate the agent");
        if (this.getEventDocument() == null) {
            return this.resultError("Update Right...: Document object is null");
        } else {
            ses = getSes();
            srv = ses.getDocumentServer();
            IDocument engDocument = this.getEventDocument();
            IUnit extUnit = ses.getDocumentServer().getUnitByName(getSes(),"ExternalReader");
            try {
                this.log.info("Update Right started for:" + engDocument.getID());
                owner = getDocumentServer().getUser(getSes() , engDocument.getOwnerID());
                prjCode = engDocument.getDescriptorValue("ccmPRJCard_code");
                if(Objects.equals(prjCode, "")){
                    return this.resultError("Ubdate Rights...Project Code is null:" + prjCode);
                }
                mainCompSName = getMainCompGVList("CCM_PARAM_CONTRACTOR-MEMBERS");
                if(Objects.equals(mainCompSName, "")){
                    return this.resultError("Ubdate Rights...Main Comp Name is null:" + mainCompSName);
                }
                log.info("Update Rights..Main CompShortName:" + mainCompSName);
                String originator = engDocument.getDescriptorValue("ccmPrjDocOiginator");
                log.info("update rights...originator:" + originator);
                if(extUnit != null){
                    List<String> units = Arrays.asList(owner.getUnitIDs());
                    isExternal = units.contains(extUnit.getID());
                }
                if(!isExternal && originator != null && !originator.equals(mainCompSName)){
                    isExternal = true;
                    ownerCompSName = originator.toUpperCase();
                }
                if(isExternal) {
                    IDocument ownerContactFile = getContactFolder(owner.getEMailAddress());
                    if (originator != null) {
                        IDocument ownerContractorFile = getContractorFolder(originator.toUpperCase());
                        if (ownerContractorFile != null) {
                            ownerCompSName = (ownerContractorFile.getDescriptorValue("ContactShortName") != null ? ownerContractorFile.getDescriptorValue("ContactShortName") : "");
                        } else if (ownerContactFile != null) {
                            ownerContractorFile = getContractorFolder(ownerContactFile.getDescriptorValue("ObjectNumber"));
                            ownerCompSName = (ownerContractorFile != null ? ownerContractorFile.getDescriptorValue("ContactShortName") : "");
                        }
                    } else if (ownerContactFile != null) {
                        IDocument ownerContractorFile = getContractorFolder(ownerContactFile.getDescriptorValue("ObjectNumber"));
                        ownerCompSName = (ownerContractorFile != null ? ownerContractorFile.getDescriptorValue("ContactShortName") : "");
                    }
                    log.info("Update Rights..Is External...Owner CompShortName:" + ownerCompSName);
                    compShortName = ownerCompSName;
                    engDocument.setDescriptorValue("ccmSenderCode", ownerCompSName);
                    engDocument.setDescriptorValue("ccmReceiverCode", mainCompSName);
                    engDocument.commit();

                    String unitName = prjCode + "_" + compShortName;
                    log.info("Ubdate Rights..Is External..unit name :" + unitName);
                    IUnit unit = getDocumentServer().getUnitByName(getSes(), unitName);
                    if(unit!=null){
                        engDocument.setDescriptorValue("AbacOrgaRead",unit.getID());
                        engDocument.commit();
                        log.info("Ubdate Rights..rights set for the unit:" + unit.getName());
                    }else {
                        log.info("Ubdate Rights...unit is null :" + unitName);
                        return this.resultError("Ubdate Rights..IsExternal..unit null :" + unitName);
                    }
                }else {
                    compShortName = mainCompSName;
                    engDocument.setDescriptorValue("ccmSenderCode", mainCompSName);
                    engDocument.commit();

                    String unitName = prjCode;
                    log.info("Ubdate Rights..Is Internal..unit name :" + unitName);
                    IUnit unit = getDocumentServer().getUnitByName(getSes(), unitName);
                    if(unit!=null){
                        engDocument.setDescriptorValue("AbacOrgaRead",unit.getID());
                        engDocument.commit();
                        log.info("Ubdate Rights..rights set for the unit:" + unit.getName());
                    }else {
                        log.info("Ubdate Rights...unit is null :" + unitName);
                        return this.resultError("Ubdate Rights..IsInternal..unit is null :" + unitName);
                    }
                }
            }catch (Exception e) {
                log.error("Exception Caught");
                log.error(e.getMessage());
                return resultError(e.getMessage());
            }
            this.log.info("Update Right finished for:" + engDocument.getID());
            return this.resultSuccess("Ended successfully");
        }
    }
    public boolean isMainCompGVList(String paramName, String compSName) {
        boolean rtrn = false;
        IStringMatrix settingsMatrix = getDocumentServer().getStringMatrix(paramName, getSes());
        String rowValuePrjCode = "";
        String rowValueParamDCC = "";
        String rowValueParamCompSName = "";
        String rowValueParamMainComp = "";
        for(int i = 0; i < settingsMatrix.getRowCount(); i++) {
            //rowValuePrjCode = settingsMatrix.getValue(i, 0);
            //rowValueParamDCC = settingsMatrix.getValue(i, 6);
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            //if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
            //if (!Objects.equals(rowValueParamDCC, key1)){continue;}
            if (!Objects.equals(rowValueParamCompSName, compSName)){continue;}
            if (!Objects.equals(rowValueParamMainComp, "1")){continue;}

            return true;
        }
        return rtrn;
    }
    public String getMainCompGVList(String paramName) {
        String rtrn = "";
        IStringMatrix settingsMatrix = getDocumentServer().getStringMatrix(paramName, getSes());
        String rowValuePrjCode = "";
        String rowValueParamDCC = "";
        String rowValueParamCompSName = "";
        String rowValueParamMainComp = "";
        for(int i = 0; i < settingsMatrix.getRowCount(); i++) {
            //rowValuePrjCode = settingsMatrix.getValue(i, 0);
            //rowValueParamDCC = settingsMatrix.getValue(i, 6);
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            //if (!Objects.equals(rowValuePrjCode, prjCode)){continue;}
            //if (!Objects.equals(rowValueParamDCC, key1)){continue;}
            //if (!Objects.equals(rowValueParamCompSName, compSName)){continue;}
            if (!Objects.equals(rowValueParamMainComp, "1")){continue;}

            return rowValueParamCompSName;
        }
        return rtrn;
    }
    public IDocument getContactFolder(String eMail)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(Constants.ClassIDs.SupplierContactWS).append("'")
                .append(" AND ")
                .append("PrimaryEMail").append(" = '").append(eMail).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[]{Databases.BPWS} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public IDocument getContractorFolder(String compSCode)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(ClassIDs.InvolveParty).append("'")
                .append(" AND ")
                .append("ccmPRJCard_code").append(" = '").append(prjCode).append("'")
                .append(" AND ")
                .append("ContactShortName").append(" = '").append(compSCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[]{Databases.BPWS} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public IInformationObject[] createQuery(String[] dbNames , String whereClause , int maxHits){
        String[] databaseNames = dbNames;

        ISerClassFactory fac = getSrv().getClassFactory();
        IQueryParameter que = fac.getQueryParameterInstance(
                getSes() ,
                databaseNames ,
                fac.getExpressionInstance(whereClause) ,
                null,null);
        if(maxHits > 0) {
            que.setMaxHits(maxHits);
            que.setHitLimit(maxHits + 1);
            que.setHitLimitThreshold(maxHits + 1);
        }
        IDocumentHitList hits = que.getSession() != null? que.getSession().getDocumentServer().query(que, que.getSession()):null;
        if(hits == null) return null;
        else return hits.getInformationObjects();
    }
}
