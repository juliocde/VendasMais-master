package br.com.dsx.vendamais.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email extends AsyncTask<Void,Void,String> {
    public static final String EMAIL ="vendamais.dsxconsultoria@gmail.com";
    public static final String PASSWORD ="salazarfilipe";
    private Context context;
    private String email;
    private String subject;
    private String message;
    private ProgressDialog progressDialog;
    private Email.EmailResponse delegate;
    private String popupMessage;
    private String filename;

    public Email(Context context, String email, String subject, String message,EmailResponse delegate) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.delegate = delegate;
        this.popupMessage = "Enviando código de verificação para seu email.";
    }

    public Email(Context context, String email, String subject, String message,
                 String popupMessage, EmailResponse delegate) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.delegate = delegate;
        this.popupMessage = popupMessage;
    }

    public Email(Context context, String email, String subject, String message,
                 String popupMessage, String filename, EmailResponse delegate) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.delegate = delegate;
        this.popupMessage = popupMessage;
        this.filename = filename;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(
                context,popupMessage,
                "Por favor, aguarde...",false,false);
    }

    @Override
    protected void onPostExecute(String value){
        super.onPostExecute(value);
        progressDialog.dismiss();
        delegate.processFinish(value);
    }

    @Override
    protected String doInBackground(Void... params) {

        Properties props = new Properties();
        props.setProperty("mail.host", "mailhost");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.quitwait", "false");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Session session = Session.getInstance(props, new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });
        try {
            MimeMessage mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(EMAIL));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mm.setSubject(subject);
            if (Util.isNotBlank(filename)) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(message);
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);

                mm.setContent(multipart);
            }else {
                mm.setText(message);
            }


            Transport.send(mm);
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro";
        }
        return null;
    }

    public interface EmailResponse {
        void processFinish(String value);
    }
}