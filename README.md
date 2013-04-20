SimpleMail
==========

Simple wrapper for javax.mail.  Supports attachments, multiple recipients

Basic Usage:
```java
SimpleMail myMail = new SimpleMail("smtp.gmail.com",465,"myUsername","myPassword");
myMail.setFrom("myEmailAddress@email.com");
myMail.addRecipient("recipient@email.com");
myMail.setSubject("Test SimpleEmail");
myMail.setBody("this is the body that I am testing");
myMail.sendMail();
```
