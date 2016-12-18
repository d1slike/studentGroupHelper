package ru.disdev.entity.trash;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "clients")
public class Client {

    public static final int NEW_CLIENT = 1;
    public static final int VALID = 3;
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String accessToken;
    private String productId;
    private String contactEmail;
    private String gmail;
    private Date paymentDate;
    private Date expireDate;
    private int status;

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String[] toCSVRow() {
        return new String[]{
                getName(),
                getContactEmail(),
                getGmail(),
                DATE_FORMAT.format(getPaymentDate()),
                DATE_FORMAT.format(getExpireDate())
        };
    }
}
