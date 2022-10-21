package com.ibm.requirement.typemanagement.oslc.client.dngcm;

public class CallStatus {
	
	// The status of the call, if it failed or succeeded 
	// False if the call fails for some reason.
	boolean callSuccess = false;
	// The result of the call, if the call has a result
	// True if the call succeeded
	boolean callResult = false;
	String message = "";
	Integer noResults = 0;

	public Integer getNoResults() {
		return noResults;
	}

	public void setNoResults(Integer noResults) {
		this.noResults = noResults;
	}

	public void setCallSuccess(boolean callSuccess) {
		this.callSuccess = callSuccess;
	}

	public boolean getCallResult() {
		return callResult;
	}

	public void setCallResult(boolean callResult) {
		this.callResult = callResult;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;		
	}

//	public CallStatus(boolean b, String string) {
//		// TODO Auto-generated constructor stub
//	}
//
	public CallStatus() {
		// TODO Auto-generated constructor stub
	}
//
//	public CallStatus(boolean b) {
//		// TODO Auto-generated constructor stub
//	}
//
	public CallStatus(String message) {
		this.message = message;
	}
//
//
//	public void set(boolean b, String string) {
//		// TODO Auto-generated method stub
//		
//	}
//
	public boolean callFailed() {
		return !callSuccess;
	}
	

}
