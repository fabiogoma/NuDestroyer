package br.com.nubank.destroyer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import br.com.nubank.destroyer.pojos.Job;

public class Destroyer {
	private static Logger logger = Logger.getLogger(Destroyer.class);
	
	public void Destroy(Job job) {
		
		List<String> instanceIds = new ArrayList<String>();
		List<String> requestIds = new ArrayList<String>();
		
		if (job.getInstanceId() != null){
			instanceIds.add(job.getInstanceId());
			requestIds.add(job.getRequestId());
			
			logger.info("Terminate Instance: " + job.getInstanceId());
			logger.info("Cancel Spot Instance Request: " + job.getRequestId());
			try {
				AWSCredentials credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
				AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(System.getenv("REGION")).build();
				
				//Terminate instance
			    TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
			    ec2.terminateInstances(terminateRequest);
			    
			    //Cancel Spot Instance Request
			    CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(requestIds);
			    ec2.cancelSpotInstanceRequests(cancelRequest);
			    
			    updateStatus(credentials, job);
			    
			} catch (AmazonServiceException e) {
			    logger.error("Error terminating instances");
			    logger.error("Caught Exception: " + e.getMessage());
			    logger.error("Reponse Status Code: " + e.getStatusCode());
			    logger.error("Error Code: " + e.getErrorCode());
			    logger.error("Request ID: " + e.getRequestId());
			}
		}
	}
	
	private void updateStatus(AWSCredentials credentials, Job job){		
		logger.info("Sending message to queue sqs_update");
        AmazonSQS sqs = new AmazonSQSClient(credentials);
        
		job.setStatus("terminated");
        
        JSONObject jobJson = new JSONObject(job);
        
		sqs.sendMessage(new SendMessageRequest(System.getenv("SQS_UPDATE_URL"), jobJson.toString()));
	}
}
