package br.com.nubank.destroyer;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import br.com.nubank.destroyer.pojos.Job;

public class DestroyerListener {
	private static Logger logger = Logger.getLogger(DestroyerListener.class);
	
	public static void main(String[] args) throws InterruptedException {
		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		AmazonSQS sqs = new AmazonSQSClient(credentials);
		
		logger.info("Receiving messages from sqs_destroy");
		String myQueueUrl = "https://us-west-2.queue.amazonaws.com/678982507510/sqs_destroy";
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        
        while(true){
        	List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	        for (Message message : messages) {
	        	
	        	JSONObject jsonObject = new JSONObject(message.getBody());
	        	
	        	Job job = new Job();
	        	
	        	job.setInstanceId(jsonObject.getString("instanceId"));
	        	job.setRequestId(jsonObject.getString("requestId"));
	        	job.setSchedule(jsonObject.getString("schedule"));
	        	job.setStatus(jsonObject.getString("status"));
	        	
	        	Destroyer destroyer = new Destroyer();
	        	destroyer.Destroy(job);
	        	
	        	String messageRecieptHandle = messages.get(0).getReceiptHandle();
	        	sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
	        }
	        Thread.sleep(1000);
        }

	}

}
