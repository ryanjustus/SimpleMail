/*
 * 01/30/2009
 *
 * SimpleEmail.java - java email library
 *
 * Copyright (c) 2011, Ryan Justus
 * All rights reserved.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */


package com.ryanjustus.simplemail;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Collection;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;

/**
 * Class to send emails through an smtp server.  Supports multiple attachments, including saving a byte array
 * as an attachment so that you don't have to save data your program has manipulated to a file before
 * you can attach it.  There isn't much to this class but it abstracts the Mime parts
 * out of sending an email.
 *
 * example usage:
 * <pre>
 * 		SimpleEmail myMail = new SimpleEmail("smtp.gmail.com",465,"myUsername","myPassword");
 *		myMail.setFrom("myEmailAddress@email.com");
 *		myMail.addRecipient("recipient@email.com");
 *		myMail.setSubject("Test SimpleEmail");
 *		myMail.setBody("this is the body that I am testing");
 *		myMail.sendMail();
 * </pre>
 * @author ryan
 */

public class SimpleMail {

    private Session mailSession;
    private MimeMessage message;
    private MimeMultipart multipart;

    /**
     * Constructor for SimpleEmail with username/password smtp authentication
     * @param username
     * @param password
     */
	public SimpleMail(String smtpserver, int port, String username, String password)
	{
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.host", smtpserver);
            if(username!=null && !username.isEmpty()){
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else{
                props.put("mail.smtp.auth", "false");
            }
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));            
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.quitwait", "false");

            Authenticator auth = new Authenticator(username, password);
            mailSession = Session.getInstance(props, auth);
            message=new MimeMessage(mailSession);
            multipart = new MimeMultipart("related");
	}

    /**
     * Constructor for an smtp server without authentication
     * @param smtpserver
     * @param port 
     */
    public SimpleMail(String smtpserver, int port)
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", smtpserver);
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.socketFactory.port", String.valueOf(port));
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        mailSession = Session.getDefaultInstance(props);
        message=new MimeMessage(mailSession);
        multipart = new MimeMultipart("related");
    }
    
    /**
     * Adds a recipient of a type "to", "cc", or "bcc"
     * @param recipient
     * @param type
     * @return
     */
    public void addRecipient(String recipient, Message.RecipientType type) throws AddressException, MessagingException
    {
        InternetAddress recip=new InternetAddress(recipient);
        message.addRecipient(type, recip);
    }
    /**
     * adds a collection of recipients to the email.
     * @param recipients
     */
    public void addRecipients(Collection<String> recipients) throws AddressException, MessagingException
    {
      for(String recipient:recipients)
      {
         addRecipient(recipient);
      }
    }

    /**
     * adds a recipient to the email with as a Message.RecipientType.TO
     * @param recipient
     * @throws AddressException
     * @throws MessagingException
     */
    public void addRecipient(String recipient) throws AddressException, MessagingException
    {
        InternetAddress recip=new InternetAddress(recipient);
        message.addRecipient(Message.RecipientType.TO, recip);
    }
    /**
     * sets the from address of the email
     * @param sender
     * @return
     */
    public boolean setFrom(String sender) throws MessagingException
    {
            message.setFrom(new InternetAddress(sender));
            return true;
    }
    
   /**
    * Set the subject of the email
    * @param subject
    * @return
    * @throws MessagingException
    */
    public void setSubject(String subject) throws MessagingException
    {
            message.setSubject(subject);
    }
    
    /**
     * Sets the message with a content type "text/html"
     * @param body
     * @throws MessagingException
     */
    public void setMessage(String message) throws MessagingException
    {

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");
        multipart.addBodyPart(messageBodyPart);

    }
    /**
     * Add a file attachment f to the message.  The contentid is so that you can 
     * reference the attachment in the html content of your email.
     * @param f
     * @param contentid
     * @return
     */
    public void addAttachment(File f, String contentid) throws IOException, MessagingException
    {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.attachFile(f);
        if(contentid!=null)
            mbp.setHeader("Content-ID", "<"+contentid+">");
        multipart.addBodyPart(mbp);

    }
    /**
     * Add a byte[] as an attachment, contenttype is the mime type of the attachment,
     * contentid is so that that you can reference the attachment in the
     * html content of your message, and the filename is the filename you 
     * wish to have for the attachment.
     * @param file
     * @param contenttype
     * @param contentid
     * @param filename
     * @return
     */
	public void addAttachment(byte[] file, String contenttype, String contentid, String filename) throws MessagingException
	{

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            InternetHeaders ih = new InternetHeaders();
            MimeBodyPart mimein = new MimeBodyPart(ih, file);
            DataSource datain = new MimePartDataSource(mimein);
            messageBodyPart.setDataHandler(new DataHandler(datain));
            mimein.setHeader("Content-Type", contenttype);
            messageBodyPart.setHeader("Content-ID", "<"+contentid+">");
            messageBodyPart.setFileName(filename);
            // add it
            multipart.addBodyPart(messageBodyPart);

	}
    /**
     * Send the mail
     * @throws javax.mail.MessagingException
     */
    public synchronized void sendMail() throws javax.mail.MessagingException
    {
        message.setContent(multipart);
        Transport.send(message);
    }    
}
/**
 * class to provide password authentication to authenticated smtp servers
 * @author ryan
 */
class  Authenticator extends javax.mail.Authenticator{

    private String username;
    private String password;
    public Authenticator(String username, String password)
    {
        super();
        this.username=username;
        this.password=password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(username, password);
    }
}