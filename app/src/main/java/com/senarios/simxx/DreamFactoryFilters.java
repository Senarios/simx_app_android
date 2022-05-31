package com.senarios.simxx;

public interface DreamFactoryFilters {

    default String getJobCandidatesWithShortList(String broadcast, Boolean isshortlisted){
        return "((broadcast="+broadcast+") AND (isshortlisted="+isshortlisted+"))";
    }

}
