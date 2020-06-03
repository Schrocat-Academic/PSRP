/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PRSP.PrEPSTI.reporter;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.logging.Level ;


/**
 *
 * @author MichaelWalker
 */
public class PopulationReporter extends Reporter {
    
    static String DEATH = "death" ;
    static String BIRTH = "birth" ;
    static String AGE = "age" ;
    static String START_AGE = "startAge" ;
    
    public PopulationReporter()
    {
        
    }
    
    public PopulationReporter(String simname, ArrayList<String> report) 
    {
        super(simname, report);
    }

    /**
     * FIXME: Passing and implementation of fileName not finalised.
     * @param simName
     * @param reportFilePath
     */
    public PopulationReporter(String simName, String reportFilePath)
    {
        super(simName, reportFilePath) ;
    }
    
    /**
     * 
     * @return Report showing population in each cycle.
     */
    public ArrayList<String> preparePopulationReport()
    {
        ArrayList<String> populationReport = new ArrayList<String>() ;
        ArrayList<Integer> countBirthReport = new ArrayList<Integer>() ;
        
        ArrayList<ArrayList<Comparable>> agentBirthReport = prepareAgentBirthReport() ;
        ArrayList<ArrayList<Comparable>> agentDeathReport = prepareAgentDeathReport() ;
        
        int reportSize = agentBirthReport.size() ;
        
        Integer maxBirthId = getPopulation() - 1 ;    //TODO: Read from METADATA
        countBirthReport.add(maxBirthId + 1) ;    //TODO: Read from METADATA
            
        for (int recordIndex = 0 ; recordIndex < reportSize; recordIndex++ )
        {
            ArrayList<Comparable> birthRecordObject = agentBirthReport.get(recordIndex) ;
            ArrayList<String> birthRecord = new ArrayList<String>() ; 
            if (!birthRecordObject.isEmpty())
            {
                for (Object agentId : birthRecordObject)
                    birthRecord.add((String) agentId) ;
            
                maxBirthId = Integer.valueOf(Collections.max(birthRecord));
            }
            // +1 Allows for numbering from 0
            countBirthReport.add(maxBirthId+1) ;
        }
        
        // how many deaths?
        int nbDeaths = 0 ;
        for (int recordIndex = 0 ; recordIndex < reportSize ; recordIndex++ )
        {
            ArrayList<Comparable> deathRecord = agentDeathReport.get(recordIndex) ;
            nbDeaths += deathRecord.size() ;
            String record = "Population:" ;
            int currentValue = countBirthReport.get(recordIndex + 1) ;
            populationReport.add(record + String.valueOf(currentValue - nbDeaths)) ;
        }
        return populationReport ;
    }
    
    
    @Override
    public String getInitialRecord()
    {
        // super() extracts first text line in Report
        String initialRecord = super.getInitialRecord() ;
        return initialRecord.substring(0, initialRecord.indexOf(DEATH)) ;
    }
    
    /**
     * 
     * @param sortingProperty
     * @return (HashMap) agentId maps to (String) value of sortingProperty
     */
    protected HashMap<Object,Object> sortedAgentIds(String sortingProperty)
    {
        HashMap<Object,Object> sortedHashMap = new HashMap<Object,Object>() ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        
        for (String record : birthReport)
        {
            ArrayList<String> censusArray = EXTRACT_ARRAYLIST(record,AGENTID) ;
            for (String birth : censusArray)
            {
                String agentId = EXTRACT_VALUE(AGENTID,birth) ;
                String sortingValue = EXTRACT_VALUE(sortingProperty,birth) ;
                sortedHashMap.put(agentId, sortingValue) ;
            }
        }
        return sortedHashMap ;
    }
    
    /**
     * 
     * @param sortingProperty
     * @return HashMap of sortingProperty's values to ArrayList of agentIds with
     * the appropriate sortingProperty value. 
     */
    protected HashMap<Object,ArrayList<String>> agentIdSorted(String sortingProperty)
    {
        return agentIdSorted(sortingProperty,getMaxCycles()) ;
    }
    
    /**
     * 
     * @param endCycle (int) The cycle at which the values of sortingProperty are of interest.
     * @param sortingProperty (String) the property we are sorting the Agents by.
     * @return HashMap of sortingProperty's values to ArrayList of agentIds with
     * the appropriate sortingProperty value. 
     */
    protected HashMap<Object,ArrayList<String>> agentIdSorted(String sortingProperty, int endCycle)
    {
        HashMap<Object,ArrayList<String>> sortedHashMap = new HashMap<Object,ArrayList<String>>() ;
        //LOGGER.info("birthReport");
        
        HashMap<String,String> propertyReport = prepareCensusPropertyReport(sortingProperty,endCycle) ;
        for (String agentId : propertyReport.keySet())
        {
            String sortingValue = propertyReport.get(agentId) ;
            if (!sortedHashMap.containsKey(sortingValue))
                sortedHashMap.put(sortingValue, new ArrayList<String>()) ;
            ArrayList<String> agentIdList = (ArrayList<String>) sortedHashMap.get(sortingValue).clone() ;
            agentIdList.add(agentId) ;
            sortedHashMap.put(sortingValue, (ArrayList<String>) agentIdList.clone()) ;
        }
        //LOGGER.log(Level.INFO,"{0}", sortedHashMap) ;
        return sortedHashMap ;
    }
    
    
    public String preparePropertyCorrelationReport(String property1, String property2)
    {
        String report = "" ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        ArrayList<String> agentReport ;
        double property2not1 = 0 ;
        double property1not2 = 0 ; 
        double property1and2 = 0 ;
        int propertyNeither = 0 ;
        
        for (String record : birthReport)
        {
            agentReport = EXTRACT_ARRAYLIST(record,AGENTID) ;
            for (String agentRecord : agentReport)
            {
                boolean has1 = Boolean.valueOf(EXTRACT_VALUE(property1,agentRecord)) ;
                boolean has2 = Boolean.valueOf(EXTRACT_VALUE(property2,agentRecord)) ;

                if (has2 && !has1)
                    property2not1++ ;
                else if (has1 && !has2)
                    property1not2++ ;
                else if (has1 && has2)
                    property1and2++ ;
                else    // if (!has1 && !has2)
                    propertyNeither++ ;
            }
        }
        double proportion1with2 = property1and2/(property1and2 + property1not2) ;
        double proportion2with1 = property1and2/(property1and2 + property2not1) ;
        double proportionNot1with2 = property2not1/(propertyNeither + property2not1) ;
        double proportionNot2with1 = property1not2/(propertyNeither + property1not2) ;
        
        // LOGGER.log(Level.INFO,"{0} {1} {2} {3}", new Object[] {property2not1, property1not2, property1and2, propertyNeither});
        
        report = "Of those with " + property1 + " true, " ;
        report += String.valueOf(proportion1with2) + " had " + property2 + " true.\n" ;
        report += "Of those with " + property1 + " false, " ;
        report += String.valueOf(proportionNot1with2) + " had " + property2 + " true.\n" ;
        report += "Of those with " + property2 + " true, " ;
        report += String.valueOf(proportion2with1) + " had " + property1 + " true.\n" ;
        report += "Of those with " + property2 + " false, " ;
        report += String.valueOf(proportionNot2with1) + " had " + property1 + " true.\n" ;
        
        return report ;
    }
    
    /**
     * 
     * @param recordNb
     * @return List of agentIds of Agents living at recordNb.
     */
    public ArrayList<String> prepareAgentsAliveRecord(int recordNb)
    {
        ArrayList<String> agentsAliveRecord = new ArrayList<String>() ;
        
        int maxAgentId = getMaxAgentId() ;
        ArrayList<Comparable> agentsDeadRecord = prepareAgentsDeadRecord(recordNb) ;
        
        // Cycle through all born and keep those who haven't died.
        for (int agentAlive = 0 ; agentAlive <= maxAgentId ; agentAlive++ )
            if (!agentsDeadRecord.contains(String.valueOf(agentAlive)))
                agentsAliveRecord.add(String.valueOf(agentAlive)) ;
        
        return agentsAliveRecord ;
    }
    
    /**
     * 
     * @param propertyName
     * @param propertyValue
     * @param fullReport
     * @return (ArrayList) Report filtered according to propertyValue of Agents.
     */
    protected ArrayList<String> filterByAgent(String propertyName, String propertyValue, ArrayList<String> fullReport)
    {
        if (propertyName.isEmpty())
            return fullReport ;
        
        ArrayList<String> filteredReport = new ArrayList<String>() ;
        
        String filteredRecord ;
        
        HashMap<String,String> censusPropertyReport = prepareCensusPropertyReport(propertyName) ;
        
        String agentString ;
        if (fullReport.get(0).contains(AGENTID))
            agentString = AGENTID ;
        else
            agentString = AGENTID0 ;
        
        for (String fullRecord : fullReport)
        {
            ArrayList<String> propertyList = EXTRACT_ARRAYLIST(fullRecord,agentString) ;
            filteredRecord = "" ;
            
            for (String propertyEntry : propertyList)
            {
                String agentId = EXTRACT_VALUE(agentString,propertyEntry) ;
                String agentValue = EXTRACT_VALUE(propertyName,censusPropertyReport.get(agentId)) ;
                if (COMPARE_VALUE(propertyName,propertyValue,agentValue,0)) ;
                    filteredRecord += propertyEntry ;
            }
            filteredReport.add(filteredRecord) ;
        }
        return filteredReport ;
        
    }
    
    /**
     * 
     * @return (int) agentId of last Agent born.
     */
    public int getMaxAgentId()
    {
        return getMaxAgentId(getMaxCycles()) ;
    }
    
    /**
     * 
     * @param recordNb (int) The record of interest.
     * @return (int) agentId of last Agent born on or before cycle recordNb.
     */
    public int getMaxAgentId(int recordNb)
    {
        String populationRecord ;
        int deathIndex ;
        String birthRecord = "" ;
        int birthIndex = -1 ;
        //Cycle backwards until final birth is found
        for( int recordIndex = recordNb ; birthIndex < 0 ; recordIndex-- )
        {
            populationRecord = getBackCyclesReport(0, 0, 1, recordIndex).get(0) ;
            //populationRecord = populationReport.get(recordIndex);
            deathIndex = populationRecord.indexOf(DEATH);
            birthRecord = populationRecord.substring(0, deathIndex);
            birthIndex = birthRecord.lastIndexOf(AGENTID) ;
        }
        
        return Integer.valueOf(EXTRACT_VALUE(AGENTID,birthRecord,birthIndex)) ;
    }
    
    /**
     * Cycles through deathReport up to recordNb to find agentIds of Agents who 
     * have died.
     * @param recordNb
     * @return List of agentIds of dead Agents.
     */
    public ArrayList<Comparable> prepareAgentsDeadRecord(int recordNb)
    {
        ArrayList<Comparable> agentsDeadRecord = new ArrayList<Comparable>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        
        for (int recordIndex = 0 ; recordIndex < recordNb ; recordIndex++ )
        {
            String record = deathReport.get(recordIndex) ;
            ArrayList<Comparable> deadAgentList = EXTRACT_ALL_VALUES(AGENTID, record) ;
            agentsDeadRecord.addAll(deadAgentList) ;
        }
            
        return agentsDeadRecord ;
    }
    /**
     * 
     * @return ArrayList of ArrayLists of (String) agentIds of agents 'born'
     * in each cycle
     */
    public ArrayList<ArrayList<Comparable>> prepareAgentBirthReport()
    {
        ArrayList<ArrayList<Comparable>> agentBirthReport = new ArrayList<ArrayList<Comparable>>() ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        int startIndex ;
        // Zeroeth record left out to stop population swamping the plot.
        for (int reportNb = 1 ; reportNb < birthReport.size() ; reportNb++ )
        {
            String report = birthReport.get(reportNb) ;
            startIndex = INDEX_OF_PROPERTY(AGENTID,report);
            agentBirthReport.add(EXTRACT_ALL_VALUES(AGENTID, report, startIndex)) ;
        }
        return agentBirthReport ;
    }
    
    /**
     * 
     * @return ArrayList of ArrayLists of (String) ages-at-birth of agents 'born'
     * in each cycle
     */
    public ArrayList<ArrayList<Comparable>> prepareAgeBirthReport()
    {
        ArrayList<ArrayList<Comparable>> ageBirthReport = new ArrayList<ArrayList<Comparable>>() ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        
        for (int reportNb = 0 ; reportNb < birthReport.size() ; reportNb++ )
        {
            String report = birthReport.get(reportNb) ;
            int startIndex = INDEX_OF_PROPERTY(AGE,report) ;
            ageBirthReport.add(EXTRACT_ALL_VALUES(AGE, report, startIndex)) ;
        }
        return ageBirthReport ;
    }
    
    /**
     * 
     * @return ArrayList of ArrayLists of (String) agentIds of agents who died 
     * in each cycle
     */
    public ArrayList<ArrayList<Comparable>> prepareAgentDeathReport()
    {
        ArrayList<ArrayList<Comparable>> agentDeathReport = new ArrayList<ArrayList<Comparable>>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        
        for (int recordNb = 0 ; recordNb < deathReport.size() ; recordNb++ )
        {
            String record = deathReport.get(recordNb) ;
            //LOGGER.info(record);
//            ArrayList<String> deathRecords = EXTRACT_ARRAYLIST(record, DEATH) ;
//            for (String deathRecord : deathRecords)
            agentDeathReport.add(EXTRACT_ALL_VALUES(AGENTID,record,0)) ;
        }
        return agentDeathReport ;
    }
    
    /**
     * Assumes death report structure of death:agentId:x age:y agentId:x age:y ...
     * @return ArrayList of ArrayLists of (String) ages-at-death of agents who died 
     * in each cycle
     */
    public HashMap<Object,Integer> prepareAgeDeathReport()
    {
        ArrayList<ArrayList<Object>> ageDeathReport = new ArrayList<ArrayList<Object>>() ;
        
        HashMap<Object,Integer> agentAgeHashMap = new HashMap<Object,Integer>() ;
        
        int daysInYear = 365 ;
        
        ArrayList<String>  birthReport = prepareBirthReport() ;
        ArrayList<String>  deathReport = prepareDeathReport() ;
        
        ArrayList<Object> ageDeathRecord ;
        
        for (int reportNb = 0 ; reportNb < deathReport.size() ; reportNb++ )
        {
            ageDeathRecord = new ArrayList<Object>() ;  //.clear();
            String record = deathReport.get(reportNb) ;
            ArrayList<Comparable> agentIds = EXTRACT_ALL_VALUES(AGENTID,record) ;
            for (Object agentId : agentIds)
                agentAgeHashMap.put(agentId, reportNb/daysInYear) ;
        }
        
        for (int recordNb = 0 ; recordNb < birthReport.size() ; recordNb++ )
        {
            String record = birthReport.get(recordNb) ;
            ArrayList<String> birthRecords = EXTRACT_ARRAYLIST(record,AGENTID) ;
            for (String birthRecord : birthRecords )
            {
                String agentId = EXTRACT_VALUE(AGENTID,birthRecord) ;
                if (agentAgeHashMap.keySet().contains(agentId))
                {
                    int birthAge = Integer.valueOf(EXTRACT_VALUE(AGE,birthRecord)) ;
                    agentAgeHashMap.put(agentId, agentAgeHashMap.get(agentId) - recordNb/daysInYear + birthAge) ;
                }
            }
        }
        return agentAgeHashMap ;
    }
    
    /**
     * 
     * @return (HashMap) key is String.valueOf(age) and value is the number to
     * die at that age.
     */
    public HashMap<Comparable,Number> prepareAgeAtDeathReport()
    {
        HashMap<Comparable,Number> ageAtDeathMap = new HashMap<Comparable,Number>() ;
        
        // Contains age-at-death data
        HashMap<Object,Integer> ageDeathReport = prepareAgeDeathReport() ;
        
        for (Object agentId : ageDeathReport.keySet())
        {
            String ageString = String.valueOf(ageDeathReport.get(agentId)) ;
            ageAtDeathMap = INCREMENT_HASHMAP(ageString,ageAtDeathMap) ;
        }
        return ageAtDeathMap ;
    }
    
    /**
     * cycle maps to (ArrayList) agentIds
     * @return (ArrayList) report of (ArrayList) of agentId who dies in each cycle.
     */
    public ArrayList<ArrayList<Comparable>> prepareDeathsPerCycleReport()
    {
        ArrayList<ArrayList<Comparable>> deathsPerCycleReport = new ArrayList<ArrayList<Comparable>>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        for (String record : deathReport)
        {
            ArrayList<String> stringArray = EXTRACT_ARRAYLIST(record,AGENTID);
            deathsPerCycleReport.add((ArrayList<Comparable>) stringArray.clone()) ;
        }
        return deathsPerCycleReport ;
        
    }
    
    /**
     * 
     * @param startRecordNb
     * @param endRecordNb
     * @return List of agentIds of agents who died from startRecordNb to before
     * endRecordNb.
     */
    public ArrayList<Object> prepareDeathsDuringPeriodReport(int startRecordNb, int endRecordNb)
    {
        ArrayList<Object> deathsDuringPeriodReport = new ArrayList<Object>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        
        for (int recordNb = startRecordNb ; recordNb < endRecordNb ; recordNb++ )
        {
            ArrayList<Comparable> stringArray = EXTRACT_ALL_VALUES(AGENTID,deathReport.get(recordNb),0) ;
            deathsDuringPeriodReport.addAll(stringArray) ;
        }
        
        return deathsDuringPeriodReport ;
    }
    
    /**
     * 
     * @return Report listing deaths in each cycle.
     */
    private ArrayList<String> prepareDeathReport()
    {
        ArrayList<String> deathReport = new ArrayList<String>() ;
        
        String record ;
        int deathIndex ;
        //LOGGER.info("preparedDeathReport()");

        for (boolean nextInput = true ; nextInput ; nextInput = updateReport() )
            for (int reportNb = 0 ; reportNb < input.size() ; reportNb += outputCycle )
            {
                record = input.get(reportNb) ;
                //LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[] {reportNb,deathReport.size(),input.size()});
                deathIndex = INDEX_OF_PROPERTY(DEATH,record) ;
                if (deathIndex < 0)
                    deathReport.add("") ;
                else
                    deathReport.add(record.substring(deathIndex)) ;
            }
            
        return deathReport ;
    }
    
    /**
     * 
     * @return (ArrayList(String)) report of which agentIds were born in which cycle
     */
    public ArrayList<String> prepareBirthReport()
    {
        int maxCycles = getMaxCycles() ;
        
        return prepareBirthReport(0, 0, maxCycles, maxCycles) ;
    }
    
    /**
     * 
     * @param backYears
     * @param backMonths
     * @param backDays
     * @param endCycle
     * @return (ArrayList(String)) report of which agentIds were born in which cycle
     * during specified time period.
     */
    public ArrayList<String> prepareBirthReport(int backYears, int backMonths, int backDays, int endCycle)
    {
        ArrayList<String> birthReport = new ArrayList<String>() ;
        
        String record ;
        int endIndex ;
        //int backCycles = getBackCycles(backYears, backMonths, backDays) ;
        ArrayList<String> backCyclesReport = getBackCyclesReport(backYears, backMonths, backDays, endCycle) ;
        for (int reportNb = 0 ; reportNb < backCyclesReport.size() ; reportNb += outputCycle )
        {
            record = backCyclesReport.get(reportNb) ;
            endIndex = record.indexOf("!") ;
            if (endIndex < 0)
                endIndex = record.length() ;
            birthReport.add(record.substring(INDEX_OF_PROPERTY("birth",record),endIndex)) ;
        }
        
        return birthReport ;
    }
    
    /**
     * 
     * @return (ArrayList(String)) report of which agentIds were changed in which cycle
     */
    public ArrayList<String> prepareChangeReport()
    {
        int maxCycles = getMaxCycles() ;
        return prepareChangeReport(0,0,maxCycles-1,maxCycles) ;
    }
    
    /**
     * 
     * @param endCycle
     * @return (ArrayList(String)) report of which agentId changes up to which cycle
     */
    public ArrayList<String> prepareChangeReport(int backYears, int backMonths, int backDays, int endCycle)
    {
        ArrayList<String> changeReport = new ArrayList<String>() ;
        
        String record ;
        int changeIndex ;
        int deathIndex ;
        
        //int backCycles = getBackCycles(backYears, backMonths, backDays) ;
        ArrayList<String> backCyclesReport = getBackCyclesReport(backYears, backMonths, backDays, endCycle) ;
        for (int reportNb = 0 ; reportNb < backCyclesReport.size() ; reportNb += outputCycle )
        {
            record = backCyclesReport.get(reportNb) ;
            changeIndex = INDEX_OF_PROPERTY("change",record) ;
            if (changeIndex < 0)
                changeReport.add("") ;
            else
            {
                deathIndex = record.indexOf("death") ;
                if (deathIndex < 0)
                    deathIndex = record.length() ;
                changeReport.add(record.substring(changeIndex,deathIndex)) ;
            }
        }
        
        return changeReport ;
    }
    
    
    /**
     * 
     * @param propertyName
     * @param endCycle
     * @return (ArrayList(String)) report of which agentId changes up to which cycle
     */
    public ArrayList<String> prepareChangeReport(String propertyName, int endCycle)
    {
        ArrayList<String> propertyChangeReport = new ArrayList<String>() ;
        
        String findPropertyName = propertyName ;
        Class[] parameterClasses = new Class[] {int.class} ;
        Object[] parameterArray = new Object[] {endCycle} ;
        ArrayList<String> changeReport = prepareChangeReport(0,0,endCycle-1,endCycle) ;
        //(ArrayList<String>) getReport("change",this,parameterClasses,parameterArray) ; 
        
        String propertyRecord ;
        int propertyIndex ;
        int endPropertyIndex ;
        ArrayList<String> riskyList = new ArrayList<String>() ;
        Collections.addAll(riskyList, new String[] {"probabilityUseCondom","prepStatus","riskyStatus"}) ;
        
        if (riskyList.contains(propertyName))
            findPropertyName = "riskiness" ;
        
        for (String record : changeReport)
        {
            propertyIndex = record.indexOf(findPropertyName) ;
            if (propertyIndex < 0)
            {
                //LOGGER.info("Change report has no record of " + propertyName) ;
                propertyChangeReport.add(EMPTY) ;
                continue ;
            }
            endPropertyIndex = INDEX_OF_PROPERTY("change",propertyIndex,record) ; // record.indexOf("change:",propertyIndex) ;
            if (endPropertyIndex < 0)
                endPropertyIndex = record.indexOf("!", propertyIndex) ;
            propertyRecord = record.substring(propertyIndex + findPropertyName.length() + 1, endPropertyIndex) ;
            // if ("riskiness".equals(propertyName))
            // LOGGER.info(propertyRecord);
            propertyChangeReport.add(propertyRecord) ;
        }
        
        return propertyChangeReport ;
    }
    
    /**
     * 
     * @param propertyName
     * @return (ArrayList(String)) report of which agentId changes up to which cycle
     */
    public ArrayList<String> prepareChangeReport(String propertyName)
    {
        return prepareChangeReport(propertyName,getMaxCycles()) ;
    }
    
    /**
     * For sorting final record according to Agent.getAge() .
     * @return HashMap (int) AgentId mapped to age.
     */
    public HashMap<Object,Integer> sortAgeRecord()
    {
        //HashMap<Object,ArrayList<Object>> sortAgeRecord = new HashMap<Object,ArrayList<Object>>() ;
        
        HashMap<Object,Integer> agentAgeHashMap = new HashMap<Object,Integer>() ;
        
        int daysInYear = 365 ;
        
        ArrayList<String>  birthReport = prepareBirthReport() ;
        ArrayList<String>  deathReport = prepareDeathReport() ;
        
        int nbCycles = birthReport.size() ;
        
        for (int recordIndex = 0 ; recordIndex < nbCycles ; recordIndex++ )
        {
            String birthRecord = birthReport.get(recordIndex) ;
            ArrayList<String> birthArray = EXTRACT_ARRAYLIST(birthRecord,AGENTID) ;
            for (String birthAgent : birthArray)
            {
                Object agentId = EXTRACT_VALUE(AGENTID,birthAgent) ;
                int age = Integer.valueOf(EXTRACT_VALUE(AGE,birthAgent)) ;
                agentAgeHashMap.put(agentId, age + (nbCycles - recordIndex)/daysInYear) ;
            }
            String deathRecord = deathReport.get(recordIndex) ;
            ArrayList<String> deathArray = EXTRACT_ARRAYLIST(deathRecord,AGENTID) ;
            for (String deathAgent : deathArray) 
            {
                Object agentId = EXTRACT_VALUE(AGENTID,deathAgent) ;
                int correctAge = agentAgeHashMap.get(agentId) - (nbCycles - recordIndex)/daysInYear ;
                agentAgeHashMap.put(agentId,correctAge) ;
            }
            
        }
        
        // Put into form ageRange={agentId}
//        for (Object agentId : agentAgeHashMap.keySet()) 
//        {
//            int age = agentAgeHashMap.get(agentId) ;
//            // Sort into age ranges (n*5 + 1) to (n+1)*5
//            int ageRange = ((age-1)/5) * 5 + 5 ;
//            sortAgeRecord = UPDATE_HASHMAP(ageRange,agentId,sortAgeRecord) ;
//        }
        return agentAgeHashMap ;
    }
    
    /**
     * 
     * @param propertyName
     * @param pairs
     * @return (HashMap) pair of agentIds maps to whether they are concordant in propertyName.
     */
    public HashMap<String[],Boolean> getConcordants(String propertyName, ArrayList<String[]> pairs)
    {
        HashMap<String[],Boolean> concordants = new HashMap<String[],Boolean>() ;
        
        HashMap<String,String> censusPropertyReport = prepareCensusPropertyReport(propertyName) ;
        
        String status0 ;
        String status1 ;
        for (String[] pair : pairs)
        {
            status0 = censusPropertyReport.get(pair[0]);
            status1 = censusPropertyReport.get(pair[1]);
            concordants.put(pair,status0.equals(status1)) ;
        }
        
        return concordants ;
    }
    
    /**
     * 
     * @return (HashMap) agentId maps to String describing census properties.
     */
    public HashMap<Object,String> prepareCensusPropertyReport()
    {
        HashMap<Object,String> censusPropertyReport = new HashMap<Object,String>() ;
        
        ArrayList<String>  birthReport = prepareBirthReport() ;
        
        for (String birthRecord : birthReport)
        {
            ArrayList<String> birthArray = EXTRACT_ARRAYLIST(birthRecord,AGENTID) ;
            for (String birthAgent : birthArray)
            {
                Object agentId = EXTRACT_VALUE(AGENTID,birthAgent) ;
                String propertyValue ;
                String censusEntry = "" ;
                ArrayList<String> propertyNames = IDENTIFY_PROPERTIES(birthAgent) ;
                propertyNames.remove(AGENTID) ;
                for (String propertyName : propertyNames)
                {
                    propertyValue = EXTRACT_VALUE(propertyName,birthAgent);
                    censusEntry += Reporter.ADD_REPORT_PROPERTY(propertyName, propertyValue) ;
                }
                censusPropertyReport.put(agentId, censusEntry) ;
            }
        }
        return censusPropertyReport ;
    }
    
    /**
     * 
     * @param propertyName
     * @return (HashMap) agentId maps to (String) value of propertyName
     */
    public HashMap<String,String> prepareCensusPropertyReport(String propertyName)
    {
        HashMap<String,String> censusPropertyReport = new HashMap<String,String>() ;
        
        ArrayList<String>  birthReport = prepareBirthReport() ;
        
        for (String birthRecord : birthReport)
        {
            ArrayList<String> birthArray = EXTRACT_ARRAYLIST(birthRecord,AGENTID) ;
            for (String birthAgent : birthArray)
            {
                String agentId = EXTRACT_VALUE(AGENTID,birthAgent) ;
                String propertyValue = EXTRACT_VALUE(propertyName,birthAgent) ;
                censusPropertyReport.put(agentId, propertyValue) ;
            }
        }
        return censusPropertyReport ;
    }
    
    /**
     * 
     * @param propertyName
     * @param endCycle
     * @return (HashMap) agentId maps to value of propertyName at cycle endCycle.
     */
    public HashMap<String,String> prepareCensusPropertyReport(String propertyName, int endCycle)
    {
        HashMap<String,String> censusPropertyReport = new HashMap<String,String>() ;
        
        // Census at birth
        HashMap<String,String>  birthReport = prepareCensusPropertyReport(propertyName) ;
        HashSet<String> agentIdSet = new HashSet<String>() ;
        Collections.addAll(agentIdSet, birthReport.keySet().toArray(new String[0])) ;
        //LOGGER.info(agentIdSet.toString());
        ArrayList<String> changeReport = prepareChangeReport(propertyName,endCycle) ;
        //LOGGER.info(changeReport.toString());
    
        // changeReport.size() is zero if no changes are made
        for (int index = changeReport.size() - 1 ; index >= 0 ; index-- )
        {
            String changeRecord = changeReport.get(index) ;
            ArrayList<String> changeAgentIds = IDENTIFY_PROPERTIES(changeRecord) ;
            //LOGGER.info(changeAgentIds.toString()) ;
            changeAgentIds.retainAll(agentIdSet) ;
            
            for (String agentId : changeAgentIds)
            {
                int agentIndex = INDEX_OF_PROPERTY(agentId.toString(),changeRecord) ;
                int colonIndex = changeRecord.indexOf(":",agentIndex) ;
                int nextColonIndex = changeRecord.indexOf(":",colonIndex + 1) ;
                int spaceIndex = changeRecord.lastIndexOf(SPACE, nextColonIndex) ;
                if (spaceIndex < 0)
                    spaceIndex = changeRecord.length() ;
                String value = changeRecord.substring(colonIndex + 1, spaceIndex) ;
                
                //LOGGER.info(value) ;
                // Might be HashMap of values
                if (value.startsWith("{"))
                {
                    if (!value.contains(propertyName))
                        continue ;
                    value = value.replace("=", ":") ;
                    value = EXTRACT_VALUE(propertyName,value) ;
                    value = value.substring(0, value.length() - 1) ;
                }
                censusPropertyReport.put(agentId, value) ;
                agentIdSet.remove(agentId) ;
            }
            if (agentIdSet.isEmpty())
                break ;
        }
        
        for (String agentId : agentIdSet)
            censusPropertyReport.put(agentId, birthReport.get(agentId)) ;
        
        return censusPropertyReport ;
    }
    
    /**
     * 
     * @return (HashMap) key: prepStatus, value: ArrayList of agentIds
     */
    public HashMap<Comparable,ArrayList<Comparable>> sortPrepStatus()
    {
        ArrayList<String> openingArray = new ArrayList<String>() ;
        openingArray.add(input.get(0)) ;
        return SORT_BOUNDED_STRING_ARRAY("prepStatus", 
                new String[] {TRUE,FALSE}, AGENTID, openingArray ) ;
    }
    
    /**
     * 
     * @return (HashMap) key: prepStatus, value: ArrayList of agentIds
     */
    public HashMap<Comparable,ArrayList<Comparable>> sortStatusHIV()
    {
        return SORT_BOUNDED_STRING_ARRAY("statusHIV", 
                new String[] {TRUE,FALSE}, AGENTID, input ) ;
    }
    
}