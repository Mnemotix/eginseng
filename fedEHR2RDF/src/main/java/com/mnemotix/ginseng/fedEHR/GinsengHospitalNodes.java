package com.mnemotix.ginseng.fedEHR;
public enum GinsengHospitalNodes {

	ALL_NODES ("${ALL_NODES}"),
	GW_DEVEL_I3S ("GW_DEVEL_I3S"),
	GW_DEVEL_IN2P3 ("GW_DEVEL_IN2P3"),
	GW_DEVEL_CREATIS ("GW_DEVEL_CREATIS");
	
    private final String name;       

    private GinsengHospitalNodes(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false : name.equals(otherName);
    }

    public String toString(){
       return name;
    }
}
