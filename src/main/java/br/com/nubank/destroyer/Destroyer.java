package br.com.nubank.destroyer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import br.com.nubank.destroyer.pojos.Job;

public class Destroyer {
	private static Logger logger = Logger.getLogger(Destroyer.class);
	
	public void Destroy(String instanceId) {
		
		List<String> instanceIds = new ArrayList<String>();
		
		if (instanceId != null){
			instanceIds.add(instanceId);
			
			logger.info("Terminate Instance: " + instanceId);
			try {
				AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
				AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
	
			    TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
			    ec2.terminateInstances(terminateRequest);
			    updateStatus(credentials, instanceId);
			} catch (AmazonServiceException e) {
			    logger.error("Error terminating instances");
			    logger.error("Caught Exception: " + e.getMessage());
			    logger.error("Reponse Status Code: " + e.getStatusCode());
			    logger.error("Error Code: " + e.getErrorCode());
			    logger.error("Request ID: " + e.getRequestId());
			}
		}
	}
	
	private void updateStatus(AWSCredentials credentials, String instanceId){		
		logger.info("Sending message to queue sqs_update");
        AmazonSQS sqs = new AmazonSQSClient(credentials);
        
		Job job = new Job();
		job.setInstanceId(instanceId);
		job.setSchedule("");
		job.setStatus("terminated");
        
        JSONObject jobJson = new JSONObject(job);
        
		sqs.sendMessage(new SendMessageRequest("https://us-west-2.queue.amazonaws.com/678982507510/sqs_update", jobJson.toString()));
	}
}
