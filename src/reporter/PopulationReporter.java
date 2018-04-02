/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporter;

import java.util.ArrayList;

import java.util.logging.Level;

/**
 *
 * @author MichaelWalker
 */
public class PopulationReporter extends Reporter {
    
    public PopulationReporter(String simname, ArrayList<String> reports) 
    {
        super(simname, reports);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     * @return ArrayList of ArrayLists of (String) agentIds of agents 'born'
     * in each cycle
     */
    public ArrayList<ArrayList<String>> prepareAgentBirthReport()
    {
        ArrayList<ArrayList<String>> agentBirthReport = new ArrayList<ArrayList<String>>() ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        
        for (int reportNb = 0 ; reportNb < birthReport.size() ; reportNb++ )
        {
            String report = birthReport.get(reportNb) ;
            int startIndex = report.indexOf("agendId") ;
            agentBirthReport.add(extractAllValues("agentId", report, startIndex)) ;
        }
        return agentBirthReport ;
    }
    
    /**
     * 
     * @return ArrayList of ArrayLists of (String) ages-at-birth of agents 'born'
     * in each cycle
     */
    public ArrayList<ArrayList<String>> prepareAgeBirthReport()
    {
        ArrayList<ArrayList<String>> ageBirthReport = new ArrayList<ArrayList<String>>() ;
        
        ArrayList<String> birthReport = prepareBirthReport() ;
        
        for (int reportNb = 0 ; reportNb < birthReport.size() ; reportNb++ )
        {
            String report = birthReport.get(reportNb) ;
            int startIndex = report.indexOf("age") ;
            ageBirthReport.add(extractAllValues("age", report, startIndex)) ;
        }
        return ageBirthReport ;
    }
    
    /**
     * 
     * @return ArrayList of ArrayLists of (String) agentIds of agents who died 
     * in each cycle
     */
    public ArrayList<ArrayList<String>> prepareAgentDeathReport()
    {
        ArrayList<ArrayList<String>> agentDeathReport = new ArrayList<ArrayList<String>>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        
        for (int reportNb = 0 ; reportNb < deathReport.size() ; reportNb++ )
        {
            String report = deathReport.get(reportNb) ;
            int startIndex = report.indexOf("agentId") ;
            agentDeathReport.add(extractAllValues("agentId", report, startIndex)) ;
        }
        return agentDeathReport ;
    }
    
    /**
     * 
     * @return ArrayList of ArrayLists of (String) ages-at-death of agents who died 
     * in each cycle
     */
    public ArrayList<ArrayList<String>> prepareAgeDeathReport()
    {
        ArrayList<ArrayList<String>> ageDeathReport = new ArrayList<ArrayList<String>>() ;
        
        ArrayList<String> deathReport = prepareDeathReport() ;
        
        for (int reportNb = 0 ; reportNb < deathReport.size() ; reportNb++ )
        {
            String report = deathReport.get(reportNb) ;
            int startIndex = report.indexOf("age") ;
            ageDeathReport.add(extractAllValues("age", report, startIndex)) ;
        }
        return ageDeathReport ;
    }
    
    private ArrayList<String> prepareDeathReport()
    {
        ArrayList<String> deathReport = new ArrayList<String>() ;
        
        String report ;
        int agentIndex ;
        String valueString ;

        for (int reportNb = 0 ; reportNb < input.size() ; reportNb += outputCycle )
        {
            report = input.get(reportNb) ;
            agentIndex = report.indexOf("death") ;
            LOGGER.info(report) ;
            if (agentIndex < 0)
            {
                continue ;
            }
            deathReport.add(report.substring(agentIndex)) ;
            LOGGER.log(Level.INFO, "prepare: {0}", report) ;
        }
        return deathReport ;
    }
    
    public ArrayList<String> prepareBirthReport()
    {
        ArrayList<String> birthReport = new ArrayList<String>() ;
        
        String report ;
        int agentIndex ;
        String valueString ;

        for (int reportNb = 0 ; reportNb < input.size() ; reportNb += outputCycle )
        {
            report = input.get(reportNb) ;
            birthReport.add(report.substring(report.indexOf("birth"),report.indexOf("death"))) ;
            LOGGER.log(Level.INFO, "prepare: {0}", report) ;
        }
        return birthReport ;
    }
    
}
