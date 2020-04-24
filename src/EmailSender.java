import com.sendgrid.*;
import java.io.IOException;


public class EmailSender {

    Email from;
    String subject;
    Email to;
    Content content;
    Mail mail;
    SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

    EmailSender(String to, String itemName ,String url, String price)
    {
        this.from = new Email("collie@colliecolliecollie.ninja");
        this.subject = "SUPERDRY PRICE ALERT: "+itemName+" went down to $"+price;
        this.to = new Email(to);
        this.content = new Content("text/plain","You asked to be notified when an item you're tracking went down in price. \n"+itemName+" is now $"+price+"\n you can find it here: \n"+ url);
        this.mail = new Mail(this.from, this.subject, this.to, this.content);
    }

    public void sendAlert() throws IOException {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());

        } catch (IOException ex) {
            throw ex;
        }

    }


}
